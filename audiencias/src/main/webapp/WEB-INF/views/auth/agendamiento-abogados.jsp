<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>SGA Audiencias | Agendamiento</title>

  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/abogados.css?v=2.1">
</head>
<body>
<div class="page layout-figma">

  <!-- Barra Lateral Institucional -->
  <aside class="sidebar" aria-label="Menú lateral">
    <div>
      <a class="sidebar-brand" href="inicioabogados.jsp" aria-label="SGA Audiencias, inicio">
        <svg width="46" height="46" viewBox="0 0 48 48" fill="none" aria-hidden="true">
          <path d="M24 7v33M15 40h18M11 15h26" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          <path d="M11 15 6 26h10l-5-11ZM37 15l-5 11h10l-5-11Z" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/>
          <path d="M6 26c0 2.8 2.2 4.5 5 4.5s5-1.7 5-4.5M32 26c0 2.8 2.2 4.5 5 4.5s5-1.7 5-4.5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
        </svg>
        <span class="sidebar-brand-title">Audiencias</span>
      </a>

      <nav class="sidebar-nav" aria-label="Navegación principal">
        <a href="inicioabogados.jsp" class="sidebar-link">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M3 10.5 12 3l9 7.5V20a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1v-9.5Z" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round"/>
          </svg>
          <span>Inicio</span>
        </a>

        <button type="button" class="sidebar-link active-link" aria-current="page">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <rect x="3" y="4" width="18" height="17" rx="2.5" stroke="currentColor" stroke-width="1.7"/>
            <path d="M8 2.5v3M16 2.5v3M3 9.5h18M8 14h3v3H8v-3Z" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>Agendamiento</span>
        </button>

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
      <a href="login.jsp" class="sidebar-logout">
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
          <a href="inicioabogados.jsp">Inicio</a>
          <span aria-hidden="true">&rsaquo;</span>
          <span class="breadcrumb-current">Agendamiento</span>
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
            <a href="login.jsp" role="menuitem" class="profile-menu-link">
              <button type="button" class="profile-menu-btn">Cerrar sesión</button>
            </a>
          </span>
        </div>
      </div>
    </header>

    <main>
      <!-- Encabezado y Filtros -->
      <section aria-labelledby="page-title" class="hero-banner-justice">
        <div class="heading-rule"></div>
        <h1 id="page-title" class="font-serif fw-bold text-dark mb-1">Agendamiento</h1>
        <p class="intro">Gestiona las audiencias por agendar/reagendar y las ya agendadas</p>

        <form class="filters" id="hearingFilters">
          <div class="search-wrap">
            <svg class="search-icon" viewBox="0 0 20 20" fill="none" width="16" height="16">
              <circle cx="9" cy="9" r="6" stroke="currentColor" stroke-width="1.8"/>
              <path d="m13.5 13.5 4 4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
            </svg>
            <input class="search-field" id="caseSearch" type="search" placeholder="Buscar por expediente" aria-label="Buscar por expediente">
          </div>

          <select class="subject-filter" id="subjectFilter" aria-label="Materia">
            <option value="">Materia</option>
            <option value="Penal">Penal</option>
            <option value="Civil">Civil</option>
            <option value="Familiar">Familiar</option>
            <option value="Mercantil">Mercantil</option>
          </select>

          <button class="apply-filters" type="submit">
            <svg viewBox="0 0 20 20" fill="none" aria-hidden="true" width="16" height="16">
              <path d="M2.5 5.8h15M4.8 10h10.4M7.5 14.2h5" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round"/>
            </svg>
            Aplicar filtros
          </button>
        </form>

        <!-- Pestañas -->
        <div class="tabs-navigation mt-4">
          <button class="tab-button tab-btn active" id="scheduledTab" type="button" data-status="Agendada">Agendado</button>
          <button class="tab-button tab-btn" id="toScheduleTab" type="button" data-status="Por agendar">Por agendar</button>
          <button class="tab-button tab-btn" id="rescheduleTab" type="button" data-status="Por reagendar">Por reagendar</button>
        </div>
      </section>

      <!-- Tarjeta Principal -->
      <section class="agenda" aria-labelledby="agenda-title">
        <div class="agenda-heading">
          <div>
            <h2 id="agenda-title" class="font-serif fw-bold text-dark">Audiencias</h2>
            <div class="agenda-subtitle" id="agendaSubtitle">Consulta las denuncias por agendar</div>
          </div>
          <div class="record-count text-muted text-sm" id="recordCount" aria-live="polite">0 registros</div>
        </div>

        <div class="table-scroll">
          <table class="abogados-table">
            <thead id="hearingThead"></thead>
            <tbody id="hearingRows"></tbody>
          </table>
        </div>

        <footer class="table-footer">
          <span id="paginationSummary" aria-live="polite"></span>
          <nav class="pagination" aria-label="Paginación de audiencias" id="pagination"></nav>
        </footer>
      </section>
    </main>
  </div>
</div>

<!-- Modal emergente para desplegar Calendario/Hora -->
<dialog id="hearingDialog" class="custom-modal" aria-labelledby="dialogTitle">
  <div class="modal-body-custom">
    <h2 id="dialogTitle" class="font-serif modal-title">Agendar audiencia</h2>
    <p class="modal-subtitle">Selecciona un día y una hora para el agendamiento de la audiencia</p>
    <hr class="modal-divider">

    <div class="modal-form-grid">
      <div class="form-group">
        <label class="detail-label">Número de expediente</label>
        <span id="detailCase" class="form-control detail-readonly"></span>
      </div>

      <div class="form-group">
        <label class="detail-label">Mesa asignada</label>
        <span id="detailDesk" class="form-control detail-readonly"></span>
      </div>

      <div class="form-group">
        <label for="detailDateInput" class="detail-label">Fecha</label>
        <input type="date" id="detailDateInput" class="form-control" required>
      </div>

      <div class="form-group">
        <label for="detailTimeInput" class="detail-label">Hora</label>
        <input type="time" id="detailTimeInput" class="form-control" required>
      </div>

      <div class="info-banner full-width">
        <svg viewBox="0 0 20 20" fill="none" width="18" height="18" class="info-icon">
          <circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="1.6"/>
          <path d="M10 9v5M10 6.5h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <span>La fecha y hora quedarán reservadas inmediatamente en el sistema.</span>
      </div>

      <div class="modal-actions full-width">
        <button type="button" class="btn-link-reset text-decoration-none text-muted" id="cancelDialog">Cancelar</button>
        <button type="button" class="btn-auth-primary px-4 py-2" id="saveDialog">Agendar audiencia</button>
      </div>
    </div>
  </div>
</dialog>

<script src="${pageContext.request.contextPath}/assets/js/agendamiento.js?v=1.2"></script>
</body>
</html>
