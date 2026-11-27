/* Signed PDF upload modal: picker, drag and drop, client-side validation. */
(function () {
  var input = document.getElementById('fileInput');
  if (!input) return;
  var zone = document.getElementById('dropzone'), chip = document.getElementById('fileChip'), err = document.getElementById('fileError'), btn = document.getElementById('uploadBtn');
  var MAX = 20 * 1024 * 1024;
  function reset() { input.value = ''; chip.hidden = true; err.hidden = true; btn.disabled = true; }
  function accept(file) {
    err.hidden = true;
    if (!file) return reset();
    if (!/\.pdf$/i.test(file.name) || (file.type && file.type !== 'application/pdf')) { reset(); err.textContent = 'Solo se permiten archivos PDF.'; err.hidden = false; return; }
    if (file.size > MAX) { reset(); err.textContent = 'El archivo excede el máximo de 20 MB.'; err.hidden = false; return; }
    document.getElementById('fileName').textContent = file.name;
    document.getElementById('fileMeta').textContent = 'PDF · ' + (file.size / 1048576).toFixed(1) + ' MB';
    chip.hidden = false; btn.disabled = false;
  }
  document.getElementById('pickFile').onclick = function () { input.click(); };
  document.getElementById('removeFile').onclick = reset;
  input.onchange = function () { accept(input.files[0]); };
  ['dragenter', 'dragover'].forEach(function (n) { zone.addEventListener(n, function (e) { e.preventDefault(); zone.classList.add('drag'); }); });
  ['dragleave', 'drop'].forEach(function (n) { zone.addEventListener(n, function (e) { e.preventDefault(); zone.classList.remove('drag'); }); });
  zone.addEventListener('drop', function (e) {
    var f = e.dataTransfer.files[0];
    if (f) { var dt = new DataTransfer(); dt.items.add(f); input.files = dt.files; accept(f); }
  });
})();
