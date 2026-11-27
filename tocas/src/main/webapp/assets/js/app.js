/* Comportamiento compartido: mostrar contraseña, modales sobre el backdrop y confirmaciones con modal. */

/* Los modales de Bootstrap se mueven a <body>: dentro de .app-layout (z-index propio) quedaban bajo el backdrop. */
function relocateModals() {
  document.querySelectorAll('.modal').forEach(function (m) { if (m.parentNode !== document.body) document.body.appendChild(m); });
}
relocateModals();
document.addEventListener('DOMContentLoaded', relocateModals);

/* Confirmación: reemplaza window.confirm() por #confirmModal (title/rows/warning/ok en data-confirm-*). */
(function () {
  var modalEl = null, okBtn = null, onOk = null;
  function parseRows(text) {
    return (text || '').split(';').filter(Boolean).map(function (r) { var i = r.indexOf('|'); return [r.slice(0, i), r.slice(i + 1)]; });
  }
  function ask(src, proceed) {
    modalEl = document.getElementById('confirmModal');
    if (!modalEl || !window.bootstrap) { if (window.confirm(src.dataset.confirm)) proceed(); return; }
    okBtn = document.getElementById('confirmOk');
    document.getElementById('confirmTitle').textContent = src.dataset.confirmTitle || 'CONFIRMAR';
    document.getElementById('confirmQuestion').textContent = src.dataset.confirm;
    var dl = document.getElementById('confirmRows'); dl.textContent = '';
    parseRows(src.dataset.confirmRows).forEach(function (r) {
      var dt = document.createElement('dt'); dt.textContent = r[0];
      var dd = document.createElement('dd'); dd.textContent = r[1];
      dl.appendChild(dt); dl.appendChild(dd);
    });
    var w = document.getElementById('confirmWarning');
    w.textContent = src.dataset.confirmWarning || ''; w.hidden = !src.dataset.confirmWarning;
    okBtn.textContent = src.dataset.confirmOk || 'Confirmar';
    onOk = proceed;
    bootstrap.Modal.getOrCreateInstance(modalEl).show();
  }
  document.addEventListener('click', function (e) {
    if (e.target.closest('#confirmOk')) {
      var fn = onOk; onOk = null;
      bootstrap.Modal.getOrCreateInstance(modalEl).hide();
      if (fn) fn();
      return;
    }
    var btn = e.target.closest('button[data-confirm]');
    if (btn) {
      e.preventDefault();
      ask(btn, function () { if (btn.form) btn.form.requestSubmit(btn); else btn.dispatchEvent(new Event('confirmed')); });
    }
  });
  document.addEventListener('submit', function (e) {
    var form = e.target;
    if (form.dataset && form.dataset.confirm && !form._confirmed) {
      e.preventDefault();
      ask(form, function () { form._confirmed = true; form.requestSubmit(); });
    }
  });
})();

document.addEventListener('click', function (e) {
  var toggle = e.target.closest('[data-toggle-password]');
  if (toggle) {
    var input = document.querySelector(toggle.dataset.togglePassword);
    var show = input.type === 'password';
    input.type = show ? 'text' : 'password';
    toggle.textContent = show ? 'Ocultar' : 'Mostrar';
  }
});
