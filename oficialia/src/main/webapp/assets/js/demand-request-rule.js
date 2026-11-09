// Behaviour of the ruling page of a demand request.
document.addEventListener("DOMContentLoaded", () => {
  const reasonBlock = document.querySelector("[data-reason-block]");
  const reason = document.querySelector("#reason");
  const counter = document.querySelector("[data-reason-count]");

  // The reason is only asked when the request is inadmissible
  const syncReason = () => {
    const chosen = document.querySelector("input[name=decision]:checked");
    reasonBlock.hidden = !chosen || chosen.value !== "INADMISSIBLE";
  };
  document.querySelectorAll("input[name=decision]").forEach((radio) => {
    radio.addEventListener("change", () => {
      syncReason();
      if (!reasonBlock.hidden) {
        reason.focus();
      }
    });
  });
  syncReason();

  const updateCounter = () => {
    counter.textContent = reason.value.length;
  };
  reason.addEventListener("input", updateCounter);
  updateCounter();

  // Opens the confirmation returned by the server
  const modalElement = document.querySelector("[data-show-on-load]");
  if (modalElement) {
    bootstrap.Modal.getOrCreateInstance(modalElement).show();
  }
});
