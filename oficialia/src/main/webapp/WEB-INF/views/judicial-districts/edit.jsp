<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Actualizar distrito judicial" />
<c:set var="activeNav" value="districts" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>

<header class="app-topbar">
  <div>
    <a class="back-link" href="${ctx}/judicial-districts"><i class="bi bi-arrow-left"></i> Volver a distritos</a>
    <h1 class="page-title">Actualizar distrito judicial</h1>
    <p class="page-subtitle">Cambie el nombre o la sede cuando el distrito cambie.</p>
  </div>
  <%@ include file="/WEB-INF/views/layout/user-chip.jspf" %>
</header>

<div class="row g-4">
  <div class="col-xl-8">
    <form class="card-mor p-4" method="post" action="${ctx}/judicial-districts/edit" novalidate>
      <input type="hidden" name="code" value="<c:out value='${code}' />">
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
        <div class="col-md-8">
          <label for="seatMunicipality" class="form-label">Municipio sede *</label>
          <input type="text" id="seatMunicipality" name="seatMunicipality" maxlength="${maxSeatLength}"
                 autocomplete="off" class="form-control ${not empty errors.seatMunicipality ? 'is-invalid' : ''}"
                 value="<c:out value='${form.seatMunicipality}' />">
          <c:if test="${not empty errors.seatMunicipality}">
            <div class="invalid-feedback"><c:out value="${errors.seatMunicipality}" /></div>
          </c:if>
        </div>
      </div>

      <div class="d-flex justify-content-end gap-3 mt-4">
        <a class="btn btn-outline-primary" href="${ctx}/judicial-districts">Cancelar</a>
        <button type="submit" class="btn btn-primary"><i class="bi bi-check-lg me-1"></i> Guardar cambios</button>
      </div>
    </form>
  </div>
</div>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
