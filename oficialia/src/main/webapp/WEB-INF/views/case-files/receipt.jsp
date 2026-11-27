<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Acuse de pase a segunda instancia" />
<c:set var="activeNav" value="case-files" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>

<c:url var="detailUrl" value="/case-files/detail">
  <c:param name="number" value="${caseFile.number}" />
</c:url>

<header class="page-head d-print-none">
  <div class="page-head-main">
    <h1 class="page-title is-record">Acuse de pase a segunda instancia</h1>
    <p class="page-subtitle">Imprímalo y entréguelo a la parte que se inconformó.</p>
  </div>
  <div class="page-head-side">
    <button type="button" class="btn btn-primary" onclick="window.print()">Imprimir acuse</button>
    <a class="back-link" href="<c:out value='${detailUrl}' />">Volver al expediente</a>
  </div>
</header>

<c:if test="${not empty flashMessage}">
  <div class="alert alert-mor-success d-flex gap-2 align-items-start d-print-none" role="status">
    <i class="bi bi-check-circle mt-1"></i> <span><c:out value="${flashMessage}" /></span>
  </div>
</c:if>

<div class="row g-4">
  <div class="col-xl-8">
    <section class="section-card">
      <div class="section-card-header">
        <h2 class="section-card-title">Oficialía de Partes · <c:out value="${caseFile.courtName}" /></h2>
      </div>
      <div class="section-card-body">
        <p class="section-card-text">
          Se hace constar que el expediente <strong><c:out value="${caseFile.number}" /></strong> pasó a segunda
          instancia el <c:out value="${caseFile.secondInstanceAtText}" /> y se entregó al sistema de tocas.
        </p>
        <dl class="field-list">
          <div><dt>Expediente</dt><dd><c:out value="${caseFile.number}" /></dd></div>
          <div><dt>Materia</dt><dd><c:out value="${caseFile.matter}" /></dd></div>
          <div><dt>Actor</dt><dd><c:out value="${caseFile.plaintiffName}" /></dd></div>
          <div><dt>Demandado</dt><dd><c:out value="${caseFile.defendantName}" /></dd></div>
          <div><dt>Resolución de primera instancia</dt><dd><c:out value="${caseFile.firstInstanceResolvedAtText}" /></dd></div>
          <div><dt>Sala</dt>
            <dd><c:out value="${caseFile.chamberPending ? 'Pendiente: el sistema de tocas no respondió' : caseFile.chamberName}" /></dd></div>
        </dl>
      </div>
    </section>
  </div>
</div>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
