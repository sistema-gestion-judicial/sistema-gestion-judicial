// Behaviour of step 1 of the demand request registration.
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

  // Opens the duplicity warning returned by the server
  const modalElement = document.querySelector("[data-show-on-load]");
  if (modalElement) {
    bootstrap.Modal.getOrCreateInstance(modalElement).show();
  }
});
