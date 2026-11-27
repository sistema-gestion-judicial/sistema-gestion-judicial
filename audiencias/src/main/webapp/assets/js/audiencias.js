document.addEventListener("DOMContentLoaded", () => {
  const RESUMEN_URL = `${window.APP_CONTEXT || ""}/resumenAudiencia.jsp`;

  const hearingsData = [
    {
      expediente: "PEN-2025-0891",
      materia: "Familiar",
      recepcion: "02/10/2026",
      juzgado: "Juzgado de Zapata",
      mesa: "M-06",
      fecha: "26/10/2026",
      hora: "10:00 hrs",
      estatus: "Próxima"
    },
    {
      expediente: "CIV-2026-0412",
      materia: "Civil",
      recepcion: "08/10/2026",
      juzgado: "Juzgado Primero Civil",
      mesa: "M-02",
      fecha: "12/11/2026",
      hora: "11:30 hrs",
      estatus: "En curso"
    },
    {
      expediente: "PEN-2026-0118",
      materia: "Penal",
      recepcion: "15/09/2026",
      juzgado: "Juzgado de Control",
      mesa: "M-01",
      fecha: "15/11/2026",
      hora: "09:00 hrs",
      estatus: "Próxima"
    },
    {
      expediente: "FAM-2026-0309",
      materia: "Familiar",
      recepcion: "20/08/2026",
      juzgado: "Juzgado Segundo Familiar",
      mesa: "M-04",
      fecha: "03/10/2026",
      hora: "13:00 hrs",
      estatus: "Concluida"
    },
    {
      expediente: "PEN-2026-0540",
      materia: "Penal",
      recepcion: "01/10/2026",
      juzgado: "Juzgado de Zapata",
      mesa: "M-03",
      fecha: "18/11/2026",
      hora: "12:00 hrs",
      estatus: "Próxima"
    },
    {
      expediente: "CIV-2025-0987",
      materia: "Civil",
      recepcion: "10/07/2026",
      juzgado: "Juzgado Tercero Civil",
      mesa: "M-05",
      fecha: "29/09/2026",
      hora: "16:00 hrs",
      estatus: "Concluida"
    }
  ];

  const profileButton = document.getElementById("profileButton");
  const profileMenu = document.getElementById("profileMenu");
  const filterForm = document.getElementById("hearingFilters");
  const caseSearch = document.getElementById("caseSearch");
  const subjectFilter = document.getElementById("subjectFilter");
  const statusTabs = [...document.querySelectorAll(".tab-button")];
  const tbody = document.getElementById("hearingRows");
  const recordCount = document.getElementById("recordCount");
  const paginationSummary = document.getElementById("paginationSummary");
  const paginationNav = document.getElementById("pagination");

  let activeStatus = "Todas";
  let currentPage = 1;
  const rowsPerPage = 5;

  if (profileButton && profileMenu) {
    profileButton.addEventListener("click", () => {
      const expanded = profileButton.getAttribute("aria-expanded") === "true";
      profileButton.setAttribute("aria-expanded", String(!expanded));
      profileMenu.classList.toggle("is-open", !expanded);
    });

    document.addEventListener("click", (event) => {
      if (event.target instanceof Node && !profileButton.contains(event.target) && !profileMenu.contains(event.target)) {
        profileButton.setAttribute("aria-expanded", "false");
        profileMenu.classList.remove("is-open");
      }
    });
  }

  function updateStats() {
    const totalEl = document.getElementById("statTotal");
    const proximasEl = document.getElementById("statProximas");
    const enCursoEl = document.getElementById("statEnCurso");
    const concluidasEl = document.getElementById("statConcluidas");

    if (totalEl) totalEl.textContent = hearingsData.length;
    if (proximasEl) proximasEl.textContent = hearingsData.filter(h => h.estatus === "Próxima").length;
    if (enCursoEl) enCursoEl.textContent = hearingsData.filter(h => h.estatus === "En curso").length;
    if (concluidasEl) concluidasEl.textContent = hearingsData.filter(h => h.estatus === "Concluida").length;
  }

  function getFilteredHearings() {
    const query = (caseSearch?.value || "").trim().toLowerCase();
    const subject = subjectFilter?.value || "";

    return hearingsData.filter((item) => {
      const matchesStatus = activeStatus === "Todas" || item.estatus === activeStatus;
      const matchesSubject = !subject || item.materia === subject;
      const matchesQuery =
        !query ||
        item.expediente.toLowerCase().includes(query) ||
        item.juzgado.toLowerCase().includes(query);

      return matchesStatus && matchesSubject && matchesQuery;
    });
  }

  function renderTable() {
    if (!tbody) return;
    const filtered = getFilteredHearings();
    const totalItems = filtered.length;
    const totalPages = Math.max(1, Math.ceil(totalItems / rowsPerPage));

    if (currentPage > totalPages) currentPage = totalPages;

    const startIndex = (currentPage - 1) * rowsPerPage;
    const pageItems = filtered.slice(startIndex, startIndex + rowsPerPage);

    if (recordCount) {
      recordCount.textContent = `${totalItems} ${totalItems === 1 ? "registro" : "registros"}`;
    }

    tbody.replaceChildren();

    if (pageItems.length === 0) {
      const tr = document.createElement("tr");
      const td = document.createElement("td");
      td.colSpan = 7;
      td.style.textAlign = "center";
      td.style.color = "var(--muted)";
      td.textContent = "No se encontraron audiencias con los filtros seleccionados.";
      tr.append(td);
      tbody.append(tr);
    } else {
      for (const item of pageItems) {
        const tr = document.createElement("tr");
        tr.innerHTML = `
          <td>${item.expediente}</td>
          <td>${item.materia}</td>
          <td>${item.juzgado} · ${item.mesa}</td>
          <td>${item.fecha}</td>
          <td>${item.hora}</td>
          <td>${item.estatus}</td>
          <td>
            <button type="button" class="view-button" data-expediente="${item.expediente}">
              Ver
              <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
                <path d="M4 10h12m-5-5 5 5-5 5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </td>
        `;
        tbody.append(tr);
      }
    }

    renderPagination(totalItems, totalPages, startIndex, pageItems.length);
  }

  function renderPagination(totalItems, totalPages, startIndex, shownCount) {
    if (paginationSummary) {
      if (totalItems === 0) {
        paginationSummary.textContent = "Mostrando 0 de 0 registros";
      } else {
        paginationSummary.textContent = `Mostrando ${startIndex + 1}–${startIndex + shownCount} de ${totalItems} registros`;
      }
    }

    if (!paginationNav) return;
    paginationNav.replaceChildren();

    const prevBtn = document.createElement("button");
    prevBtn.type = "button";
    prevBtn.className = "page-button arrow";
    prevBtn.textContent = "‹";
    prevBtn.disabled = currentPage === 1;
    prevBtn.setAttribute("aria-label", "Página anterior");
    prevBtn.addEventListener("click", () => {
      if (currentPage > 1) {
        currentPage--;
        renderTable();
      }
    });
    paginationNav.append(prevBtn);

    for (let page = 1; page <= totalPages; page++) {
      const btn = document.createElement("button");
      btn.type = "button";
      btn.className = `page-button${page === currentPage ? " current" : ""}`;
      btn.textContent = String(page);
      btn.addEventListener("click", () => {
        currentPage = page;
        renderTable();
      });
      paginationNav.append(btn);
    }

    const nextBtn = document.createElement("button");
    nextBtn.type = "button";
    nextBtn.className = "page-button arrow";
    nextBtn.textContent = "›";
    nextBtn.disabled = currentPage === totalPages;
    nextBtn.setAttribute("aria-label", "Página siguiente");
    nextBtn.addEventListener("click", () => {
      if (currentPage < totalPages) {
        currentPage++;
        renderTable();
      }
    });
    paginationNav.append(nextBtn);
  }

  if (tbody) {
    tbody.addEventListener("click", (event) => {
      const btn = event.target instanceof Element ? event.target.closest(".view-button") : null;
      if (!btn) return;

      const expedienteId = btn.dataset.expediente;
      const selected = hearingsData.find((h) => h.expediente === expedienteId);
      if (selected) {
        sessionStorage.setItem("selectedHearing", JSON.stringify(selected));
      }
      window.location.href = `${RESUMEN_URL}?expediente=${encodeURIComponent(expedienteId)}`;
    });
  }

  if (filterForm) {
    filterForm.addEventListener("submit", (event) => {
      event.preventDefault();
      currentPage = 1;
      renderTable();
    });
  }

  if (caseSearch) {
    caseSearch.addEventListener("input", () => {
      currentPage = 1;
      renderTable();
    });
  }

  for (const tab of statusTabs) {
    tab.addEventListener("click", () => {
      statusTabs.forEach((t) => t.setAttribute("aria-selected", "false"));
      tab.setAttribute("aria-selected", "true");
      activeStatus = tab.dataset.status || "Todas";
      currentPage = 1;
      renderTable();
    });
  }

  updateStats();
  renderTable();
});
