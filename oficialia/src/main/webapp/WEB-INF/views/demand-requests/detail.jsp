<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Solicitud ${demandRequest.folio}" />
<c:set var="activeNav" value="requests" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>

<c:url var="ruleUrl" value="/demand-requests/rule">
  <c:param name="folio" value="${demandRequest.folio}" />
</c:url>
<c:set var="canRule" value="${demandRequest.pending and sessionScope.currentUser.clerk}" />

<header class="page-head">
  <div class="page-head-main">
    <div class="page-head-title">
      <h1 class="page-title is-record">Solicitud <c:out value="${demandRequest.folio}" /></h1>
      <span class="badge-mor ${demandRequest.status.cssClass}">
        <span class="dot"></span> <c:out value="${demandRequest.status.label}" />
      </span>
    </div>
    <p class="page-subtitle">
      <c:out value="${demandRequest.plaintiff.displayName}" /> vs.
      <c:out value="${demandRequest.defendant.displayName}" /> ·
      <c:out value="${demandRequest.courtName}" />
    </p>
  </div>
  <div class="page-head-side">
    <c:if test="${canRule}">
      <a class="btn btn-primary" href="${ruleUrl}">Calificar solicitud</a>
    </c:if>
    <a class="back-link" href="${ctx}/demand-requests">Volver a Solicitudes</a>
  </div>
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

