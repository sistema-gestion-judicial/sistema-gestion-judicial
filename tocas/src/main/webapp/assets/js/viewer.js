/* Placeholder document viewer for the "Expediente completo" tab (page and zoom controls). */
(function () {
  var root = document.getElementById('viewer');
  if (!root) return;
  var total = parseInt(root.dataset.pages, 10), page = 1, zoom = 100;
  var sheet = document.getElementById('sheet');
  function lines(seed) {
    var html = '', w;
    for (var i = 0; i < 16; i++) { w = 55 + ((seed * 37 + i * 29) % 45); html += '<div class="v-line" style="width:' + w + '%"></div>'; if (i % 5 === 4) html += '<div style="height:12px"></div>'; }
    return html;
  }
  function render() {
    var head = page === 1 ? '<h5>' + root.dataset.title.toUpperCase() + '</h5><p>Expediente ' + root.dataset.file + '</p>' : '<p>Página ' + page + '</p>';
    sheet.innerHTML = head + lines(page);
    sheet.style.transform = 'scale(' + zoom / 100 + ')';
    document.getElementById('pageLabel').textContent = 'Página ' + page + ' de ' + total;
    document.getElementById('zoomLabel').textContent = zoom + ' %';
    document.getElementById('prevPage').disabled = page <= 1;
    document.getElementById('nextPage').disabled = page >= total;
    document.getElementById('zoomOut').disabled = zoom <= 50;
    document.getElementById('zoomIn').disabled = zoom >= 200;
  }
  document.getElementById('prevPage').onclick = function () { page--; render(); };
  document.getElementById('nextPage').onclick = function () { page++; render(); };
  document.getElementById('zoomOut').onclick = function () { zoom -= 25; render(); };
  document.getElementById('zoomIn').onclick = function () { zoom += 25; render(); };
  render();
})();
