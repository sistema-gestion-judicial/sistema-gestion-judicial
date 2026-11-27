"use strict";

/**
 * Administración de mesas. Habla con el servlet /mesas:
 *   GET  ?action=list|lawyers|courts      POST action=create|update|toggle
 * El contexto de la aplicación llega en <body data-ctx> y el token CSRF en <meta name="csrf-token">.
 */
(() => {
  // Si el script se cargara dos veces, la segunda copia no registra otra tanda de listeners
  if (window.__mesasInitialized) return;
  window.__mesasInitialized = true;

  document.addEventListener("DOMContentLoaded", init);

  const PAGE_SIZE = 4;

  class ApiError extends Error {
    constructor(message, status) {
      super(message);
      this.status = status;
    }
  }

  function init() {
    const ctx = document.body.dataset.ctx || "";
    const BASE_URL = `${ctx}/mesas`;
    const LOGIN_URL = `${ctx}/index.jsp`;
    const csrfToken = document.querySelector('meta[name="csrf-token"]')?.content || "";

    const $ = (id) => document.getElementById(id);

    const rows = $("mesasRows");
    const pagination = $("pagination");
    const summary = $("paginationSummary");
    const modalRegistrar = $("modalRegistrarMesa");
    const modalEditar = $("modalEditarMesa");

    let currentPage = 1;
    let searchQuery = "";
    let activeMateria = "";
    let loadSequence = 0;
    let busy = false;

    let mesasData = [];
    let lawyersList = [];
    let courtsList = [];
    const courtsMap = new Map();

    // ---------- Red ----------

    async function fetchJson(url, options = {}) {
      const res = await fetch(url, {
        credentials: "same-origin",
        ...options,
        headers: { Accept: "application/json", ...(options.headers || {}) }
      });

      if (res.status === 401) {
        window.location.assign(LOGIN_URL);
        throw new ApiError("Sesión no iniciada.", 401);
      }

      const isJson = (res.headers.get("Content-Type") || "").includes("application/json");
      const body = isJson ? await res.json() : null;

      if (!res.ok) {
        throw new ApiError(body?.message || `El servidor respondió con el error ${res.status}.`, res.status);
      }
      if (body === null) {
        throw new ApiError("La respuesta del servidor no es válida.", res.status);
      }
      return body;
    }

    function post(params) {
      return fetchJson(BASE_URL, {
        method: "POST",
        headers: { "X-CSRF-Token": csrfToken },
        body: new URLSearchParams(params)
      });
    }

    function showError(err, fallback = "No se pudo completar la operación.") {
      if (err instanceof ApiError && err.status === 401) return; // ya se está redirigiendo al login
      console.error(err);
      alert(err instanceof Error && err.message ? err.message : fallback);
    }

    // ---------- Catálogos ----------

    function formatCourt(courtId) {
      if (!courtId) return "Sin asignar";
      const court = courtsMap.get(courtId);
      return court ? court.name : `Juzgado ${courtId}`;
    }

    async function loadCourts() {
      try {
        const json = await fetchJson(`${BASE_URL}?action=courts`);
        courtsList = Array.isArray(json.data) ? json.data : [];
        courtsMap.clear();
        courtsList.forEach((c) => courtsMap.set(c.code, c));
        populateCourtSelect($("regJuzgado"));
      } catch (err) {
        console.error("Error cargando juzgados:", err);
        const select = $("regJuzgado");
        select.replaceChildren(placeholder("No se pudo cargar el catálogo de juzgados", true));
      }
    }

    async function loadLawyers() {
      try {
        const json = await fetchJson(`${BASE_URL}?action=lawyers`);
        lawyersList = Array.isArray(json) ? json : [];
        populateLawyerSelect($("regAbogado"), "Selecciona un abogado", true, null);
      } catch (err) {
        console.error("Error cargando abogados:", err);
        $("regAbogado").replaceChildren(placeholder("No se pudo cargar la lista de abogados", true));
      }
    }

    function placeholder(text, disabled) {
      const option = new Option(text, "");
      option.disabled = disabled;
      option.selected = true;
      return option;
    }

    /** Solo juzgados activos; la mesa que se edita conserva el suyo con ensureOption. */
    function populateCourtSelect(select) {
      select.replaceChildren(placeholder("Selecciona un juzgado", true));
      courtsList
        .filter((c) => c.active)
        .forEach((c) => select.append(new Option(c.name, c.code)));
    }

    /**
     * Abogados activos. Los que ya tienen otra mesa salen deshabilitados; el de la mesa
     * que se edita (currentDeskId) sigue disponible.
     */
    function populateLawyerSelect(select, placeholderText, placeholderDisabled, currentDeskId) {
      select.replaceChildren(placeholder(placeholderText, placeholderDisabled));
      lawyersList.forEach((lawyer) => {
        const takenElsewhere = lawyer.deskId != null && lawyer.deskId !== currentDeskId;
        const option = new Option(
          takenElsewhere ? `${lawyer.fullName} (asignado a otra mesa)` : lawyer.fullName,
          String(lawyer.id)
        );
        option.disabled = takenElsewhere;
        select.append(option);
      });
    }

    /** Agrega la opción si el select no la trae (juzgado desactivado, materia fuera de la lista…). */
    function ensureOption(select, value, label) {
      if (!value) return;
      if ([...select.options].some((o) => o.value === value)) return;
      select.append(new Option(label, value));
    }

    function updateDistrict(prefix) {
      const court = courtsMap.get($(`${prefix}Juzgado`).value);
      $(`${prefix}Distrito`).value = court?.judicialDistrict || "";
    }

    $("regJuzgado").addEventListener("change", () => updateDistrict("reg"));
    $("editJuzgado").addEventListener("change", () => updateDistrict("edit"));

    // ---------- Validación de formularios ----------

    /** Devuelve null si no se eligió abogado; lanza si el valor no es un id válido. */
    function parseUserId(raw) {
      if (raw === "" || raw == null) return null;
      const id = Number(raw);
      if (!Number.isInteger(id) || id <= 0) {
        throw new Error("El abogado seleccionado no es válido.");
      }
      return id;
    }

    function deskParams(action, fields) {
      const params = {
        action,
        deskName: fields.deskName.trim(),
        courtId: fields.courtId,
        matter: fields.matter,
        active: String(fields.active)
      };
      if (fields.id != null) params.id = String(fields.id);
      const userId = parseUserId(fields.userId);
      if (userId !== null) params.userId = String(userId);
      return params;
    }

    // ---------- Mesas ----------

    async function loadMesas() {
      const sequence = ++loadSequence;
      const params = new URLSearchParams({
        action: "list",
        search: searchQuery.trim(),
        matter: activeMateria
      });

      try {
        const data = await fetchJson(`${BASE_URL}?${params}`);
        if (sequence !== loadSequence) return; // llegó una respuesta más nueva
        mesasData = Array.isArray(data) ? data : [];
        currentPage = 1;
        renderRows();
      } catch (err) {
        if (sequence !== loadSequence) return;
        console.error(err);
        if (err instanceof ApiError && err.status === 401) return;
        rows.replaceChildren(messageRow("Error al cargar las mesas"));
        summary.textContent = "";
        pagination.replaceChildren();
      }
    }

    function messageRow(text) {
      const tr = document.createElement("tr");
      const td = document.createElement("td");
      td.colSpan = 5;
      td.style.textAlign = "center";
      td.style.padding = "20px";
      td.textContent = text;
      tr.append(td);
      return tr;
    }

    function setBadge(badge, active) {
      badge.className = active ? "badge-status active" : "badge-status inactive";
      badge.textContent = active ? "Activo" : "Inactivo";
    }

    function renderRows() {
      rows.replaceChildren();

      const total = mesasData.length;
      const startIndex = (currentPage - 1) * PAGE_SIZE;
      const pageItems = mesasData.slice(startIndex, startIndex + PAGE_SIZE);

      pageItems.forEach((item) => rows.append(buildRow(item)));
      if (pageItems.length === 0) rows.append(messageRow("No se encontraron mesas"));

      const start = total ? startIndex + 1 : 0;
      const end = Math.min(startIndex + PAGE_SIZE, total);
      summary.textContent = `Mostrando ${start}–${end} de ${total}`;
      renderPagination(total);
    }

    function buildRow(item) {
      const row = document.createElement("tr");

      const tdNumero = document.createElement("td");
      tdNumero.textContent = item.deskName;
      tdNumero.style.fontWeight = "650";

      const tdJuzgado = document.createElement("td");
      tdJuzgado.textContent = formatCourt(item.courtId);

      const tdMateria = document.createElement("td");
      tdMateria.textContent = item.matter;

      const tdEstado = document.createElement("td");
      const badge = document.createElement("span");
      setBadge(badge, item.active);
      tdEstado.append(badge);

      const switchInput = document.createElement("input");
      switchInput.type = "checkbox";
      switchInput.checked = Boolean(item.active);
      switchInput.setAttribute("aria-label", `Activar o desactivar ${item.deskName}`);
      switchInput.addEventListener("change", async () => {
        const nextActive = switchInput.checked;
        switchInput.disabled = true;
        try {
          await post({ action: "toggle", id: String(item.id), active: String(nextActive) });
          item.active = nextActive;
          setBadge(badge, nextActive);
        } catch (err) {
          switchInput.checked = !nextActive;
          showError(err);
        } finally {
          switchInput.disabled = false;
        }
      });

      const switchLabel = document.createElement("label");
      switchLabel.className = "switch-toggle";
      const switchSlider = document.createElement("span");
      switchSlider.className = "slider";
      switchLabel.append(switchInput, switchSlider);

      const editBtn = document.createElement("button");
      editBtn.type = "button";
      editBtn.className = "btn-icon-action";
      editBtn.setAttribute("aria-label", `Editar ${item.deskName}`);
      editBtn.innerHTML = '<svg viewBox="0 0 20 20" fill="none" width="16" height="16" aria-hidden="true"><path d="M13.5 3.5l3 3L6.5 16.5H3.5v-3L13.5 3.5z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>';
      editBtn.addEventListener("click", () => openEditModal(item));

      const actionsWrap = document.createElement("div");
      actionsWrap.className = "table-actions-cell";
      actionsWrap.append(switchLabel, editBtn);

      const tdAcciones = document.createElement("td");
      tdAcciones.style.textAlign = "right";
      tdAcciones.append(actionsWrap);

      row.append(tdNumero, tdJuzgado, tdMateria, tdEstado, tdAcciones);
      return row;
    }

    function pageButton(text, className, disabled, onClick) {
      const btn = document.createElement("button");
      btn.type = "button";
      btn.className = className;
      btn.textContent = text;
      btn.disabled = disabled;
      btn.addEventListener("click", onClick);
      return btn;
    }

    function renderPagination(total) {
      pagination.replaceChildren();
      const pages = Math.ceil(total / PAGE_SIZE);
      if (!pages) return;

      const goTo = (page) => {
        if (page >= 1 && page <= pages && page !== currentPage) {
          currentPage = page;
          renderRows();
        }
      };

      pagination.append(pageButton("<", "page-button arrow", currentPage === 1, () => goTo(currentPage - 1)));

      const visiblePages = new Set([1, currentPage, Math.min(pages, currentPage + 1), pages]);
      let previous = 0;
      for (const page of [...visiblePages].sort((a, b) => a - b)) {
        if (page - previous > 1) {
          const ellipsis = document.createElement("span");
          ellipsis.className = "ellipsis";
          ellipsis.textContent = "..";
          pagination.append(ellipsis);
        }
        const btn = pageButton(String(page), `page-button${page === currentPage ? " current" : ""}`, false,
          () => goTo(page));
        if (page === currentPage) btn.setAttribute("aria-current", "page");
        pagination.append(btn);
        previous = page;
      }

      pagination.append(pageButton(">", "page-button arrow", currentPage === pages, () => goTo(currentPage + 1)));
    }

    // ---------- Modales ----------

    $("btnRegisterMesa").addEventListener("click", () => {
      $("formRegistrarMesa").reset();
      // reset() deja seleccionada la primera opción habilitada de los selects armados por JS
      populateCourtSelect($("regJuzgado"));
      populateLawyerSelect($("regAbogado"), "Selecciona un abogado", true, null);
      $("regDistrito").value = "";
      modalRegistrar.showModal();
    });
    $("btnCancelReg").addEventListener("click", () => modalRegistrar.close());
    $("btnCancelEdit").addEventListener("click", () => modalEditar.close());

    [modalRegistrar, modalEditar].forEach((modal) => {
      modal.addEventListener("click", (e) => {
        if (e.target === modal) modal.close();
      });
    });

    function openEditModal(item) {
      $("editMesaId").value = item.id;
      $("editNumero").value = item.deskName || "";

      // Juzgados activos + el que ya tiene la mesa, aunque Oficialía lo haya desactivado
      const courtSelect = $("editJuzgado");
      populateCourtSelect(courtSelect);
      if (item.courtId) {
        const court = courtsMap.get(item.courtId);
        ensureOption(courtSelect, item.courtId,
          court ? `${court.name} (inactivo)` : `Juzgado ${item.courtId} (fuera del catálogo)`);
      }
      courtSelect.value = item.courtId || "";
      updateDistrict("edit");

      ensureOption($("editMateria"), item.matter, item.matter);
      $("editMateria").value = item.matter || "";

      populateLawyerSelect($("editAbogado"), "Sin abogado asignado", false, item.id);
      $("editAbogado").value = item.userId != null ? String(item.userId) : "";

      $("editEstado").checked = Boolean(item.active);
      modalEditar.showModal();
    }

    /** Un solo envío a la vez: bloquea el botón mientras la petición está en curso. */
    async function submitDesk(form, modal, buildParams) {
      if (busy) return;
      const submitButton = form.querySelector('[type="submit"]');
      busy = true;
      submitButton.disabled = true;
      try {
        await post(buildParams());
        modal.close();
        await Promise.all([loadMesas(), loadLawyers()]);
      } catch (err) {
        showError(err);
      } finally {
        busy = false;
        submitButton.disabled = false;
      }
    }

    $("formRegistrarMesa").addEventListener("submit", (e) => {
      e.preventDefault();
      submitDesk($("formRegistrarMesa"), modalRegistrar, () => {
        if ($("regAbogado").value === "") throw new Error("Selecciona un abogado de audiencia.");
        return deskParams("create", {
          deskName: $("regNumero").value,
          courtId: $("regJuzgado").value,
          matter: $("regMateria").value,
          userId: $("regAbogado").value,
          active: $("regEstado").checked
        });
      });
    });

    $("formEditarMesa").addEventListener("submit", (e) => {
      e.preventDefault();
      submitDesk($("formEditarMesa"), modalEditar, () => deskParams("update", {
        id: $("editMesaId").value,
        deskName: $("editNumero").value,
        courtId: $("editJuzgado").value,
        matter: $("editMateria").value,
        userId: $("editAbogado").value,
        active: $("editEstado").checked
      }));
    });

    // ---------- Filtros ----------

    $("mesasFilters").addEventListener("submit", (e) => {
      e.preventDefault();
      searchQuery = $("mesaSearch").value;
      activeMateria = $("materiaFilter").value;
      loadMesas();
    });

    $("btnResetFilters").addEventListener("click", () => {
      $("mesaSearch").value = "";
      $("materiaFilter").value = "";
      searchQuery = "";
      activeMateria = "";
      loadMesas();
    });

    // ---------- Menú de perfil ----------

    const profileButton = $("profileButton");
    const profileMenu = $("profileMenu");
    const closeProfileMenu = () => {
      profileButton.setAttribute("aria-expanded", "false");
      profileMenu.classList.remove("is-open");
    };

    profileButton.addEventListener("click", () => {
      const expanded = profileButton.getAttribute("aria-expanded") === "true";
      profileButton.setAttribute("aria-expanded", String(!expanded));
      profileMenu.classList.toggle("is-open", !expanded);
    });
    document.addEventListener("click", (e) => {
      if (!profileButton.contains(e.target) && !profileMenu.contains(e.target)) closeProfileMenu();
    });
    document.addEventListener("keydown", (e) => {
      if (e.key === "Escape") closeProfileMenu();
    });

    // ---------- Arranque ----------

    Promise.all([loadCourts(), loadLawyers()]).then(loadMesas);
  }
})();
