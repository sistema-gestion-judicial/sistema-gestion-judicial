<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" uri="http://utez.edu.mx/tocas/format" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="canConclude" value="${resolution != null && resolution.finalized && resolution.signed && !appeal.concluded}"/>
<t:appealPage appeal="${appeal}" tab="resolucion">
  <jsp:attribute name="actions">
    <c:if test="${!appeal.concluded && resolution != null}">
      <button type="button" class="btn btn-outline-g ${canConclude ? '' : 'is-disabled'}" ${canConclude ? 'data-bs-toggle="modal" data-bs-target="#concludeModal"' : 'disabled'}>Concluir toca</button>
    </c:if>
    <t:writeResolutionButton appeal="${appeal}" resolution="${resolution}"/>
  </jsp:attribute>
  <jsp:attribute name="scripts"><script src="${ctx}/assets/js/upload.js"></script></jsp:attribute>
  <jsp:body>
  <div class="row g-3">
    <div class="col-lg-8">
      <section class="panel mb-3"><div class="panel-head"><h2>Versión digital</h2></div>
        <c:choose>
        <c:when test="${empty resolution}"><div class="p-3"><p class="small text-muted">Aún no se ha iniciado la redacción de la resolución.</p></div></c:when>
        <c:otherwise>
          <div class="data-grid cols-3">
            <div><span>Estado</span><strong>${f:resolutionStatus(resolution.status)}</strong></div>
            <div><span>Última modificación</span><strong>${f:dateTime12(resolution.lastModified)}</strong></div>
            <div><span>Versión</span><strong>${resolution.version}</strong></div>
          </div>
          <div class="doc-row"><t:icon name="doc" size="20"/><div><strong>${resolution.draft ? 'Proyecto de resolución' : 'Resolución'}</strong><small>Documento · ${resolution.pages} páginas</small></div>
            <a class="btn btn-outline-g btn-sm" href="${ctx}${resolution.draft ? '/tocas/resolution/edit' : '/tocas/resolution/preview'}?id=${appeal.id}">Abrir</a></div>
          <div class="p-3">
            <c:if test="${resolution.draft && !appeal.concluded}"><p class="small">Al finalizar, la resolución ya no podrá editarse.</p></c:if>
            <c:if test="${resolution.finalized}"><div class="info-box mb-3">Resolución finalizada. Ya no puede editarse.</div></c:if>
            <div class="d-flex flex-wrap gap-2">
              <a class="btn btn-outline-g" href="${ctx}/tocas/resolution/preview?id=${appeal.id}">Vista previa</a>
              <a class="btn btn-outline-g" href="${ctx}/tocas/resolution/preview?id=${appeal.id}&print=1">Imprimir</a>
              <c:if test="${resolution.draft && !appeal.concluded}">
                <form method="post" action="${ctx}/tocas/resolution/finalize" data-confirm="¿Finalizar la resolución? Ya no podrá editarse.">
                  <input type="hidden" name="id" value="${appeal.id}">
                  <input type="hidden" name="resultando" value="<c:out value='${resolution.resultando}'/>">
                  <input type="hidden" name="considerando" value="<c:out value='${resolution.considerando}'/>">
                  <input type="hidden" name="resuelve" value="<c:out value='${resolution.resuelve}'/>">
                  <button class="btn btn-dark-g" type="submit">Finalizar resolución</button></form>
              </c:if>
            </div>
          </div>
        </c:otherwise></c:choose>
      </section>
      <section class="panel"><div class="panel-head"><h2>Versión firmada / escaneada</h2></div>
        <div class="doc-row"><t:icon name="doc" size="20"/>
          <div><strong>Resolución firmada</strong>
            <small><c:choose><c:when test="${resolution != null && resolution.signed}">PDF · ${resolution.signedPages} páginas · cargado el ${f:dateNum(resolution.signedUploadedAt.toLocalDate())}</c:when><c:otherwise>Aún no se ha cargado el PDF escaneado</c:otherwise></c:choose></small></div>
          <c:if test="${resolution != null && resolution.signed}"><a class="btn btn-outline-g btn-sm" target="_blank" href="${ctx}/tocas/resolution/signed?id=${appeal.id}">Abrir</a></c:if></div>
        <c:if test="${resolution != null && !resolution.signed}"><div class="p-3">
          <p class="small">Imprima la resolución, fírmela y súbala escaneada en PDF.</p>
          <c:choose><c:when test="${resolution.finalized && !appeal.concluded}"><button class="btn btn-dark-g" type="button" data-bs-toggle="modal" data-bs-target="#uploadModal">Subir resolución firmada</button></c:when>
          <c:otherwise><button class="btn btn-dark-g is-disabled" type="button" disabled title="Primero finalice la resolución">Subir resolución firmada</button></c:otherwise></c:choose>
        </div></c:if>
      </section>
    </div>
    <div class="col-lg-4">
      <section class="panel mb-3"><div class="panel-head"><h2>Seguimiento</h2></div>
        <ol class="timeline"><c:forEach var="s" items="${timeline}">
          <li class="tl-${s.state}"><t:icon name="${s.state == 'DONE' ? 'check-filled' : s.state == 'CURRENT' ? 'ring-dot' : 'ring'}" size="16"/><div><strong>${s.label}</strong><small>${s.dateText}</small></div></li>
        </c:forEach></ol></section>
      <section class="panel"><div class="panel-head"><h2>Requisitos de cierre</h2></div>
        <ol class="timeline no-line">
          <li class="${resolution.finalized ? 'tl-DONE' : 'tl-PENDING'}"><t:icon name="${resolution.finalized ? 'check-filled' : 'ring'}" size="16"/><div><strong>Resolución finalizada</strong><small>${resolution.finalized ? 'Cumplido' : 'Pendiente'}</small></div></li>
          <li class="${resolution.signed ? 'tl-DONE' : 'tl-PENDING'}"><t:icon name="${resolution.signed ? 'check-filled' : 'ring'}" size="16"/><div><strong>PDF firmado cargado</strong><small>${resolution.signed ? 'Cumplido' : 'Pendiente'}</small></div></li>
        </ol></section>
    </div>
  </div>

  <c:if test="${resolution != null && resolution.finalized && !appeal.concluded && !resolution.signed}">
  <div class="modal fade" id="uploadModal" tabindex="-1" aria-hidden="true"><div class="modal-dialog modal-dialog-centered">
    <form class="modal-content app-modal" method="post" enctype="multipart/form-data" action="${ctx}/tocas/resolution/upload" id="uploadForm">
      <input type="hidden" name="id" value="${appeal.id}">
      <div class="modal-head"><h2>Subir resolución firmada</h2><button type="button" class="modal-x" data-bs-dismiss="modal" aria-label="Cerrar"><t:icon name="close" size="16"/></button></div>
      <div class="modal-body-g">
        <p class="small">Seleccione el PDF de la resolución impresa, firmada y sellada.</p>
        <div class="dropzone" id="dropzone"><t:icon name="upload" size="28"/>
          <div><strong class="small">Arrastre el archivo aquí o use el botón</strong><br><small>Solo PDF · máximo 20 MB</small><br>
            <button type="button" class="btn btn-outline-g btn-sm mt-2" id="pickFile">Seleccionar archivo</button></div>
          <input type="file" name="file" id="fileInput" accept="application/pdf,.pdf" hidden></div>
        <div class="file-chip" id="fileChip" hidden><div><strong id="fileName"></strong><br><small id="fileMeta"></small></div><button type="button" class="link-g small fw-bold border-0 bg-transparent" id="removeFile">Quitar</button></div>
        <div class="alert-g alert-error mt-2" id="fileError" hidden></div>
        <div class="info-box mt-3">Se conservarán ambas versiones de la resolución: la digital y la firmada.</div>
      </div>
      <div class="modal-foot"><button type="button" class="btn btn-outline-g" data-bs-dismiss="modal">Cancelar</button>
        <button type="submit" class="btn btn-dark-g" id="uploadBtn" disabled>Subir archivo</button></div>
    </form></div></div>
  </c:if>
  <c:if test="${canConclude}">
  <div class="modal fade" id="concludeModal" tabindex="-1" aria-hidden="true"><div class="modal-dialog modal-dialog-centered">
    <form class="modal-content app-modal" method="post" action="${ctx}/tocas/resolution/conclude">
      <input type="hidden" name="id" value="${appeal.id}">
      <div class="modal-head"><h2>Concluir toca</h2><button type="button" class="modal-x" data-bs-dismiss="modal" aria-label="Cerrar"><t:icon name="close" size="16"/></button></div>
      <div class="modal-body-g">
        <strong>¿Desea concluir la toca ${appeal.number}?</strong><p class="small"><c:out value="${appeal.parties}"/> · <c:out value="${appeal.originCourt}"/></p>
        <div class="warn-box">Solo el magistrado titular puede concluir una toca. Esta acción no se puede deshacer.</div>
        <p class="mini-label mt-3"><strong>Requisitos de cierre</strong></p>
        <div class="req-row"><t:icon name="check-filled" size="16"/><span>Resolución finalizada</span><small>Cumplido</small></div>
        <div class="req-row"><t:icon name="check-filled" size="16"/><span>PDF firmado cargado</span><small>Cumplido</small></div>
      </div>
      <div class="modal-foot"><button type="button" class="btn btn-outline-g" data-bs-dismiss="modal">Cancelar</button><button type="submit" class="btn btn-dark-g">Concluir toca</button></div>
    </form></div></div>
  </c:if>
  </jsp:body>
</t:appealPage>
