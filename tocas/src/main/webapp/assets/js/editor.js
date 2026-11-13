/* Resolution editor: toolbar, section status, autosave through the JSON API. */
(function () {
  var form = document.getElementById('editorForm');
  if (!form) return;
  var editors = Array.prototype.slice.call(document.querySelectorAll('.paper-edit'));
  var status = document.getElementById('saveStatus');
  var dirty = false;
  try { document.execCommand('styleWithCSS', false, true); } catch (e) { /* ignore */ }

  function syncFields() { editors.forEach(function (el) { document.getElementById(el.dataset.field).value = el.innerHTML; }); }
  function updateStructure() {
    document.querySelectorAll('.structure li').forEach(function (li) {
      var len = document.getElementById(li.dataset.for).innerText.trim().length;
      var text = len === 0 ? 'Pendiente' : len < 120 ? 'en redacción' : 'Listo';
      li.className = len === 0 ? '' : len < 120 ? 'writing' : 'ready';
      li.querySelector('.st-text').textContent = (li.dataset.prefix && len > 0 ? li.dataset.prefix : '') + text;
    });
  }
  editors.forEach(function (el) { el.addEventListener('input', function () { dirty = true; updateStructure(); }); });
  document.querySelectorAll('[data-cmd]').forEach(function (b) {
    b.addEventListener('mousedown', function (e) { e.preventDefault(); document.execCommand(b.dataset.cmd, false, null); dirty = true; updateStructure(); });
  });
  var aligns = ['justifyLeft', 'justifyCenter', 'justifyRight', 'justifyFull'], ai = 0;
  document.getElementById('alignBtn').addEventListener('mousedown', function (e) { e.preventDefault(); document.execCommand(aligns[ai++ % 4], false, null); dirty = true; });
  form.addEventListener('submit', syncFields);
  document.querySelectorAll('button[form="editorForm"]').forEach(function (b) { b.addEventListener('click', syncFields); });

  function autosave() {
    if (!dirty) return;
    syncFields();
    var body = new URLSearchParams(new FormData(form));
    fetch(form.dataset.autosave, { method: 'POST', body: body, credentials: 'same-origin' })
      .then(function (r) { return r.json(); })
      .then(function (j) { if (j.ok) { dirty = false; status.textContent = 'Guardado automáticamente hoy a las ' + j.savedAt; } else { status.textContent = j.message; } })
      .catch(function () { status.textContent = 'No se pudo guardar automáticamente.'; });
  }
  setInterval(autosave, 20000);
  window.addEventListener('beforeunload', function (e) { if (dirty) { e.preventDefault(); e.returnValue = ''; } });
  form.addEventListener('submit', function () { dirty = false; });
  document.querySelectorAll('button[form="editorForm"]').forEach(function (b) { b.addEventListener('click', function () { dirty = false; }); });
  updateStructure();
})();
