<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="es"><head><meta charset="UTF-8"><title>Error · SSCT</title>
<link rel="stylesheet" href="${ctx}/webjars/bootstrap/5.3.3/css/bootstrap.min.css">
<link rel="stylesheet" href="${ctx}/assets/css/app.css"></head>
<body class="app-body"><div class="container-xl app-shell"><div class="panel mt-5 p-5 text-center">
<h1 class="page-title">No se encontró la página</h1>
<p class="page-sub">El recurso solicitado no existe o no tiene acceso a él.</p>
<a class="btn btn-dark-g" href="${ctx}/home">Ir al inicio</a></div></div></body></html>
