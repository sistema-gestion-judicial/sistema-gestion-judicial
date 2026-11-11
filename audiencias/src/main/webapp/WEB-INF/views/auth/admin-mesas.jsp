<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>SGA Audiencias | Mesas</title>

  <!-- Reusamos el CSS global y el CSS que creó tu compañera -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/abogados.css">
</head>
<body>
<div class="page">

  <!-- Topbar con clases de abogados.css -->
  <header class="topbar">
    <a class="brand" href="#" aria-label="SGA Audiencias, inicio">
      <span class="logo">Logo</span>
      <span>
        <span class="brand-name">SGA</span>
        <span class="brand-subtitle" style="display:block">Audiencias</span>
      </span>
    </a>

    <nav class="main-nav" aria-label="Navegación principal">
      <button class="nav-link" aria-current="page">Mesas</button>
      <a href="#" class="nav-link" style="text-decoration:none;">Abogados de audiencia</a>
    </nav>

    <div class="profile-wrap">
      <button class="profile" id="profileButton" aria-expanded="false" aria-haspopup="true" aria-controls="profileMenu" type="button">
        <span class="avatar" aria-hidden="true">
          <svg width="35" height="35" viewBox="0 0 36 36" fill="none">
            <circle cx="18" cy="11" r="7" stroke="currentColor" stroke-width="2"/>
            <path d="M5.5 32c.9-7.3 5.2-11 12.5-11s11.6 3.7 12.5 11" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </span>
        <span>
          <span class="profile-name">Ricardo Salgado</span>
          <span class="profile-role" style="display:block">Administrador</span>
        </span>
        <svg class="profile-chevron" viewBox="0 0 20 20" fill="none" aria-hidden="true">
          <path d="m4 7 6 6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>

      <span class="profile-menu" id="profileMenu" role="menu">
        <button type="button" role="menuitem">Mi perfil</button>
        <a href="${pageContext.request.contextPath}/views/auth/login.jsp" style="text-decoration: none;">
          <button type="button" role="menuitem" style="width:100%; border:0; background:transparent; text-align:left; cursor:pointer;">Cerrar sesión</button>
        </a>
      </span>
    </div>
  </header>

  <main>

    <section aria-labelledby="page-title">
      <div class="heading-rule"></div>
      <h1 id="page-title" class="font-serif">Mesas</h1>
      <p class="intro">Gestiona las mesas pertenecientes al sistema</p>

      <form class="filters" id="mesasFilters">
        <input class="search-field" id="expedienteSearch" type="search" placeholder="Buscar por expediente" aria-label="Buscar por expediente">

        <select class="subject-filter" id="materiaFilter" aria-label="Materia">
          <option value="">Materia</option>
          <option value="Familiar">Familiar</option>
          <option value="Civil">Civil</option>
          <option value="Mercantil">Mercantil</option>
          <option value="Penal">Penal</option>
        </select>

        <button type="button" id="btnResetFilters" class="btn-link-reset">Limpiar filtros</button>

        <button class="apply-filters" type="submit">
          <svg viewBox="0 0 20 20" fill="none" aria-hidden="true" width="16" height="16">
            <path d="M2.5 5.8h15M4.8 10h10.4M7.5 14.2h5" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round"/>
          </svg>
          Aplicar filtros
        </button>
      </form>
    </section>

    <!-- Contenedor Tabla de Mesas -->
    <section class="agenda" aria-labelledby="table-title">
      <div class="agenda-heading">
        <div>
          <h2 id="table-title" class="font-serif fst-italic">Mesas</h2>
          <div class="agenda-subtitle">Consulta las mesas</div>
        </div>
        <button type="button" id="btnRegisterMesa" class="apply-filters rounded-pill border-0" style="background-color: #2b3929;">
          + Registrar mesa
        </button>
      </div>

      <div class="table-scroll">
        <table>
          <thead>
          <tr>
            <th scope="col">N-Mesa</th>
            <th scope="col">Juzgado Asignado</th>
            <th scope="col">Materia</th>
            <th scope="col">Estado</th>
            <th scope="col" style="text-align:right;">Acciones</th>
          </tr>
          </thead>
          <tbody id="mesasRows"></tbody>
        </table>
      </div>


      <footer class="table-footer">
        <span id="paginationSummary" aria-live="polite">Mostrando 1–4 de 36</span>
        <nav class="pagination" aria-label="Paginación de mesas" id="pagination"></nav>
      </footer>
    </section>
  </main>
