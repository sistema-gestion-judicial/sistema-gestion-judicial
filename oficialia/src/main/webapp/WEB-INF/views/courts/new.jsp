<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Registrar juzgado" />
<c:set var="activeNav" value="courts" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>

<header class="page-head">
  <div class="page-head-main">
    <h1 class="page-title is-record">Registrar juzgado</h1>
    <p class="page-subtitle">
      El juzgado queda activo con su administrador, que registra después a los oficiales de partes.
    </p>
  </div>
  <div class="page-head-side">
    <a class="back-link" href="${ctx}/courts">Volver a Juzgados</a>
  </div>
</header>

<div class="row g-4">
  <div class="col-xl-8">
    <c:if test="${not empty errors}">
      <div class="alert alert-mor-warning d-flex gap-2 align-items-start" role="alert">
        <i class="bi bi-exclamation-triangle mt-1"></i>
        <span>Revise los campos marcados. El juzgado no se guardó.</span>
      </div>
    </c:if>

    <form id="courtForm" method="post" action="${ctx}/courts/new" novalidate data-unsaved-guard>
      <section class="section-card">
        <div class="section-card-header">
          <h2 class="section-card-title">Datos del juzgado</h2>
        </div>
        <div class="section-card-body">
          <div class="row g-3">
            <div class="col-md-3">
              <label for="code" class="form-label">Clave *</label>
              <input type="text" id="code" name="code" maxlength="${codeDigits}" inputmode="numeric" autofocus
                     autocomplete="off" aria-describedby="codeHelp"
                     class="form-control ${not empty errors.code ? 'is-invalid' : ''}"
                     value="<c:out value='${form.code}' />">
              <c:if test="${not empty errors.code}">
                <div class="invalid-feedback"><c:out value="${errors.code}" /></div>
              </c:if>
              <div id="codeHelp" class="form-text">${codeDigits} dígitos. Va al final del número de expediente.</div>
            </div>
            <div class="col-md-3">
              <label for="number" class="form-label">Número *</label>
              <input type="text" id="number" name="number" maxlength="3" inputmode="numeric" autocomplete="off"
                     class="form-control ${not empty errors.number ? 'is-invalid' : ''}"
                     value="<c:out value='${form.number}' />">
              <c:if test="${not empty errors.number}">
                <div class="invalid-feedback"><c:out value="${errors.number}" /></div>
              </c:if>
            </div>
            <div class="col-md-6">
              <label for="name" class="form-label">Nombre *</label>
              <input type="text" id="name" name="name" maxlength="${maxNameLength}" autocomplete="off"
                     class="form-control ${not empty errors.name ? 'is-invalid' : ''}"
                     placeholder="Juzgado de ..." value="<c:out value='${form.name}' />">
              <c:if test="${not empty errors.name}">
                <div class="invalid-feedback"><c:out value="${errors.name}" /></div>
              </c:if>
            </div>
            <div class="col-12">
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
          </div>
        </div>
      </section>
      <section class="section-card mt-4">
        <div class="section-card-header">
          <h2 class="section-card-title">Dirección</h2>
        </div>
        <div class="section-card-body">
          <div class="row g-3">
            <div class="col-md-8">
              <label for="street" class="form-label">Calle y número *</label>
              <input type="text" id="street" name="street" maxlength="${maxStreetLength}" autocomplete="off"
                     class="form-control ${not empty errors.street ? 'is-invalid' : ''}"
                     value="<c:out value='${form.street}' />">
              <c:if test="${not empty errors.street}">
                <div class="invalid-feedback"><c:out value="${errors.street}" /></div>
              </c:if>
            </div>
            <div class="col-md-4">
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
              <input type="text" id="postalCode" name="postalCode" maxlength="5" inputmode="numeric"
                     autocomplete="off" class="form-control ${not empty errors.postalCode ? 'is-invalid' : ''}"
                     value="<c:out value='${form.postalCode}' />">
              <c:if test="${not empty errors.postalCode}">
                <div class="invalid-feedback"><c:out value="${errors.postalCode}" /></div>
              </c:if>
            </div>
            <div class="col-md-5">
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
                     class="form-control ${not empty errors.phone ? 'is-invalid' : ''}"
                     value="<c:out value='${form.phone}' />">
              <c:if test="${not empty errors.phone}">
                <div class="invalid-feedback"><c:out value="${errors.phone}" /></div>
              </c:if>
            </div>
          </div>
        </div>
      </section>
      <section class="section-card mt-4">
        <div class="section-card-header">
          <h2 class="section-card-title">Materias que atiende *</h2>
        </div>
        <div class="section-card-body">
          <fieldset aria-describedby="mattersHelp">
            <legend class="visually-hidden">Materias que atiende el juzgado</legend>
            <div class="d-flex flex-wrap gap-4">
              <c:forEach var="matter" items="${matters}">
                <div class="form-check">
                  <input class="form-check-input ${not empty errors.subjectMatterCodes ? 'is-invalid' : ''}"
                         type="checkbox" id="matter-${matter.code}" name="subjectMatterCodes" value="${matter.code}"
                         ${form.subjectMatterCodes.contains(matter.code) ? 'checked' : ''}>
                  <label class="form-check-label" for="matter-${matter.code}"><c:out value="${matter.name}" /></label>
                </div>
              </c:forEach>
            </div>
            <c:if test="${not empty errors.subjectMatterCodes}">
              <div class="text-danger small mt-2" role="alert"><c:out value="${errors.subjectMatterCodes}" /></div>
            </c:if>
            <div id="mattersHelp" class="form-text">Elija al menos una.</div>
          </fieldset>
        </div>
      </section>
      <section class="section-card mt-4">
        <div class="section-card-header">
          <h2 class="section-card-title">Administrador del juzgado</h2>
        </div>
        <div class="section-card-body">
          <div class="row g-3">
            <div class="col-md-6">
              <label for="adminFirstName" class="form-label">Nombre(s) *</label>
              <input type="text" id="adminFirstName" name="adminFirstName" maxlength="${maxPersonNameLength}"
                     autocomplete="off" class="form-control ${not empty errors.adminFirstName ? 'is-invalid' : ''}"
                     value="<c:out value='${form.adminFirstName}' />">
              <c:if test="${not empty errors.adminFirstName}">
                <div class="invalid-feedback"><c:out value="${errors.adminFirstName}" /></div>
              </c:if>
            </div>
            <div class="col-md-6">
              <label for="adminLastName" class="form-label">Apellidos *</label>
              <input type="text" id="adminLastName" name="adminLastName" maxlength="${maxPersonNameLength}"
                     autocomplete="off" class="form-control ${not empty errors.adminLastName ? 'is-invalid' : ''}"
                     value="<c:out value='${form.adminLastName}' />">
              <c:if test="${not empty errors.adminLastName}">
                <div class="invalid-feedback"><c:out value="${errors.adminLastName}" /></div>
              </c:if>
            </div>
            <div class="col-md-6">
              <label for="adminEmail" class="form-label">Correo electrónico *</label>
              <input type="email" id="adminEmail" name="adminEmail" maxlength="${maxEmailLength}" autocomplete="off"
                     class="form-control ${not empty errors.adminEmail ? 'is-invalid' : ''}"
                     value="<c:out value='${form.adminEmail}' />">
              <c:if test="${not empty errors.adminEmail}">
                <div class="invalid-feedback"><c:out value="${errors.adminEmail}" /></div>
              </c:if>
            </div>
            <div class="col-md-6">
              <label for="adminPassword" class="form-label">Contraseña temporal *</label>
              <%-- Never filled in again: after an error the password has to be typed once more --%>
              <input type="password" id="adminPassword" name="adminPassword" autocomplete="new-password"
                     aria-describedby="passwordHelp"
                     class="form-control ${not empty errors.adminPassword ? 'is-invalid' : ''}">
              <c:if test="${not empty errors.adminPassword}">
                <div class="invalid-feedback"><c:out value="${errors.adminPassword}" /></div>
              </c:if>
              <div id="passwordHelp" class="form-text">
                Mínimo 8 caracteres, con letras y números. Se cambia al iniciar sesión por primera vez.
              </div>
            </div>
          </div>
        </div>
      </section>
    </form>
  </div>

  <div class="col-xl-4 d-flex flex-column gap-4">
    <aside class="section-card">
      <div class="section-card-header">
        <h2 class="section-card-title">Qué pasa al registrar</h2>
      </div>
      <div class="section-card-body">
        <p class="section-card-text">
          El juzgado se guarda activo junto con su administrador. La clave va al final de cada número de expediente
          y no se puede cambiar después. El administrador entra con la contraseña temporal y el sistema le pide
          cambiarla la primera vez.
        </p>
      </div>
    </aside>
    <div class="d-grid gap-3">
      <button type="submit" class="btn btn-primary" form="courtForm">
        <i class="bi bi-plus-lg me-1"></i> Registrar juzgado
      </button>
      <a class="btn btn-secondary" href="${ctx}/courts">Cancelar</a>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
