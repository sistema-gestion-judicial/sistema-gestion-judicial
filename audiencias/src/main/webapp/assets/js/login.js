function togglePassword() {
  const input = document.getElementById('password');
  const btn = document.getElementById('btnTogglePassword');
  if (input.type === 'password') {
    input.type = 'text';
    btn.textContent = 'Ocultar';
  } else {
    input.type = 'password';
    btn.textContent = 'Mostrar';
  }
}

// Redirección temporal para probar la interfaz antes de crear el LoginServlet
document.addEventListener('DOMContentLoaded', () => {
  const loginForm = document.querySelector('form');
  if (!loginForm) return;

  loginForm.addEventListener('submit', (e) => {
    e.preventDefault();
    const correo = document.getElementById('email').value.trim().toLowerCase();
    const context = window.APP_CONTEXT || '';

    if (correo.includes('admin')) {
      window.location.href = `${context}/mesas.jsp`;
    } else {
      window.location.href = `${context}/inicioabogados.jsp`;
    }
  });
});
