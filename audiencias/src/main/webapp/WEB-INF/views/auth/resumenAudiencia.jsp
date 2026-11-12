<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="theme-color" content="#e9e9e2">
  <title>SGA Audiencias | Resumen del expediente</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/abogados.css?v=<%= System.currentTimeMillis() %>">
</head>
<body>
<div class="page">

  <header class="topbar">
    <a class="brand" href="${pageContext.request.contextPath}/inicioabogados.jsp" aria-label="SGA Audiencias, inicio">
      <span class="logo">Logo</span>
      <span>
        <span class="brand-name">SGA</span>
        <span class="brand-subtitle" style="display:block">Audiencias</span>
      </span>
    </a>
    <nav class="main-nav" aria-label="Navegación principal">
      <a class="nav-link" href="${pageContext.request.contextPath}/inicioabogados.jsp">Inicio</a>
      <a class="nav-link" href="${pageContext.request.contextPath}/agendamientoAbogados.jsp">Agendamiento</a>
      <a class="nav-link" href="${pageContext.request.contextPath}/inicioAudienciasAbogados.jsp" aria-current="page">Audiencias</a>
      <button class="nav-link" type="button">Notificaciones</button>
    </nav>
    <div class="profile-wrap">
      <button class="profile" id="profileButton" aria-expanded="false" aria-haspopup="true" aria-controls="profileMenu" type="button">
        <span class="avatar" aria-hidden="true">
          <svg viewBox="0 0 36 36" fill="none"><circle cx="18" cy="11" r="7" stroke="currentColor" stroke-width="2"/><path d="M5.5 32c.9-7.3 5.2-11 12.5-11s11.6 3.7 12.5 11" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
        </span>
        <span>
          <span class="profile-name">Ricardo Salgado</span>
          <span class="profile-role" style="display:block">Abogado de audiencias</span>
        </span>
        <svg class="profile-chevron" viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="M5 7.5L10 12.5L15 7.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </button>
      <span class="profile-menu" id="profileMenu" role="menu">
        <button type="button" role="menuitem">Mi perfil</button>
        <button type="button" role="menuitem">Cerrar sesión</button>
      </span>
    </div>
  </header>

  <main>
    <section class="case-heading" aria-labelledby="case-title">
      <div class="heading-title">
        <div class="heading-rule"></div>
        <h1 id="case-title">PEN-2025-0891</h1>
      </div>
      <div class="heading-actions">
        <button class="back-link" id="backToHearings" type="button">
          <svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="M12.5 4 6.5 10l6 6M7 10h10" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/></svg>
          Volver a Audiencias
        </button>
      </div>
    </section>

    <nav class="case-tabs" role="tablist" aria-label="Secciones del expediente">
      <button class="case-tab" id="tab-resumen" type="button" role="tab" aria-selected="true" aria-controls="panel-resumen">Resumen</button>
      <button class="case-tab" id="tab-verbatim" type="button" role="tab" aria-selected="false" aria-controls="panel-verbatim">Verbatim</button>
      <button class="case-tab" id="tab-acta" type="button" role="tab" aria-selected="false" aria-controls="panel-acta">Acta</button>
      <button class="case-tab" id="tab-expediente" type="button" role="tab" aria-selected="false" aria-controls="panel-expediente">Expediente completo</button>
      <button class="case-tab" id="tab-peritaje" type="button" role="tab" aria-selected="false" aria-controls="panel-peritaje">Peritaje</button>
      <button class="case-tab" id="tab-bitacora" type="button" role="tab" aria-selected="false" aria-controls="panel-bitacora">Bitácora</button>
      <button class="case-tab" id="tab-resolucion" type="button" role="tab" aria-selected="false" aria-controls="panel-resolucion">Resolución</button>
    </nav>

    <jsp:include page="tabs/tab-resumen.jsp" />
    <jsp:include page="tabs/tab-verbatim.jsp" />
    <jsp:include page="tabs/tab-acta.jsp" />
    <jsp:include page="tabs/tab-expediente.jsp" />
    <jsp:include page="tabs/tab-peritaje.jsp" />
    <jsp:include page="tabs/tab-bitacora.jsp" />
    <jsp:include page="tabs/tab-resolucion.jsp" />

  </main>
</div>

<dialog id="actionDialog" aria-labelledby="dialogTitle" aria-describedby="dialogMessage">
  <div class="modal-header"><h2 id="dialogTitle"></h2></div>
  <div class="modal-body"><p id="dialogMessage"></p></div>
  <div id="dialogActions" style="padding: 0 24px 24px; display: flex; justify-content: flex-end; gap: 16px;"></div>
</dialog>

<script>window.APP_CONTEXT = "${pageContext.request.contextPath}";</script>
<script src="${pageContext.request.contextPath}/assets/js/resumenAudiencia.js?v=<%= System.currentTimeMillis() %>"></script>
</body>
</html>
