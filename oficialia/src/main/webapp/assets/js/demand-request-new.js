// Behaviour of the demand request registration.
document.addEventListener("DOMContentLoaded", () => {
  // Disables the fields of a party when it is marked as anonymous or unknown
  document.querySelectorAll("[data-party-toggle]").forEach((checkbox) => {
    const party = document.querySelector(`[data-party="${checkbox.dataset.partyToggle}"]`);
    const syncFields = () => {
      party.querySelectorAll("[data-party-field]").forEach((field) => {
        field.disabled = checkbox.checked;
      });
    };
    checkbox.addEventListener("change", syncFields);
    syncFields();
  });

  // RFC and CURP are always written in upper case
  document.querySelectorAll("[data-uppercase]").forEach((input) => {
    input.addEventListener("input", () => {
      input.value = input.value.toUpperCase();
    });
  });

  // Checks the size of the annexes before sending them: the server refuses them over the limit and the
  // capture would have to start again
  const annexes = document.querySelector("[data-max-file-mb]");
  if (annexes) {
    const error = document.querySelector("[data-annexes-error]");
    const maxFile = Number(annexes.dataset.maxFileMb) * 1024 * 1024;
    const maxTotal = Number(annexes.dataset.maxTotalMb) * 1024 * 1024;
    annexes.form.addEventListener("submit", (event) => {
      const files = Array.from(annexes.files);
      const tooBig = files.find((file) => file.size > maxFile);
      const total = files.reduce((sum, file) => sum + file.size, 0);
      let message = "";
      if (tooBig) {
        message = `El anexo «${tooBig.name}» pasa de ${annexes.dataset.maxFileMb} MB.`;
      } else if (total > maxTotal) {
        message = `Los anexos juntos pasan de ${annexes.dataset.maxTotalMb} MB.`;
      }
      if (message) {
        event.preventDefault();
        error.textContent = message;
        annexes.classList.add("is-invalid");
        annexes.focus();
      }
    });
    annexes.addEventListener("change", () => annexes.classList.remove("is-invalid"));
  }

  // Opens the duplicity warning returned by the server
  const modalElement = document.querySelector("[data-show-on-load]");
  if (modalElement) {
    // The app shell is its own stacking context, so a modal left inside it would sit under the backdrop
    document.body.appendChild(modalElement);
    bootstrap.Modal.getOrCreateInstance(modalElement).show();
  }
});
