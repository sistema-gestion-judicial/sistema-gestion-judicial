<%-- Admin 5 · Detalle de solicitud y asignación: request summary, assigned perito, follow-up and next hearing. --%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Detalle de solicitud" />
<c:set var="navRole" value="admin" />
<c:set var="activeNav" value="solicitudes" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>
<c:set var="pageHeading" value="Solicitud ${solicitud.folio}" />
<c:set var="pageDescription" value="Expediente ${solicitud.caseNumber} · ${solicitud.subject} · ${solicitud.court}" />
<%@ include file="/WEB-INF/views/layout/page-header.jspf" %>

<div class="d-flex justify-content-between align-items-start flex-wrap gap-3 mt-3">
  <div>
    <a class="fw-semibold d-inline-block mb-2" href="${ctx}/admin/solicitudes"><i class="bi bi-arrow-left"></i> Volver a Solicitudes</a>
    <span class="badge-mor"><c:out value="${solicitud.statusLabel}" /></span>
  </div>
  <div class="d-flex gap-2">
    <a class="btn btn-outline-secondary" href="${ctx}/admin/expedientes/${solicitud.caseNumber}">Ver expediente completo</a>
    <button type="button" class="btn btn-primary">Notificar al perito</button>
  </div>
</div>

<ul class="nav nav-tabs mt-4" role="tablist">
  <li class="nav-item"><a class="nav-link active" aria-current="page" href="${ctx}/admin/solicitudes/${solicitud.id}">Resumen</a></li>
  <li class="nav-item"><a class="nav-link" href="${ctx}/admin/solicitudes/${solicitud.id}/expediente">Expediente completo</a></li>
  <li class="nav-item"><a class="nav-link" href="${ctx}/admin/solicitudes/${solicitud.id}/asignacion">Asignación</a></li>
  <li class="nav-item"><a class="nav-link" href="${ctx}/admin/solicitudes/${solicitud.id}/dictamen">Dictamen</a></li>
  <li class="nav-item"><a class="nav-link" href="${ctx}/admin/solicitudes/${solicitud.id}/bitacora">Bitácora</a></li>
</ul>

<div class="row g-4 mt-1">
  <div class="col-lg-8">
    <section class="card-mor p-4 mb-4">
      <h2 class="page-section-title mb-3">Datos de la solicitud</h2>
      <div class="row g-3">
        <div class="col-md-4"><div class="summary-label">Folio</div><div class="summary-value"><c:out value="${solicitud.folio}" /></div></div>
        <div class="col-md-4"><div class="summary-label">Fecha de solicitud</div><div class="summary-value"><c:out value="${solicitud.requestedOn}" /></div></div>
        <div class="col-md-4"><div class="summary-label">Número de expediente</div><div class="summary-value"><c:out value="${solicitud.caseNumber}" /></div></div>
        <div class="col-md-4"><div class="summary-label">Materia</div><div class="summary-value"><c:out value="${solicitud.subject}" /></div></div>
        <div class="col-md-4"><div class="summary-label">Especialidad requerida</div><div class="summary-value"><c:out value="${solicitud.requiredSpecialty}" /></div></div>
        <div class="col-md-4"><div class="summary-label">Participantes</div><div class="summary-value"><c:out value="${solicitud.parties}" /></div></div>
      </div>
      <div class="mt-4">
        <div class="summary-label">Razón o justificación</div>
        <p class="fw-semibold mb-0"><c:out value="${solicitud.justification}" /></p>
      </div>
      <div class="mt-3">
        <div class="summary-label">Anexos a revisar (enviados por el abogado)</div>
        <ul class="list-unstyled mb-0">
          <c:forEach var="anexo" items="${solicitud.annexes}">
            <li><a href="${ctx}/admin/documentos/${anexo.id}"><c:out value="${anexo.name}" /></a></li>
          </c:forEach>
        </ul>
      </div>
    </section>

    <section class="card-mor p-4">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <h2 class="page-section-title">Perito asignado
          <span class="badge rounded-pill text-bg-light border ms-2"><i class="bi bi-lightning"></i> ${solicitud.assignmentModeLabel}</span>
        </h2>
        <button type="button" class="btn btn-outline-secondary" data-bs-toggle="modal" data-bs-target="#editarPeritoModal">Editar</button>
      </div>
      <c:choose>
        <c:when test="${not empty solicitud.assignedPerito}">
          <div class="d-flex align-items-center gap-3">
            <div class="user-chip-avatar"><i class="bi bi-person"></i></div>
            <div>
              <div class="fw-semibold"><c:out value="${solicitud.assignedPerito.fullName}" /></div>
              <div class="text-muted"><c:out value="${solicitud.assignedPerito.summaryLine}" /></div>
            </div>
          </div>
        </c:when>
        <c:otherwise>
          <p class="text-muted mb-0">Sin perito asignado.</p>
        </c:otherwise>
      </c:choose>
      <p class="alert alert-light border mt-3 mb-0">
        <i class="bi bi-info-circle"></i> <c:out value="${solicitud.assignmentReason}" />
      </p>
    </section>
  </div>

  <div class="col-lg-4">
    <aside class="card-mor mb-4">
      <h2 class="page-section-title mb-3">Seguimiento</h2>
      <ol class="list-unstyled mb-0">
        <c:forEach var="paso" items="${solicitud.timeline}">
          <li class="mb-3">
            <div class="fw-semibold"><c:out value="${paso.title}" /></div>
            <div class="text-muted small"><c:out value="${paso.detail}" /></div>
          </li>
        </c:forEach>
      </ol>
    </aside>

    <aside class="card-mor">
      <h2 class="page-section-title mb-3"><i class="bi bi-calendar-event"></i> Próxima audiencia</h2>
      <c:if test="${not empty nextHearing}">
        <div class="fw-semibold"><c:out value="${nextHearing.title}" /></div>
        <div class="text-muted"><c:out value="${nextHearing.timeAndRoom}" /></div>
        <p class="small text-warning-emphasis mt-2 mb-2"><c:out value="${nextHearing.note}" /></p>
      </c:if>
      <a class="fw-semibold" href="${ctx}/admin/audiencias?expediente=${solicitud.caseNumber}">Ver las audiencias</a>
    </aside>
  </div>
</div>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
