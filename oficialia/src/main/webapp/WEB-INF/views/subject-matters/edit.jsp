<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Actualizar materia" />
<c:set var="activeNav" value="matters" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>

<header class="page-head">
  <div class="page-head-main">
    <h1 class="page-title is-record">Actualizar materia</h1>
    <p class="page-subtitle">Cambie el nombre o la descripción. Las solicitudes registradas siguen ligadas por su clave.</p>
  </div>
  <div class="page-head-side">
    <a class="back-link" href="${ctx}/subject-matters">Volver a Materias</a>
  </div>
</header>

<div class="row g-4">
  <div class="col-xl-8">
    <form class="section-card" method="post" action="${ctx}/subject-matters/edit" novalidate data-unsaved-guard>
      <input type="hidden" name="code" value="<c:out value='${code}' />">
      <div class="section-card-header">
        <h2 class="section-card-title">Datos de la materia</h2>
      </div>
      <div class="section-card-body">
        <c:if test="${not empty errors}">
          <div class="alert alert-mor-warning py-2" role="alert">
            <i class="bi bi-exclamation-triangle me-1"></i> Revise los campos marcados. Los cambios no se guardaron.
          </div>
        </c:if>

        <div class="row g-3">
          <div class="col-md-4">
            <label for="codeShown" class="form-label">Clave</label>
            <input type="text" id="codeShown" class="form-control" value="<c:out value='${code}' />"
                   aria-describedby="codeHelp" disabled readonly>
            <div id="codeHelp" class="form-text">La clave no se puede modificar.</div>
          </div>
          <div class="col-md-8">
            <label for="name" class="form-label">Nombre *</label>
            <input type="text" id="name" name="name" maxlength="${maxNameLength}" autofocus autocomplete="off"
                   class="form-control ${not empty errors.name ? 'is-invalid' : ''}"
                   value="<c:out value='${form.name}' />">
            <c:if test="${not empty errors.name}">
              <div class="invalid-feedback"><c:out value="${errors.name}" /></div>
            </c:if>
          </div>
          <div class="col-12">
            <label for="description" class="form-label">Descripción</label>
            <textarea id="description" name="description" rows="3" maxlength="${maxDescriptionLength}"
                      class="form-control ${not empty errors.description ? 'is-invalid' : ''}"><c:out value="${form.description}" /></textarea>
            <c:if test="${not empty errors.description}">
              <div class="invalid-feedback"><c:out value="${errors.description}" /></div>
            </c:if>
          </div>
        </div>
      </div>
      <div class="section-card-footer">
        <a class="btn btn-secondary" href="${ctx}/subject-matters">Cancelar</a>
        <button type="submit" class="btn btn-primary">Guardar cambios</button>
      </div>
    </form>
  </div>
</div>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
