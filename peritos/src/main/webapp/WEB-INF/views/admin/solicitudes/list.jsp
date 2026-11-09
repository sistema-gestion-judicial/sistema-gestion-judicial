<%-- Admin 4 · Solicitudes de peritaje: all requests with summary, filters and the detail action. --%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Solicitudes de peritaje" />
<c:set var="navRole" value="admin" />
<c:set var="activeNav" value="solicitudes" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>
<c:set var="pageHeading" value="Solicitudes de peritaje" />
<c:set var="pageDescription" value="Consulte todas las solicitudes y seleccione una para ver su detalle y asignación." />
<%@ include file="/WEB-INF/views/layout/page-header.jspf" %>

<section class="card-mor mt-4 p-4">
  <div class="summary-strip">
    <div><div class="summary-label">Total de solicitudes</div><div class="summary-value"><c:out value="${totalRequests}" /></div></div>
    <div><div class="summary-label">Recibidas</div><div class="summary-value"><c:out value="${receivedRequests}" /></div></div>
    <div><div class="summary-label">Asignadas</div><div class="summary-value"><c:out value="${assignedRequests}" /></div></div>
    <div><div class="summary-label">En proceso</div><div class="summary-value"><c:out value="${inProgressRequests}" /></div></div>
    <div><div class="summary-label">Resueltas</div><div class="summary-value"><c:out value="${resolvedRequests}" /></div></div>
  </div>
</section>

<form class="card-mor mt-4 p-4 d-flex flex-wrap gap-3 align-items-center" method="get" action="${ctx}/admin/solicitudes">
  <div class="input-group" style="max-width: 330px;">
    <span class="input-group-text bg-white"><i class="bi bi-search"></i></span>
    <input type="search" class="form-control" name="q" placeholder="Buscar folio o expediente" value="${param.q}">
  </div>
  <select class="form-select" name="materia" style="max-width: 180px;" aria-label="Materia">
    <option value="">Materia: Todas</option>
    <c:forEach var="materia" items="${materias}">
      <option value="${materia.id}" ${param.materia == materia.id ? 'selected' : ''}><c:out value="${materia.name}" /></option>
    </c:forEach>
  </select>
  <select class="form-select" name="estado" style="max-width: 180px;" aria-label="Estado">
    <option value="">Estado: Todos</option>
    <option value="RECIBIDA" ${param.estado == 'RECIBIDA' ? 'selected' : ''}>Recibida</option>
    <option value="ASIGNADA" ${param.estado == 'ASIGNADA' ? 'selected' : ''}>Asignada</option>
    <option value="EN_PROCESO" ${param.estado == 'EN_PROCESO' ? 'selected' : ''}>En proceso</option>
    <option value="RESUELTA" ${param.estado == 'RESUELTA' ? 'selected' : ''}>Resuelta</option>
  </select>
  <input type="date" class="form-control" name="fecha" style="max-width: 180px;" aria-label="Fecha de solicitud" value="${param.fecha}">
  <a class="fw-semibold" href="${ctx}/admin/solicitudes">Limpiar filtros</a>
  <div class="ms-auto">
    <button type="submit" class="btn btn-primary">Aplicar filtros</button>
  </div>
</form>

<section class="table-mor-wrapper mt-4">
  <div class="table-responsive">
    <table class="table table-mor mb-0">
      <thead>
        <tr>
          <th>Folio</th><th>Expediente</th><th>Materia</th><th>Fecha de solicitud</th>
          <th>Perito asignado</th><th>Estado</th><th>Acciones</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="solicitud" items="${solicitudes}">
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
  <div class="d-flex justify-content-between align-items-center px-4 py-3">
    <span class="text-muted"><c:out value="${paginationSummary}" /></span>
    <%-- Pagination controls are added together with the database query. --%>
    <nav aria-label="Paginación"></nav>
  </div>
</section>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
