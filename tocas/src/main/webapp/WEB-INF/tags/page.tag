<%@ tag language="java" pageEncoding="UTF-8" body-content="scriptless" description="Main page layout: hero, top bar, title, content" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="subtitle" required="false" %>
<%@ attribute name="active" required="false" %>
<%@ attribute name="crumb" required="false" %>
<%@ attribute name="titleExtra" fragment="true" required="false" %>
<%@ attribute name="actions" fragment="true" required="false" %>
<%@ attribute name="tabs" fragment="true" required="false" %>
<%@ attribute name="scripts" fragment="true" required="false" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title><c:out value="${title}"/> · SSCT</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Merriweather:wght@400;700&family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="${ctx}/webjars/bootstrap/5.3.3/css/bootstrap.min.css">
  <link rel="stylesheet" href="${ctx}/assets/css/styles.css">
</head>
<body class="app-body">
<div class="hero" aria-hidden="true"></div>
<div class="app-layout">
  <jsp:include page="/WEB-INF/views/includes/sidebar.jsp"><jsp:param name="active" value="${active}"/></jsp:include>
  <div class="app-main">
  <jsp:include page="/WEB-INF/views/includes/topbar.jsp"><jsp:param name="title" value="${title}"/><jsp:param name="crumb" value="${crumb}"/></jsp:include>

  <section class="page-head">
    <div class="page-head-row">
      <div class="page-head-text">
        <span class="accent-line"></span>
        <h1 class="page-title"><c:out value="${title}"/> <c:if test="${not empty titleExtra}"><jsp:invoke fragment="titleExtra"/></c:if></h1>
        <c:if test="${not empty subtitle}"><p class="page-sub"><c:out value="${subtitle}"/></p></c:if>
      </div>
      <c:if test="${not empty actions}"><div class="page-actions"><jsp:invoke fragment="actions"/></div></c:if>
    </div>
    <c:if test="${not empty tabs}"><jsp:invoke fragment="tabs"/></c:if>
  </section>

  <main class="page-main">
    <c:if test="${not empty sessionScope.flashError}">
      <div class="alert-g alert-error" role="alert"><t:icon name="alert" size="18"/> <span><c:out value="${sessionScope.flashError}"/></span></div>
      <c:remove var="flashError" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.flashSuccess}">
      <div class="alert-g alert-success" role="status"><t:icon name="check-filled" size="18"/> <span><c:out value="${sessionScope.flashSuccess}"/></span></div>
      <c:remove var="flashSuccess" scope="session"/>
    </c:if>
    <jsp:doBody/>
  </main>
  </div>
</div>
<jsp:include page="/WEB-INF/views/includes/confirm-modal.jsp"/>
<script src="${ctx}/webjars/bootstrap/5.3.3/js/bootstrap.bundle.min.js"></script>
<script src="${ctx}/assets/js/app.js"></script>
<c:if test="${not empty scripts}"><jsp:invoke fragment="scripts"/></c:if>
</body>
</html>
