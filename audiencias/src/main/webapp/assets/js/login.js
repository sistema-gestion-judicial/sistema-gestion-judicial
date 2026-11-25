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
