/* Registro de la ponencia: atajo Ctrl + Enter, evita doble envío y baja al final del registro. */
(function () {
  var form = document.getElementById('entryForm');
  var list = document.getElementById('logList');
  if (list) list.scrollTop = list.scrollHeight;
  if (!form) return;
  var area = document.getElementById('entryText');
  area.addEventListener('keydown', function (e) {
    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') { e.preventDefault(); if (area.value.trim()) form.requestSubmit(); }
  });
  form.addEventListener('submit', function () {
    var b = form.querySelector('button[type="submit"]');
    if (b) b.disabled = true;
  });
})();
