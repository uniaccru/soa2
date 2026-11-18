#!/usr/bin/env bash
# Сервис 2 (Spring MVC, Payara) по http + его вызовы сервиса 1 по https.
# Запуск (из папки killer-service): CURL_OPTS="--cacert docker/server.crt" ./scripts/check-stage4.sh
# Скрипт создаёт тестовых драконов в сервисе 1 (weight 888777) и сам их удаляет.
BASE="${BASE:-http://localhost:9080/killer-service}"
S1="${S1:-https://localhost:8443/dragon-service}"
W=888777
TMP="$(mktemp)"
PASS=0
FAIL=0
STATUS=""

curl() { command curl $CURL_OPTS "$@"; }

# call <base> <имя> <ожидаемый статус> <метод> <путь> [тело]
call() {
  local base="$1" name="$2" expected="$3" method="$4" path="$5" body="${6:-}"
  local args=(-s -o "$TMP" -w '%{http_code}' -X "$method" -H 'Content-Type: application/xml' -H 'Accept: application/xml' "$base$path")
  [[ -n "$body" ]] && args+=(--data "$body")
  STATUS="$(curl "${args[@]}")"
  if [[ "$STATUS" == "$expected" ]]; then
    PASS=$((PASS + 1)); echo "  ok   $name ($STATUS)"
  else
    FAIL=$((FAIL + 1)); echo "  FAIL $name: ожидали $expected, получили $STATUS"; sed 's/^/       /' "$TMP"; echo
  fi
}
req() { call "$BASE" "$@"; }   # запрос к сервису 2
s1()  { call "$S1" "$@"; }     # запрос к сервису 1 (подготовка данных)

has() {
  if grep -qF -- "$2" "$TMP"; then
    PASS=$((PASS + 1)); echo "  ok   $1"
  else
    FAIL=$((FAIL + 1)); echo "  FAIL $1: в ответе нет '$2'"; sed 's/^/       /' "$TMP"; echo
  fi
}

cave_id() { grep -o '<cave><id>[0-9]*</id>' "$TMP" | head -1 | grep -o '[0-9]*'; }

mk() { # имя глубина|-
  local cave=""
  [[ "$2" != "-" ]] && cave="<cave><depth>$2</depth></cave>"
  echo "<dragon><name>$1</name><coordinates><x>1</x><y>1</y></coordinates><weight>$W</weight>$cave</dragon>"
}

cleanup() {
  curl -s -o /dev/null -X DELETE "$S1/dragons/by-weight/$W"
  rm -f "$TMP"
}
trap cleanup EXIT
cleanup_now() { curl -s -o /dev/null -X DELETE "$S1/dragons/by-weight/$W"; }

echo "== Сервис 2 отвечает как Spring-приложение?"
code=$(curl -s -o "$TMP" -w '%{http_code}' "$BASE/killer/nothing")
if [[ "$code" != "404" ]] || ! grep -q '<error>' "$TMP"; then
  echo "  FAIL: ожидали 404 с XML <error>, получили $code. Приложение, скорее всего, не задеплоилось."
  echo "  Смотри: docker compose logs --no-log-prefix killer-service | grep -E 'SEVERE|Caused by|deploy'"
  exit 1
fi
echo "  ok   сервис 2 жив"

echo "== Подготовка данных в сервисе 1"
cleanup_now
s1 "сервис 1 доступен" 200 GET "/dragons?size=1"
BEFORE=$(grep -o '<totalCount>[0-9]*' "$TMP" | grep -o '[0-9]*')
CLEAN=0; [[ "$BEFORE" == "0" ]] && CLEAN=1
[[ $CLEAN -eq 0 ]] && echo "  (в БД есть чужие драконы: проверки 'shallowest' и 'ниже самой мелкой' будут пропущены; для полной проверки нужна чистая БД)"

s1 "A: пещера 900010" 201 POST /dragons "$(mk chk4-a 900010)"
s1 "B: пещера 900050" 201 POST /dragons "$(mk chk4-b 900050)"
s1 "C: пещера 900050 (та же глубина, id больше)" 201 POST /dragons "$(mk chk4-c 900050)"
s1 "D: без пещеры" 201 POST /dragons "$(mk chk4-d -)"
s1 "E: пещера 900100" 201 POST /dragons "$(mk chk4-e 900100)"
echo "  (создаю ещё 105 драконов для проверки обхода нескольких страниц)"
for i in $(seq 0 104); do
  code=$(curl -s -o /dev/null -w '%{http_code}' -X POST -H 'Content-Type: application/xml' --data "$(mk "chk4-bulk-$i" $((900200 + i)))" "$S1/dragons")
  if [[ "$code" != "201" ]]; then FAIL=$((FAIL + 1)); echo "  FAIL bulk $i: $code"; break; fi
