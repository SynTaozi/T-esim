package com.tesim.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Standard paginated payload for {@link ApiResponse#getData()}.
 * Field names match common Spring Data / front-end conventions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int number;
    private int size;

    public static <T> PageResult<T> empty(int page, int size) {
        return new PageResult<>(Collections.emptyList(), 0, 0, page, size);
    }

    public static <T> PageResult<T> of(List<T> content, long total, int page, int size) {
        int totalPages = size <= 0 ? 0 : (int) ((total + size - 1) / size);
        return new PageResult<>(content, total, totalPages, page, size);
    }

    public static <T> PageResult<T> of(Page<T> page) {
        return new PageResult<>(
                page.getRecords(),
                page.getTotal(),
                (int) page.getPages(),
                (int) Math.max(0, page.getCurrent() - 1),
                (int) page.getSize()
        );
    }

    public static <S, T> PageResult<T> of(Page<S> page, Function<S, T> mapper) {
        List<T> list = page.getRecords().stream().map(mapper).toList();
        return new PageResult<>(
                list,
                page.getTotal(),
                (int) page.getPages(),
                (int) Math.max(0, page.getCurrent() - 1),
                (int) page.getSize()
        );
    }
}
