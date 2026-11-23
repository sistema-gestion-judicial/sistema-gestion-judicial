<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Calificar solicitud ${demandRequest.folio}" />
<c:set var="activeNav" value="requests" />
<c:set var="pageScript" value="demand-request-rule.js" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>
<c:url var="detailUrl" value="/demand-requests/detail">
  <c:param name="folio" value="${demandRequest.folio}" />
</c:url>
<c:set var="isInadmissible" value="${form.decisionCode == 'INADMISSIBLE'}" />

<header class="page-head">
  <div class="page-head-main">
    <div class="page-head-title">
      <h1 class="page-title is-record">Calificar solicitud <c:out value="${demandRequest.folio}" /></h1>
      <span class="badge-mor ${demandRequest.status.cssClass}">
        <span class="dot"></span> <c:out value="${demandRequest.status.label}" />
      </span>
    </div>
    <p class="page-subtitle">
      <c:out value="${demandRequest.partiesText}" /> · <c:out value="${demandRequest.courtName}" />
    </p>
  </div>
  <div class="page-head-side">
    <a class="back-link" href="${detailUrl}">Volver al detalle</a>
  </div>
</header>

<div class="row g-4">
  <div class="col-xl-8">
    <form id="ruleForm" class="section-card" method="post" action="${ctx}/demand-requests/rule" novalidate>
      <input type="hidden" name="folio" value="<c:out value='${demandRequest.folio}' />">
      <div class="section-card-header">
        <h2 class="section-card-title">Resultado de la calificación</h2>
      </div>
      <div class="section-card-body">
      <p class="section-card-text mb-3">Revise el escrito y los anexos antes de elegir.</p>

      <fieldset>
        <legend class="visually-hidden">Resultado</legend>
        <div class="decision-options ${not empty errors.decision ? 'is-invalid' : ''}">
          <label class="decision-option is-admissible">
            <input class="form-check-input" type="radio" name="decision" value="ADMISSIBLE"
                   data-decision-label="procedente" ${form.decisionCode == 'ADMISSIBLE' ? 'checked' : ''}>
            <span class="decision-icon"><i class="bi bi-check-circle"></i></span>
            <span>
              <span class="decision-title">Procedente</span>
              <span class="decision-text">Se abre el expediente con número AAAA/NNNNN/JJ y se pide la mesa
                para la primera audiencia.</span>
            </span>
          </label>
          <label class="decision-option is-inadmissible">
            <input class="form-check-input" type="radio" name="decision" value="INADMISSIBLE"
                   data-decision-label="no procedente" ${isInadmissible ? 'checked' : ''}>
            <span class="decision-icon"><i class="bi bi-x-circle"></i></span>
            <span>
              <span class="decision-title">No procedente</span>
              <span class="decision-text">Se guarda el motivo del descarte y se genera el acuse para el
                demandante.</span>
            </span>
          </label>
        </div>
        <c:if test="${not empty errors.decision}">
          <div class="invalid-feedback d-block"><c:out value="${errors.decision}" /></div>
        </c:if>
      </fieldset>

      <div class="mt-4" data-reason-block ${isInadmissible ? '' : 'hidden'}>
        <label for="reason" class="form-label">Motivo del descarte *</label>
        <textarea id="reason" name="reason" rows="4" maxlength="${maxReasonLength}"
                  class="form-control ${not empty errors.reason ? 'is-invalid' : ''}"
                  aria-describedby="reasonHelp"><c:out value="${form.reason}" /></textarea>
        <c:if test="${not empty errors.reason}">
          <div class="invalid-feedback"><c:out value="${errors.reason}" /></div>
        </c:if>
        <div id="reasonHelp" class="d-flex justify-content-between form-text">
          <span>Aparece en el acuse que recibe el demandante.</span>
          <span><span data-reason-count>0</span> / <c:out value="${maxReasonLength}" /></span>
        </div>
      </div>

      <div class="alert alert-mor-info mt-4 mb-0" role="note">
        La calificación no se puede revertir. Antes de guardarla, el sistema le pedirá confirmar.
      </div>
      </div>

      <div class="section-card-footer">
        <a class="btn btn-secondary" href="${detailUrl}">Cancelar</a>
        <button type="submit" class="btn btn-primary">Continuar</button>
      </div>
    </form>
  </div>

  <div class="col-xl-4 d-flex flex-column gap-4">
    <section class="section-card">
      <div class="section-card-header">
        <h2 class="section-card-title">Solicitud</h2>
      </div>
      <div class="section-card-body">
      <dl class="field-list is-stacked">
        <div><dt>Materia</dt><dd><c:out value="${demandRequest.matter}" /></dd></div>
        <div><dt>Demandante</dt><dd><c:out value="${demandRequest.plaintiff.displayName}" /></dd></div>
        <div><dt>Demandado</dt><dd><c:out value="${demandRequest.defendant.displayName}" /></dd></div>
        <div><dt>Fecha de recepción</dt><dd><c:out value="${demandRequest.receivedDateText}" /></dd></div>
        <div><dt>Anexos</dt><dd><c:out value="${demandRequest.annexes.size()}" /> archivos</dd></div>
      </dl>
      </div>
    </section>
    <c:if test="${not empty demandRequest.duplicityWarning}">
      <p class="alert-mor-note" role="note">
        Se registró con advertencia de duplicidad:
        <c:out value="${demandRequest.duplicityWarning.courtName}" />,
        <c:out value="${demandRequest.duplicityWarning.matter}" />.
      </p>
    </c:if>
  </div>
</div>

<c:if test="${askConfirmation}">
  <div class="modal modal-mor fade" id="confirmModal" tabindex="-1" aria-labelledby="confirmModalTitle"
       data-bs-backdrop="static" data-show-on-load>
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h2 class="modal-title" id="confirmModalTitle">Confirmar calificación</h2>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
        </div>
        <div class="modal-body">
          <p class="modal-lead">¿Desea calificar la solicitud <c:out value="${demandRequest.folio}" /> como
            <c:out value="${fn:toLowerCase(form.decision.label)}" />?</p>
          <p class="modal-lead-sub"><c:out value="${demandRequest.partiesText}" /> ·
            <c:out value="${demandRequest.courtName}" /></p>
          <c:choose>
            <c:when test="${isInadmissible}">
              <p class="modal-lead mt-4 mb-2">Motivo que verá el demandante</p>
              <blockquote class="alert alert-mor-info mb-0"><c:out value="${form.reason}" /></blockquote>
            </c:when>
            <c:otherwise>
              <p class="mt-4 mb-0">Se generará el número de expediente y se pedirá la mesa al sistema de Audiencias.</p>
            </c:otherwise>
          </c:choose>
          <p class="alert-mor-note mt-4">Esta acción no se puede revertir.</p>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Revisar de nuevo</button>
          <button type="submit" class="btn btn-primary" form="ruleForm" name="confirmed" value="true">
            Sí, calificar
          </button>
        </div>
      </div>
    </div>
  </div>
</c:if>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
