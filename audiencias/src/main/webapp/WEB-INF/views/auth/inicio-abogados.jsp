<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html lang="es">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="theme-color" content="#e9e9e2">
  <title>SGA Audiencias | Resumen general</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/abogados.css"></head>
<body>
<div class="page">
  <header class="topbar">
    <a class="brand" href="#" aria-label="SGA Audiencias, inicio">
      <span class="logo">Logo</span>
      <span>
<span class="brand-name">SGA</span>
<span class="brand-subtitle" style="display:block">Audiencias</span>
</span>
    </a>
    <nav class="main-nav" aria-label="Navegación principal">
      <button class="nav-link" aria-current="page">Inicio</button>
      <a href="agendamientoAbogados.jsp" class="nav-link" style="text-decoration:none;">Agendamiento</a>
      <button class="nav-link">Audiencias</button>
      <button class="nav-link">Notificaciones</button>
    </nav>
    <div class="profile-wrap">
      <button class="profile" id="profileButton"
              aria-expanded="false" aria-haspopup="true" aria-controls="profileMenu"
              type="button">
<span class="avatar" aria-hidden="true">
  <svg width="35" height="35" viewBox="0 0 36 36" fill="none">
    <circle cx="18" cy="11" r="7" stroke="currentColor" stroke-width="2"/>
    <path d="M5.5 32c.9-7.3 5.2-11 12.5-11s11.6 3.7 12.5 11"
      stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
  </svg>
</span>
        <span>
<span class="profile-name">Ricardo Salgado</span>
<span class="profile-role" style="display:block">Abogado de audiencias</span>
</span>
        <svg class="profile-chevron" viewBox="0 0 20 20" fill="none"
             aria-hidden="true"><path d="m4 7 6 6 6-6" stroke="currentColor"
                                      stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </button>
        <span class="profile-menu" id="profileMenu" role="menu">
          <button type="button" role="menuitem">Mi perfil</button>
<a href="${pageContext.request.contextPath}views/auth/login.jsp" style="text-decoration: none;">
  <button type="button" role="menuitem">Cerrar sesión</button>
</a>
        </span>
    </div>
  </header>
  <main>
    <section aria-labelledby="welcome-title">
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
