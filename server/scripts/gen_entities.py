#!/usr/bin/env python3
"""Generate MyBatis-Plus entity classes from MySQL information_schema dump."""
from __future__ import annotations

from collections import defaultdict
from pathlib import Path

COLS_PATH = Path(r"C:\Users\12039\AppData\Local\Temp\tesim_cols.tsv")
ENTITY_DIR = Path(r"d:\IdeaProjects\T-esim\server\src\main\java\com\tesim\entity")


def to_pascal(name: str) -> str:
    return "".join(p.capitalize() for p in name.split("_"))


def to_camel(name: str) -> str:
    p = to_pascal(name)
    return p[0].lower() + p[1:]


def java_type(data_type: str, column_type: str) -> str:
    dt = data_type.lower()
    ct = column_type.lower()
    if dt == "tinyint":
        return "Boolean" if ct.startswith("tinyint(1)") else "Integer"
    if dt in ("int", "integer", "mediumint", "smallint"):
        return "Integer"
    if dt == "bigint":
        return "Long"
    if dt == "decimal":
        return "BigDecimal"
    if dt in ("datetime", "timestamp"):
        return "LocalDateTime"
    if dt == "date":
        return "LocalDate"
    if dt in ("varchar", "char", "text", "mediumtext", "longtext", "json"):
        return "String"
    if dt == "float":
        return "Float"
    if dt == "double":
        return "Double"
    return "String"


def read_rows() -> dict[str, list[dict]]:
    raw = COLS_PATH.read_bytes()
    if raw.startswith(b"\xff\xfe") or raw.startswith(b"\xfe\xff"):
        text = raw.decode("utf-16")
    else:
        text = raw.decode("utf-8")

    by_table: dict[str, list[dict]] = defaultdict(list)
    for line in text.splitlines():
        line = line.strip("\ufeff").strip()
        if not line or "\t" not in line:
            continue
        parts = line.split("\t")
        if len(parts) < 7:
            continue
        table, col, data_type, column_type, column_key, extra, is_nullable = parts[:7]
        if table.startswith("mysql:"):
            continue
        by_table[table].append(
            {
                "col": col,
                "data_type": data_type,
                "column_type": column_type,
                "column_key": column_key,
                "extra": extra.lower(),
                "nullable": is_nullable.upper() == "YES",
            }
        )
    return by_table


def render_entity(table: str, cols: list[dict]) -> str:
    cls = to_pascal(table)
    imports: set[str] = {
        "com.baomidou.mybatisplus.annotation.TableName",
        "lombok.Data",
    }
    pk_cols = [c for c in cols if c["column_key"] == "PRI"]
    first_pk_done = False
    field_blocks: list[str] = []

    for info in cols:
        jt = java_type(info["data_type"], info["column_type"])
        if jt == "BigDecimal":
            imports.add("java.math.BigDecimal")
        elif jt == "LocalDateTime":
            imports.add("java.time.LocalDateTime")
        elif jt == "LocalDate":
            imports.add("java.time.LocalDate")

        field = to_camel(info["col"])
        anns: list[str] = []
        extra = info["extra"]

        if "auto_increment" in extra:
            anns.append("@TableId(type = IdType.AUTO)")
            imports.add("com.baomidou.mybatisplus.annotation.TableId")
            imports.add("com.baomidou.mybatisplus.annotation.IdType")
        elif info["column_key"] == "PRI":
            if len(pk_cols) == 1:
                anns.append(f'@TableId(value = "{info["col"]}", type = IdType.INPUT)')
                imports.add("com.baomidou.mybatisplus.annotation.TableId")
                imports.add("com.baomidou.mybatisplus.annotation.IdType")
            elif not first_pk_done:
                # composite PK: mark first column only (MP limitation)
                anns.append(f'@TableId(value = "{info["col"]}", type = IdType.INPUT)')
                imports.add("com.baomidou.mybatisplus.annotation.TableId")
                imports.add("com.baomidou.mybatisplus.annotation.IdType")
                first_pk_done = True

        # 仅处理 STORED/VIRTUAL GENERATED，勿匹配 DEFAULT_GENERATED
        if "stored generated" in extra or "virtual generated" in extra:
            anns.append(
                f'@TableField(value = "{info["col"]}", '
                "insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)"
            )
            imports.add("com.baomidou.mybatisplus.annotation.TableField")
            imports.add("com.baomidou.mybatisplus.annotation.FieldStrategy")
        elif info["col"] == "created_at":
            anns.append("@TableField(fill = FieldFill.INSERT)")
            imports.add("com.baomidou.mybatisplus.annotation.TableField")
            imports.add("com.baomidou.mybatisplus.annotation.FieldFill")
        elif info["col"] == "updated_at":
            anns.append("@TableField(fill = FieldFill.INSERT_UPDATE)")
            imports.add("com.baomidou.mybatisplus.annotation.TableField")
            imports.add("com.baomidou.mybatisplus.annotation.FieldFill")

        lines = [f"    {a}" for a in anns]
        lines.append(f"    private {jt} {field};")
        field_blocks.append("\n".join(lines))

    mp_imports = sorted(i for i in imports if i.startswith("com.baomidou"))
    other = sorted(
        i
        for i in imports
        if not i.startswith("com.baomidou") and not i.startswith("java.") and i != "lombok.Data"
    )
    java_imports = sorted(i for i in imports if i.startswith("java."))

    import_lines: list[str] = []
    for i in mp_imports:
        import_lines.append(f"import {i};")
    import_lines.append("")
    import_lines.append("import lombok.Data;")
    if java_imports:
        import_lines.append("")
        for i in java_imports:
            import_lines.append(f"import {i};")
    for i in other:
        import_lines.append(f"import {i};")

    body = "\n\n".join(field_blocks)
    return (
        f"package com.tesim.entity;\n\n"
        + "\n".join(import_lines)
        + "\n\n"
        + f"/**\n * {table}\n */\n"
        + "@Data\n"
        + f'@TableName("{table}")\n'
        + f"public class {cls} {{\n\n"
        + body
        + "\n}\n"
    )


def main() -> None:
    by_table = read_rows()
    ENTITY_DIR.mkdir(parents=True, exist_ok=True)
    for table, cols in sorted(by_table.items()):
        content = render_entity(table, cols)
        path = ENTITY_DIR / f"{to_pascal(table)}.java"
        path.write_text(content, encoding="utf-8")
        print("wrote", path.name, len(cols), "fields")
    print("total", len(by_table))


if __name__ == "__main__":
    main()
