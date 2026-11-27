/* Editor de resolución (JavaScript puro): formato, estado por sección, panel lateral y autoguardado vía API JSON. */
(function () {
  var form = document.getElementById('editorForm');
  if (!form) return;
  var editors = Array.prototype.slice.call(document.querySelectorAll('.paper-edit'));
  var status = document.getElementById('saveStatus');
  var dirty = false, current = editors[0];
  try { document.execCommand('styleWithCSS', false, false); document.execCommand('defaultParagraphSeparator', false, 'p'); } catch (e) { /* ignorar */ }

  /* Vuelca el HTML de cada área editable en su input hidden (se envía como resultando/considerando/resuelve). */
  function syncFields() { editors.forEach(function (el) { document.getElementById(el.dataset.field).value = el.innerHTML; }); }

  function updateStructure() {
    document.querySelectorAll('.structure li').forEach(function (li) {
      var len = document.getElementById(li.dataset.for).innerText.trim().length;
      var text = len === 0 ? 'Pendiente' : len < 120 ? 'en redacción' : 'Listo';
      li.className = len === 0 ? '' : len < 120 ? 'writing' : 'ready';
      li.querySelector('.st-text').textContent = (li.dataset.prefix && len > 0 ? li.dataset.prefix : '') + text;
    });
  }
  function markDirty() { dirty = true; updateStructure(); }

  function refreshButtons() {
    document.querySelectorAll('.tb-btn[data-cmd]').forEach(function (b) {
      var on = false;
      try { on = !/^(undo|redo)$/.test(b.dataset.cmd) && document.activeElement && document.activeElement.classList.contains('paper-edit') && document.queryCommandState(b.dataset.cmd); } catch (e) { /* ignorar */ }
      b.classList.toggle('on', !!on);
    });
  }

  editors.forEach(function (el) {
    el.addEventListener('focus', function () { current = el; refreshButtons(); });
    el.addEventListener('input', markDirty);
    el.addEventListener('keyup', refreshButtons);
    el.addEventListener('mouseup', refreshButtons);
    el.addEventListener('paste', function (e) { /* pega solo texto plano */
      var text = (e.clipboardData || window.clipboardData).getData('text/plain');
      if (text == null) return;
      e.preventDefault();
      document.execCommand('insertText', false, text);
    });
  });

  document.querySelectorAll('.tb-btn[data-cmd]').forEach(function (b) {
    b.addEventListener('mousedown', function (e) {
      e.preventDefault(); /* conserva la selección del editor */
      if (!document.activeElement || !document.activeElement.classList.contains('paper-edit')) current.focus();
      document.execCommand(b.dataset.cmd, false, null);
      markDirty(); refreshButtons();
    });
  });

  /* Panel lateral: pestañas, ampliar y ocultar */
  var layout = document.getElementById('editorLayout');
  document.querySelectorAll('.side-tab').forEach(function (tab) {
    tab.addEventListener('click', function () {
      document.querySelectorAll('.side-tab').forEach(function (t) { var a = t === tab; t.classList.toggle('active', a); t.setAttribute('aria-selected', a); });
      document.querySelectorAll('.side-pane').forEach(function (p) { p.hidden = p.id !== tab.dataset.pane; });
    });
  });
  var widen = document.getElementById('widenPanel'), hide = document.getElementById('hidePanel');
  if (widen) widen.addEventListener('click', function () { var w = layout.classList.toggle('is-wide'); widen.textContent = w ? 'Reducir panel' : 'Ampliar panel'; });
  if (hide) hide.addEventListener('click', function () {
    layout.classList.add('is-hidden');
    if (!document.getElementById('showPanel')) {
      var s = document.createElement('button');
      s.type = 'button'; s.id = 'showPanel'; s.className = 'chip mb-2'; s.textContent = 'Mostrar panel';
      s.addEventListener('click', function () { layout.classList.remove('is-hidden'); s.remove(); });
      layout.parentNode.insertBefore(s, layout);
    }
  });

  /* Al enviar (Guardar borrador / Finalizar) se vuelca el contenido a los inputs hidden */
  form.addEventListener('submit', function () { syncFields(); dirty = false; });

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
  updateStructure();
})();
