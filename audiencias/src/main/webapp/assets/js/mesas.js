document.addEventListener("DOMContentLoaded", () => {
  const pageSize = 4;
  let currentPage = 1;
  let searchQuery = "";
  let activeMateria = "";

  const rows = document.getElementById("mesasRows");
  const pagination = document.getElementById("pagination");
  const summary = document.getElementById("paginationSummary");

  const modalRegistrar = document.getElementById("modalRegistrarMesa");
  const modalEditar = document.getElementById("modalEditarMesa");

  // Lista de mesas en memoria
  let mesasData = Array.from({ length: 36 }, (_, index) => ({
    id: index + 1,
    numero: `M-0${(index % 5) + 1}`,
    juzgado: `Juzgado ${(index % 3) + 1}°`,
    materia: index % 3 === 0 ? "Familiar" : index % 3 === 1 ? "Civil" : "Penal",
    activo: true
  }));

  function getFilteredData() {
    const query = searchQuery.trim().toLowerCase();
    return mesasData.filter(mesa => {
      const matchesSearch = !query || mesa.numero.toLowerCase().includes(query) || mesa.juzgado.toLowerCase().includes(query);
      const matchesMateria = !activeMateria || mesa.materia === activeMateria;
      return matchesSearch && matchesMateria;
    });
  }

  function renderRows() {
    if (!rows) return;
    rows.replaceChildren();

    const filtered = getFilteredData();
    const totalRecords = filtered.length;
    const startIndex = (currentPage - 1) * pageSize;
    const count = Math.min(pageSize, Math.max(0, totalRecords - startIndex));

    for (let i = 0; i < count; i += 1) {
      const item = filtered[startIndex + i];
      const row = document.createElement("tr");

      // Columna N-Mesa
      const tdNumero = document.createElement("td");
      tdNumero.textContent = item.numero;
      tdNumero.style.fontWeight = "650";

      // Columna Juzgado Asignado
      const tdJuzgado = document.createElement("td");
      tdJuzgado.textContent = item.juzgado;

      // Columna Materia
      const tdMateria = document.createElement("td");
      tdMateria.textContent = item.materia;

      // Columna Estado
      const tdEstado = document.createElement("td");
      const badge = document.createElement("span");
      badge.className = item.activo ? "badge-status active" : "badge-status inactive";
      badge.textContent = item.activo ? "Activo" : "Inactivo";
      tdEstado.append(badge);

      // Columna Acciones (Switch + Botón Lápiz)
      const tdAcciones = document.createElement("td");
      tdAcciones.style.textAlign = "right";

      const actionsWrap = document.createElement("div");
      actionsWrap.className = "table-actions-cell";

      // Toggle Switch de tabla
      const switchLabel = document.createElement("label");
      switchLabel.className = "switch-toggle";

      const switchInput = document.createElement("input");
      switchInput.type = "checkbox";
      switchInput.checked = item.activo;

      switchInput.addEventListener("change", () => {
        item.activo = switchInput.checked;
        badge.className = item.activo ? "badge-status active" : "badge-status inactive";
        badge.textContent = item.activo ? "Activo" : "Inactivo";
      });

      const switchSlider = document.createElement("span");
      switchSlider.className = "slider";
      switchLabel.append(switchInput, switchSlider);

      // Botón Editar (Abre el modal)
      const editBtn = document.createElement("button");
      editBtn.type = "button";
      editBtn.className = "btn-icon-action";
      editBtn.setAttribute("aria-label", `Editar ${item.numero}`);
      editBtn.innerHTML = `<svg viewBox="0 0 20 20" fill="none" width="16" height="16"><path d="M13.5 3.5l3 3L6.5 16.5H3.5v-3L13.5 3.5z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>`;

      // Evento para abrir el modal de edición
      editBtn.addEventListener("click", () => {
        openEditModal(item);
      });

      actionsWrap.append(switchLabel, editBtn);
      tdAcciones.append(actionsWrap);

      row.append(tdNumero, tdJuzgado, tdMateria, tdEstado, tdAcciones);
      rows.append(row);
    }

    if (count === 0) {
      const emptyRow = document.createElement("tr");
      const emptyTd = document.createElement("td");
      emptyTd.colSpan = 5;
      emptyTd.style.textAlign = "center";
      emptyTd.style.padding = "20px";
      emptyTd.textContent = "No se encontraron mesas";
      emptyRow.append(emptyTd);
      rows.append(emptyRow);
    }

    const start = totalRecords ? startIndex + 1 : 0;
    const end = Math.min(startIndex + pageSize, totalRecords);
    if (summary) summary.textContent = `Mostrando ${start}–${end} de ${totalRecords}`;

    renderPagination(totalRecords);
  }

  function renderPagination(totalRecords) {
    if (!pagination) return;
    pagination.replaceChildren();

    const pages = Math.ceil(totalRecords / pageSize);
    if (!pages) return;

    const prevBtn = document.createElement("button");
    prevBtn.type = "button";
    prevBtn.className = "page-button arrow";
    prevBtn.textContent = "<";
    prevBtn.disabled = currentPage === 1;
    prevBtn.addEventListener("click", () => {
      if (currentPage > 1) {
        currentPage -= 1;
        renderRows();
      }
    });
    pagination.append(prevBtn);

    const visiblePages = new Set([1, currentPage, Math.min(pages, currentPage + 1), pages]);
    let prevPage = 0;

    for (const page of [...visiblePages].sort((a, b) => a - b)) {
      if (page - prevPage > 1) {
        const ellipsis = document.createElement("span");
        ellipsis.className = "ellipsis";
        ellipsis.textContent = "..";
        pagination.append(ellipsis);
      }

      const pBtn = document.createElement("button");
      pBtn.type = "button";
      pBtn.className = `page-button${page === currentPage ? " current" : ""}`;
      pBtn.textContent = String(page);

      pBtn.addEventListener("click", () => {
        if (page !== currentPage) {
          currentPage = page;
          renderRows();
        }
      });

      pagination.append(pBtn);
      prevPage = page;
    }

    const nextBtn = document.createElement("button");
    nextBtn.type = "button";
    nextBtn.className = "page-button arrow";
    nextBtn.textContent = ">";
    nextBtn.disabled = currentPage === pages;
    nextBtn.addEventListener("click", () => {
      if (currentPage < pages) {
        currentPage += 1;
        renderRows();
      }
    });
    pagination.append(nextBtn);
  }

  // --- LÓGICA DE MODALES ---

  // Abrir Modal Registrar
  const btnRegisterMesa = document.getElementById("btnRegisterMesa");
  if (btnRegisterMesa && modalRegistrar) {
    btnRegisterMesa.addEventListener("click", () => {
      const form = document.getElementById("formRegistrarMesa");
      if (form) form.reset();
      modalRegistrar.showModal();
    });
  }

  const btnCancelReg = document.getElementById("btnCancelReg");
  if (btnCancelReg && modalRegistrar) {
    btnCancelReg.addEventListener("click", () => modalRegistrar.close());
  }

  // Abrir Modal Editar con datos
  function openEditModal(item) {
    if (!modalEditar) return;
    document.getElementById("editMesaId").value = item.id;
    document.getElementById("editNumero").value = item.numero;
    document.getElementById("editJuzgado").value = item.juzgado;
    document.getElementById("editMateria").value = item.materia;
    document.getElementById("editEstado").checked = item.activo;

    modalEditar.showModal();
  }

  const btnCancelEdit = document.getElementById("btnCancelEdit");
  if (btnCancelEdit && modalEditar) {
    btnCancelEdit.addEventListener("click", () => modalEditar.close());
  }

  // Cerrar al dar clic fuera del cuadro blanco
  [modalRegistrar, modalEditar].forEach(modal => {
    if (modal) {
      modal.addEventListener("click", (e) => {
        if (e.target === modal) modal.close();
      });
    }
  });

  // Guardar Nuevo Registro
  const formRegistrar = document.getElementById("formRegistrarMesa");
  if (formRegistrar) {
    formRegistrar.addEventListener("submit", (e) => {
      e.preventDefault();
      const newMesa = {
        id: mesasData.length + 1,
        numero: document.getElementById("regNumero").value.trim(),
        juzgado: document.getElementById("regJuzgado").value,
        materia: document.getElementById("regMateria").value,
        activo: document.getElementById("regEstado").checked
      };

      mesasData.unshift(newMesa);
      modalRegistrar.close();
      currentPage = 1;
      renderRows();
    });
  }

  // Guardar Cambios de Edición
  const formEditar = document.getElementById("formEditarMesa");
  if (formEditar) {
    formEditar.addEventListener("submit", (e) => {
      e.preventDefault();
      const id = parseInt(document.getElementById("editMesaId").value, 10);
      const target = mesasData.find(m => m.id === id);

      if (target) {
        target.numero = document.getElementById("editNumero").value.trim();
        target.juzgado = document.getElementById("editJuzgado").value;
        target.materia = document.getElementById("editMateria").value;
        target.activo = document.getElementById("editEstado").checked;
      }

      modalEditar.close();
      renderRows();
    });
  }

  // Filtros
  const filterForm = document.getElementById("mesasFilters");
  if (filterForm) {
    filterForm.addEventListener("submit", (e) => {
      e.preventDefault();
      searchQuery = document.getElementById("expedienteSearch").value;
      activeMateria = document.getElementById("materiaFilter").value;
      currentPage = 1;
      renderRows();
    });
  }

  const btnReset = document.getElementById("btnResetFilters");
  if (btnReset) {
    btnReset.addEventListener("click", () => {
      document.getElementById("expedienteSearch").value = "";
      document.getElementById("materiaFilter").value = "";
      searchQuery = "";
      activeMateria = "";
      currentPage = 1;
      renderRows();
    });
  }

  // Menú Perfil
  const profileButton = document.getElementById("profileButton");
  const profileMenu = document.getElementById("profileMenu");
  if (profileButton && profileMenu) {
    profileButton.addEventListener("click", () => {
      const expanded = profileButton.getAttribute("aria-expanded") === "true";
      profileButton.setAttribute("aria-expanded", String(!expanded));
      profileMenu.classList.toggle("is-open", !expanded);
    });

    document.addEventListener("click", (e) => {
      if (!profileButton.contains(e.target)) {
        profileButton.setAttribute("aria-expanded", "false");
        profileMenu.classList.remove("is-open");
      }
    });
  }

  renderRows();
});
