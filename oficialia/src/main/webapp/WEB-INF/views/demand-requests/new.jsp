<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Registrar solicitud de demanda" />
<c:set var="activeNav" value="new-request" />
<c:set var="pageScript" value="demand-request-new.js" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>

<header class="page-head mb-0">
  <div class="page-head-main">
    <h1 class="page-title is-record">Registrar solicitud de demanda</h1>
    <p class="page-subtitle">
      Primero capture los datos mínimos para verificar que la demanda no esté activa en otro juzgado.
    </p>
  </div>
  <div class="page-head-side">
    <a class="back-link" href="${ctx}/demand-requests">Volver a Solicitudes</a>
  </div>
</header>

<ol class="stepper">
  <li class="stepper-item ${verified ? 'is-done' : 'is-active'}">
    <span class="stepper-number">
      <c:choose><c:when test="${verified}"><i class="bi bi-check-lg"></i></c:when><c:otherwise>1</c:otherwise></c:choose>
    </span>
    Verificar duplicidad
  </li>
  <li class="stepper-line" aria-hidden="true"></li>
  <li class="stepper-item ${verified ? 'is-active' : ''}">
    <span class="stepper-number">2</span> Completar registro
  </li>
</ol>

<div class="row g-4">
  <div class="col-xl-8">

    <c:if test="${verified and empty continuedWithWarning}">
      <div class="alert alert-mor-success d-flex gap-2 align-items-start" role="status">
        <i class="bi bi-check-circle mt-1"></i>
        <span>Verificación completada: no se encontró una demanda activa con estos datos en ningún juzgado del estado.
          Ya puede completar el registro.</span>
      </div>
    </c:if>
    <c:if test="${not empty continuedWithWarning}">
      <div class="alert alert-mor-warning d-flex gap-2 align-items-start" role="status">
        <i class="bi bi-exclamation-triangle mt-1"></i>
        <span>Se guardó su decisión de continuar pese a la advertencia
          (<c:out value="${continuedWithWarning.courtName}" />, <c:out value="${continuedWithWarning.matter}" />).
          La solicitud quedará marcada como registrada con advertencia.</span>
      </div>
    </c:if>

    <c:if test="${not verified}">
    <form id="duplicityForm" class="section-card" method="post" action="${ctx}/demand-requests/new" novalidate data-unsaved-guard>
      <div class="section-card-header">
        <h2 class="section-card-title">Datos para verificar duplicidad</h2>
      </div>
      <fieldset class="section-card-body" ${verified ? 'disabled' : ''}>

        <div class="row g-3">
          <div class="col-md-4">
            <label for="matter" class="form-label">Materia *</label>
            <select id="matter" name="matter" class="form-select ${not empty errors.matter ? 'is-invalid' : ''}">
              <option value="">Seleccione</option>
              <c:forEach var="matter" items="${matters}">
                <option value="${matter}" ${form.matter == matter ? 'selected' : ''}><c:out value="${matter}" /></option>
              </c:forEach>
            </select>
            <div class="invalid-feedback"><c:out value="${errors.matter}" /></div>
          </div>
          <div class="col-md-4">
            <label for="receivedDate" class="form-label">Fecha de recepción</label>
            <input id="receivedDate" class="form-control" value="${receivedDate}" readonly>
          </div>
          <div class="col-md-4">
            <label for="court" class="form-label">Juzgado</label>
            <input id="court" class="form-control" value="${fn:escapeXml(sessionScope.currentUser.courtName)}" readonly>
          </div>
        </div>

        <hr class="section-divider my-4">
        <h3 class="section-subtitle">Demandante (actor)</h3>
        <div class="row g-3" data-party="plaintiff">
          <div class="col-md-4">
            <label for="plaintiffType" class="form-label">Tipo de persona</label>
            <select id="plaintiffType" name="plaintiffType" class="form-select" data-party-field>
              <option value="INDIVIDUAL" ${form.plaintiffType == 'INDIVIDUAL' ? 'selected' : ''}>Persona física</option>
              <option value="COMPANY" ${form.plaintiffType == 'COMPANY' ? 'selected' : ''}>Persona moral</option>
            </select>
          </div>
          <div class="col-md-4">
            <label for="plaintiffName" class="form-label">Nombre o razón social</label>
            <input id="plaintiffName" name="plaintiffName" data-party-field
                   class="form-control ${not empty errors.plaintiffName ? 'is-invalid' : ''}"
                   value="${fn:escapeXml(form.plaintiffName)}">
            <div class="invalid-feedback"><c:out value="${errors.plaintiffName}" /></div>
          </div>
          <div class="col-md-4">
            <label for="plaintiffId" class="form-label">RFC o CURP</label>
            <input id="plaintiffId" name="plaintiffId" data-party-field data-uppercase maxlength="18"
                   class="form-control ${not empty errors.plaintiffId ? 'is-invalid' : ''}"
                   value="${fn:escapeXml(form.plaintiffId)}">
            <div class="invalid-feedback"><c:out value="${errors.plaintiffId}" /></div>
          </div>
          <div class="col-12">
            <div class="form-check">
              <input class="form-check-input" type="checkbox" id="plaintiffAnonymous" name="plaintiffAnonymous"
                     data-party-toggle="plaintiff" ${form.plaintiffAnonymous ? 'checked' : ''}>
              <label class="form-check-label" for="plaintiffAnonymous">Demanda anónima (no se identifica al demandante)</label>
            </div>
          </div>
        </div>

        <h3 class="section-subtitle mt-4 pt-4 border-top">Demandado</h3>
        <div class="row g-3" data-party="defendant">
          <div class="col-md-4">
            <label for="defendantType" class="form-label">Tipo de persona</label>
            <select id="defendantType" name="defendantType" class="form-select" data-party-field>
              <option value="INDIVIDUAL" ${form.defendantType == 'INDIVIDUAL' ? 'selected' : ''}>Persona física</option>
              <option value="COMPANY" ${form.defendantType == 'COMPANY' ? 'selected' : ''}>Persona moral</option>
            </select>
          </div>
          <div class="col-md-4">
            <label for="defendantName" class="form-label">Nombre o razón social</label>
            <input id="defendantName" name="defendantName" data-party-field
                   class="form-control ${not empty errors.defendantName ? 'is-invalid' : ''}"
                   value="${fn:escapeXml(form.defendantName)}">
            <div class="invalid-feedback"><c:out value="${errors.defendantName}" /></div>
          </div>
          <div class="col-md-4">
            <label for="defendantId" class="form-label">RFC o CURP</label>
            <input id="defendantId" name="defendantId" data-party-field data-uppercase maxlength="18"
                   class="form-control ${not empty errors.defendantId ? 'is-invalid' : ''}"
                   value="${fn:escapeXml(form.defendantId)}">
            <div class="invalid-feedback"><c:out value="${errors.defendantId}" /></div>
          </div>
          <div class="col-12">
            <div class="form-check">
              <input class="form-check-input" type="checkbox" id="defendantUnknown" name="defendantUnknown"
                     data-party-toggle="defendant" ${form.defendantUnknown ? 'checked' : ''}>
              <label class="form-check-label" for="defendantUnknown">Se desconoce el nombre del demandado</label>
            </div>
          </div>
        </div>

        <c:if test="${not empty errors.parties}">
          <div class="alert alert-mor-danger mt-4 mb-0" role="alert"><c:out value="${errors.parties}" /></div>
        </c:if>
        <div class="alert alert-mor-info mt-4 mb-0">
          Capture al menos una parte con su nombre y su RFC o CURP. La duplicidad se verifica con el RFC
          o la CURP, porque dos personas pueden llamarse igual.
        </div>
      </fieldset>
    </form>
    </c:if>

    <c:choose>
      <c:when test="${verified}">
        <form id="registerForm" class="section-card" method="post" action="${ctx}/demand-requests/new"
              enctype="multipart/form-data" novalidate data-unsaved-guard>
          <%-- Step 1 is locked once verified, so its values travel again here; the server checks duplicity again --%>
          <input type="hidden" name="matter" value="${fn:escapeXml(form.matter)}">
          <input type="hidden" name="plaintiffType" value="${fn:escapeXml(form.plaintiffType)}">
          <input type="hidden" name="plaintiffName" value="${fn:escapeXml(form.plaintiffName)}">
          <input type="hidden" name="plaintiffId" value="${fn:escapeXml(form.plaintiffId)}">
          <c:if test="${form.plaintiffAnonymous}"><input type="hidden" name="plaintiffAnonymous" value="on"></c:if>
          <input type="hidden" name="defendantType" value="${fn:escapeXml(form.defendantType)}">
          <input type="hidden" name="defendantName" value="${fn:escapeXml(form.defendantName)}">
          <input type="hidden" name="defendantId" value="${fn:escapeXml(form.defendantId)}">
          <c:if test="${form.defendantUnknown}"><input type="hidden" name="defendantUnknown" value="on"></c:if>
          <c:if test="${not empty continuedWithWarning}"><input type="hidden" name="warningAccepted" value="true"></c:if>

          <div class="section-card-header">
            <div>
              <%-- Goes back to step 1 with what was typed there; the details of step 2 are not kept --%>
              <button type="submit" name="action" value="back" formnovalidate
                      class="back-link border-0 bg-transparent p-0 mb-2">
                <i class="bi bi-arrow-left" aria-hidden="true"></i> Volver a la verificación
              </button>
              <h2 class="section-card-title">Datos complementarios</h2>
            </div>
          </div>
          <div class="section-card-body">
            <dl class="field-list mb-4">
              <div><dt>Materia</dt><dd><c:out value="${form.matter}" /></dd></div>
              <div><dt>Demandante</dt>
                <dd><c:out value="${form.plaintiffAnonymous ? 'Anónimo' : form.plaintiffName.concat(' · ').concat(form.plaintiffId)}" /></dd></div>
              <div><dt>Demandado</dt>
                <dd><c:out value="${form.defendantUnknown ? 'Desconocido' : form.defendantName.concat(' · ').concat(form.defendantId)}" /></dd></div>
            </dl>
            <c:if test="${not empty detailErrors}">
              <div class="alert alert-mor-warning py-2" role="alert">
                <i class="bi bi-exclamation-triangle me-1"></i> Revise los campos marcados. La solicitud no se registró.
                <c:if test="${not empty details.annexes or not empty detailErrors.annexes}"> Vuelva a elegir los anexos.</c:if>
              </div>
            </c:if>

            <c:if test="${not form.plaintiffAnonymous}">
              <h3 class="section-subtitle">Demandante · <c:out value="${form.plaintiffName}" /></h3>
              <div class="row g-3 mb-4">
                <div class="col-12">
                  <label for="plaintiffAddress" class="form-label">Domicilio *</label>
                  <input id="plaintiffAddress" name="plaintiffAddress" maxlength="255"
                         class="form-control ${not empty detailErrors.plaintiffAddress ? 'is-invalid' : ''}"
                         placeholder="Calle, número, colonia, municipio y código postal"
                         value="${fn:escapeXml(details.plaintiffAddress)}">
                  <div class="invalid-feedback"><c:out value="${detailErrors.plaintiffAddress}" /></div>
                </div>
                <div class="col-md-5">
                  <label for="plaintiffPhone" class="form-label">Teléfono *</label>
                  <input id="plaintiffPhone" name="plaintiffPhone" type="tel" maxlength="20" placeholder="10 dígitos"
                         class="form-control ${not empty detailErrors.plaintiffPhone ? 'is-invalid' : ''}"
                         value="${fn:escapeXml(details.plaintiffPhone)}">
                  <div class="invalid-feedback"><c:out value="${detailErrors.plaintiffPhone}" /></div>
                </div>
                <div class="col-md-7">
                  <label for="plaintiffEmail" class="form-label">Correo electrónico</label>
                  <input id="plaintiffEmail" name="plaintiffEmail" type="email" maxlength="150"
                         class="form-control ${not empty detailErrors.plaintiffEmail ? 'is-invalid' : ''}"
                         value="${fn:escapeXml(details.plaintiffEmail)}">
                  <div class="invalid-feedback"><c:out value="${detailErrors.plaintiffEmail}" /></div>
                </div>
              </div>
            </c:if>

            <c:if test="${not form.defendantUnknown}">
              <h3 class="section-subtitle">Demandado · <c:out value="${form.defendantName}" /></h3>
              <div class="row g-3 mb-4">
                <div class="col-12">
                  <label for="defendantAddress" class="form-label">Domicilio para emplazar *</label>
                  <input id="defendantAddress" name="defendantAddress" maxlength="255"
                         class="form-control ${not empty detailErrors.defendantAddress ? 'is-invalid' : ''}"
                         placeholder="Calle, número, colonia, municipio y código postal"
                         value="${fn:escapeXml(details.defendantAddress)}">
                  <div class="invalid-feedback"><c:out value="${detailErrors.defendantAddress}" /></div>
                </div>
                <div class="col-md-5">
                  <label for="defendantPhone" class="form-label">Teléfono</label>
                  <input id="defendantPhone" name="defendantPhone" type="tel" maxlength="20" placeholder="10 dígitos"
                         class="form-control ${not empty detailErrors.defendantPhone ? 'is-invalid' : ''}"
                         value="${fn:escapeXml(details.defendantPhone)}">
                  <div class="invalid-feedback"><c:out value="${detailErrors.defendantPhone}" /></div>
                </div>
                <div class="col-md-7">
                  <label for="defendantEmail" class="form-label">Correo electrónico</label>
                  <input id="defendantEmail" name="defendantEmail" type="email" maxlength="150"
                         class="form-control ${not empty detailErrors.defendantEmail ? 'is-invalid' : ''}"
                         value="${fn:escapeXml(details.defendantEmail)}">
                  <div class="invalid-feedback"><c:out value="${detailErrors.defendantEmail}" /></div>
                </div>
              </div>
            </c:if>

            <h3 class="section-subtitle">Presentación</h3>
            <div class="row g-3">
              <div class="col-md-7">
                <label for="legalRepresentative" class="form-label">Representante legal</label>
                <input id="legalRepresentative" name="legalRepresentative" maxlength="150"
                       class="form-control ${not empty detailErrors.legalRepresentative ? 'is-invalid' : ''}"
                       value="${fn:escapeXml(details.legalRepresentative)}">
                <div class="invalid-feedback"><c:out value="${detailErrors.legalRepresentative}" /></div>
              </div>
              <div class="col-md-5">
                <label for="filingType" class="form-label">Tipo de presentación *</label>
                <select id="filingType" name="filingType"
                        class="form-select ${not empty detailErrors.filingType ? 'is-invalid' : ''}">
                  <option value="">Seleccione</option>
                  <c:forEach var="type" items="${filingTypes}">
                    <option value="${type}" ${details.filingType == type.name() ? 'selected' : ''}><c:out value="${type.label}" /></option>
                  </c:forEach>
                </select>
                <div class="invalid-feedback"><c:out value="${detailErrors.filingType}" /></div>
              </div>
              <div class="col-12">
                <label for="annexes" class="form-label">Anexos (PDF) *</label>
                <input id="annexes" name="annexes" type="file" accept="application/pdf,.pdf" multiple
                       class="form-control ${not empty detailErrors.annexes ? 'is-invalid' : ''}"
                       aria-describedby="annexesHelp">
                <div class="invalid-feedback"><c:out value="${detailErrors.annexes}" /></div>
                <div id="annexesHelp" class="form-text">
                  Al menos uno (el escrito inicial de demanda). Hasta ${maxAnnexes} archivos de 10 MB cada uno.
                </div>
              </div>
            </div>
          </div>
        </form>
      </c:when>
    </c:choose>
  </div>

  <div class="col-xl-4 d-flex flex-column gap-4">
    <aside class="section-card">
      <div class="section-card-header">
        <h2 class="section-card-title">Cómo funciona</h2>
      </div>
      <div class="section-card-body">
        <p class="section-card-text">
          El sistema busca en todos los juzgados del estado una demanda activa en la que coincida el RFC o la CURP
          del demandante o del demandado, junto con la materia. Si hay coincidencia verá el juzgado y la materia,
          y podrá decidir si continúa.
        </p>
      </div>
    </aside>
    <div class="d-grid gap-3">
      <c:choose>
        <c:when test="${verified}">
          <button type="submit" class="btn btn-primary" form="registerForm" name="action" value="register">
            Registrar solicitud
          </button>
          <a class="btn btn-secondary" href="${ctx}/demand-requests">Cancelar</a>
        </c:when>
        <c:otherwise>
          <button type="submit" class="btn btn-primary" form="duplicityForm" name="action" value="verify">
            Verificar duplicidad
          </button>
          <a class="btn btn-secondary" href="${ctx}/demand-requests">Cancelar</a>
        </c:otherwise>
      </c:choose>
    </div>
  </div>
