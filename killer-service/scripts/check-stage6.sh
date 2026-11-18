#!/usr/bin/env bash
# CORS в обоих сервисах (запросы с заголовком Origin, как их делает браузер).
# Запуск (из папки killer-service): CURL_OPTS="--cacert docker/server.crt" ./scripts/check-stage6.sh
S1="${S1:-https://localhost:8443/dragon-service}"
S2="${S2:-https://localhost:8181/killer-service}"
ALLOWED="${ALLOWED:-https://se.ifmo.ru}"          # должен быть в CORS_ALLOWED_ORIGINS
ALLOWED_LOCAL="${ALLOWED_LOCAL:-http://localhost:5173}"
DENIED="https://evil.example.com"                  # не должен быть в списке
HDRS="$(mktemp)"
PASS=0
FAIL=0
STATUS=""

curl() { command curl $CURL_OPTS "$@"; }
trap 'rm -f "$HDRS"' EXIT

# probe <метод> <url> <origin или ""> [доп. аргументы curl]
probe() {
  local method="$1" url="$2" origin="$3"; shift 3
  local args=(-s -o /dev/null -D "$HDRS" -X "$method")
  [[ -n "$origin" ]] && args+=(-H "Origin: $origin")
  curl "${args[@]}" "$@" "$url"
  STATUS="$(head -1 "$HDRS" | awk '{print $2}')"
}
hdr() { grep -i "^$1:" "$HDRS" | head -1 | sed 's/^[^:]*: *//' | tr -d '\r'; }

ok()   { PASS=$((PASS + 1)); echo "  ok   $1"; }
fail() { FAIL=$((FAIL + 1)); echo "  FAIL $1"; }
expect() { # имя, ожидание (glob), факт
  if [[ "$3" == $2 ]]; then ok "$1 ($3)"; else fail "$1: ожидали '$2', получили '$3'"; fi
}

PRE=(-H "Access-Control-Request-Method: POST" -H "Access-Control-Request-Headers: content-type")

# service <название> <url ресурса с ошибкой 404> <url для preflight>
check_service() {
  local name="$1" err_url="$2" pre_url="$3"
  echo "== $name"

  probe OPTIONS "$pre_url" "$ALLOWED" "${PRE[@]}"
  expect "preflight с разрешённым origin: статус" "2??" "$STATUS"
  expect "preflight: Allow-Origin" "$ALLOWED" "$(hdr Access-Control-Allow-Origin)"
  expect "preflight: Allow-Methods содержит POST" "*POST*" "$(hdr Access-Control-Allow-Methods)"
  expect "preflight: Allow-Headers содержит content-type" "*[Cc]ontent-[Tt]ype*" "$(hdr Access-Control-Allow-Headers)"

  probe OPTIONS "$pre_url" "$ALLOWED_LOCAL" "${PRE[@]}"
  expect "preflight с локальным origin ($ALLOWED_LOCAL)" "$ALLOWED_LOCAL" "$(hdr Access-Control-Allow-Origin)"

  probe OPTIONS "$pre_url" "$DENIED" "${PRE[@]}"
  expect "preflight с чужим origin: нет Allow-Origin" "" "$(hdr Access-Control-Allow-Origin)"
  expect "preflight с чужим origin: статус 4xx" "4??" "$STATUS"

  probe GET "$err_url" "$ALLOWED"
  expect "ответ-ошибка 404 тоже с Allow-Origin (фронт должен прочитать <error>)" "$ALLOWED" "$(hdr Access-Control-Allow-Origin)"
  expect "статус ошибки не изменился" 404 "$STATUS"

  probe GET "$err_url" "$DENIED"
  expect "обычный запрос с чужим origin: нет Allow-Origin" "" "$(hdr Access-Control-Allow-Origin)"

  probe GET "$err_url" ""
  expect "запрос без Origin: нет Allow-Origin" "" "$(hdr Access-Control-Allow-Origin)"
  expect "запрос без Origin работает как раньше" 404 "$STATUS"
}

check_service "сервис 1 (dragon-service)" "$S1/dragons/999999999" "$S1/dragons"
check_service "сервис 2 (killer-service)" "$S2/killer/dragon/find-by-cave-depth/-1" "$S2/killer/teams/create/1/a/1/1"

echo
echo "Итого: ok=$PASS, fail=$FAIL"
[[ "$FAIL" -eq 0 ]] && echo "ЭТАП 6 OK"
[[ "$FAIL" -eq 0 ]]
