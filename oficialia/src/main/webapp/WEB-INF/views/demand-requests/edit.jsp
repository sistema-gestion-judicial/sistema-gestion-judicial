<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Corregir solicitud ${demandRequest.folio}" />
<c:set var="activeNav" value="requests" />
<c:set var="pageScript" value="demand-request-new.js" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>

<c:url var="detailUrl" value="/demand-requests/detail">
  <c:param name="folio" value="${demandRequest.folio}" />
</c:url>

<header class="page-head">
  <div class="page-head-main">
    <h1 class="page-title is-record">Corregir solicitud <c:out value="${demandRequest.folio}" /></h1>
    <p class="page-subtitle">
      Corrija un error de captura antes de calificarla. Si cambia una parte o la materia se vuelve a verificar
      la duplicidad.
    </p>
  </div>
  <div class="page-head-side">
    <a class="back-link" href="<c:out value='${detailUrl}' />">Volver al detalle</a>
  </div>
</header>

<div class="row g-4">
  <div class="col-xl-8">
    <form id="correctionForm" class="section-card" method="post" action="${ctx}/demand-requests/edit" novalidate
          data-unsaved-guard>
      <input type="hidden" name="folio" value="<c:out value='${demandRequest.folio}' />">
      <div class="section-card-header">
        <h2 class="section-card-title">Datos de la solicitud</h2>
      </div>
      <fieldset class="section-card-body">
        <c:if test="${not empty errors}">
          <div class="alert alert-mor-warning py-2" role="alert">
            <i class="bi bi-exclamation-triangle me-1"></i> Revise los campos marcados. Los cambios no se guardaron.
          </div>
        </c:if>

        <div class="row g-3">
          <div class="col-md-4">
            <label for="folioShown" class="form-label">Folio</label>
            <input id="folioShown" class="form-control" value="<c:out value='${demandRequest.folio}' />" readonly>
          </div>
          <div class="col-md-4">
            <label for="receivedDate" class="form-label">Fecha de recepción</label>
            <input id="receivedDate" class="form-control" value="<c:out value='${demandRequest.receivedDateText}' />" readonly>
          </div>
          <div class="col-md-4">
            <label for="court" class="form-label">Juzgado</label>
            <input id="court" class="form-control" value="<c:out value='${demandRequest.courtName}' />" readonly>
          </div>
          <div class="col-md-4">
            <label for="matter" class="form-label">Materia *</label>
            <select id="matter" name="matter" class="form-select ${not empty errors.matter ? 'is-invalid' : ''}">
              <c:forEach var="matter" items="${matters}">
                <option value="${fn:escapeXml(matter)}" ${form.matter == matter ? 'selected' : ''}><c:out value="${matter}" /></option>
              </c:forEach>
            </select>
            <div class="invalid-feedback"><c:out value="${errors.matter}" /></div>
          </div>
          <div class="col-md-8">
            <label for="legalRepresentative" class="form-label">Representante legal</label>
            <input id="legalRepresentative" name="legalRepresentative" maxlength="${maxRepresentativeLength}"
                   class="form-control ${not empty errors.legalRepresentative ? 'is-invalid' : ''}"
                   value="${fn:escapeXml(legalRepresentative)}">
            <div class="invalid-feedback"><c:out value="${errors.legalRepresentative}" /></div>
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
      </fieldset>
      <div class="section-card-footer">
        <a class="btn btn-secondary" href="<c:out value='${detailUrl}' />">Cancelar</a>
        <button type="submit" class="btn btn-primary" name="action" value="save">Guardar corrección</button>
      </div>
    </form>
  </div>

  <div class="col-xl-4">
    <section class="section-card">
      <div class="section-card-header">
        <h2 class="section-card-title">Anexos</h2>
      </div>
      <div class="section-card-body">
        <p class="section-card-text small">Los anexos no se corrigen aquí.</p>
        <ul class="list-unstyled mb-0">
          <c:forEach var="annex" items="${demandRequest.annexes}">
            <li><i class="bi bi-paperclip me-1"></i> <c:out value="${annex.fileName}" /></li>
          </c:forEach>
        </ul>
      </div>
    </section>
  </div>
</div>

<c:if test="${not empty duplicityMatch}">
  <div class="modal modal-mor fade" id="duplicityModal" tabindex="-1" aria-labelledby="duplicityModalTitle"
       data-bs-backdrop="static" data-show-on-load>
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h2 class="modal-title" id="duplicityModalTitle">Posible demanda duplicada</h2>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
        </div>
        <div class="modal-body">
          <p class="modal-lead">Con los datos corregidos ya hay una demanda activa:
            <c:out value="${duplicityMatch.matchText}" /></p>
          <p class="modal-lead-sub"><c:out value="${duplicityMatch.courtName}" /> ·
            <c:out value="${duplicityMatch.matter}" /> · <c:out value="${duplicityMatch.instance}" /></p>
          <p class="alert-mor-note mt-4">Si continúa, la solicitud quedará marcada con esta advertencia.</p>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Revisar de nuevo</button>
          <button type="submit" class="btn btn-primary" form="correctionForm" name="action" value="continue">
            Continuar con la corrección
          </button>
        </div>
      </div>
    </div>
  </div>
</c:if>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
