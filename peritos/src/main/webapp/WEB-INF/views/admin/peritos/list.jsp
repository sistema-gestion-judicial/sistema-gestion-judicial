<%-- Admin 1 · Peritos: list of registered peritos with filters and status actions. --%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Peritos registrados" />
<c:set var="navRole" value="admin" />
<c:set var="activeNav" value="peritos" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>
<c:set var="pageHeading" value="Peritos registrados" />
<c:set var="pageDescription" value="Consulte, registre y edite a los peritos, y cambie su estado." />
<%@ include file="/WEB-INF/views/layout/page-header.jspf" %>

<section class="card-mor mt-4 p-4">
  <div class="summary-strip">
    <div><div class="summary-label">Peritos registrados</div><div class="summary-value"><c:out value="${totalPeritos}" /></div></div>
    <div><div class="summary-label">Activos</div><div class="summary-value"><c:out value="${activePeritos}" /></div></div>
    <div><div class="summary-label">Inactivos</div><div class="summary-value"><c:out value="${inactivePeritos}" /></div></div>
    <div><div class="summary-label">Certificación en revisión</div><div class="summary-value"><c:out value="${reviewCertification}" /></div></div>
    <div><div class="summary-label">Casos activos</div><div class="summary-value"><c:out value="${activeCases}" /></div></div>
  </div>
</section>

<form class="card-mor mt-4 p-4 d-flex flex-wrap gap-3 align-items-center" method="get" action="${ctx}/admin/peritos">
  <div class="input-group" style="max-width: 330px;">
    <span class="input-group-text bg-white"><i class="bi bi-search"></i></span>
    <input type="search" class="form-control" name="q" placeholder="Buscar por nombre o materia" value="${param.q}">
  </div>
  <select class="form-select" name="materia" style="max-width: 180px;" aria-label="Materia">
    <option value="">Materia: Todas</option>
    <c:forEach var="materia" items="${materias}">
      <option value="${materia.id}" ${param.materia == materia.id ? 'selected' : ''}><c:out value="${materia.name}" /></option>
    </c:forEach>
  </select>
  <select class="form-select" name="estado" style="max-width: 170px;" aria-label="Estado">
    <option value="">Estado: Todos</option>
    <option value="ACTIVO" ${param.estado == 'ACTIVO' ? 'selected' : ''}>Activo</option>
    <option value="INACTIVO" ${param.estado == 'INACTIVO' ? 'selected' : ''}>Inactivo</option>
  </select>
  <a class="fw-semibold" href="${ctx}/admin/peritos">Limpiar filtros</a>
  <div class="ms-auto">
    <a class="btn btn-primary" href="${ctx}/admin/peritos/nuevo">Registrar perito</a>
  </div>
</form>

<section class="table-mor-wrapper mt-4">
  <div class="table-responsive">
    <table class="table table-mor mb-0">
      <thead>
        <tr>
          <th>Nombre</th><th>Materias</th><th>Certificaciones</th><th>Casos activos</th><th>Estado</th><th>Acciones</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="perito" items="${peritos}">
          <tr>
            <td class="cell-strong"><c:out value="${perito.fullName}" /></td>
            <td><c:out value="${perito.subjectsLabel}" /></td>
            <td><c:out value="${perito.certificationLabel}" /></td>
            <td><c:out value="${perito.activeCases}" /></td>
            <td><span class="badge-mor ${perito.active ? '' : 'is-neutral'}"><c:out value="${perito.active ? 'Activo' : 'Inactivo'}" /></span></td>
            <td class="text-nowrap">
              <a class="fw-semibold me-3" href="${ctx}/admin/peritos/${perito.id}/editar"><i class="bi bi-pencil"></i> Editar</a>
              <button type="button" class="btn btn-link p-0 fw-semibold align-baseline text-decoration-none"
                      data-bs-toggle="modal" data-bs-target="#estadoPeritoModal"
                      data-perito-id="${perito.id}" data-perito-name="${perito.fullName}"
                      data-perito-cases="${perito.activeCases}" data-perito-active="${perito.active}">
                <i class="bi bi-power"></i> ${perito.active ? 'Desactivar' : 'Activar'}
              </button>
            </td>
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

<%-- Admin 3 · Cambiar estado del perito: confirmation modal with the reason for the change. --%>
<div class="modal fade" id="estadoPeritoModal" tabindex="-1" aria-labelledby="estadoPeritoTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <form class="modal-content" method="post" action="${ctx}/admin/peritos/0/estado" data-action-template="${ctx}/admin/peritos/__ID__/estado">
      <input type="hidden" name="activar" value="false">
      <div class="modal-header border-0 pb-0">
        <h2 class="modal-title page-section-title" id="estadoPeritoTitle">Desactivar perito</h2>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
      </div>
      <div class="modal-body">
        <p>¿Seguro que desea desactivar a <strong data-perito-name-target></strong>? Mientras esté inactivo no recibirá nuevas asignaciones automáticas.</p>
        <div class="alert alert-warning d-none" role="alert" data-perito-cases-alert>
          Tiene <span data-perito-cases-target></span> caso(s) activo(s). Deberá reasignar a otro perito antes de que se apliquen los cambios.
        </div>
        <label class="form-label" for="motivoEstado">Motivo del cambio de estado *</label>
        <textarea id="motivoEstado" name="motivo" class="form-control" rows="4" placeholder="Escriba el motivo..." required></textarea>
      </div>
      <div class="modal-footer border-0">
        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Cancelar</button>
        <button type="submit" class="btn btn-primary">Desactivar perito</button>
      </div>
    </form>
  </div>
</div>

<c:set var="pageScript" value="peritos-estado.js" />
<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
