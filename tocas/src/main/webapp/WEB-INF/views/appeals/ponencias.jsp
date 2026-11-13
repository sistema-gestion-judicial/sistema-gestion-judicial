<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" uri="http://utez.edu.mx/tocas/format" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:appealPage appeal="${appeal}" tab="ponencias">
  <jsp:attribute name="actions"><c:if test="${!appeal.concluded}"><button class="btn btn-dark-g" type="button" data-bs-toggle="modal" data-bs-target="#scheduleModal">Agendar ponencia</button></c:if></jsp:attribute>
  <jsp:body>
  <div class="row g-3">
    <div class="col-lg-8">
      <section class="panel"><div class="panel-head"><h2>Ponencias de la toca</h2></div>
        <div class="table-responsive"><table class="grid-table">
          <thead><tr><th>Fecha</th><th>Ponente</th><th>Tipo</th><th>Estado</th><th class="text-end">Acciones</th></tr></thead>
          <tbody>
          <c:forEach var="h" items="${hearings}">
            <tr class="${h.id == selectedHearing.id ? 'row-selected' : ''}">
              <td><strong>${f:dateNum(h.date)}</strong><small>${f:time12(h.time)}</small></td>
              <td><c:out value="${h.judgeName}"/></td><td>${f:hearingType(h.type)}</td>
              <td><span class="tag ${h.done ? 'tag-green tag-dot' : 'tag-tan'}">${f:hearingStatus(h.status)}</span></td>
              <td class="text-end"><a class="row-action" href="${ctx}/tocas/detail?id=${appeal.id}&tab=ponencias&hearing=${h.id}"><t:icon name="eye" size="16"/> Ver</a></td>
            </tr>
          </c:forEach>
          </tbody></table></div>
        <c:if test="${empty hearings}"><p class="text-muted text-center p-4 m-0">Esta toca aún no tiene ponencias.</p></c:if>
        <div class="table-foot"><span>Mostrando ${hearings.size()} de ${hearings.size()} ponencias de esta toca.</span></div>
      </section>
    </div>
    <div class="col-lg-4">
      <section class="panel"><div class="panel-head"><h2>Detalle de ponencia</h2></div>
        <c:if test="${not empty selectedHearing}">
        <div class="p-3">
          <div class="d-flex gap-3 mb-3"><span class="date-tile"><strong>${f:dayNum(selectedHearing.date)}</strong><small>${f:monthAbbr(selectedHearing.date)}</small></span>
            <div class="small"><strong>${f:hearingType(selectedHearing.type)}</strong><br>${f:dateNum(selectedHearing.date)} · ${f:hearingStatus(selectedHearing.status)}</div></div>
          <div class="kv"><span>Hora</span><strong>${f:time12(selectedHearing.time)}</strong></div>
          <div class="kv"><span>Ponente</span><strong><c:out value="${selectedHearing.judgeName}"/></strong></div>
          <div class="kv"><span>Tipo</span><strong>${f:hearingType(selectedHearing.type)}</strong></div>
          <div class="kv"><span>Sala</span><strong>${selectedHearing.roomName}</strong></div>
          <c:if test="${selectedHearing.autoAssigned}"><p class="small">Fecha asignada automáticamente.</p></c:if>
          <a class="btn btn-outline-g w-100 mt-3" href="${ctx}/agenda?month=${f:isoMonth(selectedHearing.date)}&date=${selectedHearing.date}">Ver en la agenda</a>
        </div></c:if>
        <c:if test="${empty selectedHearing}"><p class="text-muted p-3 m-0">Seleccione una ponencia.</p></c:if>
      </section>
    </div>
  </div>
  <c:if test="${!appeal.concluded}"><t:scheduleModal appeal="${appeal}" proposal="${proposal}" returnTab="ponencias" minDate="${minDate}"/></c:if>
  </jsp:body>
</t:appealPage>
