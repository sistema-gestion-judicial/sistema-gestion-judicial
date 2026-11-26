<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="theme-color" content="#e9e9e2">
  <title>SGA Audiencias | Resumen del expediente</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/abogados.css?v=<%= System.currentTimeMillis() %>">
</head>
<body>
<div class="page layout-figma">

  <!-- Barra Lateral Institucional -->
  <aside class="sidebar" aria-label="Menú lateral">
    <div>
      <a class="sidebar-brand" href="${pageContext.request.contextPath}/inicioabogados.jsp" aria-label="SGA Audiencias, inicio">
        <svg width="46" height="46" viewBox="0 0 48 48" fill="none" aria-hidden="true">
          <path d="M24 7v33M15 40h18M11 15h26" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          <path d="M11 15 6 26h10l-5-11ZM37 15l-5 11h10l-5-11Z" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/>
          <path d="M6 26c0 2.8 2.2 4.5 5 4.5s5-1.7 5-4.5M32 26c0 2.8 2.2 4.5 5 4.5s5-1.7 5-4.5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
        </svg>
        <span class="sidebar-brand-title">Audiencias</span>
      </a>

      <nav class="sidebar-nav" aria-label="Navegación principal">
        <a href="${pageContext.request.contextPath}/inicioabogados.jsp" class="sidebar-link">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M3 10.5 12 3l9 7.5V20a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1v-9.5Z" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round"/>
          </svg>
          <span>Inicio</span>
        </a>

        <a href="${pageContext.request.contextPath}/agendamientoAbogados.jsp" class="sidebar-link">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <rect x="3" y="4" width="18" height="17" rx="2.5" stroke="currentColor" stroke-width="1.7"/>
            <path d="M8 2.5v3M16 2.5v3M3 9.5h18M8 14h3v3H8v-3Z" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>Agendamiento</span>
        </a>

        <a href="${pageContext.request.contextPath}/inicioAudienciasAbogados.jsp" class="sidebar-link active-link" aria-current="page">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <rect x="3" y="4" width="18" height="17" rx="2.5" stroke="currentColor" stroke-width="1.7"/>
            <path d="M8 2.5v3M16 2.5v3M3 9.5h18M8 14h8M8 17.5h5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
          </svg>
          <span>Audiencias</span>
        </a>

        <a href="${pageContext.request.contextPath}/notificacionesAbogados.jsp" class="sidebar-link">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M18 8a6 6 0 1 0-12 0c0 7-3 9-3 9h18s-3-2-3-9ZM13.73 21a2 2 0 0 1-3.46 0" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>Notificaciones</span>
        </a>
      </nav>
    </div>

    <div class="sidebar-footer">
      <a href="${pageContext.request.contextPath}/login.jsp" class="sidebar-logout">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span>Cerrar sesión</span>
      </a>
    </div>
  </aside>

  <!-- Columna Derecha: Topbar + Contenido -->
  <div class="content-shell">

    <!-- Topbar Flotante (sin flecha en perfil) -->
    <header class="topbar">
      <div class="topbar-left">
        <div class="topbar-brand-compact">
          <span class="brand-name">SGA</span>
          <span class="brand-subtitle">Audiencias</span>
        </div>

        <span class="topbar-divider" aria-hidden="true"></span>

        <nav class="breadcrumb-nav" aria-label="Ruta de navegación">
          <a href="${pageContext.request.contextPath}/inicioabogados.jsp">Inicio</a>
          <span aria-hidden="true">&rsaquo;</span>
          <a href="${pageContext.request.contextPath}/inicioAudienciasAbogados.jsp">Audiencias</a>
          <span aria-hidden="true">&rsaquo;</span>
          <span class="breadcrumb-current">PEN-2025-0891</span>
        </nav>
      </div>

      <div class="topbar-right">
        <a href="${pageContext.request.contextPath}/notificacionesAbogados.jsp" class="notif-pill">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M18 8a6 6 0 1 0-12 0c0 7-3 9-3 9h18s-3-2-3-9ZM13.73 21a2 2 0 0 1-3.46 0" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>Notificaciones</span>
        </a>

        <div class="profile-wrap">
          <button class="profile" id="profileButton" aria-expanded="false" aria-haspopup="true" aria-controls="profileMenu" type="button">
            <span class="avatar" aria-hidden="true">RS</span>
            <span class="profile-text">
              <span class="profile-name">Ricardo Salgado</span>
              <span class="profile-role">Abogado de audiencias</span>
            </span>
          </button>
          <span class="profile-menu" id="profileMenu" role="menu">
            <button type="button" role="menuitem">Mi perfil</button>
            <button type="button" role="menuitem">Cerrar sesión</button>
          </span>
        </div>
      </div>
    </header>

    <main>
      <section class="case-heading hero-banner-justice" aria-labelledby="case-title">
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
