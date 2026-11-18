#!/usr/bin/env bash
# https работает с нашим сертификатом, http редиректит на https.
# Запуск: CURL_OPTS="--cacert docker/server.crt" ./scripts/check-stage3.sh
# (если curl ругается на сертификат: CURL_OPTS=-k, но тогда сертификат не проверяется)
HTTP="${HTTP:-http://localhost:8080/dragon-service}"
HTTPS="${HTTPS:-https://localhost:8443/dragon-service}"
FAIL=0

check() { # имя, ожидание (glob), факт
  if [[ "$3" == $2 ]]; then echo "  ok   $1 ($3)"; else echo "  FAIL $1: ожидали '$2', получили '$3'"; FAIL=1; fi
}

echo "== https"
check "GET по https" 200 "$(curl $CURL_OPTS -s -o /dev/null -w '%{http_code}' "$HTTPS/dragons")"

echo "== http -> https"
check "GET по http редиректит" "30[1278] https://*" \
  "$(curl $CURL_OPTS -s -o /dev/null -w '%{http_code} %{redirect_url}' "$HTTP/dragons")"
check "POST по http не создаёт дракона" "30[1278]" \
  "$(curl $CURL_OPTS -s -o /dev/null -w '%{http_code}' -X POST -H 'Content-Type: application/xml' --data '<dragon/>' "$HTTP/dragons")"

echo "== полный прогон по https"
BASE="$HTTPS" "$(dirname "$0")/check-stage2.sh" || FAIL=1

[[ "$FAIL" -eq 0 ]] && echo "ЭТАП 3 OK" || echo "ЕСТЬ ОШИБКИ"
exit $FAIL
