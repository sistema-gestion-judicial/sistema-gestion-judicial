<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Calificar solicitud ${demandRequest.folio}" />
<c:set var="activeNav" value="requests" />
<c:set var="pageScript" value="demand-request-rule.js" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>
<c:url var="detailUrl" value="/demand-requests/detail">
  <c:param name="folio" value="${demandRequest.folio}" />
</c:url>
<c:set var="isInadmissible" value="${form.decisionCode == 'INADMISSIBLE'}" />

<header class="app-topbar">
  <div>
    <a class="back-link" href="${detailUrl}"><i class="bi bi-arrow-left"></i> Volver al detalle</a>
    <h1 class="page-title">Calificar solicitud</h1>
    <p class="page-subtitle">
      <c:out value="${demandRequest.folio}" /> · <c:out value="${demandRequest.partiesText}" />
    </p>
  </div>
  <%@ include file="/WEB-INF/views/layout/user-chip.jspf" %>
</header>

<div class="row g-4">
  <div class="col-xl-8">
    <form id="ruleForm" class="card-mor p-4" method="post" action="${ctx}/demand-requests/rule" novalidate>
      <input type="hidden" name="folio" value="<c:out value='${demandRequest.folio}' />">
      <h2 class="card-title fs-5 mb-1"><i class="bi bi-clipboard-check me-2"></i>Resultado de la calificación</h2>
      <p class="text-secondary mb-3">Revise el escrito y los anexos antes de elegir.</p>

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

      <div class="alert alert-mor-info d-flex gap-2 align-items-start mt-4 mb-4" role="note">
        <i class="bi bi-info-circle mt-1"></i>
        <span>La calificación no se puede revertir. Antes de guardarla, el sistema le pedirá confirmar.</span>
      </div>

      <div class="d-flex justify-content-end gap-3">
        <a class="btn btn-outline-primary" href="${detailUrl}">Cancelar</a>
        <button type="submit" class="btn btn-primary">Continuar <i class="bi bi-arrow-right ms-1"></i></button>
      </div>
    </form>
  </div>

  <div class="col-xl-4 d-flex flex-column gap-4">
    <section class="card-mor p-4">
      <h2 class="card-title fs-5 mb-3"><i class="bi bi-file-earmark-text me-2"></i>Solicitud</h2>
      <dl class="detail-list mb-0">
        <div><dt>Materia</dt><dd><c:out value="${demandRequest.matter}" /></dd></div>
        <div><dt>Demandante</dt><dd><c:out value="${demandRequest.plaintiff.displayName}" /></dd></div>
        <div><dt>Demandado</dt><dd><c:out value="${demandRequest.defendant.displayName}" /></dd></div>
        <div><dt>Fecha de recepción</dt><dd><c:out value="${demandRequest.receivedDateText}" /></dd></div>
        <div><dt>Anexos</dt><dd><c:out value="${demandRequest.annexes.size()}" /> archivos</dd></div>
      </dl>
    </section>
    <c:if test="${not empty demandRequest.duplicityWarning}">
      <div class="alert alert-mor-warning d-flex gap-2 align-items-start mb-0" role="note">
        <i class="bi bi-exclamation-triangle mt-1"></i>
        <span>Se registró con advertencia de duplicidad:
          <c:out value="${demandRequest.duplicityWarning.courtName}" />,
          <c:out value="${demandRequest.duplicityWarning.matter}" />.</span>
      </div>
    </c:if>
  </div>
</div>

<c:if test="${askConfirmation}">
  <div class="modal fade" id="confirmModal" tabindex="-1" aria-labelledby="confirmModalTitle"
       data-bs-backdrop="static" data-show-on-load>
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content p-3" style="border-radius: var(--mor-radius);">
        <div class="modal-header border-0">
          <h2 class="modal-title fs-4 fw-bold font-title" id="confirmModalTitle">Confirmar calificación</h2>
        </div>
        <div class="modal-body pt-0">
          <p>Va a calificar la solicitud <strong><c:out value="${demandRequest.folio}" /></strong> como
            <strong><c:out value="${form.decision.label}" /></strong>.</p>
          <c:choose>
            <c:when test="${isInadmissible}">
              <p class="mb-2">Motivo que verá el demandante:</p>
              <blockquote class="alert-mor-warning p-3 mb-3"><c:out value="${form.reason}" /></blockquote>
            </c:when>
            <c:otherwise>
              <p>Se generará el número de expediente y se pedirá la mesa al sistema de Audiencias.</p>
            </c:otherwise>
          </c:choose>
          <p class="fw-bold mb-0"><i class="bi bi-exclamation-triangle me-1"></i> Esta acción no se puede revertir.</p>
        </div>
        <div class="modal-footer border-0 d-grid gap-3" style="grid-template-columns: 1fr 1fr;">
          <button type="button" class="btn btn-outline-primary m-0" data-bs-dismiss="modal">Revisar de nuevo</button>
          <button type="submit" class="btn btn-primary m-0" form="ruleForm" name="confirmed" value="true">
            Sí, calificar
          </button>
        </div>
      </div>
    </div>
  </div>
</c:if>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
