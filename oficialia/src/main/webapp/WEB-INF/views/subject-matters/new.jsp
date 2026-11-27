<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Registrar materia" />
<c:set var="activeNav" value="matters" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>

<header class="page-head">
  <div class="page-head-main">
    <h1 class="page-title is-record">Registrar materia</h1>
    <p class="page-subtitle">La materia queda activa y disponible para las solicitudes, los juzgados y los demás sistemas.</p>
  </div>
  <div class="page-head-side">
    <a class="back-link" href="${ctx}/subject-matters">Volver a Materias</a>
  </div>
</header>

<div class="row g-4">
  <div class="col-xl-8">
    <form class="section-card" method="post" action="${ctx}/subject-matters/new" novalidate data-unsaved-guard>
      <div class="section-card-header">
        <h2 class="section-card-title">Datos de la materia</h2>
      </div>
      <div class="section-card-body">
        <c:if test="${not empty errors}">
          <div class="alert alert-mor-warning py-2" role="alert">
            <i class="bi bi-exclamation-triangle me-1"></i> Revise los campos marcados. La materia no se guardó.
          </div>
        </c:if>

        <div class="row g-3">
          <div class="col-md-4">
            <label for="code" class="form-label">Clave *</label>
            <input type="text" id="code" name="code" maxlength="${maxCodeLength}" autofocus autocomplete="off"
                   class="form-control text-uppercase ${not empty errors.code ? 'is-invalid' : ''}"
                   aria-describedby="codeHelp" value="<c:out value='${form.code}' />">
            <c:if test="${not empty errors.code}">
              <div class="invalid-feedback"><c:out value="${errors.code}" /></div>
            </c:if>
            <div id="codeHelp" class="form-text">Ejemplo: CIV. Los demás sistemas la usan; no se podrá cambiar.</div>
          </div>
          <div class="col-md-8">
            <label for="name" class="form-label">Nombre *</label>
            <input type="text" id="name" name="name" maxlength="${maxNameLength}" autocomplete="off"
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
        <button type="submit" class="btn btn-primary">Registrar materia</button>
      </div>
    </form>
  </div>
</div>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