</div>
<!-- Modal 1: Registrar Mesa -->
<dialog id="modalRegistrarMesa" class="custom-modal">
  <div class="modal-body-custom">
    <h2 class="font-serif modal-title">Registrar mesa</h2>
    <p class="modal-subtitle">Ingresa los datos para dar de alta una nueva mesa en el sistema</p>
    <hr class="modal-divider">

    <form id="formRegistrarMesa" class="modal-form-grid">
      <div class="form-group">
        <label for="regNumero">Número de mesa</label>
        <input type="text" id="regNumero" class="form-control" placeholder="Ej. M-06" required>
      </div>

      <div class="form-group">
        <label for="regJuzgado">Juzgado asignado</label>
        <select id="regJuzgado" class="form-select" required>
          <option value="" disabled selected>Ej. Juzgado 2°</option>
          <option value="Juzgado 1°">Juzgado 1°</option>
          <option value="Juzgado 2°">Juzgado 2°</option>
          <option value="Juzgado 3°">Juzgado 3°</option>
        </select>
      </div>

      <div class="form-group">
        <label for="regDistrito">Distrito judicial</label>
        <select id="regDistrito" class="form-select" required>
          <option value="Distrito 1 - Cuernavaca">Distrito 1 - Cuernavaca</option>
          <option value="Distrito 2 - Cuautla">Distrito 2 - Cuautla</option>
        </select>
      </div>

      <div class="form-group">
        <label for="regMateria">Materia jurídica</label>
        <select id="regMateria" class="form-select" required>
          <option value="" disabled selected>Selecciona una materia</option>
          <option value="Familiar">Familiar</option>
          <option value="Civil">Civil</option>
          <option value="Mercantil">Mercantil</option>
          <option value="Penal">Penal</option>
        </select>
      </div>

      <div class="form-group">
        <label for="regAbogado">Abogado de audiencia</label>
        <select id="regAbogado" class="form-select" required>
          <option value="" disabled selected>Selecciona un abogado</option>
          <option value="Lic. Ricardo Salgado">Lic. Ricardo Salgado</option>
          <option value="Lic. María Gómez">Lic. María Gómez</option>
        </select>
      </div>

      <div class="form-group">
        <label>Estado inicial</label>
        <div class="d-flex align-items-center gap-2 mt-1">
          <label class="switch-toggle">
            <input type="checkbox" id="regEstado" checked>
            <span class="slider"></span>
          </label>
          <span class="text-sm fw-medium">Activo</span>
        </div>
      </div>

      <!-- Banner Informativo Modal Registrar -->
      <div class="info-banner full-width">
        <svg viewBox="0 0 20 20" fill="none" width="18" height="18" style="flex-shrink:0;">
          <circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="1.6"/>
          <path d="M10 9v5M10 6.5h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <span>La mesa quedará habilitada inmediatamente para la asignación de audiencias.</span>
      </div>

      <div class="modal-actions full-width">
        <button type="button" class="btn-link-reset text-decoration-none text-muted" id="btnCancelReg">Cancelar</button>
        <button type="submit" class="btn-auth-primary px-4 py-2">Registrar mesa</button>
      </div>
    </form>
  </div>
</dialog>

<!-- Modal 2: Editar Mesa -->
<dialog id="modalEditarMesa" class="custom-modal">
  <div class="modal-body-custom">
    <h2 class="font-serif modal-title">Editar mesa</h2>
    <p class="modal-subtitle">Actualiza la información, materia o estado de la mesa seleccionada</p>
    <hr class="modal-divider">

    <form id="formEditarMesa" class="modal-form-grid">
      <input type="hidden" id="editMesaId">

      <div class="form-group">
        <label for="editNumero">Número de mesa</label>
        <input type="text" id="editNumero" class="form-control" required>
      </div>

      <div class="form-group">
        <label for="editJuzgado">Juzgado asignado</label>
        <select id="editJuzgado" class="form-select" required>
          <option value="Juzgado 1°">Juzgado 1°</option>
          <option value="Juzgado 2°">Juzgado 2°</option>
          <option value="Juzgado 3°">Juzgado 3°</option>
        </select>
      </div>

      <div class="form-group">
        <label for="editDistrito">Distrito judicial</label>
        <select id="editDistrito" class="form-select" required>
          <option value="Distrito 1 - Cuernavaca">Distrito 1 - Cuernavaca</option>
          <option value="Distrito 2 - Cuautla">Distrito 2 - Cuautla</option>
        </select>
      </div>

      <div class="form-group">
        <label for="editMateria">Materia jurídica</label>
        <select id="editMateria" class="form-select" required>
          <option value="Familiar">Familiar</option>
          <option value="Civil">Civil</option>
          <option value="Mercantil">Mercantil</option>
          <option value="Penal">Penal</option>
        </select>
      </div>

      <div class="form-group">
        <label for="editAbogado">Abogado de audiencia</label>
        <select id="editAbogado" class="form-select" required>
          <option value="Lic. Ricardo Salgado">Lic. Ricardo Salgado</option>
          <option value="Lic. María Gómez">Lic. María Gómez</option>
        </select>
      </div>

      <div class="form-group">
        <label>Estado de la mesa</label>
        <div class="d-flex align-items-center gap-2 mt-1">
          <label class="switch-toggle">
            <input type="checkbox" id="editEstado">
            <span class="slider"></span>
          </label>
          <span class="text-sm fw-medium">Activo</span>
        </div>
      </div>

      <div class="info-banner full-width">
        <i class="bi bi-info-circle me-2"></i>
        <span>Los cambios afectarán la disponibilidad de la mesa para nuevos agendamientos.</span>
      </div>

      <div class="modal-actions full-width">
        <button type="button" class="btn-link-reset text-decoration-none text-muted" id="btnCancelEdit">Cancelar</button>
        <button type="submit" class="btn-auth-primary px-4 py-2">Guardar cambios</button>
      </div>
    </form>
  </div>
</dialog>

<script src="${pageContext.request.contextPath}/assets/js/mesas.js?v=1.1"></script>
</body>
</html>
