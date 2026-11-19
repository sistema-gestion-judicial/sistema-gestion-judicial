<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" uri="http://utez.edu.mx/tocas/format" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:appealPage appeal="${appeal}" tab="resumen">
  <jsp:attribute name="actions"><t:writeResolutionButton appeal="${appeal}" resolution="${resolution}"/></jsp:attribute>
  <jsp:attribute name="scripts"></jsp:attribute>
  <jsp:body>
  <div class="row g-3">
    <div class="col-lg-8">
      <section class="panel mb-3"><div class="panel-head"><h2>Datos generales</h2></div>
        <div class="data-grid">
          <div><span>Número de toca</span><strong>${appeal.number}</strong></div>
          <div><span>Partes</span><strong><c:out value="${appeal.parties}"/></strong></div>
          <div><span>Fecha de recepción</span><strong>${f:dateNum(appeal.receivedDate)}</strong></div>
          <div><span>Juzgado de origen</span><strong><c:out value="${appeal.originCourt}"/></strong></div>
          <div><span>Tipo de recurso</span><strong>${appeal.appealType}</strong></div>
          <div><span>Fecha de ponencia</span><strong><c:choose><c:when test="${not empty nextHearing}">${f:dateNum(nextHearing.date)}</c:when><c:otherwise>Sin agendar</c:otherwise></c:choose></strong></div>
          <div><span>Expediente de origen</span><strong>${appeal.originFile}</strong></div>
          <div><span>Sala</span><strong>${appeal.roomName}</strong></div>
        </div>
      </section>
      <section class="panel"><div class="panel-head d-flex justify-content-between align-items-center"><h2>Documentos del expediente</h2>
          <a class="link-g small" href="${ctx}/tocas/detail?id=${appeal.id}&tab=expediente">Ver expediente completo <t:icon name="arrow-right" size="14"/></a></div>
        <c:forEach var="d" items="${recentDocs}">
          <div class="doc-row"><t:icon name="doc" size="20"/><div><strong>${d.name}</strong><small>PDF · ${d.pages} páginas · ${f:dateNum(d.date)}</small></div>
            <a class="btn btn-outline-g btn-sm" href="${ctx}/tocas/detail?id=${appeal.id}&tab=expediente&doc=${d.id}">Abrir</a></div>
        </c:forEach>
        <p class="small p-3 m-0">Mostrando 3 de ${totalDocs} actuaciones. El expediente completo incluye todas, desde la primera instancia.</p>
      </section>
    </div>
    <div class="col-lg-4">
      <section class="panel mb-3"><div class="panel-head"><h2>Seguimiento</h2></div>
        <ol class="timeline">
          <c:forEach var="s" items="${timeline}">
            <li class="tl-${s.state}"><t:icon name="${s.state == 'DONE' ? 'check-filled' : s.state == 'CURRENT' ? 'ring-dot' : 'ring'}" size="16"/>
              <div><strong>${s.label}</strong><small>${s.dateText}</small><c:if test="${not empty s.badge}"><span class="tag tag-tan">${s.badge}</span></c:if></div></li>
          </c:forEach>
        </ol>
      </section>
      <section class="panel"><div class="panel-head"><h2>Ponencias</h2></div>
        <div class="p-3">
          <c:choose><c:when test="${not empty nextHearing}">
            <div class="d-flex gap-3 mb-3"><span class="date-tile"><strong>${f:dayNum(nextHearing.date)}</strong><small>${f:monthAbbr(nextHearing.date)}</small></span>
              <div class="small"><strong>${f:hearingType(nextHearing.type)}</strong><br>${f:dateNum(nextHearing.date)} · Próxima<br><span class="text-muted"><c:if test="${nextHearing.autoAssigned}">Fecha asignada automáticamente.</c:if></span></div></div>
          </c:when><c:otherwise><p class="small text-muted">No hay ponencias próximas.</p></c:otherwise></c:choose>
          <c:if test="${!appeal.concluded}"><button class="btn btn-outline-g w-100" type="button" data-bs-toggle="modal" data-bs-target="#scheduleModal">Agendar otra ponencia</button></c:if>
        </div>
      </section>
    </div>
  </div>
  <c:if test="${!appeal.concluded}"><t:scheduleModal appeal="${appeal}" proposal="${proposal}" returnTab="resumen" minDate="${minDate}"/></c:if>
  </jsp:body>
</t:appealPage>
