<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" uri="http://utez.edu.mx/tocas/format" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:appealPage appeal="${appeal}" tab="resolucion">
  <jsp:attribute name="actions">
    <button class="btn btn-outline-g" type="submit" form="editorForm" formaction="${ctx}/tocas/resolution/save">Guardar borrador</button>
    <button class="btn btn-dark-g" type="submit" form="editorForm" formaction="${ctx}/tocas/resolution/finalize" data-confirm="¿Finalizar la resolución? Ya no podrá editarse.">Finalizar resolución</button>
  </jsp:attribute>
  <jsp:attribute name="scripts"><script src="${ctx}/assets/js/editor.js"></script></jsp:attribute>
  <jsp:body>
  <form id="editorForm" method="post" action="${ctx}/tocas/resolution/save" data-autosave="${ctx}/api/v1/appeals/${appeal.id}/resolution/autosave">
    <input type="hidden" name="id" value="${appeal.id}">
    <input type="hidden" name="resultando" id="fResultando">
    <input type="hidden" name="considerando" id="fConsiderando">
    <input type="hidden" name="resuelve" id="fResuelve">
  </form>
  <div class="row g-3">
    <div class="col-lg-8">
      <section class="panel"><div class="panel-head d-flex justify-content-between"><h2>Editor de resolución</h2><small>Borrador · versión ${resolution.version}</small></div>
        <div class="editor-toolbar">
          <div class="d-flex flex-wrap gap-2">
            <button type="button" class="tb-btn fw-bold" data-cmd="bold" aria-label="Negrita">B</button>
            <button type="button" class="tb-btn fst-italic" data-cmd="italic" aria-label="Cursiva">I</button>
            <button type="button" class="tb-btn text-decoration-underline" data-cmd="underline" aria-label="Subrayado">U</button>
            <button type="button" class="tb-btn" data-cmd="insertUnorderedList">Lista</button>
            <button type="button" class="tb-btn" id="alignBtn">Alinear</button></div>
          <div class="d-flex gap-2"><button type="button" class="tb-btn" data-cmd="undo">Deshacer</button><button type="button" class="tb-btn" data-cmd="redo">Rehacer</button></div>
        </div>
        <div class="editor-stage"><div class="paper">
          <h3 class="paper-title">RESOLUCIÓN</h3>
          <p class="paper-sub">Toca ${appeal.number}<br><c:out value="${appeal.parties}"/></p>
          <h4>RESULTANDO</h4><div class="paper-edit" contenteditable="true" id="eResultando" data-field="fResultando">${resolution.resultando}</div>
          <h4>CONSIDERANDO</h4><div class="paper-edit" contenteditable="true" id="eConsiderando" data-field="fConsiderando">${resolution.considerando}</div>
          <h4>RESUELVE</h4><div class="paper-edit" contenteditable="true" id="eResuelve" data-field="fResuelve">${resolution.resuelve}</div>
        </div></div>
        <div class="table-foot"><span id="saveStatus">Guardado el ${f:dateTime12(resolution.lastModified)}</span></div>
      </section>
    </div>
    <div class="col-lg-4">
      <section class="panel"><div class="panel-head"><h2>Estructura</h2></div>
        <div class="p-3">
          <ul class="structure">
            <li data-for="eResultando" data-name="Resultandos"><span class="st-icon"></span><div><strong>Resultandos</strong><small class="st-text"></small></div></li>
            <li data-for="eConsiderando" data-name="Considerandos" data-prefix="Justificaciones legales · "><span class="st-icon"></span><div><strong>Considerandos</strong><small class="st-text"></small></div></li>
            <li data-for="eResuelve" data-name="Puntos resolutivos"><span class="st-icon"></span><div><strong>Puntos resolutivos</strong><small class="st-text"></small></div></li>
          </ul>
          <hr><p class="mini-label"><strong>Datos de la toca</strong></p>
          <div class="kv"><span>Toca</span><strong>${appeal.number}</strong></div>
          <div class="kv"><span>Sala</span><strong>${appeal.roomName}</strong></div>
          <div class="kv"><span>Magistrado</span><strong><c:out value="${sessionScope.user.fullName}"/></strong></div>
          <div class="kv"><span>Versión</span><strong>${resolution.version}</strong></div>
          <div class="info-box mt-3">Al finalizar, la resolución ya no podrá editarse.</div>
        </div></section>
    </div>
  </div>
  </jsp:body>
</t:appealPage>
