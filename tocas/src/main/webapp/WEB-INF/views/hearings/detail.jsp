<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" uri="http://utez.edu.mx/tocas/format" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="typeName" value="${f:hearingType(hearing.type)}"/>
<t:page title="${typeName} · Toca ${appeal.number}" active="tocas" crumb="Mis tocas › ${appeal.number} › Ponencia"
        subtitle="${appeal.parties} · ${hearing.roomName} · ${f:dateNum(hearing.date)} · ${f:time12(hearing.time)}">
  <jsp:attribute name="titleExtra">
    <span class="tag ${hearing.done ? 'tag-green tag-dot' : hearing.inProgress ? 'tag-sage' : 'tag-tan'} align-middle">${hearing.statusLabel}</span>
  </jsp:attribute>
  <jsp:attribute name="actions">
    <c:if test="${!hearing.done}">
      <button class="btn btn-dark-g" type="submit" form="finishForm"
              data-confirm="¿Desea finalizar la ${typeName} de la toca ${appeal.number}?"
              data-confirm-title="FINALIZAR PONENCIA"
              data-confirm-rows="Anotaciones|${entries.size()};Duración|${durationText}"
              data-confirm-warning="El registro ya no podrá modificarse."
              data-confirm-ok="Finalizar ponencia">Finalizar ponencia</button>
    </c:if>
  </jsp:attribute>
  <jsp:attribute name="scripts"><script src="${ctx}/assets/js/ponencia.js"></script></jsp:attribute>
  <jsp:body>
  <form id="finishForm" method="post" action="${ctx}/tocas/hearing/finish"><input type="hidden" name="id" value="${hearing.id}"></form>
  <a class="back-link d-inline-flex align-items-center gap-2 mb-3" href="${ctx}/tocas/detail?id=${appeal.id}&tab=ponencias&hearing=${hearing.id}"><t:icon name="arrow-left" size="16"/> Volver a Ponencias de la toca</a>
  <div class="hearing-layout">
    <section class="panel">
      <div class="panel-head d-flex justify-content-between align-items-center"><h2>Registro de lo ocurrido</h2>
        <c:if test="${not empty entries}"><small>Guardado a las ${f:time24(entries.get(entries.size() - 1).timestamp)}</small></c:if></div>
      <div class="log-list" id="logList">
        <c:forEach var="e" items="${entries}">
          <div class="log-item">
            <div class="log-head"><strong class="log-time">${f:timeSec(e.timestamp)}</strong>
              <span class="tag tag-sage">${e.typeLabel}</span>
              <c:if test="${e.edited}"><span class="log-edited">editada</span></c:if></div>
            <p class="log-text"><c:out value="${e.text}"/></p>
          </div>
        </c:forEach>
        <c:if test="${empty entries}"><p class="text-muted p-4 m-0">Aún no hay anotaciones. Agregue la primera para iniciar el registro.</p></c:if>
      </div>
      <c:choose>
      <c:when test="${!hearing.done}">
        <form class="log-form" id="entryForm" method="post" action="${ctx}/tocas/hearing/entry">
          <input type="hidden" name="id" value="${hearing.id}">
          <div class="log-form-row">
            <div class="log-type"><label class="mini-label" for="entryType">Tipo de anotación</label>
              <select class="field" id="entryType" name="type">
                <c:forEach var="t" items="${entryTypes}"><option value="${t.key}">${t.value}</option></c:forEach>
              </select></div>
            <div class="log-text-field"><label class="mini-label" for="entryText">Escriba lo que sucede en la ponencia</label>
              <textarea class="field" id="entryText" name="text" rows="3" maxlength="2000" placeholder="Escriba lo que sucede en la ponencia…" required></textarea></div>
          </div>
          <div class="d-flex align-items-center gap-3 mt-3 flex-wrap">
            <button class="btn btn-dark-g" type="submit">Agregar al registro</button>
            <small>Atajo: Ctrl + Enter. Las anotaciones no se pueden eliminar.</small>
          </div>
        </form>
      </c:when>
      <c:otherwise><div class="p-3"><div class="info-box">La ponencia está finalizada. El registro ya no puede modificarse.</div></div></c:otherwise>
      </c:choose>
    </section>
    <div class="d-grid gap-3 align-content-start">
      <section class="panel"><div class="panel-head"><h2>Datos de la ponencia</h2></div>
        <div class="p-3">
          <div class="kv-row"><span>Toca</span><strong>${appeal.number}</strong></div>
          <div class="kv-row"><span>Tipo</span><strong>${typeName}</strong></div>
          <div class="kv-row"><span>Sala</span><strong><c:out value="${hearing.roomName}"/></strong></div>
          <div class="kv-row"><span>Ubicación</span><strong>${location}</strong></div>
          <div class="kv-row"><span>Hora programada</span><strong>${f:time12(hearing.time)}</strong></div>
          <div class="kv-row"><span>Inicio real</span><strong><c:choose><c:when test="${not empty hearing.startedAt}">${f:time12(hearing.startedAt.toLocalTime())}</c:when><c:otherwise>Sin iniciar</c:otherwise></c:choose></strong></div>
          <c:if test="${hearing.done}"><div class="kv-row"><span>Finalización</span><strong>${f:time12(hearing.finishedAt.toLocalTime())}</strong></div></c:if>
        </div></section>
      <section class="panel"><div class="panel-head"><h2>Asistentes</h2></div>
        <div class="p-3">
          <c:forEach var="a" items="${attendees}"><div class="kv-row"><strong><c:out value="${a[0]}"/></strong><span><c:out value="${a[1]}"/></span></div></c:forEach>
        </div></section>
      <section class="panel"><div class="panel-head"><h2>Consulta rápida</h2></div>
        <div class="p-3 d-grid gap-2">
          <a class="btn btn-outline-g" href="${ctx}/tocas/detail?id=${appeal.id}&tab=expediente">Ver expediente completo</a>
          <a class="btn btn-outline-g" href="${ctx}/tocas/detail?id=${appeal.id}&tab=ponencias">Ponencias anteriores</a>
        </div></section>
    </div>
  </div>
  </jsp:body>
</t:page>
