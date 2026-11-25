document.addEventListener("DOMContentLoaded", () => {
  const AUDIENCIAS_URL = `${window.APP_CONTEXT || ""}/inicioAudienciasAbogados.jsp`;

  const profileButton = document.getElementById("profileButton");
  const profileMenu = document.getElementById("profileMenu");
  const dialog = document.getElementById("actionDialog");
  const dialogTitle = document.getElementById("dialogTitle");
  const dialogMessage = document.getElementById("dialogMessage");
  const dialogActions = document.getElementById("dialogActions");
  const tabs = [...document.querySelectorAll(".case-tab")];

  function loadSelectedHearing() {
    const raw = sessionStorage.getItem("selectedHearing");
    const params = new URLSearchParams(window.location.search);
    const urlExpediente = params.get("expediente");

    if (!raw && !urlExpediente) return;

    try {
      const data = raw ? JSON.parse(raw) : null;
      const exp = (data && data.expediente) || urlExpediente;

      if (exp) {
        const titleEl = document.getElementById("case-title");
        const expEl = document.getElementById("detailExpediente");
        if (titleEl) titleEl.textContent = exp;
        if (expEl) expEl.textContent = exp;
      }

      if (data) {
        const map = {
          detailMateria: data.materia,
          detailRecepcion: data.recepcion,
          detailJuzgado: data.juzgado,
          detailMesa: data.mesa,
          detailFecha: data.fecha,
          timelineNextDate: data.fecha
        };

        for (const [id, value] of Object.entries(map)) {
          const el = document.getElementById(id);
          if (el && value) el.textContent = value;
        }
      }
    } catch {
    }
  }

  function showDialog(title, message, actions) {
    if (!dialog) return;
    if (dialogTitle) dialogTitle.textContent = title;
    if (dialogMessage) dialogMessage.textContent = message;
    if (dialogActions) {
      dialogActions.replaceChildren();
      for (const action of actions) {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "close-button";
        button.textContent = action.label;
        button.addEventListener("click", () => {
          dialog.close();
          if (action.run) action.run();
        });
        dialogActions.append(button);
      }
    }
    dialog.showModal();
  }

  // Menú de perfil
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

  // Navegación por pestañas
  function activateTab(tab) {
    for (const item of tabs) {
      const selected = item === tab;
      item.setAttribute("aria-selected", String(selected));
      item.tabIndex = selected ? 0 : -1;
      const control = document.getElementById(item.getAttribute("aria-controls"));
      if (control) control.hidden = !selected;
    }
  }

  for (const tab of tabs) {
    tab.addEventListener("click", () => activateTab(tab));
    tab.addEventListener("keydown", (event) => {
      if (event.key !== "ArrowRight" && event.key !== "ArrowLeft") return;
      event.preventDefault();
      const direction = event.key === "ArrowRight" ? 1 : -1;
      const currentIndex = tabs.indexOf(tab);
      const next = tabs[(currentIndex + direction + tabs.length) % tabs.length];
      activateTab(next);
      next.focus();
    });
  }

  // Vista previa de documentos
  document.querySelectorAll(".open-document").forEach((button) => {
    button.addEventListener("click", () => {
      const currentExp = document.getElementById("case-title")?.textContent || "PEN-2025-0891";
      showDialog(
        button.dataset.document || "Documento",
        `Vista previa del documento PDF del expediente ${currentExp}.`,
        [{ label: "Cerrar" }]
      );
    });
  });

  // Enlace a Expediente completo
  const viewAllFiles = document.getElementById("viewAllFiles");
  if (viewAllFiles) {
    viewAllFiles.addEventListener("click", () => {
      const tab = document.getElementById("tab-expediente");
      if (tab) {
        activateTab(tab);
        tab.focus();
      }
    });
  }

  // Volver a Audiencias
  const btnBack = document.getElementById("backToHearings");
  if (btnBack) {
    btnBack.addEventListener("click", () => {
      window.location.href = AUDIENCIAS_URL;
    });
  }

  if (dialog) {
    dialog.addEventListener("click", (event) => {
      if (event.target === dialog) dialog.close();
    });
  }

  if (tabs.length > 0) {
    tabs[0].tabIndex = 0;
    tabs.slice(1).forEach((tab) => { tab.tabIndex = -1; });
  }

  loadSelectedHearing();
});