<div class="row g-4">
  <div class="col-xl-8 d-flex flex-column gap-4">

    <section class="section-card">
      <div class="section-card-header">
        <h2 class="section-card-title">Datos de la solicitud</h2>
      </div>
      <div class="section-card-body">
        <dl class="field-list">
          <div><dt>Folio</dt><dd><c:out value="${demandRequest.folio}" /></dd></div>
          <div><dt>Materia</dt><dd><c:out value="${demandRequest.matter}" /></dd></div>
          <div><dt>Fecha de recepción</dt><dd><c:out value="${demandRequest.receivedDateText}" /></dd></div>
          <div><dt>Juzgado</dt><dd><c:out value="${demandRequest.courtName}" /></dd></div>
          <div><dt>Capturó</dt><dd><c:out value="${demandRequest.capturedBy}" /></dd></div>
          <div><dt>Expediente</dt><dd><c:out value="${demandRequest.caseFileText}" /></dd></div>
        </dl>
      </div>
    </section>

    <section class="section-card">
      <div class="section-card-header">
        <h2 class="section-card-title">Partes</h2>
      </div>
      <div class="section-card-body">
        <div class="row g-4">
          <c:forEach var="party" items="${[demandRequest.plaintiff, demandRequest.defendant]}">
            <div class="col-md-6">
              <p class="party-role"><c:out value="${party.role.label}" /></p>
              <p class="party-name"><c:out value="${party.displayName}" /></p>
              <c:choose>
                <c:when test="${party.identified}">
                  <dl class="field-list is-stacked">
                    <div><dt>Tipo</dt><dd><c:out value="${party.personType}" /></dd></div>
                    <div><dt><c:out value="${party.personIdLabel}" /></dt>
                      <dd><c:out value="${party.personId}" /></dd></div>
                    <div><dt>Teléfono</dt>
                      <dd><c:out value="${empty party.phone ? 'No registrado' : party.phone}" /></dd></div>
                    <div><dt>Correo</dt>
                      <dd><c:out value="${empty party.email ? 'No registrado' : party.email}" /></dd></div>
                    <div><dt>Domicilio</dt>
                      <dd><c:out value="${empty party.address ? 'No registrado' : party.address}" /></dd></div>
                  </dl>
                </c:when>
                <c:otherwise>
                  <p class="section-card-text">
                    Registrado como <c:out value="${party.role eq 'PLAINTIFF' ? 'anónimo' : 'desconocido'}" />;
                    no hay datos de identificación.
                  </p>
                </c:otherwise>
              </c:choose>
            </div>
          </c:forEach>
        </div>
        <hr class="section-divider">
        <dl class="field-list">
          <div><dt>Tipo de presentación</dt>
            <dd><c:out value="${empty demandRequest.filingType ? 'No registrado' : demandRequest.filingType.label}" /></dd></div>
          <div><dt>Representante legal</dt>
            <dd><c:out value="${empty demandRequest.legalRepresentative ? 'No registrado' : demandRequest.legalRepresentative}" /></dd></div>
        </dl>
      </div>
    </section>

    <section class="section-card">
      <div class="section-card-header">
        <h2 class="section-card-title">Anexos</h2>
        <span class="card-mor-count"><c:out value="${demandRequest.annexes.size()}${demandRequest.annexes.size() == 1 ? ' archivo' : ' archivos'}" /></span>
      </div>
      <div class="section-card-body">
        <ul class="doc-list">
          <c:forEach var="annex" items="${demandRequest.annexes}">
            <li class="doc-row">
              <span class="doc-row-icon" aria-hidden="true"></span>
              <div class="doc-row-body">
                <p class="doc-row-title"><c:out value="${annex.fileName}" /></p>
                <p class="doc-row-meta"><c:out value="${annex.sizeText}" /></p>
              </div>
              <span class="btn btn-outline-primary disabled" aria-disabled="true"
                    title="Se podrá abrir cuando exista el almacenamiento de archivos">Abrir</span>
            </li>
          </c:forEach>
        </ul>
      </div>
    </section>
  </div>

  <div class="col-xl-4 d-flex flex-column gap-4">
    <section class="section-card">
      <div class="section-card-header">
        <h2 class="section-card-title">Calificación</h2>
      </div>
      <div class="section-card-body">
        <c:choose>
          <c:when test="${demandRequest.pending}">
            <p class="section-card-text">
              <c:out value="${sessionScope.currentUser.clerk
                  ? 'Revise los datos y los anexos, y califique la solicitud como procedente o no procedente.'
                  : 'Solo la Oficial de Partes puede calificar esta solicitud.'}" />
            </p>
            <c:if test="${canRule}">
              <a class="btn btn-primary w-100 mt-4" href="${ruleUrl}">Calificar solicitud</a>
            </c:if>
          </c:when>
          <c:when test="${demandRequest.status.code == 'ADMISSIBLE'}">
            <dl class="field-list is-stacked">
              <div><dt>Resultado</dt><dd>Procedente</dd></div>
              <div><dt>Número de expediente</dt><dd><c:out value="${demandRequest.caseFileNumber}" /></dd></div>
              <div><dt>Mesa para la primera audiencia</dt>
                <dd><c:out value="${demandRequest.deskPending ? 'Mesa pendiente' : demandRequest.deskName}" /></dd></div>
            </dl>
            <c:if test="${demandRequest.deskPending}">
              <div class="alert alert-mor-info mt-3 mb-0" role="status">
                El servicio de Audiencias no respondió; la mesa se asigna en cuanto responda.
              </div>
            </c:if>
            <hr class="section-divider">
            <p class="section-card-text">
              Calificada por <c:out value="${demandRequest.ruledBy}" /> el
              <c:out value="${demandRequest.ruledAtText}" />. Los datos se muestran en solo lectura.
            </p>
          </c:when>
          <c:otherwise>
            <dl class="field-list is-stacked">
              <div><dt>Resultado</dt><dd>No procedente</dd></div>
              <div><dt>Motivo del descarte</dt><dd><c:out value="${demandRequest.rejectionReason}" /></dd></div>
            </dl>
            <hr class="section-divider">
            <p class="section-card-text">
              Calificada por <c:out value="${demandRequest.ruledBy}" /> el
              <c:out value="${demandRequest.ruledAtText}" />. Los datos se muestran en solo lectura.
            </p>
          </c:otherwise>
        </c:choose>
      </div>
    </section>

    <section class="section-card">
      <div class="section-card-header">
        <h2 class="section-card-title">Seguimiento</h2>
      </div>
      <div class="section-card-body">
        <ol class="steps">
          <li class="step is-done">
            <p class="step-title">Solicitud registrada</p>
            <p class="step-text"><c:out value="${demandRequest.receivedDateText}" /> ·
              <c:out value="${demandRequest.capturedBy}" /></p>
          </li>
          <c:if test="${not empty demandRequest.duplicityWarning}">
            <li class="step is-done">
              <p class="step-title">Advertencia de duplicidad</p>
              <p class="step-text">
                Coincidencia en <c:out value="${demandRequest.duplicityWarning.courtName}" />,
                materia <c:out value="${demandRequest.duplicityWarning.matter}" />.
                Decisión: <c:out value="${demandRequest.duplicityWarning.decision}" />.
              </p>
            </li>
          </c:if>
          <c:choose>
            <c:when test="${demandRequest.pending}">
              <li class="step is-current">
                <p class="step-title">Calificación</p>
                <p class="step-text">Pendiente</p>
              </li>
            </c:when>
            <c:otherwise>
              <li class="step is-done">
                <p class="step-title">Calificada: <c:out value="${demandRequest.status.label}" /></p>
                <p class="step-text"><c:out value="${demandRequest.ruledAtText}" /> ·
                  <c:out value="${demandRequest.ruledBy}" /></p>
              </li>
            </c:otherwise>
          </c:choose>
        </ol>
      </div>
    </section>
  </div>
</div>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
