const totalHearings = 36;
const pageSize = 4;
let currentPage = 1;
const rows = document.getElementById("hearingRows");
const pagination = document.getElementById("pagination");
const summary = document.getElementById("paginationSummary");
const dialog = document.getElementById("hearingDialog");

function hearingFor(index) {
  const base = (currentPage - 1) * pageSize + index + 1;
  if (currentPage === 1) {
    return { expediente: "PEN-2025-0891", mesa: "M-06", fecha: "26 oct. 2026", hora: "13:00" };
  }
  const time = `${String(8 + Math.floor((base - 1) / 4)).padStart(2, "0")}:00`;
  return {
    expediente: `PEN-2025-${String(891 + base - 1).padStart(4, "0")}`,
    mesa: `M-${String(5 + (base % 4)).padStart(2, "0")}`,
    fecha: "26 oct. 2026",
    hora: time
  };
}

function renderRows() {
  const rows = document.getElementById("hearingRows");
  const summary = document.getElementById("paginationSummary");
  if (!rows) return;

  rows.replaceChildren();
  const count = Math.min(pageSize, totalHearings - (currentPage - 1) * pageSize);

  for (let index = 0; index < count; index += 1) {
    const hearing = hearingFor(index);
    const row = document.createElement("tr");

    row.innerHTML =
      `<td>${hearing.expediente}</td>` +
      `<td>${hearing.mesa}</td>` +
      `<td>${hearing.fecha}</td>` +
      `<td>${hearing.hora}</td>` +
      `<td><button class="view-button" type="button" aria-label="Ver audiencia ${hearing.expediente}">` +
      `<svg viewBox="0 0 20 20" fill="none" aria-hidden="true" width="16" height="16"><path d="M1.5 10s3.1-5.3 8.5-5.3 8.5 5.3 8.5 5.3-3.1 5.3-8.5 5.3S1.5 10 1.5 10z" stroke="currentColor" stroke-width="1.8"/><circle cx="10" cy="10" r="2.5" stroke="currentColor" stroke-width="1.8"/></svg>` +
      `<span> Ver</span></button></td>`;

    const btn = row.querySelector(".view-button");
    if (btn) btn.addEventListener("click", () => openHearing(hearing));
    rows.append(row);
  }

  const start = (currentPage - 1) * pageSize + 1;
  const end = Math.min(currentPage * pageSize, totalHearings);

  if (summary) summary.textContent = `Mostrando ${start}-${end} de ${totalHearings}`;
  const recordCountEl = document.getElementById("recordCount");
  if (recordCountEl) recordCountEl.textContent = `${totalHearings} registros`;

  if (typeof renderPagination === "function") renderPagination();
}

document.addEventListener("DOMContentLoaded", () => {
  renderRows();
});

function pageButton (label, page, options = {}) {
  const button = document.createElement("button");
  button.type = "button";
  button.className = `page-button ${options.className ? options.className : ""}`;
  button.textContent = label;
  button.disabled = Boolean(options.disabled);
  if (options.current) button.setAttribute("aria-current", "page");
  button.setAttribute("aria-label", options.ariaLabel || `Página ${page}`);
  button.addEventListener("click", () => {
    if (page !== currentPage) {
      currentPage = page;
      renderRows();
    }
  });
  return button;
}

function renderPagination() {
  pagination.replaceChildren();
  const pages = Math.ceil(totalHearings / pageSize);
  pagination.append(pageButton("<", Math.max(1, currentPage - 1), {
    className: "arrow", disabled: currentPage === 1, ariaLabel: "Página anterior" }));
  const visiblePages = new Set ([1, currentPage, Math.min(pages, currentPage + 1), pages]);
  let previous = 0;
  for (const page of [...visiblePages].sort((a, b) => a - b)) {
    if (page - previous > 1) {
      const ellipsis = document.createElement("span");
      ellipsis.className = "ellipsis";
      ellipsis.textContent = "...";
      pagination.append(ellipsis);
    }
    pagination.append(pageButton (String(page), page, { current:
        page === currentPage, className: page === currentPage ? "current": "" }));
    previous = page;
  }
  pagination.append(pageButton(">", Math.min(pages, currentPage + 1), { className: "arrow", disabled: currentPage === pages, ariaLabel: "Página siguiente" }));
}

function openHearing (hearing) {
  document.getElementById("detailCase").textContent = hearing.expediente;
  document.getElementById("detailDesk").textContent = hearing.mesa;
  document.getElementById("detailDate").textContent = hearing.fecha;
  document.getElementById("detailTime").textContent = hearing.hora;
  dialog.showModal();
}

document.getElementById("closeDialog").addEventListener("click", () => dialog.close());
dialog.addEventListener("click", event => {
  if (event.target === dialog) dialog.close();
});

const profileButton = document.getElementById("profileButton");
const profileMenu = document.getElementById("profileMenu");
profileButton.addEventListener("click", () => {
  const expanded = profileButton.getAttribute("aria-expanded") === "true";
  profileButton.setAttribute("aria-expanded", String(!expanded));
  profileMenu.classList.toggle("is-open", !expanded);
});

document.addEventListener("click", event => {
  if (!profileButton.contains(event.target)) {
    profileButton.setAttribute("aria-expanded", "false");
    profileMenu.classList.remove("is-open");
  }
});

document.querySelectorAll(".nav-link").forEach (link => {
  link.addEventListener("click", () => {
    document.querySelector(".nav-link[aria-current='page']")?.removeAttribute("aria-current");
    link.setAttribute("aria-current", "page");
  });
});


