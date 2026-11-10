// Asks for confirmation before a manual sign out when a form marked with data-unsaved-guard was edited.
document.addEventListener("DOMContentLoaded", () => {
  const signOutForm = document.querySelector("[data-sign-out]");
  const guardedForms = document.querySelectorAll("[data-unsaved-guard]");
  if (!signOutForm || guardedForms.length === 0) {
    return;
  }

  let hasUnsavedChanges = false;
  guardedForms.forEach((form) => {
    form.addEventListener("input", () => {
      hasUnsavedChanges = true;
    });
  });

  signOutForm.addEventListener("submit", (event) => {
    if (hasUnsavedChanges && !window.confirm("Los datos no guardados se perderán. ¿Desea cerrar sesión?")) {
      event.preventDefault();
    }
  });
});
