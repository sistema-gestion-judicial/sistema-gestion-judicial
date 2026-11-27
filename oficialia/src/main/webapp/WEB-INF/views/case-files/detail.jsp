<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Expediente ${caseFile.number}" />
<c:set var="activeNav" value="case-files" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>

<c:url var="secondInstanceUrl" value="/case-files/second-instance">
  <c:param name="number" value="${caseFile.number}" />
</c:url>
<c:url var="receiptUrl" value="/case-files/receipt">
  <c:param name="number" value="${caseFile.number}" />
</c:url>
<c:url var="requestUrl" value="/demand-requests/detail">
  <c:param name="folio" value="${caseFile.folio}" />
</c:url>
<%-- The clerk can still pick the chamber when the Tocas system did not answer the first time --%>
<c:set var="canMove" value="${isClerk and not caseFile.concluded and (not caseFile.secondInstance or caseFile.chamberPending)}" />

<header class="page-head">
  <div class="page-head-main">
    <div class="page-head-title">
      <h1 class="page-title is-record">Expediente <c:out value="${caseFile.number}" /></h1>
      <span class="badge-mor ${caseFile.status.cssClass}">
        <span class="dot"></span> <c:out value="${caseFile.status.label}" />
      </span>
    </div>
    <p class="page-subtitle">
      <c:out value="${caseFile.partiesText}" /> · <c:out value="${caseFile.courtName}" />
    </p>
  </div>
  <div class="page-head-side">
    <c:if test="${canMove}">
      <a class="btn btn-primary" href="<c:out value='${secondInstanceUrl}' />">
        <c:out value="${caseFile.chamberPending ? 'Elegir sala' : 'Registrar pase a segunda instancia'}" />
      </a>
    </c:if>
    <a class="back-link" href="${ctx}/case-files">Volver a Expedientes</a>
  </div>
</header>

<c:if test="${not empty flashMessage}">
  <div class="alert alert-mor-success d-flex gap-2 align-items-start" role="status">
    <i class="bi bi-check-circle mt-1"></i> <span><c:out value="${flashMessage}" /></span>
  </div>
</c:if>
<c:if test="${not empty flashWarning}">
  <div class="alert alert-mor-warning d-flex gap-2 align-items-start" role="alert">
    <i class="bi bi-exclamation-triangle mt-1"></i> <span><c:out value="${flashWarning}" /></span>
  </div>
</c:if>

<div class="row g-4">
  <div class="col-xl-8 d-flex flex-column gap-4">
    <section class="section-card">
      <div class="section-card-header">
        <h2 class="section-card-title">Datos del expediente</h2>
      </div>
      <div class="section-card-body">
        <dl class="field-list">
          <div><dt>Número</dt><dd><c:out value="${caseFile.number}" /></dd></div>
          <div><dt>Solicitud</dt><dd><a href="<c:out value='${requestUrl}' />"><c:out value="${caseFile.folio}" /></a></dd></div>
          <div><dt>Materia</dt><dd><c:out value="${caseFile.matter}" /></dd></div>
          <div><dt>Juzgado</dt><dd><c:out value="${caseFile.courtName}" /></dd></div>
          <div><dt>Mesa</dt><dd><c:out value="${empty caseFile.deskName ? 'Pendiente' : caseFile.deskName}" /></dd></div>
          <div><dt>Fecha de apertura</dt><dd><c:out value="${caseFile.openedAtText}" /></dd></div>
          <div><dt>Actor</dt><dd><c:out value="${caseFile.plaintiffName}" /></dd></div>
          <div><dt>Demandado</dt><dd><c:out value="${caseFile.defendantName}" /></dd></div>
        </dl>
      </div>
    </section>
  </div>

  <div class="col-xl-4 d-flex flex-column gap-4">
    <section class="section-card">
      <div class="section-card-header">
        <h2 class="section-card-title">Estado</h2>
      </div>
      <div class="section-card-body">
        <dl class="field-list is-stacked">
          <div><dt>Resolución de primera instancia</dt>
            <dd><c:out value="${caseFile.firstInstanceResolved ? caseFile.firstInstanceResolvedAtText : 'Sin resolución'}" /></dd></div>
          <div><dt>Instancia</dt><dd><c:out value="${caseFile.secondInstance ? 'Segunda' : 'Primera'}" /></dd></div>
          <c:if test="${caseFile.secondInstance}">
            <div><dt>Pase a segunda instancia</dt><dd><c:out value="${caseFile.secondInstanceAtText}" /></dd></div>
            <div><dt>Sala</dt><dd><c:out value="${caseFile.chamberPending ? 'Pendiente' : caseFile.chamberName}" /></dd></div>
          </c:if>
          <c:if test="${caseFile.concluded}">
            <div><dt>Conclusión</dt><dd><c:out value="${caseFile.concludedAtText}" /></dd></div>
          </c:if>
        </dl>
        <c:if test="${caseFile.secondInstance}">
          <a class="btn btn-secondary mt-3" href="<c:out value='${receiptUrl}' />">Ver acuse</a>
        </c:if>
      </div>
    </section>
    <p class="section-card-text small mb-0">
      Las audiencias y resoluciones se consultan en sus propios sistemas.
    </p>
  </div>
</div>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