</div>

<c:if test="${not empty duplicityMatch}">
  <div class="modal modal-mor fade" id="duplicityModal" tabindex="-1" aria-labelledby="duplicityModalTitle"
       data-bs-backdrop="static" data-show-on-load>
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h2 class="modal-title" id="duplicityModalTitle">Posible demanda duplicada</h2>
        </div>
        <div class="modal-body">
          <p class="modal-lead">Se encontró una demanda activa en otro juzgado.</p>
          <p class="modal-lead-sub">Coincide el <c:out value="${duplicityMatch.matchText}" />, junto con la materia.</p>
          <div class="alert-mor-note mt-4">
            <dl class="field-list">
              <div><dt>Juzgado</dt><dd><c:out value="${duplicityMatch.courtName}" /></dd></div>
              <div><dt>Materia</dt><dd><c:out value="${duplicityMatch.matter}" /></dd></div>
              <div><dt>Instancia</dt><dd><c:out value="${duplicityMatch.instance}" /></dd></div>
              <div><dt>Coincidencia</dt><dd><c:out value="${duplicityMatch.matchText}" /></dd></div>
            </dl>
          </div>
          <div class="alert alert-mor-info mt-4 mb-0">
            Si continúa, podrá capturar los datos complementarios y anexos. Después podrá calificar la
            solicitud como no procedente por duplicidad.
          </div>
        </div>
        <div class="modal-footer">
          <button type="submit" class="btn btn-secondary" form="duplicityForm" name="action" value="cancel">
            Cancelar registro
          </button>
          <button type="submit" class="btn btn-primary" form="duplicityForm" name="action" value="continue">
            Continuar con el registro
          </button>
        </div>
      </div>
    </div>
  </div>
</c:if>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
