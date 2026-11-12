<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="theme-color" content="#e9e9e2">
  <title>SGA Audiencias | Control de audiencias</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/abogados.css?v=<%= System.currentTimeMillis() %>">
</head>
<body>
<div class="page">

  <header class="topbar">
    <a class="brand" href="${pageContext.request.contextPath}/inicio-abogados" aria-label="SGA Audiencias, inicio">
      <span class="logo">Logo</span>
      <span>
        <span class="brand-name">SGA</span>
        <span class="brand-subtitle" style="display:block">Audiencias</span>
      </span>
    </a>
    <nav class="main-nav" aria-label="Navegación principal">
      <a class="nav-link" href="${pageContext.request.contextPath}/inicio-abogados">Inicio</a>
      <a class="nav-link" href="${pageContext.request.contextPath}/agendamiento-abogados">Agendamiento</a>
      <a class="nav-link" href="#" aria-current="page">Audiencias</a>
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
    <section aria-labelledby="page-title">
      <div class="heading-rule"></div>
      <h1 id="page-title">Audiencias</h1>
      <p class="intro">Consulta el listado de audiencias asignadas y accede al resumen de cada expediente</p>

      <div class="stats" aria-label="Resumen de audiencias">
        <article class="stat">
          <span class="stat-value" id="statTotal">0</span>
          <span class="stat-label">Total asignadas</span>
        </article>
        <article class="stat">
          <span class="stat-value" id="statProximas">0</span>
          <span class="stat-label">Próximas</span>
        </article>
        <article class="stat">
          <span class="stat-value" id="statEnCurso">0</span>
          <span class="stat-label">En curso</span>
        </article>
        <article class="stat">
          <span class="stat-value" id="statConcluidas">0</span>
          <span class="stat-label">Concluidas</span>
        </article>
      </div>

      <form class="filters" id="hearingFilters">
        <input class="search-field" id="caseSearch" type="search" placeholder="Buscar por expediente o juzgado" aria-label="Buscar por expediente o juzgado">
        <select class="subject-filter" id="subjectFilter" aria-label="Materia">
          <option value="">Todas las materias</option>
          <option value="Familiar">Familiar</option>
          <option value="Penal">Penal</option>
          <option value="Civil">Civil</option>
        </select>
        <button class="apply-filters" type="submit">
          <svg viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="M2.5 5.8h15M4.8 10h10.4M7.5 14.2h5" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round"/></svg>
          Aplicar filtros
        </button>
      </form>
    </section>

    <section class="agenda" aria-labelledby="agenda-title">
      <div class="agenda-heading">
        <div>
          <h2 id="agenda-title">Control de audiencias</h2>
          <div class="agenda-subtitle">Selecciona un expediente para revisar sus actuaciones y seguimiento</div>
        </div>
        <div class="record-count" id="recordCount" aria-live="polite">0 registros</div>
      </div>

      <div class="tabs" role="tablist" aria-label="Estatus de audiencias">
        <button class="tab-button" type="button" role="tab" aria-selected="true" data-status="Todas">Todas</button>
        <button class="tab-button" type="button" role="tab" aria-selected="false" data-status="Próxima">Próximas</button>
        <button class="tab-button" type="button" role="tab" aria-selected="false" data-status="En curso">En curso</button>
        <button class="tab-button" type="button" role="tab" aria-selected="false" data-status="Concluida">Concluidas</button>
      </div>

      <div class="table-scroll">
        <table>
          <thead>
          <tr>
            <th scope="col">Expediente</th>
            <th scope="col">Materia</th>
            <th scope="col">Juzgado / Mesa</th>
            <th scope="col">Fecha</th>
            <th scope="col">Hora</th>
            <th scope="col">Estatus</th>
            <th scope="col">Acciones</th>
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

<script>
  window.APP_CONTEXT = "${pageContext.request.contextPath}";
</script>
<script src="${pageContext.request.contextPath}/assets/js/audiencias.js?v=<%= System.currentTimeMillis() %>"></script>
</body>
</html>
