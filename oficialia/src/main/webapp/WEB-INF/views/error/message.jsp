<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${errorTitle}" />
<c:set var="activeNav" value="requests" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>

<header class="app-topbar">
  <div>
    <a class="back-link" href="${ctx}/home"><i class="bi bi-arrow-left"></i> Volver al inicio</a>
  </div>
  <%@ include file="/WEB-INF/views/layout/user-chip.jspf" %>
</header>

<div class="card-mor p-5 text-center mx-auto" style="max-width: 640px;">
  <span class="empty-icon mb-3"><i class="bi bi-shield-exclamation"></i></span>
  <h1 class="page-title fs-3"><c:out value="${errorTitle}" /></h1>
  <p class="mb-4"><c:out value="${errorMessage}" /></p>
  <a class="btn btn-primary" href="${ctx}/home">Ir al inicio</a>
</div>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
