/* Shared behaviour: show/hide password, confirm dialogs. */
document.addEventListener('click', function (e) {
  var toggle = e.target.closest('[data-toggle-password]');
  if (toggle) {
    var input = document.querySelector(toggle.dataset.togglePassword);
    var show = input.type === 'password';
    input.type = show ? 'text' : 'password';
    toggle.textContent = show ? 'Ocultar' : 'Mostrar';
  }
  var confirmBtn = e.target.closest('button[data-confirm]');
  if (confirmBtn && !window.confirm(confirmBtn.dataset.confirm)) e.preventDefault();
});
document.addEventListener('submit', function (e) {
  var msg = e.target.dataset && e.target.dataset.confirm;
  if (msg && !window.confirm(msg)) e.preventDefault();
});
