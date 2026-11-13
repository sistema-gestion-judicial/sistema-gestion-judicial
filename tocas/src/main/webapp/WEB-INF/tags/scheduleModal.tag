<%@ tag language="java" pageEncoding="UTF-8" body-content="empty" description="Modal: Agendar ponencia" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ attribute name="appeal" required="true" type="mx.edu.utez.judicial.tocas.model.Appeal" %>
<%@ attribute name="proposal" required="true" %>
<%@ attribute name="returnTab" required="true" %>
<%@ attribute name="minDate" required="true" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<div class="modal fade" id="scheduleModal" tabindex="-1" aria-labelledby="scheduleTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <form class="modal-content app-modal" method="post" action="${ctx}/tocas/hearings">
      <input type="hidden" name="appealId" value="${appeal.id}">
      <input type="hidden" name="returnTab" value="${returnTab}">
      <div class="modal-head"><h2 id="scheduleTitle">Agendar ponencia</h2>
        <button type="button" class="modal-x" data-bs-dismiss="modal" aria-label="Cerrar"><t:icon name="close" size="16"/></button></div>
      <div class="modal-body-g">
        <div class="mini-label">Toca</div>
        <div class="fw-bold mb-3">${appeal.number} · <c:out value="${appeal.parties}"/></div>
        <div class="row g-3">
          <div class="col-7"><label class="mini-label" for="hDate">Fecha</label>
            <input class="field" type="date" id="hDate" name="date" value="${proposal.date}" min="${minDate}" required></div>
          <div class="col-5"><label class="mini-label" for="hTime">Hora</label>
            <input class="field" type="time" id="hTime" name="time" value="${proposal.time}" required></div>
          <div class="col-12"><label class="mini-label" for="hType">Tipo de ponencia</label>
            <select class="field" id="hType" name="type" required>
              <option value="FIRST" ${proposal.type == 'FIRST' ? 'selected' : ''}>1.ª ponencia</option>
              <option value="SECOND" ${proposal.type == 'SECOND' ? 'selected' : ''}>2.ª ponencia</option>
              <option value="THIRD" ${proposal.type == 'THIRD' ? 'selected' : ''}>3.ª ponencia</option>
              <option value="ADDITIONAL" ${proposal.type == 'ADDITIONAL' ? 'selected' : ''}>Sesión adicional</option>
            </select></div>
        </div>
        <div class="info-box mt-3">Fecha propuesta por el sistema según la carga de trabajo. Puede cambiarla antes de agendar.</div>
      </div>
      <div class="modal-foot">
        <button type="button" class="btn btn-outline-g" data-bs-dismiss="modal">Cancelar</button>
        <button type="submit" class="btn btn-dark-g">Agendar ponencia</button>
      </div>
    </form>
  </div>
</div>
