// Confirmation of the users list before an account is activated or deactivated (RF 1.7).
document.addEventListener("DOMContentLoaded", () => {
  const modalElement = document.querySelector("#statusModal");
  if (!modalElement) {
    return;
  }
  // The app shell is its own stacking context, so a modal left inside it would sit under the backdrop
  document.body.appendChild(modalElement);

  const texts = {
    inactive: {
      title: "Desactivar cuenta",
      question: (name) => `¿Desea desactivar la cuenta de ${name}?`,
      effect: "No podrá iniciar sesión. Las solicitudes que capturó se conservan con su nombre.",
      submit: "Sí, desactivar",
    },
    active: {
      title: "Activar cuenta",
      question: (name) => `¿Desea activar la cuenta de ${name}?`,
      effect: "Podrá volver a iniciar sesión con su contraseña.",
      submit: "Sí, activar",
    },
  };

  // Fills the confirmation with the user of the row that opened it; textContent keeps the name as plain text
  modalElement.addEventListener("show.bs.modal", (event) => {
    const trigger = event.relatedTarget;
    const nextStatus = trigger.dataset.nextStatus;
    const text = texts[nextStatus];
    modalElement.querySelector("[data-status-id]").value = trigger.dataset.userId;
    modalElement.querySelector("[data-status-value]").value = nextStatus;
    modalElement.querySelector("[data-status-title]").textContent = text.title;
    modalElement.querySelector("[data-status-question]").textContent = text.question(trigger.dataset.userName);
    modalElement.querySelector("[data-status-effect]").textContent = text.effect;
    modalElement.querySelector("[data-status-submit]").textContent = text.submit;
    modalElement.querySelector("#statusReason").value = "";
  });
});
