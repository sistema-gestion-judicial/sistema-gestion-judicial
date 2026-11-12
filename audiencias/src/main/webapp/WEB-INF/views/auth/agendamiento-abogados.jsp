<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>SGA Audiencias Agendamiento</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/abogados.css">
</head>
<body>
<div class="page">
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
      <button class="nav-link" aria-current="page">Agendamiento</button>
      <a href="inicioAudienciasAbogados.jsp" class="nav-link" style="text-decoration:none;">Audiencias</a>
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
    <section aria-labelledby="page-title">
      <div class="heading-rule"></div>
      <h1 id="page-title">Agendamiento</h1>
      <p class="intro">Gestiona las audiencias por agendar y reagendar de manera eficiente</p>

      <form class="filters" id="hearingFilters">
        <input class="search-field" id="caseSearch" type="search" placeholder="Buscar por expediente" aria-label="Buscar por expediente">
        <select class="subject-filter" id="subjectFilter" aria-label="Materia">
          <option value="">Materia</option>
          <option value="Penal">Penal</option>
          <option value="Civil">Civil</option>
          <option value="Familiar">Familiar</option>
        </select>
        <button class="apply-filters" type="submit">
          <svg viewBox="0 0 20 20" fill="none" aria-hidden="true">
            <path d="M2.5 5.8h15M4.8 10h10.4M7.5 14.2h5" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round"/>
          </svg>
          Aplicar filtros
        </button>
      </form>
    </section>

    <section class="agenda" aria-labelledby="agenda-title">
      <div class="agenda-heading">
        <div>
          <h2 id="agenda-title">Audiencias</h2>
          <div class="agenda-subtitle">Consulta tus audiencias asignadas</div>
        </div>
        <div class="record-count" id="recordCount" aria-live="polite">0 registros</div>
      </div>

      <div class="tabs" role="tablist" aria-label="Estado de audiencias">
        <button class="tab-button" id="scheduledTab" type="button" role="tab" aria-selected="true" data-status="Agendada">Agendadas</button>
        <button class="tab-button" id="toScheduleTab" type="button" role="tab" aria-selected="false" data-status="Por agendar">Por agendar</button>
        <button class="tab-button" id="rescheduleTab" type="button" role="tab" aria-selected="false" data-status="Por reagendar">Por reagendar</button>
      </div>

      <div class="table-scroll">
        <table>
          <thead>
          <tr>
            <th scope="col">Expediente</th>
            <th scope="col">Mesa</th>
            <th scope="col">Fecha</th>
            <th scope="col">Hora</th>
            <th scope="col" style="text-align:right;">Acciones</th>
          </tr>
          </thead>
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

<dialog id="hearingDialog" aria-labelledby="dialogTitle">
  <div class="dialog-content">
    <h2 id="dialogTitle">Detalle de audiencia</h2>
    <div class="detail-grid">
      <div>
        <span class="detail-label">Expediente</span>
        <span class="detail-value" id="detailCase"></span>
      </div>
      <div>
        <span class="detail-label">Mesa</span>
        <span class="detail-value" id="detailDesk"></span>
      </div>
      <div>
        <label for="detailDateInput" class="detail-label">Fecha</label>
        <input type="date" id="detailDateInput" class="search-field" style="width: 100%; min-height: 38px; padding: 0 8px;">
      </div>
      <div>
        <label for="detailTimeInput" class="detail-label">Hora</label>
        <input type="time" id="detailTimeInput" class="search-field" style="width: 100%; min-height: 38px; padding: 0 8px;">
      </div>
    </div>
    <div class="dialog-actions" style="gap: 12px; justify-content: flex-end;">
      <button class="close-button" id="cancelDialog" type="button" style="background: #879283;">Cancelar</button>
      <button class="close-button" id="saveDialog" type="button">Aceptar</button>
    </div>
  </div>
</dialog>

<script src="${pageContext.request.contextPath}/assets/js/agendamiento.js?v=1.1"></script>
</body>
</html>
