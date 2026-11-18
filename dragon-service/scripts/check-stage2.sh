#!/usr/bin/env bash
# Проверка сервиса 1 по http.
# Запуск: BASE=http://localhost:8080/dragon-service ./scripts/check-stage2.sh
# Скрипт создаёт тестовые записи (name chk-*, weight 777777) и сам их удаляет.
# CURL_OPTS: доп. опции curl, например --cacert docker/server.crt для https
curl() { command curl $CURL_OPTS "$@"; }
BASE="${BASE:-http://localhost:8080/dragon-service}"
TMP="$(mktemp)"
PASS=0
FAIL=0
STATUS=""

# req <имя> <ожидаемый статус> <метод> <путь> [тело] [content-type]
req() {
  local name="$1" expected="$2" method="$3" path="$4" body="${5:-}" ctype="${6:-application/xml}"
  local args=(-s -o "$TMP" -w '%{http_code}' -X "$method" -H "Content-Type: $ctype" -H 'Accept: application/xml' "$BASE$path")
  [[ -n "$body" ]] && args+=(--data "$body")
  STATUS="$(curl "${args[@]}")"
  if [[ "$STATUS" == "$expected" ]]; then
    PASS=$((PASS + 1)); echo "  ok   $name ($STATUS)"
  else
    FAIL=$((FAIL + 1)); echo "  FAIL $name: ожидали $expected, получили $STATUS"; sed 's/^/       /' "$TMP"; echo
  fi
}

# has <имя> <подстрока в теле последнего ответа>
has() {
  if grep -qF -- "$2" "$TMP"; then
    PASS=$((PASS + 1)); echo "  ok   $1"
  else
    FAIL=$((FAIL + 1)); echo "  FAIL $1: в ответе нет '$2'"; sed 's/^/       /' "$TMP"; echo
  fi
}

first_id() { grep -o '<id>[0-9]*</id>' "$TMP" | head -1 | grep -o '[0-9]*'; }
cave_id()  { grep -o '<cave><id>[0-9]*</id>' "$TMP" | head -1 | grep -o '[0-9]*'; }

# dragon <name> <x> <y> <age> <weight> <color> <caveDepth|-> <killer:yes|no>
dragon() {
  local x="<dragon><name>$1</name><coordinates><x>$2</x><y>$3</y></coordinates>"
  [[ "$4" != "-" ]] && x+="<age>$4</age>"
  x+="<description>chk-desc</description>"
  [[ "$5" != "-" ]] && x+="<weight>$5</weight>"
  [[ "$6" != "-" ]] && x+="<color>$6</color>"
  [[ "$7" != "-" ]] && x+="<cave><depth>$7</depth></cave>"
  [[ "$8" == "yes" ]] && x+="<killer><name>Bard</name><height>1.8</height><eyeColor>WHITE</eyeColor><hairColor>YELLOW</hairColor></killer>"
  echo "$x</dragon>"
}

IDS=()
cleanup() {
  for id in "${IDS[@]}"; do curl -s -o /dev/null -X DELETE "$BASE/dragons/$id"; done
  curl -s -o /dev/null -X DELETE "$BASE/dragons/by-weight/777777"
  rm -f "$TMP"
}
trap cleanup EXIT

echo "== Создание"
req "POST валидный дракон (с пещерой и убийцей)" 201 POST /dragons "$(dragon chk-a 10 5 10 777777 ORANGE 100 yes)"
D1=$(first_id); CAVE1=$(cave_id); IDS+=("$D1")
has "в ответе есть creationDate" "<creationDate>"
has "в ответе есть id пещеры" "<cave><id>"
has "killer вернулся" "<name>Bard</name>"
req "POST дракон без пещеры и убийцы" 201 POST /dragons "$(dragon chk-b 0 9 20 777777 - - no)"
D2=$(first_id); IDS+=("$D2")

echo "== Валидация POST (все должны быть 400)"
req "x = -42" 400 POST /dragons "$(dragon chk-bad -42 0 1 - - - no)"
req "y = 10" 400 POST /dragons "$(dragon chk-bad 0 10 1 - - - no)"
req "age = 0" 400 POST /dragons "$(dragon chk-bad 0 0 0 - - - no)"
req "weight = 0" 400 POST /dragons "$(dragon chk-bad 0 0 1 0 - - no)"
req "неизвестный color" 400 POST /dragons "$(dragon chk-bad 0 0 1 - PURPLE - no)"
req "пустое name" 400 POST /dragons "$(dragon '' 0 0 1 - - - no)"
req "нет coordinates" 400 POST /dragons "<dragon><name>chk-bad</name></dragon>"
req "age не число" 400 POST /dragons "<dragon><name>chk-bad</name><coordinates><x>0</x><y>0</y></coordinates><age>abc</age></dragon>"
req "битый XML" 400 POST /dragons "<dragon><name>"
req "пустое тело" 400 POST /dragons ""
req "height убийцы = 0" 400 POST /dragons "<dragon><name>chk-bad</name><coordinates><x>0</x><y>0</y></coordinates><killer><name>K</name><height>0</height><eyeColor>WHITE</eyeColor><hairColor>WHITE</hairColor></killer></dragon>"
has "ошибка в формате <error>" "<error>"
req "JSON вместо XML" 415 POST /dragons '{"name":"x"}' application/json

echo "== Чтение по id"
req "GET существующий" 200 GET "/dragons/$D1"
req "GET несуществующий" 404 GET /dragons/999999999
req "GET id = abc" 400 GET /dragons/abc
req "GET id = 0" 400 GET /dragons/0

