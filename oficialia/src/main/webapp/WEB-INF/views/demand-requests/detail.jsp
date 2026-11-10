<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Solicitud ${demandRequest.folio}" />
<c:set var="activeNav" value="requests" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>

<header class="app-topbar">
  <div>
    <a class="back-link" href="${ctx}/demand-requests"><i class="bi bi-arrow-left"></i> Volver a Solicitudes</a>
    <div class="d-flex flex-wrap align-items-center gap-3">
      <h1 class="page-title mb-0">Solicitud <c:out value="${demandRequest.folio}" /></h1>
      <span class="badge-mor ${demandRequest.status.cssClass}">
        <span class="dot"></span> <c:out value="${demandRequest.status.label}" />
      </span>
    </div>
    <p class="page-subtitle">
      Recibida el <c:out value="${demandRequest.receivedDateText}" /> en la Oficialía de Partes de
      <c:out value="${demandRequest.courtName}" />.
    </p>
  </div>
  <%@ include file="/WEB-INF/views/layout/user-chip.jspf" %>
</header>

<c:if test="${not empty flashWarning}">
  <div class="alert alert-mor-warning d-flex gap-2 align-items-start" role="alert">
    <i class="bi bi-exclamation-triangle mt-1"></i> <span><c:out value="${flashWarning}" /></span>
  </div>
</c:if>
<c:if test="${not empty flashMessage}">
  <div class="alert alert-mor-success d-flex gap-2 align-items-start" role="status">
    <i class="bi bi-check-circle mt-1"></i> <span><c:out value="${flashMessage}" /></span>
  </div>
</c:if>

<c:if test="${demandRequest.status.code == 'ADMISSIBLE'}">
  <section class="ruling-result is-admissible mb-4" aria-label="Resultado de la calificación">
    <div>
      <p class="ruling-label">Número de expediente</p>
      <p class="ruling-value"><c:out value="${demandRequest.caseFileNumber}" /></p>
    </div>
    <div>
      <p class="ruling-label">Mesa para la primera audiencia</p>
      <c:choose>
        <c:when test="${demandRequest.deskPending}">
          <p class="ruling-value is-pending"><i class="bi bi-hourglass-split me-1"></i> Mesa pendiente</p>
          <p class="small mb-0">El servicio de Audiencias no respondió; la mesa se asigna en cuanto responda.</p>
        </c:when>
        <c:otherwise>
          <p class="ruling-value"><c:out value="${demandRequest.deskName}" /></p>
        </c:otherwise>
      </c:choose>
    </div>
    <p class="ruling-meta">
      Calificada como procedente por <c:out value="${demandRequest.ruledBy}" /> el
      <c:out value="${demandRequest.ruledAtText}" />. Los datos se muestran en solo lectura.
    </p>
  </section>
</c:if>

<c:if test="${demandRequest.status.code == 'INADMISSIBLE'}">
  <section class="ruling-result is-inadmissible mb-4" aria-label="Resultado de la calificación">
    <div class="flex-grow-1">
      <p class="ruling-label">Motivo del descarte</p>
      <p class="mb-0 fs-5"><c:out value="${demandRequest.rejectionReason}" /></p>
    </div>
    <p class="ruling-meta">
      Calificada como no procedente por <c:out value="${demandRequest.ruledBy}" /> el
      <c:out value="${demandRequest.ruledAtText}" />. Los datos se muestran en solo lectura.
    </p>
  </section>
</c:if>

