<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" uri="http://utez.edu.mx/tocas/format" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:page title="Editor de resolución" subtitle="Toca ${appeal.number} · ${appeal.parties}" active="tocas" crumb="Mis tocas › ${appeal.number} › Resolución">
  <jsp:attribute name="actions">
    <button class="btn btn-outline-g" type="submit" form="editorForm" formaction="${ctx}/tocas/resolution/save">Guardar borrador</button>
    <button class="btn btn-dark-g" type="submit" form="editorForm" formaction="${ctx}/tocas/resolution/finalize" data-confirm="¿Desea finalizar la resolución de la toca ${appeal.number}?" data-confirm-title="FINALIZAR RESOLUCIÓN" data-confirm-rows="Toca|${appeal.number};Versión del borrador|${resolution.version}" data-confirm-warning="La resolución ya no podrá editarse." data-confirm-ok="Finalizar resolución">Finalizar resolución</button>
  </jsp:attribute>
  <jsp:attribute name="scripts"><script src="${ctx}/assets/js/editor.js"></script></jsp:attribute>
  <jsp:body>
  <form id="editorForm" method="post" action="${ctx}/tocas/resolution/save" data-autosave="${ctx}/api/v1/appeals/${appeal.id}/resolution/autosave">
    <input type="hidden" name="id" value="${appeal.id}">
    <input type="hidden" name="resultando" id="fResultando">
    <input type="hidden" name="considerando" id="fConsiderando">
    <input type="hidden" name="resuelve" id="fResuelve">
  </form>
  <div class="editor-layout" id="editorLayout">
    <section class="panel">
      <div class="editor-toolbar" role="toolbar" aria-label="Formato de texto">
        <div class="tb-group">
          <button type="button" class="tb-btn fw-bold" data-cmd="bold" aria-label="Negrita" title="Negrita">B</button>
          <button type="button" class="tb-btn fst-italic" data-cmd="italic" aria-label="Cursiva" title="Cursiva">I</button>
          <button type="button" class="tb-btn text-decoration-underline" data-cmd="underline" aria-label="Subrayado" title="Subrayado">U</button>
          <span class="tb-sep"></span>
          <button type="button" class="tb-btn" data-cmd="justifyLeft" aria-label="Alinear a la izquierda" title="Izquierda"><t:icon name="align-left" size="18"/></button>
          <button type="button" class="tb-btn" data-cmd="justifyCenter" aria-label="Centrar" title="Centrar"><t:icon name="align-center" size="18"/></button>
          <button type="button" class="tb-btn" data-cmd="justifyFull" aria-label="Justificar" title="Justificar"><t:icon name="align-justify" size="18"/></button>
          <span class="tb-sep"></span>
          <button type="button" class="tb-btn" data-cmd="insertUnorderedList" aria-label="Lista con viñetas" title="Lista con viñetas"><t:icon name="list-ul" size="18"/></button>
          <button type="button" class="tb-btn" data-cmd="insertOrderedList" aria-label="Lista numerada" title="Lista numerada"><t:icon name="list-ol" size="18"/></button>
        </div>
        <div class="tb-group">
          <button type="button" class="tb-btn tb-text" data-cmd="undo">Deshacer</button>
          <button type="button" class="tb-btn tb-text" data-cmd="redo">Rehacer</button>
        </div>
      </div>
      <div class="editor-stage"><div class="paper">
        <p class="paper-head">PODER JUDICIAL DEL ESTADO</p>
        <h3 class="paper-title">Resolución</h3>
        <p class="paper-sub">Toca: ${appeal.number} · <c:out value="${appeal.parties}"/></p>
        <h4>RESULTANDO</h4><div class="paper-edit" contenteditable="true" id="eResultando" data-field="fResultando" data-placeholder="Pendiente de redactar." role="textbox" aria-multiline="true" aria-label="Resultando">${resolution.resultando}</div>
        <h4>CONSIDERANDO</h4><div class="paper-edit" contenteditable="true" id="eConsiderando" data-field="fConsiderando" data-placeholder="Pendiente de redactar." role="textbox" aria-multiline="true" aria-label="Considerando">${resolution.considerando}</div>
        <h4>RESUELVE</h4><div class="paper-edit" contenteditable="true" id="eResuelve" data-field="fResuelve" data-placeholder="Pendiente de redactar." role="textbox" aria-multiline="true" aria-label="Resuelve">${resolution.resuelve}</div>
      </div></div>
      <div class="editor-foot"><span id="saveStatus">Guardado el ${f:dateTime12(resolution.lastModified)}</span> · Al finalizar ya no podrá editarse.</div>
    </section>

    <aside class="panel side-panel" id="sidePanel">
      <div class="side-tabs" role="tablist">
        <button type="button" class="side-tab active" role="tab" data-pane="paneExpediente" aria-selected="true">Expediente</button>
        <button type="button" class="side-tab" role="tab" data-pane="panePonencias" aria-selected="false">Registro de ponencias</button>
        <button type="button" class="side-tab" role="tab" data-pane="paneEstructura" aria-selected="false">Estructura</button>
      </div>
      <div class="side-pane" id="paneExpediente">
        <div class="chip-row">
          <button type="button" class="chip" id="widenPanel">Ampliar panel</button>
          <button type="button" class="chip" id="hidePanel">Ocultar panel</button>
        </div>
        <div class="kv"><span>Toca</span><strong>${appeal.number}</strong></div>
        <div class="kv"><span>Juzgado de origen</span><strong><c:out value="${appeal.originCourt}"/></strong></div>
        <div class="kv"><span>Partes</span><strong><c:out value="${appeal.parties}"/></strong></div>
        <div class="kv"><span>Sala</span><strong><c:out value="${appeal.roomName}"/></strong></div>
        <a class="btn btn-outline-g w-100 mb-2" target="_blank" rel="noopener" href="${ctx}/tocas/detail?id=${appeal.id}&tab=expediente">Abrir expediente completo</a>
      </div>
      <div class="side-pane" id="panePonencias" hidden>
        <p class="small">Consulte las ponencias registradas de esta toca.</p>
        <a class="btn btn-outline-g w-100" target="_blank" rel="noopener" href="${ctx}/tocas/detail?id=${appeal.id}&tab=ponencias">Abrir registro de ponencias</a>
      </div>
      <div class="side-pane" id="paneEstructura" hidden>
        <ul class="structure">
          <li data-for="eResultando" data-name="Resultandos"><span class="st-icon"></span><div><strong>Resultandos</strong><small class="st-text"></small></div></li>
          <li data-for="eConsiderando" data-name="Considerandos" data-prefix="Justificaciones legales · "><span class="st-icon"></span><div><strong>Considerandos</strong><small class="st-text"></small></div></li>
          <li data-for="eResuelve" data-name="Puntos resolutivos"><span class="st-icon"></span><div><strong>Puntos resolutivos</strong><small class="st-text"></small></div></li>
        </ul>
        <hr><p class="mini-label"><strong>Datos de la toca</strong></p>
        <div class="kv"><span>Magistrado</span><strong><c:out value="${sessionScope.user.fullName}"/></strong></div>
        <div class="kv"><span>Versión</span><strong>${resolution.version}</strong></div>
        <div class="info-box mt-3">Al finalizar, la resolución ya no podrá editarse.</div>
      </div>
      <div class="p-3 pt-0"><a class="btn btn-dark-g w-100" href="${ctx}/tocas/detail?id=${appeal.id}&tab=resolucion">Volver a la lista</a></div>
    </aside>
  </div>
  </jsp:body>
</t:page>
