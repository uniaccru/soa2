#!/usr/bin/env bash
# https на Payara с нашим сертификатом, http редиректит на https.
# Запуск (из папки killer-service): CURL_OPTS="--cacert docker/server.crt" ./scripts/check-stage5.sh
HTTP="${HTTP:-http://localhost:9080/killer-service}"
HTTPS="${HTTPS:-https://localhost:8181/killer-service}"
FAIL=0

curl() { command curl $CURL_OPTS "$@"; }

check() { # имя, ожидание (glob), факт
  if [[ "$3" == $2 ]]; then echo "  ok   $1 ($3)"; else echo "  FAIL $1: ожидали '$2', получили '$3'"; FAIL=1; fi
}

echo "== https (сертификат проверяется по docker/server.crt)"
check "GET по https" 404 "$(curl -s -o /dev/null -w '%{http_code}' "$HTTPS/killer/dragon/find-by-cave-depth/-1")"

echo "== http -> https"
check "GET по http редиректит" "30[1278] https://*" \
  "$(curl -s -o /dev/null -w '%{http_code} %{redirect_url}' "$HTTP/killer/dragon/find-by-cave-depth/-1")"
check "POST по http редиректит, а не выполняется" "30[1278]" \
  "$(curl -s -o /dev/null -w '%{http_code}' -X POST "$HTTP/killer/teams/create/1/a/1/1")"

echo "== полный прогон по https (сервис 2 -> сервис 1 по https)"
BASE="$HTTPS" "$(dirname "$0")/check-stage4.sh" || FAIL=1

[[ "$FAIL" -eq 0 ]] && echo "ЭТАП 5 OK" || echo "ЕСТЬ ОШИБКИ"
exit $FAIL
