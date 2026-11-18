"use strict";

const S1 = window.CONFIG.dragonServiceUrl.replace(/\/$/, "");
const S2 = window.CONFIG.killerServiceUrl.replace(/\/$/, "");
const COLORS = ["YELLOW", "ORANGE", "WHITE"];

// key — имя поля в API (фильтры/сортировка) и в плоском объекте дракона.
const FIELDS = [
  { key: "id", filter: true },
  { key: "name", filter: true, form: true },
  { key: "x", filter: true, form: true },
  { key: "y", filter: true, form: true },
  { key: "creationDate", filter: true, hint: "YYYY-MM-DD" },
  { key: "age", filter: true, form: true },
  { key: "description", filter: true, form: true },
  { key: "weight", filter: true, form: true },
  { key: "color", filter: true, form: true, options: COLORS },
  { key: "caveDepth", filter: true, form: true },
  { key: "killerName", filter: true, form: true },
  { key: "killerHeight", filter: true, form: true },
  { key: "killerEyeColor", filter: true, form: true, options: COLORS },
  { key: "killerHairColor", filter: true, form: true, options: COLORS }
];
const COLUMNS = ["id", "name", "x", "y", "creationDate", "age", "description", "weight", "color",
  "caveId", "caveDepth", "killerName", "killerHeight", "killerEyeColor", "killerHairColor"];
const KILLER_KEYS = ["killerName", "killerHeight", "killerEyeColor", "killerHairColor"];

const $ = id => document.getElementById(id);

// ---------- XML ----------
const esc = s => String(s).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
const tag = (name, value) => `<${name}>${esc(value)}</${name}>`;
const optTag = (name, value) => (value === "" || value == null ? "" : tag(name, value));

function parseXml(text) {
  const doc = new DOMParser().parseFromString(text, "application/xml");
  if (doc.getElementsByTagName("parsererror").length > 0) {
    throw new ApiError(0, "Сервис вернул ответ, который не является XML");
  }
  return doc.documentElement;
}
const kid = (el, name) => (el ? Array.from(el.children).find(c => c.localName === name) || null : null);
const childText = (el, name) => { const k = kid(el, name); return k ? k.textContent : ""; };

function parseDragon(el) {
  const coordinates = kid(el, "coordinates"), cave = kid(el, "cave"), killer = kid(el, "killer");
  return {
    id: childText(el, "id"), name: childText(el, "name"),
    x: childText(coordinates, "x"), y: childText(coordinates, "y"),
    creationDate: childText(el, "creationDate"), age: childText(el, "age"),
    description: childText(el, "description"), weight: childText(el, "weight"), color: childText(el, "color"),
    caveId: childText(cave, "id"), caveDepth: childText(cave, "depth"),
    killerName: childText(killer, "name"), killerHeight: childText(killer, "height"),
    killerEyeColor: childText(killer, "eyeColor"), killerHairColor: childText(killer, "hairColor")
  };
}

function dragonToXml(d) {
  let xml = "<dragon>" + tag("name", d.name)
    + "<coordinates>" + tag("x", d.x) + tag("y", d.y) + "</coordinates>"
    + optTag("age", d.age) + optTag("description", d.description)
    + optTag("weight", d.weight) + optTag("color", d.color);
  if (d.caveDepth !== "") xml += "<cave>" + tag("depth", d.caveDepth) + "</cave>";
  if (KILLER_KEYS.some(k => d[k] !== "")) { // если заполнено хоть что-то — отправляем всё, остальное проверит сервер
    xml += "<killer>" + tag("name", d.killerName) + tag("height", d.killerHeight)
      + tag("eyeColor", d.killerEyeColor) + tag("hairColor", d.killerHairColor) + "</killer>";
  }
  return xml + "</dragon>";
}

// ---------- HTTP ----------
class ApiError extends Error {
  constructor(status, message) { super(message); this.status = status; }
}
class NetworkError extends Error {
  constructor(baseUrl) { super("network"); this.baseUrl = baseUrl; }
}

function errorText(text, response) {
  try {
    const message = childText(parseXml(text), "message");
    if (message) return message;
  } catch (e) { /* не <error> — используем статус */ }
  return response.statusText || "без описания";
}

async function call(base, method, path, body) {
  let response;
  try {
    const headers = { Accept: "application/xml" };
    if (body) headers["Content-Type"] = "application/xml";
    response = await fetch(base + path, { method, headers, body });
  } catch (e) {
    throw new NetworkError(base);
  }
  const text = await response.text();
  if (!response.ok) throw new ApiError(response.status, errorText(text, response));
  return text ? parseXml(text) : null;
}