<div class="row g-4">
  <div class="col-xl-8 d-flex flex-column gap-4">

    <section class="card-mor p-4">
      <h2 class="card-title fs-5 mb-3"><i class="bi bi-people me-2"></i>Partes</h2>
      <div class="row g-3">
        <c:forEach var="party" items="${[demandRequest.plaintiff, demandRequest.defendant]}">
          <div class="col-md-6">
            <div class="party-card h-100">
              <p class="party-role"><c:out value="${party.role.label}" /></p>
              <p class="party-name"><c:out value="${party.displayName}" /></p>
              <c:choose>
                <c:when test="${party.identified}">
                  <dl class="detail-list is-compact">
                    <div><dt>Tipo</dt><dd><c:out value="${party.personType}" /></dd></div>
                    <div><dt><c:out value="${party.personIdLabel}" /></dt>
                      <dd class="font-monospace"><c:out value="${party.personId}" /></dd></div>
                    <div><dt>Teléfono</dt>
                      <dd><c:out value="${empty party.phone ? 'No registrado' : party.phone}" /></dd></div>
                    <div><dt>Correo</dt>
                      <dd class="text-break"><c:out value="${empty party.email ? 'No registrado' : party.email}" /></dd></div>
                  </dl>
                </c:when>
                <c:otherwise>
                  <p class="text-secondary mb-0">
                    Registrado como <c:out value="${party.role eq 'PLAINTIFF' ? 'anónimo' : 'desconocido'}" />;
                    no hay datos de identificación.
                  </p>
                </c:otherwise>
              </c:choose>
            </div>
          </div>
        </c:forEach>
      </div>
      <dl class="detail-list mt-3 mb-0">
        <div><dt>Representante legal</dt>
          <dd><c:out value="${empty demandRequest.legalRepresentative ? 'No registrado' : demandRequest.legalRepresentative}" /></dd></div>
      </dl>
    </section>

    <section class="card-mor p-4">
      <h2 class="card-title fs-5 mb-3"><i class="bi bi-file-earmark-text me-2"></i>Datos de la solicitud</h2>
      <dl class="detail-list is-grid mb-0">
        <div><dt>Folio</dt><dd><c:out value="${demandRequest.folio}" /></dd></div>
        <div><dt>Materia</dt><dd><c:out value="${demandRequest.matter}" /></dd></div>
        <div><dt>Juzgado</dt><dd><c:out value="${demandRequest.courtName}" /></dd></div>
        <div><dt>Fecha de recepción</dt><dd><c:out value="${demandRequest.receivedDateText}" /></dd></div>
        <div><dt>Capturó</dt><dd><c:out value="${demandRequest.capturedBy}" /></dd></div>
        <div><dt>Expediente</dt><dd><c:out value="${demandRequest.caseFileText}" /></dd></div>
      </dl>
    </section>

    <section class="card-mor p-4">
      <h2 class="card-title fs-5 mb-3"><i class="bi bi-paperclip me-2"></i>Anexos
        <span class="badge-mor is-neutral ms-2"><c:out value="${demandRequest.annexes.size()}" /></span></h2>
      <ul class="annex-list">
        <c:forEach var="annex" items="${demandRequest.annexes}">
          <li class="annex-item">
            <i class="bi bi-file-earmark-pdf annex-icon"></i>
            <div class="flex-grow-1">
              <p class="mb-0 fw-bold"><c:out value="${annex.fileName}" /></p>
              <p class="mb-0 small text-secondary"><c:out value="${annex.sizeText}" /></p>
            </div>
            <span class="btn btn-outline-primary btn-sm disabled" aria-disabled="true"
                  title="Se podrá abrir cuando exista el almacenamiento de archivos">
              <i class="bi bi-box-arrow-up-right me-1"></i> Abrir
            </span>
          </li>
        </c:forEach>
      </ul>
    </section>
  </div>

  <div class="col-xl-4 d-flex flex-column gap-4">
    <c:if test="${demandRequest.pending}">
      <section class="card-mor p-4">
        <h2 class="card-title fs-5 mb-2"><i class="bi bi-clipboard-check me-2"></i>Calificación</h2>
        <c:choose>
          <c:when test="${sessionScope.currentUser.clerk}">
            <p class="text-secondary">Revise los datos y los anexos, y califique la solicitud como procedente
              o no procedente.</p>
            <c:url var="ruleUrl" value="/demand-requests/rule">
              <c:param name="folio" value="${demandRequest.folio}" />
            </c:url>
            <a class="btn btn-primary w-100" href="${ruleUrl}">
              <i class="bi bi-clipboard-check me-1"></i> Calificar solicitud
            </a>
          </c:when>
          <c:otherwise>
            <p class="text-secondary mb-0">Solo la Oficial de Partes puede calificar esta solicitud.</p>
          </c:otherwise>
        </c:choose>
      </section>
    </c:if>
    <section class="card-mor p-4">
      <h2 class="card-title fs-5 mb-3"><i class="bi bi-clock-history me-2"></i>Seguimiento</h2>
      <ol class="timeline">
        <li class="timeline-item is-done">
          <p class="timeline-title">Solicitud registrada</p>
          <p class="timeline-text"><c:out value="${demandRequest.receivedDateText}" /> ·
            <c:out value="${demandRequest.capturedBy}" /></p>
        </li>
        <c:if test="${not empty demandRequest.duplicityWarning}">
          <li class="timeline-item is-warning">
            <p class="timeline-title">Advertencia de duplicidad</p>
            <p class="timeline-text">
              Coincidencia en <c:out value="${demandRequest.duplicityWarning.courtName}" />,
              materia <c:out value="${demandRequest.duplicityWarning.matter}" />.
              Decisión: <c:out value="${demandRequest.duplicityWarning.decision}" />.
            </p>
          </li>
        </c:if>
        <c:choose>
          <c:when test="${demandRequest.pending}">
            <li class="timeline-item">
              <p class="timeline-title">Pendiente de calificación</p>
              <p class="timeline-text">La Oficial de Partes debe calificarla como procedente o no procedente.</p>
            </li>
          </c:when>
          <c:otherwise>
            <li class="timeline-item is-done">
              <p class="timeline-title">Calificada: <c:out value="${demandRequest.status.label}" /></p>
              <p class="timeline-text"><c:out value="${demandRequest.ruledAtText}" /> ·
                <c:out value="${demandRequest.ruledBy}" /></p>
            </li>
          </c:otherwise>
        </c:choose>
      </ol>
    </section>
  </div>
</div>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
