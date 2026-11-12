document.addEventListener("DOMContentLoaded", () => {
  const pageSize = 2;
  let currentPage = 1;
  let activeStatus = "Agendada";
  let searchQuery = "";
  let activeSubject = "";

  const rows = document.getElementById("hearingRows");
  const pagination = document.getElementById("pagination");
  const summary = document.getElementById("paginationSummary");
  const dialog = document.getElementById("hearingDialog");
  const cancelDialogBtn = document.getElementById("cancelDialog");
  const saveDialogBtn = document.getElementById("saveDialog");
  let selectedHearing = null;

  const hearings = Array.from({ length: 26 }, (_, index) => {
    const number = 891 + Math.max(0, index);
    return {
      expediente: `PEN-2025-${String(number).padStart(4, "0")}`,
      mesa: `M-${String(3 + (index % 4)).padStart(2, "0")}`,
      fecha: "26 oct. 2026",
      hora: index < 2 ? "13:00" : `${String(9 + Math.floor(index / 4)).padStart(2, "0")}:${index % 2 ? "30" : "00"}`,
      materia: "Penal",
      estado: "Agendada"
    };
  }).concat(
    [{ expediente: "PEN-2025-0917", mesa: "M-03", fecha: "27 oct. 2026", hora: "09:00", materia: "Penal", estado: "Por agendar" }],
    Array.from({ length: 5 }, (_, index) => ({
      expediente: `PEN-2025-${String(918 + index).padStart(4, "0")}`,
      mesa: `M-${String(3 + (index % 4)).padStart(2, "0")}`,
      fecha: "27 oct. 2026",
      hora: `${String(10 + index).padStart(2, "0")}:00`,
      materia: "Penal",
      estado: "Por reagendar"
    }))
  );

  function filteredHearings() {
    const normalizedQuery = searchQuery.trim().toLocaleLowerCase("es");
    return hearings.filter(hearing =>
      hearing.estado === activeStatus &&
      hearing.expediente.toLocaleLowerCase("es").includes(normalizedQuery) &&
      (!activeSubject || hearing.materia === activeSubject)
    );
  }

  function renderRows() {
    if (!rows) return;
    rows.replaceChildren();

    const visibleHearings = filteredHearings();
    const totalHearings = visibleHearings.length;
    const startIndex = (currentPage - 1) * pageSize;
    const count = Math.min(pageSize, Math.max(0, totalHearings - startIndex));

    for (let index = 0; index < count; index += 1) {
      const hearing = visibleHearings[startIndex + index];
      const row = document.createElement("tr");

      [hearing.expediente, hearing.mesa, hearing.fecha, hearing.hora].forEach((val, i) => {
        const cell = document.createElement("td");
        cell.textContent = val;
        if (i === 0) cell.style.fontWeight = "650";
        row.append(cell);
      });

      const actionCell = document.createElement("td");
      actionCell.style.textAlign = "right";

      const viewButton = document.createElement("button");
      viewButton.className = "view-button";
      viewButton.type = "button";
      viewButton.setAttribute("aria-label", `Ver audiencia ${hearing.expediente}`);
      viewButton.innerHTML = `<svg viewBox="0 0 20 20" fill="none" aria-hidden="true" width="16" height="16"><path d="M1.5 10s3.1-5.3 8.5-5.3 8.5 5.3 8.5 5.3-3.1 5.3-8.5 5.3S1.5 10 1.5 10z" stroke="currentColor" stroke-width="1.8"/><circle cx="10" cy="10" r="2.5" stroke="currentColor" stroke-width="1.8"/></svg><span> Ver</span>`;

      viewButton.addEventListener("click", () => openHearing(hearing));
      actionCell.append(viewButton);
      row.append(actionCell);
      rows.append(row);
    }

    if (count === 0) {
      const emptyRow = document.createElement("tr");
      const emptyCell = document.createElement("td");
      emptyCell.colSpan = 5;
      emptyCell.textContent = "No se encontraron audiencias";
      emptyCell.style.textAlign = "center";
      emptyRow.append(emptyCell);
      rows.append(emptyRow);
    }

    const start = totalHearings ? startIndex + 1 : 0;
    const end = Math.min(startIndex + pageSize, totalHearings);

    if (summary) summary.textContent = `Mostrando ${start}-${end} de ${totalHearings}`;
    const recEl = document.getElementById("recordCount");
    if (recEl) recEl.textContent = `${totalHearings} ${totalHearings === 1 ? "registro" : "registros"}`;

    renderPagination();
  }

  function pageButton(label, page, options = {}) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = `page-button${options.className ? " " + options.className : ""}`;
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
    if (!pagination) return;
    pagination.replaceChildren();
    const pages = Math.ceil(filteredHearings().length / pageSize);
    if (!pages) return;

    pagination.append(pageButton("<", Math.max(1, currentPage - 1), {
      className: "arrow",
      disabled: currentPage === 1,
      ariaLabel: "Página anterior"
    }));

    const visiblePages = new Set([1, currentPage, Math.min(pages, currentPage + 1), pages]);
    let previous = 0;

    for (const page of [...visiblePages].sort((a, b) => a - b)) {
      if (page - previous > 1) {
        const ellipsis = document.createElement("span");
        ellipsis.className = "ellipsis";
        ellipsis.textContent = "...";
        pagination.append(ellipsis);
      }
      pagination.append(pageButton(String(page), page, {
        current: page === currentPage,
        className: page === currentPage ? "current" : ""
      }));
      previous = page;
    }

    pagination.append(pageButton(">", Math.min(pages, currentPage + 1), {
      className: "arrow",
      disabled: currentPage === pages,
      ariaLabel: "Página siguiente"
    }));
  }

  function parseSpanishDateToISO(dateStr) {
    if (!dateStr) return "";
    const months = { "ene.": "01", "feb.": "02", "mar.": "03", "abr.": "04", "may.": "05", "jun.": "06", "jul.": "07", "ago.": "08", "sep.": "09", "oct.": "10", "nov.": "11", "dic.": "12" };
    const parts = dateStr.split(" ");
    if (parts.length === 3) {
      const day = parts[0].padStart(2, "0");
      const month = months[parts[1].toLowerCase()] || "01";
      return `${parts[2]}-${month}-${day}`;
    }
    return dateStr;
  }

  function parseISOToSpanishDate(isoStr) {
    if (!isoStr) return "";
    const months = ["ene.", "feb.", "mar.", "abr.", "may.", "jun.", "jul.", "ago.", "sep.", "oct.", "nov.", "dic."];
    const [year, month, day] = isoStr.split("-");
    if (year && month && day) {
      return `${parseInt(day, 10)} ${months[parseInt(month, 10) - 1]} ${year}`;
    }
    return isoStr;
  }

  function openHearing(hearing) {
    if (!dialog) return;
    selectedHearing = hearing;
    document.getElementById("detailCase").textContent = hearing.expediente;
    document.getElementById("detailDesk").textContent = hearing.mesa;

    document.getElementById("detailDateInput").value = parseSpanishDateToISO(hearing.fecha);
    document.getElementById("detailTimeInput").value = hearing.hora;

    dialog.showModal();
  }

  if (cancelDialogBtn && dialog) {
    cancelDialogBtn.addEventListener("click", () => {
      selectedHearing = null;
      dialog.close();
    });
  }

  if (saveDialogBtn && dialog) {
    saveDialogBtn.addEventListener("click", () => {
      if (selectedHearing) {
        const newDateVal = document.getElementById("detailDateInput").value;
        const newTimeVal = document.getElementById("detailTimeInput").value;

        if (newDateVal) selectedHearing.fecha = parseISOToSpanishDate(newDateVal);
        if (newTimeVal) selectedHearing.hora = newTimeVal;

        if (selectedHearing.estado !== "Agendada") {
          selectedHearing.estado = "Agendada";
        }
        renderRows();
      }
      selectedHearing = null;
      dialog.close();
    });
  }

  if (dialog) {
    dialog.addEventListener("click", event => {
      if (event.target === dialog) {
        selectedHearing = null;
        dialog.close();
      }
    });
  }

  const profileButton = document.getElementById("profileButton");
  const profileMenu = document.getElementById("profileMenu");
  if (profileButton && profileMenu) {
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
  }

  const hearingFilters = document.getElementById("hearingFilters");
  if (hearingFilters) {
    hearingFilters.addEventListener("submit", event => {
      event.preventDefault();
      searchQuery = document.getElementById("caseSearch").value;
      activeSubject = document.getElementById("subjectFilter").value;
      currentPage = 1;
      renderRows();
    });
  }

  document.querySelectorAll(".tab-button").forEach(tab => {
    tab.addEventListener("click", () => {
      activeStatus = tab.dataset.status;
      currentPage = 1;
      document.querySelectorAll(".tab-button").forEach(item => {
        item.setAttribute("aria-selected", String(item === tab));
      });
      renderRows();
    });
  });

  renderRows();
});
