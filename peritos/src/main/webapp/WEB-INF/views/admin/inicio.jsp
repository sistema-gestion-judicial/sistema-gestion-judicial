<%-- Admin 0 · Inicio: summary, recent requests, recent activity and the next hearing. --%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Inicio" />
<c:set var="navRole" value="admin" />
<c:set var="activeNav" value="home" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>
<c:set var="pageHeading" value="Inicio" />
<c:set var="pageDescription" value="Esto es lo que requiere su atención hoy." />
<%@ include file="/WEB-INF/views/layout/page-header.jspf" %>

<section class="card-mor mt-4 p-4">
  <div class="summary-strip">
    <div><div class="summary-label">Solicitudes sin asignar</div><div class="summary-value"><c:out value="${unassignedCount}" /></div></div>
    <div><div class="summary-label">En proceso</div><div class="summary-value"><c:out value="${inProgressCount}" /></div></div>
    <div><div class="summary-label">Dictámenes por vencer</div><div class="summary-value"><c:out value="${dueSoonCount}" /></div></div>
    <div><div class="summary-label">Peritos activos</div><div class="summary-value"><c:out value="${activePeritosCount}" /></div></div>
    <div><div class="summary-label">Certificación en revisión</div><div class="summary-value"><c:out value="${reviewCertificationCount}" /></div></div>
  </div>
</section>

<section class="table-mor-wrapper mt-4">
  <div class="d-flex justify-content-between align-items-center px-4 py-3">
    <h2 class="page-section-title">Solicitudes recientes</h2>
    <a class="btn btn-link fw-semibold" href="${ctx}/admin/solicitudes">Ver todas las solicitudes</a>
  </div>
  <div class="table-responsive">
    <table class="table table-mor mb-0">
      <thead>
        <tr>
          <th>Folio</th><th>Expediente</th><th>Materia</th><th>Fecha de solicitud</th>
          <th>Perito asignado</th><th>Estado</th><th>Acciones</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="solicitud" items="${recentRequests}">
          <tr>
            <td class="cell-strong"><c:out value="${solicitud.folio}" /></td>
            <td><c:out value="${solicitud.caseNumber}" /></td>
            <td><c:out value="${solicitud.subject}" /></td>
            <td><c:out value="${solicitud.requestedOn}" /></td>
            <td><c:out value="${solicitud.assignedPeritoName}" /></td>
            <td><span class="badge-mor"><c:out value="${solicitud.statusLabel}" /></span></td>
            <td><a class="fw-semibold" href="${ctx}/admin/solicitudes/${solicitud.id}"><i class="bi bi-eye"></i> Ver</a></td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>
</section>

<div class="row g-4 mt-1">
  <div class="col-lg-8">
    <section class="table-mor-wrapper h-100">
      <div class="d-flex justify-content-between align-items-center px-4 py-3">
        <h2 class="page-section-title">Actividad reciente</h2>
        <a class="btn btn-link fw-semibold" href="${ctx}/admin/bitacora">Ver bitácora</a>
      </div>
      <div class="table-responsive">
        <table class="table table-mor mb-0">
          <thead>
            <tr><th>Fecha</th><th>Folio</th><th>Acción</th><th>Estado</th></tr>
          </thead>
          <tbody>
            <c:forEach var="activity" items="${recentActivity}">
              <tr>
                <td><c:out value="${activity.occurredOn}" /></td>
                <td class="cell-strong"><c:out value="${activity.folio}" /></td>
                <td><c:out value="${activity.action}" /></td>
                <td><span class="badge-mor"><c:out value="${activity.statusLabel}" /></span></td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </section>
  </div>

  <div class="col-lg-4">
    <aside class="card-mor h-100">
      <h2 class="page-section-title mb-3"><i class="bi bi-calendar-event"></i> Próxima audiencia</h2>
      <c:if test="${not empty nextHearing}">
        <div class="d-flex gap-3 align-items-center mb-3">
          <div class="bg-white rounded-3 px-3 py-2 text-center">
            <div class="fw-bold fs-4"><c:out value="${nextHearing.day}" /></div>
            <div class="fw-semibold text-muted"><c:out value="${nextHearing.month}" /></div>
          </div>
          <div>
            <div class="fw-semibold"><c:out value="${nextHearing.title}" /></div>
            <div class="text-muted"><c:out value="${nextHearing.timeAndRoom}" /></div>
          </div>
        </div>
        <p class="small text-warning-emphasis mb-3"><c:out value="${nextHearing.note}" /></p>
      </c:if>
      <a class="btn btn-link fw-semibold p-0" href="${ctx}/admin/audiencias">Ver las audiencias</a>
    </aside>
  </div>
</div>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
