<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Inicio" />
<c:set var="activeNav" value="home" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>

<header class="app-topbar">
  <div>
    <h1 class="page-title">Inicio</h1>
    <p class="page-subtitle">Administración del órgano judicial.</p>
  </div>
  <%@ include file="/WEB-INF/views/layout/user-chip.jspf" %>
</header>

<div class="card-mor p-5 text-center">
  <span class="empty-icon mb-3"><i class="bi bi-bank"></i></span>
  <p class="mb-0">Las opciones de este perfil estarán disponibles en un siguiente avance.</p>
</div>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
