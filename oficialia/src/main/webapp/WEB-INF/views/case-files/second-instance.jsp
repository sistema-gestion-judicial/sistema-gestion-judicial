<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="caseFile" value="${options.caseFile}" />
<c:set var="pageTitle" value="Pase a segunda instancia" />
<c:set var="activeNav" value="case-files" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>

<c:url var="detailUrl" value="/case-files/detail">
  <c:param name="number" value="${caseFile.number}" />
</c:url>

<header class="page-head">
  <div class="page-head-main">
    <h1 class="page-title is-record">Pase a segunda instancia</h1>
    <p class="page-subtitle">
      Expediente <c:out value="${caseFile.number}" /> · <c:out value="${caseFile.partiesText}" />
    </p>
  </div>
  <div class="page-head-side">
    <a class="back-link" href="<c:out value='${detailUrl}' />">Volver al expediente</a>
  </div>
</header>

<div class="row g-4">
  <div class="col-xl-8">
    <form class="section-card" method="post" action="${ctx}/case-files/second-instance" novalidate>
      <input type="hidden" name="number" value="<c:out value='${caseFile.number}' />">
      <div class="section-card-header">
        <h2 class="section-card-title">Sala que recibe el expediente</h2>
      </div>
      <div class="section-card-body">
        <p class="section-card-text">
          Es el mismo asunto: no se registra una demanda nueva ni se revisa duplicidad. El expediente se entrega al
          sistema de tocas con la sala elegida.
        </p>
        <c:choose>
          <c:when test="${options.chambersUnavailable}">
            <div class="alert alert-mor-warning d-flex gap-2 align-items-start mb-0" role="alert">
              <i class="bi bi-exclamation-triangle mt-1"></i>
              <span>El sistema de tocas no respondió. Puede registrar el pase ahora y la sala quedará pendiente.</span>
            </div>
          </c:when>
          <c:otherwise>
            <fieldset>
              <legend class="form-label">Salas, de la más cercana a la más lejana *</legend>
              <c:forEach var="chamber" items="${options.chambers}" varStatus="loop">
                <div class="form-check mb-2">
                  <input class="form-check-input" type="radio" name="chamber" id="chamber${loop.index}"
                         value="<c:out value='${chamber.code}' />" required ${loop.first ? 'checked' : ''}>
                  <label class="form-check-label" for="chamber${loop.index}">
                    <c:out value="${chamber.name}" /> <span class="text-muted">· <c:out value="${chamber.seat}" /></span>
                  </label>
                </div>
              </c:forEach>
            </fieldset>
          </c:otherwise>
        </c:choose>
      </div>
      <div class="section-card-footer">
        <a class="btn btn-secondary" href="<c:out value='${detailUrl}' />">Cancelar</a>
        <button type="submit" class="btn btn-primary">Registrar pase y generar acuse</button>
      </div>
    </form>
  </div>
</div>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