// ---------- сообщения ----------
function showOk(message) {
  $("status").className = "ok";
  $("status").textContent = message;
}

function showError(e) {
  const box = $("status");
  box.className = "err";
  box.textContent = "";
  if (e instanceof NetworkError) {
    box.append("Не удалось связаться с сервисом. Проверьте, что запущен ssh-туннель, и откройте адрес в браузере один раз, приняв сертификат: ");
    const link = document.createElement("a");
    link.href = e.baseUrl + "/";
    link.target = "_blank";
    link.textContent = e.baseUrl;
    box.append(link);
  } else if (e instanceof ApiError) {
    let text = `Ошибка ${e.status}: ${e.message}`;
    if (e.status === 400) text = `Некорректные данные. ${text}`;
    else if ([502, 503, 504].includes(e.status)) text = `Сервис недоступен или не ответил вовремя. ${text}`;
    else if (e.status === 404) text = `Не найдено. ${text}`;
    box.textContent = text;
  } else {
    box.textContent = "Ошибка в приложении: " + e.message;
    console.error(e);
  }
}

async function run(action) {
  try {
    await action();
  } catch (e) {
    showError(e);
  }
}

function showResult(lines) {
  $("result").textContent = lines.join("\n");
}

const describeDragon = d => COLUMNS.map(k => `${k}: ${d[k] === "" ? "—" : d[k]}`);

// ---------- коллекция ----------
let page = 0;
let editingId = null;
const loaded = new Map();

function renderInputs(container, prefix, onlyForm) {
  container.innerHTML = FIELDS.filter(f => (onlyForm ? f.form : f.filter)).map(f => {
    const control = f.options
      ? `<select id="${prefix}${f.key}"><option value=""></option>${f.options.map(o => `<option>${o}</option>`).join("")}</select>`
      : `<input id="${prefix}${f.key}" placeholder="${f.hint || ""}">`;
    return `<label>${f.key}: ${control}</label>`;
  }).join("");
}

function filterParams() {
  const params = new URLSearchParams();
  for (const f of FIELDS) {
    const value = $("f_" + f.key).value.trim();
    if (value !== "") params.set(f.key, value);
  }
  if ($("sortBy").value) params.set("sortBy", $("sortBy").value);
  params.set("sortOrder", $("sortOrder").value);
  params.set("size", $("size").value || "10");
  params.set("page", String(page));
  return params;
}

async function loadList() {
  const root = await call(S1, "GET", "/dragons?" + filterParams());
  const total = Number(childText(root, "totalCount"));
  const size = Number(childText(root, "size")) || 10;
  const items = Array.from((kid(root, "items") || { children: [] }).children).map(parseDragon);

  loaded.clear();
  items.forEach(d => loaded.set(d.id, d));

  $("table").innerHTML = items.length === 0
    ? "<p>Ничего не найдено.</p>"
    : "<table><tr>" + COLUMNS.map(c => `<th>${c}</th>`).join("") + "<th></th></tr>"
      + items.map(d => "<tr>" + COLUMNS.map(c => `<td>${esc(d[c])}</td>`).join("")
        + `<td><button data-action="edit" data-id="${esc(d.id)}">Изменить</button> `
        + `<button data-action="delete" data-id="${esc(d.id)}">Удалить</button></td></tr>`).join("")
      + "</table>";

  const pages = Math.max(1, Math.ceil(total / size));
  $("pager").innerHTML = `<button id="prev" ${page <= 0 ? "disabled" : ""}>&larr;</button> `
    + `Страница ${page + 1} из ${pages}, всего драконов: ${total} `
    + `<button id="next" ${page + 1 >= pages ? "disabled" : ""}>&rarr;</button>`;
  $("prev").onclick = () => { page--; run(loadList); };
  $("next").onclick = () => { page++; run(loadList); };
}

// ---------- форма создания/редактирования ----------
function fillForm(d) {
  FIELDS.filter(f => f.form).forEach(f => { $("d_" + f.key).value = d[f.key]; });
  editingId = d.id;
  $("formTitle").textContent = `Изменить дракона id=${d.id}`;
  $("submitBtn").textContent = "Сохранить";
  $("cancelEdit").hidden = false;
  $("dragonForm").scrollIntoView();
}

