document.addEventListener("DOMContentLoaded", () => {
  const pageSize = 4;
  let currentPage = 1;
  let activeStatus = "Agendada"; // Inicia activo en "Agendado"
  let searchQuery = "";
  let activeSubject = "";

  const rows = document.getElementById("hearingRows");
  const thead = document.getElementById("hearingThead");
  const subtitle = document.getElementById("agendaSubtitle");
  const pagination = document.getElementById("pagination");
  const summary = document.getElementById("paginationSummary");
  const dialog = document.getElementById("hearingDialog");
  const cancelDialogBtn = document.getElementById("cancelDialog");
  const saveDialogBtn = document.getElementById("saveDialog");
  let selectedHearing = null;

  // Colección de datos para todos los estados
  const hearings = Array.from({ length: 26 }, (_, index) => ({
    expediente: `PEN-2025-${String(891 + index).padStart(4, "0")}`,
    mesa: `M-${String(1 + (index % 4)).padStart(2, "0")}`,
    materia: index % 2 === 0 ? "Familiar" : "Penal",
    juzgado: "Juzgado 1°",
    recepcion: "20-10-2026",
    fecha: "26 oct. 2026",
    hora: index < 2 ? "13:00" : `${String(9 + Math.floor(index / 4)).padStart(2, "0")}:${index % 2 ? "30" : "00"}`,
    estado: "Agendada"
  })).concat(
    Array.from({ length: 15 }, (_, index) => ({
      expediente: `PEN-2025-${String(920 + index).padStart(4, "0")}`,
      mesa: "M-06",
      materia: "Familiar",
      juzgado: "Juzgado 2°",
      recepcion: "03-11-2025",
      fecha: "",
      hora: "",
      estado: "Por agendar"
    })),
    Array.from({ length: 36 }, (_, index) => ({
      expediente: `PEN-2025-${String(891 + (index % 5)).padStart(4, "0")}`,
      mesa: "M-06",
      materia: "Familiar",
      juzgado: "Juzgado 2°",
      recepcion: "03-11-2025",
      fecha: "27 oct. 2026",
      hora: "11:00",
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

  // Renderiza los encabezados de tabla según la pestaña activa
  function renderHeader() {
    if (!thead) return;
    thead.replaceChildren();
    const tr = document.createElement("tr");

    // Tanto "Por agendar" como "Por reagendar" usan las 6 columnas con ícono de calendario
    if (activeStatus === "Por agendar" || activeStatus === "Por reagendar") {
      if (subtitle) {
        subtitle.textContent = activeStatus === "Por agendar"
          ? "Consulta las denuncias por agendar"
          : "Consulta las audiencias por reagendar";
      }
      ["Expediente", "Mesa", "Materia", "Juzgado", "Recepción", "Agendar"].forEach((text, i) => {
        const th = document.createElement("th");
        th.textContent = text;
        if (i === 5) th.style.textAlign = "center";
        tr.append(th);
      });
    } else {
      if (subtitle) subtitle.textContent = "Consulta tus audiencias asignadas";
      ["Expediente", "Mesa", "Fecha", "Hora", "Acciones"].forEach((text, i) => {
        const th = document.createElement("th");
        th.textContent = text;
        if (i === 4) th.style.textAlign = "right";
        tr.append(th);
      });
    }
    thead.append(tr);
  }

  function renderRows() {
    renderHeader();
    if (!rows) return;
    rows.replaceChildren();

    const visibleHearings = filteredHearings();
    const totalHearings = visibleHearings.length;
    const startIndex = (currentPage - 1) * pageSize;
    const count = Math.min(pageSize, Math.max(0, totalHearings - startIndex));

    for (let index = 0; index < count; index += 1) {
      const hearing = visibleHearings[startIndex + index];
      const row = document.createElement("tr");

      if (activeStatus === "Por agendar" || activeStatus === "Por reagendar") {
        // Estructura de 6 columnas de Figma
        [hearing.expediente, hearing.mesa, hearing.materia, hearing.juzgado, hearing.recepcion].forEach((val, i) => {
          const cell = document.createElement("td");
          cell.textContent = val;
          if (i === 0) cell.style.fontWeight = "700";
          row.append(cell);
        });

        // Ícono de calendario en la columna Agendar
        const actionCell = document.createElement("td");
        actionCell.style.textAlign = "center";

        const btnCalendar = document.createElement("button");
        btnCalendar.className = "btn-icon-action";
        btnCalendar.type = "button";
        btnCalendar.style.background = "transparent";
        btnCalendar.style.border = "none";
        btnCalendar.style.cursor = "pointer";
        btnCalendar.setAttribute("aria-label", `Agendar/Reagendar audiencia ${hearing.expediente}`);
        btnCalendar.innerHTML = `
          <svg viewBox="0 0 24 24" fill="none" width="22" height="22" stroke="#1f2937" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
            <line x1="16" y1="2" x2="16" y2="6"/>
            <line x1="8" y1="2" x2="8" y2="6"/>
            <line x1="3" y1="10" x2="21" y2="10"/>
            <circle cx="16" cy="16" r="3"/>
            <polyline points="16 15 16 16 17 16.5"/>
          </svg>
        `;

        btnCalendar.addEventListener("click", () => openHearing(hearing));
        actionCell.append(btnCalendar);
        row.append(actionCell);

      } else {
        // Estructura de 5 columnas para la pestaña "Agendado"
        [hearing.expediente, hearing.mesa, hearing.fecha, hearing.hora].forEach((val, i) => {
          const cell = document.createElement("td");
          cell.textContent = val;
          if (i === 0) cell.style.fontWeight = "700";
          row.append(cell);
        });

        const actionCell = document.createElement("td");
        actionCell.style.textAlign = "right";

        const viewButton = document.createElement("button");
        viewButton.className = "btn-icon-action";
        viewButton.type = "button";
        viewButton.setAttribute("aria-label", `Ver audiencia ${hearing.expediente}`);
        viewButton.innerHTML = `<svg viewBox="0 0 20 20" fill="none" aria-hidden="true" width="16" height="16"><path d="M1.5 10s3.1-5.3 8.5-5.3 8.5 5.3 8.5 5.3-3.1 5.3-8.5 5.3S1.5 10 1.5 10z" stroke="currentColor" stroke-width="1.8"/><circle cx="10" cy="10" r="2.5" stroke="currentColor" stroke-width="1.8"/></svg><span style="margin-left:4px;"> Ver</span>`;

        viewButton.addEventListener("click", () => openHearing(hearing));
        actionCell.append(viewButton);
        row.append(actionCell);
      }

      rows.append(row);
    }

    if (count === 0) {
      const emptyRow = document.createElement("tr");
      const emptyCell = document.createElement("td");
      emptyCell.colSpan = (activeStatus === "Por agendar" || activeStatus === "Por reagendar") ? 6 : 5;
      emptyCell.textContent = "No se encontraron audiencias";
      emptyCell.style.textAlign = "center";
      emptyCell.style.padding = "20px";
      emptyRow.append(emptyCell);
      rows.append(emptyRow);
    }

    const start = totalHearings ? startIndex + 1 : 0;
    const end = Math.min(startIndex + pageSize, totalHearings);

    if (summary) summary.textContent = `Mostrando ${start}–${end} de ${totalHearings}`;
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
      disabled: currentPage === 1
    }));

    const visiblePages = new Set([1, currentPage, Math.min(pages, currentPage + 1), pages]);
    let previous = 0;

    for (const page of [...visiblePages].sort((a, b) => a - b)) {
      if (page - previous > 1) {
        const ellipsis = document.createElement("span");
        ellipsis.className = "ellipsis";
        ellipsis.textContent = "..";
        pagination.append(ellipsis);
      }
      pagination.append(pageButton(String(page), page, {
        className: page === currentPage ? "current" : ""
      }));
      previous = page;
    }

    pagination.append(pageButton(">", Math.min(pages, currentPage + 1), {
      className: "arrow",
      disabled: currentPage === pages
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
        item.classList.toggle("active", item === tab);
      });
      renderRows();
    });
  });

  renderRows();
});
