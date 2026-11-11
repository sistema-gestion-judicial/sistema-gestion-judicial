<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>SGA Audiencias | Agendamiento</title>

  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/abogados.css">
</head>
<body>
<div class="page">

  <!-- Topbar Institucional -->
  <header class="topbar">
    <a class="brand" href="inicioabogados.jsp" aria-label="SGA Audiencias, inicio">
      <span class="logo">Logo</span>
      <span>
        <span class="brand-name">SGA</span>
        <span class="brand-subtitle" style="display:block">Audiencias</span>
      </span>
    </a>

    <nav class="main-nav" aria-label="Navegación principal">
      <a href="inicioabogados.jsp" class="nav-link" style="text-decoration:none;">Inicio</a>
      <button class="nav-link active-link" aria-current="page">Agendamiento</button>
      <button class="nav-link">Audiencias</button>
      <button class="nav-link">Notificaciones</button>
    </nav>

    <div class="profile-wrap">
      <button class="profile" id="profileButton" aria-expanded="false" aria-haspopup="true" aria-controls="profileMenu" type="button">
        <span class="avatar" aria-hidden="true">
          <svg width="35" height="35" viewBox="0 0 36 36" fill="none">
            <circle cx="18" cy="11" r="7" stroke="currentColor" stroke-width="2"/>
            <path d="M5.5 29c1.8-5.3 6.2-9 12.5-9s10.7 3.7 12.5 9" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </span>
        <span>
          <span class="profile-name">Ricardo Salgado</span>
          <span class="profile-role" style="display:block">Abogado de audiencias</span>
        </span>
        <svg class="profile-chevron" viewBox="0 0 20 20" fill="none" aria-hidden="true">
          <path d="m4 7 6 6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>

      <span class="profile-menu" id="profileMenu" role="menu">
        <button type="button" role="menuitem">Mi perfil</button>
        <a href="login.jsp" role="menuitem" style="text-decoration:none; display:block;">
          <button type="button" style="width:100%; border:0; background:transparent; text-align:left; cursor:pointer;">Cerrar sesión</button>
        </a>
      </span>
    </div>
  </header>

  <main>
    <!-- Header Banner con Fondo Institucional -->
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

      <!-- Pestañas fuera de la tarjeta principal -->
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

<!-- Modal emergente para desplegar Calendario/Hora -->
<dialog id="hearingDialog" class="custom-modal" aria-labelledby="dialogTitle">
  <div class="modal-body-custom">
    <h2 id="dialogTitle" class="font-serif modal-title">Agendar audiencia</h2>
    <p class="modal-subtitle">Selecciona un día y una hora para el agendamiento de la audiencia</p>
    <hr class="modal-divider">

    <div class="modal-form-grid">
      <div class="form-group">
        <label class="detail-label">Número de expediente</label>
        <span id="detailCase" class="form-control" style="background-color: #f3f4f6; display: flex; align-items: center; font-weight: 650;"></span>
      </div>

      <div class="form-group">
        <label class="detail-label">Mesa asignada</label>
        <span id="detailDesk" class="form-control" style="background-color: #f3f4f6; display: flex; align-items: center;"></span>
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
        <svg viewBox="0 0 20 20" fill="none" width="18" height="18" style="flex-shrink:0;">
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
