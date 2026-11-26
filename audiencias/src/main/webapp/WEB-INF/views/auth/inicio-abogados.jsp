<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html lang="es">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="theme-color" content="#e9e9e2">
  <title>SGA Audiencias | Resumen general</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/abogados.css?v=<%= System.currentTimeMillis() %>">
</head>
<body>
<div class="page layout-figma">

  <!-- Barra Lateral Institucional -->
  <aside class="sidebar" aria-label="Menú lateral">
    <div>
      <a class="sidebar-brand" href="#" aria-label="SGA Audiencias, inicio">
        <svg width="46" height="46" viewBox="0 0 48 48" fill="none" aria-hidden="true">
          <path d="M24 7v33M15 40h18M11 15h26" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          <path d="M11 15 6 26h10l-5-11ZM37 15l-5 11h10l-5-11Z" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/>
          <path d="M6 26c0 2.8 2.2 4.5 5 4.5s5-1.7 5-4.5M32 26c0 2.8 2.2 4.5 5 4.5s5-1.7 5-4.5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
        </svg>
        <span class="sidebar-brand-title">Audiencias</span>
      </a>

      <nav class="sidebar-nav" aria-label="Navegación principal">
        <button type="button" class="sidebar-link active-link" aria-current="page">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M3 10.5 12 3l9 7.5V20a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1v-9.5Z" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round"/>
          </svg>
          <span>Inicio</span>
        </button>

        <a href="agendamientoAbogados.jsp" class="sidebar-link">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <rect x="3" y="4" width="18" height="17" rx="2.5" stroke="currentColor" stroke-width="1.7"/>
            <path d="M8 2.5v3M16 2.5v3M3 9.5h18M8 14h3v3H8v-3Z" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>Agendamiento</span>
        </a>

        <a href="inicioAudienciasAbogados.jsp" class="sidebar-link">
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
      <a href="${pageContext.request.contextPath}views/auth/login.jsp" class="sidebar-logout">
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
          <span class="breadcrumb-current">Inicio</span>
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
          <button class="profile" id="profileButton"
                  aria-expanded="false" aria-haspopup="true" aria-controls="profileMenu"
                  type="button">
            <span class="avatar" aria-hidden="true">RS</span>
            <span class="profile-text">
              <span class="profile-name">Ricardo Salgado</span>
              <span class="profile-role">Abogado de audiencias</span>
            </span>
          </button>
          <span class="profile-menu" id="profileMenu" role="menu">
            <button type="button" role="menuitem">Mi perfil</button>
            <a href="${pageContext.request.contextPath}views/auth/login.jsp" class="profile-menu-link">
              <button type="button" role="menuitem" class="profile-menu-btn">Cerrar sesión</button>
            </a>
          </span>
        </div>
      </div>
    </header>

    <main>
      <section aria-labelledby="welcome-title" class="hero-banner-justice">
        <div class="heading-rule"></div>
        <h1 id="welcome-title">Buenos dias, Ricardo</h1>
        <p class="intro">Este es el resumen de sus audiencias asignadas</p>
        <div class="stats" aria-label="Resumen de audiencias">
          <article class="stat"><strong
            class="stat-value">36</strong><span class="stat-label">Audiencias hoy</span></article>
          <article class="stat"><strong
            class="stat-value">5</strong><span class="stat-label">Audiencias por reagendar</span></article>
          <article class="stat"><strong
            class="stat-value">1</strong><span class="stat-label">Audiencias por agendar</span></article>
          <article class="stat"><strong
            class="stat-value">2</strong><span class="stat-label">Peritajes abiertos</span></article>
        </div>
      </section>

      <section class="agenda" aria-labelledby="agenda-title">
        <div class="agenda-heading">
          <div>
            <h2 id="agenda-title">Agenda de hoy</h2>
            <div class="agenda-subtitle">Audiencias asignadas para hoy</div>
          </div>
          <div class="record-count" id="recordCount">36 registros</div>
        </div>
        <div class="table-scroll">
          <table>
            <thead>
            <tr><th scope="col">Expediente</th><th
              scope="col">Mesa</th><th
              scope="col">Fecha</th><th
              scope="col">Hora</th><th
              scope="col">Acciones</th></tr>
            </thead>
            <tbody id="hearingRows"></tbody>
          </table>
        </div>
        <footer class="table-footer">
          <span id="paginationSummary" aria-live="polite">Mostrando 1-4 de 36</span>
          <nav class="pagination" aria-label="Paginación de audiencias"
               id="pagination"></nav>
        </footer>
      </section>
    </main>
  </div>
</div>

<dialog id="hearingDialog" aria-labelledby="dialogTitle">
  <div class="dialog-content">
    <h2 id="dialogTitle">Detalle de audiencia</h2>
    <div class="detail-grid">
      <div><span class="detail-label">Expediente</span><span
        class="detail-value" id="detailCase"></span></div>
      <div><span class="detail-label">Mesa</span><span
        class="detail-value" id="detailDesk"></span></div>
      <div><span class="detail-label">Fecha</span><span
        class="detail-value" id="detailDate"></span></div>
      <div><span class="detail-label">Hora</span><span
        class="detail-value" id="detailTime"></span></div>
    </div>
    <div class="dialog-actions"><button class="close-button"
                                        id="closeDialog" type="button">Cerrar</button></div>
  </div>
</dialog>

<script src="${pageContext.request.contextPath}/assets/js/inicio-abogados.js"></script>
</body>
</html>
