<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" uri="http://utez.edu.mx/tocas/format" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:appealPage appeal="${appeal}" tab="resolucion">
  <jsp:attribute name="actions">
    <a class="btn btn-outline-g" href="${ctx}${resolution.draft ? '/tocas/resolution/edit?id=' : '/tocas/detail?tab=resolucion&id='}${appeal.id}">Volver a la resolución</a>
    <button class="btn btn-dark-g" type="button" id="printBtn">Imprimir</button>
  </jsp:attribute>
  <jsp:attribute name="scripts"><script src="${ctx}/assets/js/preview.js"></script></jsp:attribute>
  <jsp:body>
  <div class="row g-3" id="previewRoot" data-autoprint="${autoPrint}">
    <div class="col-lg-8">
      <section class="panel"><div class="panel-head d-flex justify-content-between"><h2>Vista previa de la resolución</h2><small>Hoja 1 de ${resolution.pages}</small></div>
        <div class="editor-stage"><div class="paper sheet" id="sheet">
          <p class="paper-head">PODER JUDICIAL DEL ESTADO<br><span>${appeal.roomName} · Segunda instancia</span></p>
          <h3 class="paper-title">RESOLUCIÓN</h3>
          <p class="paper-sub">Toca ${appeal.number} · <c:out value="${appeal.parties}"/></p>
          <h4>RESULTANDO</h4><div class="paper-text">${resolution.resultando}</div>
          <h4>CONSIDERANDO</h4><div class="paper-text">${resolution.considerando}</div>
          <h4>RESUELVE</h4><div class="paper-text">${resolution.resuelve}</div>
          <div class="signature"><span></span><small><c:out value="${sessionScope.user.fullName}"/> · ${sessionScope.user.roleTitle}</small></div>
        </div></div>
        <div class="table-foot"><span>${resolution.draft ? 'Vista previa del borrador. La resolución aún puede editarse.' : 'Así se imprimirá la hoja oficial. La resolución finalizada ya no puede editarse.'}</span></div>
      </section>
    </div>
    <div class="col-lg-4">
      <section class="panel"><div class="panel-head"><h2>Impresión</h2></div>
        <div class="p-3">
          <label class="mini-label" for="paperSize">Tamaño de hoja</label>
          <select class="field mb-2" id="paperSize"><option value="letter">Carta</option><option value="legal">Oficio</option></select>
          <label class="mini-label" for="orientation">Orientación</label>
          <select class="field mb-2" id="orientation"><option value="portrait">Vertical</option><option value="landscape">Horizontal</option></select>
          <label class="mini-label" for="pagesField">Páginas</label>
          <input class="field mb-2" id="pagesField" value="${resolution.pages}" readonly>
          <label class="mini-label" for="copies">Copias</label>
          <input class="field mb-3" id="copies" type="number" min="1" max="10" value="1">
          <div class="info-box">Imprima, firme a mano y selle la hoja. Después suba el PDF escaneado para conservar ambas versiones.</div>
        </div></section>
    </div>
  </div>
  <div id="printArea"></div>
  </jsp:body>
</t:appealPage>
