<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Actualizar juzgado" />
<c:set var="activeNav" value="courts" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>

<header class="app-topbar">
  <div>
    <a class="back-link" href="${ctx}/courts"><i class="bi bi-arrow-left"></i> Volver a juzgados</a>
    <h1 class="page-title">Actualizar juzgado</h1>
    <p class="page-subtitle">Cambie el nombre, la dirección o las materias que atiende el juzgado.</p>
  </div>
  <%@ include file="/WEB-INF/views/layout/user-chip.jspf" %>
</header>

<div class="row g-4">
  <div class="col-xl-8">
    <form class="card-mor p-4" method="post" action="${ctx}/courts/edit" novalidate>
      <input type="hidden" name="code" value="<c:out value='${court.code}' />">
      <c:if test="${not empty errors}">
        <div class="alert alert-mor-warning py-2" role="alert">
          <i class="bi bi-exclamation-triangle me-1"></i> Revise los campos marcados. Los cambios no se guardaron.
        </div>
      </c:if>

      <div class="row g-3">
        <div class="col-md-3">
          <label for="codeShown" class="form-label">Clave</label>
          <input type="text" id="codeShown" class="form-control" value="<c:out value='${court.code}' />" disabled readonly>
        </div>
        <div class="col-md-3">
          <label for="numberShown" class="form-label">Número</label>
          <input type="text" id="numberShown" class="form-control" value="<c:out value='${court.number}' />"
                 aria-describedby="lockedHelp" disabled readonly>
        </div>
        <div class="col-12">
          <div id="lockedHelp" class="form-text mt-0">La clave y el número no se pueden modificar.</div>
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
          <label for="districtCode" class="form-label">Distrito judicial *</label>
          <select id="districtCode" name="districtCode"
                  class="form-select ${not empty errors.districtCode ? 'is-invalid' : ''}">
            <option value="">Elija un distrito</option>
            <c:forEach var="district" items="${districts}">
              <option value="${district.code}" ${form.districtCode == district.code ? 'selected' : ''}>
                <c:out value="${district.name}" />
              </option>
            </c:forEach>
          </select>
          <c:if test="${not empty errors.districtCode}">
            <div class="invalid-feedback"><c:out value="${errors.districtCode}" /></div>
          </c:if>
        </div>

        <div class="col-md-8">
          <label for="street" class="form-label">Calle y número *</label>
          <input type="text" id="street" name="street" maxlength="${maxStreetLength}" autocomplete="off"
                 class="form-control ${not empty errors.street ? 'is-invalid' : ''}"
                 value="<c:out value='${form.street}' />">
          <c:if test="${not empty errors.street}">
            <div class="invalid-feedback"><c:out value="${errors.street}" /></div>
          </c:if>
        </div>
        <div class="col-md-6">
          <label for="neighborhood" class="form-label">Colonia *</label>
          <input type="text" id="neighborhood" name="neighborhood" maxlength="${maxNeighborhoodLength}"
                 autocomplete="off" class="form-control ${not empty errors.neighborhood ? 'is-invalid' : ''}"
                 value="<c:out value='${form.neighborhood}' />">
          <c:if test="${not empty errors.neighborhood}">
            <div class="invalid-feedback"><c:out value="${errors.neighborhood}" /></div>
          </c:if>
        </div>
        <div class="col-md-3">
          <label for="postalCode" class="form-label">Código postal *</label>
          <input type="text" id="postalCode" name="postalCode" maxlength="5" inputmode="numeric" autocomplete="off"
                 class="form-control ${not empty errors.postalCode ? 'is-invalid' : ''}"
                 value="<c:out value='${form.postalCode}' />">
          <c:if test="${not empty errors.postalCode}">
            <div class="invalid-feedback"><c:out value="${errors.postalCode}" /></div>
          </c:if>
        </div>
        <div class="col-md-6">
          <label for="municipality" class="form-label">Municipio *</label>
          <input type="text" id="municipality" name="municipality" maxlength="${maxMunicipalityLength}"
                 autocomplete="off" class="form-control ${not empty errors.municipality ? 'is-invalid' : ''}"
                 value="<c:out value='${form.municipality}' />">
          <c:if test="${not empty errors.municipality}">
            <div class="invalid-feedback"><c:out value="${errors.municipality}" /></div>
          </c:if>
        </div>
        <div class="col-md-4">
          <label for="phone" class="form-label">Teléfono</label>
          <input type="tel" id="phone" name="phone" maxlength="20" autocomplete="off"
                 class="form-control ${not empty errors.phone ? 'is-invalid' : ''}" placeholder="10 dígitos"
                 value="<c:out value='${form.phone}' />">
          <c:if test="${not empty errors.phone}">
            <div class="invalid-feedback"><c:out value="${errors.phone}" /></div>
          </c:if>
        </div>

        <div class="col-12">
          <fieldset>
            <legend class="form-label">Materias que atiende *</legend>
            <div class="d-flex flex-wrap gap-4 ${not empty errors.subjectMatterCodes ? 'is-invalid' : ''}">
              <c:forEach var="matter" items="${subjectMatters}">
                <div class="form-check">
                  <input class="form-check-input" type="checkbox" name="subjectMatterCodes" id="matter-${matter.code}"
                         value="${matter.code}" ${form.subjectMatterCodes.contains(matter.code) ? 'checked' : ''}>
                  <label class="form-check-label" for="matter-${matter.code}"><c:out value="${matter.name}" /></label>
                </div>
              </c:forEach>
            </div>
            <c:if test="${not empty errors.subjectMatterCodes}">
              <div class="invalid-feedback d-block"><c:out value="${errors.subjectMatterCodes}" /></div>
            </c:if>
            <div class="form-text">Las demandas ya registradas conservan su materia original.</div>
          </fieldset>
        </div>
      </div>

      <div class="d-flex justify-content-end gap-3 mt-4">
        <a class="btn btn-outline-primary" href="${ctx}/courts">Cancelar</a>
        <button type="submit" class="btn btn-primary"><i class="bi bi-check-lg me-1"></i> Guardar cambios</button>
      </div>
    </form>
  </div>
</div>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
