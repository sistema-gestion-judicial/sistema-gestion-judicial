// Asks for confirmation before activating or deactivating a catalog record. Each button marked with
// data-status-toggle carries the record code, its name and the status it would get; the modal shows the name
// and, on confirm, posts the shared status form.
document.addEventListener("DOMContentLoaded", () => {
  const modalElement = document.getElementById("statusModal");
  if (!modalElement) {
    return;
  }
  // The app shell is its own stacking context, so a modal left inside it would sit under the backdrop
  document.body.appendChild(modalElement);
  const modal = bootstrap.Modal.getOrCreateInstance(modalElement);
  const form = modalElement.querySelector("form");
  const title = modalElement.querySelector("[data-status-title]");
  const lead = modalElement.querySelector("[data-status-lead]");
  const note = modalElement.querySelector("[data-status-note]");
  const submit = modalElement.querySelector("[data-status-submit]");

  document.querySelectorAll("[data-status-toggle]").forEach((button) => {
    button.addEventListener("click", () => {
      const activate = button.dataset.active === "true";
      form.elements.code.value = button.dataset.code;
      form.elements.active.value = String(activate);
      title.textContent = activate ? button.dataset.titleOn : button.dataset.titleOff;
      lead.textContent = (activate ? "¿Desea activar " : "¿Desea desactivar ") + button.dataset.label
        + " «" + button.dataset.name + "»?";
      note.textContent = activate ? button.dataset.noteOn : button.dataset.noteOff;
      submit.textContent = activate ? "Sí, activar" : "Sí, desactivar";
      submit.classList.toggle("btn-danger", !activate);
      submit.classList.toggle("btn-primary", activate);
      modal.show();
    });
  });
});
