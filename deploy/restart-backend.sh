#!/bin/bash
# 生产环境: /tesim/server/restart-backend.sh
# 用法:
#   SPRING_PROFILE=prod SERVER_PORT=8088 bash restart-backend.sh
# 与 mocbase（默认 8080）同机部署时务必使用 8088，避免误杀旧项目进程。
set -euo pipefail

APP_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR_NAME="${JAR_NAME:-t-esim-server.jar}"
JAR_PATH="$APP_DIR/$JAR_NAME"
PID_FILE="$APP_DIR/app.pid"
LOG_FILE="$APP_DIR/app.log"
JAVA_BIN="${JAVA_BIN:-java}"
SPRING_PROFILE="${SPRING_PROFILE:-prod}"
SERVER_PORT="${SERVER_PORT:-8088}"
# CentOS/云主机常见：启动卡在 Banner 后，因 /dev/random 熵不足
JAVA_OPTS="${JAVA_OPTS:--Djava.security.egd=file:/dev/./urandom}"

if [[ ! -f "$JAR_PATH" ]]; then
  echo "ERROR: jar not found: $JAR_PATH"
  exit 1
fi

pids_on_port() {
  local port="$1"
  if command -v ss >/dev/null 2>&1; then
    ss -lptn "sport = :${port}" 2>/dev/null \
      | sed -n 's/.*pid=\([0-9]\+\).*/\1/p' \
      | sort -u
  elif command -v lsof >/dev/null 2>&1; then
    lsof -t -iTCP:"${port}" -sTCP:LISTEN 2>/dev/null || true
  elif command -v fuser >/dev/null 2>&1; then
    fuser "${port}/tcp" 2>/dev/null | tr ' ' '\n' | grep -E '^[0-9]+$' || true
  fi
}

# 仅当该 PID 的命令行包含「本目录这份 jar」时才允许杀，避免误杀同机 mocbase/其它服务
is_our_jar_pid() {
  local pid="$1"
  local cmd
  cmd="$(ps -p "$pid" -o args= 2>/dev/null || true)"
  [[ -n "$cmd" && "$cmd" == *"$JAR_PATH"* ]]
}

stop_old() {
  if [[ -f "$PID_FILE" ]]; then
    OLD_PID="$(cat "$PID_FILE" || true)"
    if [[ -n "${OLD_PID}" ]] && kill -0 "$OLD_PID" 2>/dev/null; then
      echo "Stopping pid $OLD_PID (from app.pid) ..."
      kill "$OLD_PID" || true
      for _ in $(seq 1 20); do
        kill -0 "$OLD_PID" 2>/dev/null || break
        sleep 0.5
      done
      if kill -0 "$OLD_PID" 2>/dev/null; then
        echo "Force kill $OLD_PID"
        kill -9 "$OLD_PID" || true
      fi
    fi
    rm -f "$PID_FILE"
  fi

  # 只杀「本目录这份 jar」的进程
  pkill -f -- "$JAR_PATH" 2>/dev/null || true
  sleep 1

  # 端口上若仍是本 jar 的残留进程，再清一次；绝不杀其它目录的 java（例如 /opt 的 mocbase）
  local port_pids p
  port_pids="$(pids_on_port "$SERVER_PORT" || true)"
  if [[ -n "${port_pids}" ]]; then
    for p in ${port_pids}; do
      if is_our_jar_pid "$p"; then
        echo "Stopping our leftover pid $p on :${SERVER_PORT} ..."
        kill "$p" 2>/dev/null || true
        sleep 0.5
        kill -0 "$p" 2>/dev/null && kill -9 "$p" 2>/dev/null || true
      else
        echo "WARN: :${SERVER_PORT} held by unrelated pid $p — will not kill it"
        ps -p "$p" -o pid,args= 2>/dev/null || true
      fi
    done
    sleep 1
  fi

  port_pids="$(pids_on_port "$SERVER_PORT" || true)"
  if [[ -n "${port_pids}" ]]; then
    echo "ERROR: port ${SERVER_PORT} still busy by: ${port_pids}"
    echo "If sharing a server with mocbase, keep SERVER_PORT=8088 (mocbase uses 8080)."
    ss -lptn "sport = :${SERVER_PORT}" 2>/dev/null || true
    exit 1
  fi
  echo "Port ${SERVER_PORT} is free."
}

start_new() {
  : >"$LOG_FILE"
  echo "Starting $JAR_PATH (profile=$SPRING_PROFILE, port=$SERVER_PORT) ..."
  cd "$APP_DIR"
  # shellcheck disable=SC2086
  nohup "$JAVA_BIN" $JAVA_OPTS -jar "$JAR_PATH" \
    --spring.profiles.active="$SPRING_PROFILE" \
    --server.port="$SERVER_PORT" \
    >"$LOG_FILE" 2>&1 &
  echo $! >"$PID_FILE"
  local new_pid
  new_pid="$(cat "$PID_FILE")"
  echo "Started pid $new_pid, log: $LOG_FILE"

  local i
  for i in $(seq 1 45); do
    if ! kill -0 "$new_pid" 2>/dev/null; then
      echo "ERROR: process $new_pid exited early. Last log lines:"
      tail -n 60 "$LOG_FILE" || true
      exit 1
    fi
    if grep -q "APPLICATION FAILED TO START\|Port .* was already in use" "$LOG_FILE" 2>/dev/null; then
      echo "ERROR: startup failure detected in log:"
      tail -n 60 "$LOG_FILE" || true
      exit 1
    fi
    if grep -qE "Started TEsimApplication" "$LOG_FILE" 2>/dev/null; then
      echo "Startup OK (Started TEsimApplication)."
      return 0
    fi
    sleep 1
  done

  echo "WARN: still starting after 45s; process alive. Check: tail -f $LOG_FILE"
}

stop_old
start_new
echo "Backend restart done."
