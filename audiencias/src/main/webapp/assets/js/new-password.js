document.addEventListener('DOMContentLoaded', function () {
  const newPasswordForm = document.getElementById('newPasswordForm');
  const passwordInput = document.getElementById('password');
  const confirmPasswordInput = document.getElementById('confirmPassword');
  const passwordError = document.getElementById('passwordError');

  // 1. Detectar estado de error forzado por parámetro URL (?error=true)
  const urlParams = new URLSearchParams(window.location.search);
  if (urlParams.get('error') === 'true') {
    showError('Las contraseñas no coinciden');
  }

  // 2. Limpiar marcas de error en cuanto el usuario empiece a escribir
  [passwordInput, confirmPasswordInput].forEach(input => {
    if (input) {
      input.addEventListener('input', clearError);
    }
  });

  // 3. Validar coincidencia de contraseñas al enviar el formulario
  if (newPasswordForm) {
    newPasswordForm.addEventListener('submit', function (e) {
      const passVal = passwordInput ? passwordInput.value : '';
      const confirmVal = confirmPasswordInput ? confirmPasswordInput.value : '';

      if (!passVal || !confirmVal) {
        e.preventDefault();
        showError('Ambos campos son obligatorios.');
        return;
      }

      if (passVal !== confirmVal) {
        e.preventDefault();
        showError('Las contraseñas no coinciden');
      }
    });
  }

  function showError(message) {
    if (passwordInput) passwordInput.classList.add('is-invalid');
    if (confirmPasswordInput) confirmPasswordInput.classList.add('is-invalid');
    if (passwordError) {
      passwordError.innerHTML = `<i class="bi bi-exclamation-circle me-1"></i> ${message}`;
      passwordError.classList.remove('d-none');
    }
  }

  function clearError() {
    if (passwordInput) passwordInput.classList.remove('is-invalid');
    if (confirmPasswordInput) confirmPasswordInput.classList.remove('is-invalid');
    if (passwordError) passwordError.classList.add('d-none');
  }
});
