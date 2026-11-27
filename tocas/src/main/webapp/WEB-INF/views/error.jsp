<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Error · SSCT</title>
<link href="https://fonts.googleapis.com/css2?family=Merriweather:wght@400;700&family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${ctx}/webjars/bootstrap/5.3.3/css/bootstrap.min.css">
<link rel="stylesheet" href="${ctx}/assets/css/styles.css"></head>
<body class="app-body"><div class="hero" aria-hidden="true"></div>
<div class="app-layout">
  <jsp:include page="/WEB-INF/views/includes/sidebar.jsp"><jsp:param name="active" value=""/></jsp:include>
  <div class="app-main">
    <jsp:include page="/WEB-INF/views/includes/topbar.jsp"><jsp:param name="title" value="Error"/></jsp:include>
    <div class="panel mt-5 p-5 text-center">
      <h1 class="page-title">No se encontró la página</h1>
      <p class="page-sub mb-3">El recurso solicitado no existe o no tiene acceso a él.</p>
      <a class="btn btn-dark-g" href="${ctx}/home">Ir al inicio</a></div>
  </div>
</div>
<script src="${ctx}/webjars/bootstrap/5.3.3/js/bootstrap.bundle.min.js"></script></body></html>
