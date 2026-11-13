/* Print preview: paper size, orientation, copies and print. */
(function () {
  var root = document.getElementById('previewRoot');
  if (!root) return;
  var style = document.createElement('style');
  document.head.appendChild(style);
  function pageRule() { style.textContent = '@page { size: ' + document.getElementById('paperSize').value + ' ' + document.getElementById('orientation').value + '; margin: 15mm; }'; }
  document.getElementById('paperSize').onchange = pageRule;
  document.getElementById('orientation').onchange = pageRule;
  function print() {
    var copies = Math.min(10, Math.max(1, parseInt(document.getElementById('copies').value, 10) || 1));
    var html = '', sheet = document.getElementById('sheet').outerHTML;
    for (var i = 0; i < copies; i++) html += sheet.replace('id="sheet"', '');
    document.getElementById('printArea').innerHTML = html;
    pageRule();
    window.print();
  }
  document.getElementById('printBtn').onclick = print;
  pageRule();
  if (root.dataset.autoprint === 'true') setTimeout(print, 400);
})();