function resetForm() {
  FIELDS.filter(f => f.form).forEach(f => { $("d_" + f.key).value = ""; });
  editingId = null;
  $("formTitle").textContent = "Создать дракона";
  $("submitBtn").textContent = "Создать";
  $("cancelEdit").hidden = true;
}

function readForm() {
  const d = {};
  FIELDS.filter(f => f.form).forEach(f => { d[f.key] = $("d_" + f.key).value.trim(); });
  return d;
}

async function saveDragon() {
  const body = dragonToXml(readForm());
  const root = editingId === null
    ? await call(S1, "POST", "/dragons", body)
    : await call(S1, "PUT", "/dragons/" + encodeURIComponent(editingId), body);
  showOk(editingId === null ? `Дракон создан, id=${childText(root, "id")}` : `Дракон id=${editingId} изменён`);
  resetForm();
  await loadList();
}

// ---------- прочие операции ----------
async function getById() {
  const root = await call(S1, "GET", "/dragons/" + encodeURIComponent($("getId").value.trim()));
  showResult(describeDragon(parseDragon(root)));
  showOk("Дракон найден");
}

async function deleteByWeight() {
  const weight = $("delWeight").value.trim();
  await call(S1, "DELETE", "/dragons/by-weight/" + encodeURIComponent(weight));
  showOk(`Драконы с weight=${weight} удалены (если были)`);
  await loadList();
}

async function averageAge() {
  const root = await call(S1, "GET", "/dragons/average-age");
  const value = childText(root, "averageAge");
  showResult([value === "" ? "Средний возраст: нет данных (нет драконов с age)" : `Средний возраст: ${value}`]);
  showOk("Готово");
}

async function uniqueDescriptions() {
  const root = await call(S1, "GET", "/dragons/unique-descriptions");
  const values = Array.from((kid(root, "values") || { children: [] }).children).map(v => v.textContent);
  showResult(values.length === 0 ? ["Нет описаний"] : ["Уникальные description:", ...values.map(v => "• " + v)]);
  showOk("Готово");
}

async function findByCaveDepth() {
  const max = encodeURIComponent($("caveMax").value.trim());
  const root = await call(S2, "GET", `/killer/dragon/find-by-cave-depth/${max}?mode=${$("caveMode").value}`);
  showResult(describeDragon(parseDragon(root)));
  showOk("Дракон найден");
}

async function createTeam() {
  const part = id => encodeURIComponent($(id).value.trim());
  const root = await call(S2, "POST",
    `/killer/teams/create/${part("teamId")}/${part("teamName")}/${part("teamSize")}/${part("teamCave")}`);
  showResult(["Команда создана:", `teamId: ${childText(root, "teamId")}`, `teamName: ${childText(root, "teamName")}`,
    `teamSize: ${childText(root, "teamSize")}`, `startCaveId: ${childText(root, "startCaveId")}`]);
  showOk("Команда создана");
}

// ---------- инициализация ----------
renderInputs($("filters"), "f_", false);
renderInputs($("formFields"), "d_", true);
$("sortBy").innerHTML = '<option value="">id</option>'
  + FIELDS.map(f => `<option value="${f.key}">${f.key}</option>`).join("");

$("apply").onclick = () => { page = 0; run(loadList); };
$("reset").onclick = () => {
  FIELDS.forEach(f => { $("f_" + f.key).value = ""; });
  $("sortBy").value = "";
  page = 0;
  run(loadList);
};
$("table").onclick = event => {
  const button = event.target.closest("button[data-action]");
  if (!button) return;
  const id = button.dataset.id;
  if (button.dataset.action === "edit") {
    fillForm(loaded.get(id));
  } else if (confirm(`Удалить дракона id=${id}?`)) {
    run(async () => {
      await call(S1, "DELETE", "/dragons/" + encodeURIComponent(id));
      showOk(`Дракон id=${id} удалён`);
      await loadList();
    });
  }
};
$("dragonForm").onsubmit = event => { event.preventDefault(); run(saveDragon); };
$("cancelEdit").onclick = resetForm;
$("getBtn").onclick = () => run(getById);
$("delWeightBtn").onclick = () => run(deleteByWeight);
$("avgBtn").onclick = () => run(averageAge);
$("descBtn").onclick = () => run(uniqueDescriptions);
$("caveBtn").onclick = () => run(findByCaveDepth);
$("teamBtn").onclick = () => run(createTeam);

run(loadList);
