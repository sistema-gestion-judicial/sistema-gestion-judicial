document.addEventListener('DOMContentLoaded', () => {
  const recoverForm = document.getElementById('recoverForm');
  const emailInput = document.getElementById('email');

  if (recoverForm) {
    recoverForm.addEventListener('submit', (e) => {
      if (!emailInput.value || !emailInput.checkValidity()) {
        emailInput.classList.add('is-invalid');
      }
    });

    // Limpiar estado de error al escribir de nuevo
    emailInput.addEventListener('input', () => {
      emailInput.classList.remove('is-invalid');
    });
  }
});