done
PASS=$((PASS + 1)); echo "  ok   bulk создан"

echo "== find-by-cave-depth"
req "deepest по умолчанию, max=900060" 200 GET /killer/dragon/find-by-cave-depth/900060
has "это B (при равной глубине меньший id)" "<name>chk4-b</name>"
has "в ответе пещера" "<cave>"
req "mode=deepest явно" 200 GET "/killer/dragon/find-by-cave-depth/900060?mode=deepest"
has "снова B" "<name>chk4-b</name>"
req "max=900049 -> A" 200 GET /killer/dragon/find-by-cave-depth/900049
has "A" "<name>chk4-a</name>"
req "max=900100 -> E" 200 GET /killer/dragon/find-by-cave-depth/900100
has "E" "<name>chk4-e</name>"
req "max=900400: обход нескольких страниц" 200 GET /killer/dragon/find-by-cave-depth/900400
has "самая глубокая из bulk" "<name>chk4-bulk-104</name>"
if [[ $CLEAN -eq 1 ]]; then
  req "shallowest, max=900060 -> A" 200 GET "/killer/dragon/find-by-cave-depth/900060?mode=shallowest"
  has "A" "<name>chk4-a</name>"
  req "нет пещер с глубиной <= 900009" 404 GET /killer/dragon/find-by-cave-depth/900009
fi
req "max=-1: нет пещер" 404 GET /killer/dragon/find-by-cave-depth/-1
has "ошибка в формате <error>" "<error>"
req "shallowest, max=-1" 404 GET "/killer/dragon/find-by-cave-depth/-1?mode=shallowest"
req "mode=bogus" 400 GET "/killer/dragon/find-by-cave-depth/900060?mode=bogus"
req "max=abc" 400 GET /killer/dragon/find-by-cave-depth/abc
req "max вне int64" 400 GET /killer/dragon/find-by-cave-depth/99999999999999999999

echo "== teams/create"
s1 "дракон с пещерой для команды" 201 POST /dragons "$(mk chk4-t 5)"
CAVE=$(cave_id)
TEAM=$((900000 + RANDOM))
req "создать команду" 201 POST "/killer/teams/create/$TEAM/Alpha%20Team/3/$CAVE"
has "teamId" "<teamId>$TEAM</teamId>"
has "имя раскодировано" "<teamName>Alpha Team</teamName>"
has "startCaveId" "<startCaveId>$CAVE</startCaveId>"
req "дубликат teamId" 400 POST "/killer/teams/create/$TEAM/Alpha/3/$CAVE"
has "ошибка в формате <error>" "<error>"
req "несуществующая пещера" 400 POST "/killer/teams/create/$((TEAM + 1))/Beta/3/999999999"
req "team-id = 0" 400 POST "/killer/teams/create/0/Beta/3/$CAVE"
req "team-id < 0" 400 POST "/killer/teams/create/-5/Beta/3/$CAVE"
req "team-id = abc" 400 POST "/killer/teams/create/abc/Beta/3/$CAVE"
req "team-size = 0" 400 POST "/killer/teams/create/$((TEAM + 2))/Beta/0/$CAVE"
req "team-size = abc" 400 POST "/killer/teams/create/$((TEAM + 2))/Beta/abc/$CAVE"
req "start-cave-id = abc" 400 POST "/killer/teams/create/$((TEAM + 2))/Beta/3/abc"

echo "== Ошибки фреймворка в формате <error>"
req "неизвестный путь" 404 GET /killer/nothing
has "<error>" "<error>"
req "неверный метод" 405 GET "/killer/teams/create/1/a/1/1"

echo
echo "Итого: ok=$PASS, fail=$FAIL"
echo "Вручную: docker compose stop dragon-service, затем GET $BASE/killer/dragon/find-by-cave-depth/10 — ожидаем 503."
[[ "$FAIL" -eq 0 ]]