echo "== Обновление"
req "PUT меняет name и убирает killer" 200 PUT "/dragons/$D1" "$(dragon chk-a2 11 6 10 777777 ORANGE 150 no)"
has "name обновился" "<name>chk-a2</name>"
has "id пещеры сохранился" "<cave><id>$CAVE1</id>"
req "PUT несуществующий" 404 PUT /dragons/999999999 "$(dragon chk-x 0 0 1 - - - no)"
req "PUT невалидный" 400 PUT "/dragons/$D1" "$(dragon chk-x -42 0 1 - - - no)"
req "PUT id = abc" 400 PUT /dragons/abc "$(dragon chk-x 0 0 1 - - - no)"

echo "== Коллекция: пагинация, сортировка, фильтры"
req "список без параметров" 200 GET /dragons
has "есть totalCount" "<totalCount>"
req "фильтр weight=777777" 200 GET "/dragons?weight=777777"
has "totalCount = 2" "<totalCount>2</totalCount>"
req "фильтры по И: weight + age" 200 GET "/dragons?weight=777777&age=20"
has "totalCount = 1" "<totalCount>1</totalCount>"
req "фильтр по caveDepth=150" 200 GET "/dragons?weight=777777&caveDepth=150"
has "нашёлся chk-a2" "<name>chk-a2</name>"
req "фильтр color=ORANGE" 200 GET "/dragons?weight=777777&color=ORANGE"
req "фильтр по x" 200 GET "/dragons?weight=777777&x=11"
req "sortBy=age desc, size=1" 200 GET "/dragons?weight=777777&sortBy=age&sortOrder=desc&size=1"
has "первый chk-b (age 20)" "<name>chk-b</name>"
req "вторая страница" 200 GET "/dragons?weight=777777&sortBy=age&sortOrder=desc&size=1&page=1"
has "на второй chk-a2" "<name>chk-a2</name>"
req "страница за пределами" 200 GET "/dragons?weight=777777&size=1&page=50"
has "totalCount всё равно 2" "<totalCount>2</totalCount>"
req "sortBy=caveDepth не теряет драконов без пещеры" 200 GET "/dragons?weight=777777&sortBy=caveDepth"
has "totalCount = 2" "<totalCount>2</totalCount>"
req "sortBy=killerName" 200 GET "/dragons?weight=777777&sortBy=killerName"
req "неизвестный sortBy" 400 GET "/dragons?sortBy=foo"
req "sortOrder=up" 400 GET "/dragons?sortOrder=up"
req "size=0" 400 GET "/dragons?size=0"
req "page=-1" 400 GET "/dragons?page=-1"
req "page=abc" 400 GET "/dragons?page=abc"
req "age=abc" 400 GET "/dragons?age=abc"
req "color=PURPLE" 400 GET "/dragons?color=PURPLE"
req "creationDate=не дата" 400 GET "/dragons?creationDate=yesterday"

echo "== Дополнительные операции"
req "average-age" 200 GET /dragons/average-age
has "корень averageAge" "<averageAge>"
req "unique-descriptions" 200 GET /dragons/unique-descriptions
has "есть chk-desc" "<value>chk-desc</value>"
req "by-weight = 0" 400 DELETE /dragons/by-weight/0
req "by-weight = abc" 400 DELETE /dragons/by-weight/abc
req "by-weight несуществующий вес" 204 DELETE /dragons/by-weight/888888
req "by-weight 777777" 204 DELETE /dragons/by-weight/777777
req "после by-weight дракон исчез" 404 GET "/dragons/$D1"

echo "== Удаление"
req "POST для удаления" 201 POST /dragons "$(dragon chk-del 0 0 1 - - - no)"
DEL=$(first_id)
req "DELETE существующий" 204 DELETE "/dragons/$DEL"
req "DELETE повторно" 404 DELETE "/dragons/$DEL"
req "DELETE id = abc" 400 DELETE /dragons/abc

echo "== Команды (/teams)"
req "POST дракон с пещерой для команды" 201 POST /dragons "$(dragon chk-t 0 0 1 777777 - 5 no)"
DT=$(first_id); CAVET=$(cave_id); IDS+=("$DT")
TEAM=$((900000 + RANDOM))
team() { echo "<team><teamId>$1</teamId><teamName>$2</teamName><teamSize>$3</teamSize><startCaveId>$4</startCaveId></team>"; }
req "создать команду" 201 POST /teams "$(team $TEAM chk-team 3 "$CAVET")"
has "вернулся teamId" "<teamId>$TEAM</teamId>"
req "дубликат teamId" 400 POST /teams "$(team $TEAM chk-team 3 "$CAVET")"
req "несуществующая пещера" 400 POST /teams "$(team $((TEAM + 1)) chk-team 3 999999999)"
req "teamSize = 0" 400 POST /teams "$(team $((TEAM + 2)) chk-team 0 "$CAVET")"
req "пустое teamName" 400 POST /teams "$(team $((TEAM + 3)) '' 3 "$CAVET")"
req "teamId = 0" 400 POST /teams "$(team 0 chk-team 3 "$CAVET")"

echo "== Неизвестный путь"
req "GET /nothing" 404 GET /nothing
has "ошибка в формате <error>" "<error>"

echo
echo "Итого: ok=$PASS, fail=$FAIL"
echo "Вручную: останови БД (или ssh-туннель) и вызови GET /dragons — ожидаем 503."
[[ "$FAIL" -eq 0 ]]
