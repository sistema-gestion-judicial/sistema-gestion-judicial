<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>SGA Audiencias | Abogados de audiencia</title>

  <!-- Hojas de estilo compartidas -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/abogados.css">
</head>
<body>
<div class="page">

  <!-- Barra Superior (Topbar) -->
  <header class="topbar">
    <a class="brand" href="#" aria-label="SGA Audiencias, inicio">
      <span class="logo">Logo</span>
      <span>
        <span class="brand-name">SGA</span>
        <span class="brand-subtitle" style="display:block">Audiencias</span>
      </span>
    </a>

    <nav class="main-nav" aria-label="Navegación principal">
      <a href="${pageContext.request.contextPath}/mesas.jsp" class="nav-link" style="text-decoration:none;">Mesas</a>
      <button class="nav-link" aria-current="page">Abogados de audiencia</button>
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
    <!-- Encabezado con Fondo de Estatua de la Justicia -->
    <section aria-labelledby="page-title" class="hero-banner-justice">
      <div class="heading-rule"></div>
      <h1 id="page-title" class="font-serif fw-normal text-dark mb-1">Abogados de audiencia</h1>
      <p class="intro">Gestiona los Abogados de Audiencia inscritos al sistema.</p>

      <form class="filters" id="abogadosFilters">
        <div class="search-wrap">
          <svg class="search-icon" viewBox="0 0 20 20" fill="none" aria-hidden="true" width="16" height="16">
            <circle cx="9" cy="9" r="6" stroke="currentColor" stroke-width="1.8"/>
            <path d="m13.5 13.5 4 4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          </svg>
          <input class="search-field" id="nombreSearch" type="search" placeholder="Buscar por nombre o acreditación" aria-label="Buscar por nombre o acreditación">
        </div>

        <select class="subject-filter" id="materiaFilter" aria-label="Materia">
          <option value="">Materia</option>
          <option value="Familiar">Familiar</option>
          <option value="Civil">Civil</option>
          <option value="Mercantil">Mercantil</option>
          <option value="Penal">Penal</option>
        </select>

        <button type="button" id="btnResetFilters" class="btn-link-reset text-decoration-none">Limpiar filtros</button>

        <button class="apply-filters" type="submit">
          <svg viewBox="0 0 20 20" fill="none" aria-hidden="true" width="16" height="16">
            <path d="M2.5 5.8h15M4.8 10h10.4M7.5 14.2h5" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round"/>
          </svg>
          Aplicar filtros
        </button>
      </form>
    </section>

    <!-- Contenedor Tabla de Abogados -->
    <section class="agenda" aria-labelledby="table-title">
      <div class="agenda-heading">
        <div>
          <h2 id="table-title" class="font-serif italic-title fw-normal">Abogados de audiencia</h2>
          <div class="agenda-subtitle">Consulta los abogados de audiencia inscritos</div>
        </div>
        <button type="button" id="btnRegisterAbogado" class="btn-register-pill">
          + Registrar abogado
        </button>
      </div>

      <div class="table-scroll">
        <table class="abogados-table">
          <thead>
          <tr>
            <th scope="col">Nombre</th>
            <th scope="col">Acreditación</th>
            <th scope="col">Materia</th>
            <th scope="col">Estado</th>
            <th scope="col" style="text-align:right;">Acciones</th>
          </tr>
          </thead>
          <tbody id="abogadosRows"></tbody>
        </table>
      </div>

      <footer class="table-footer">
        <span id="paginationSummary" aria-live="polite">Mostrando 1–4 de 36</span>
        <nav class="pagination" aria-label="Paginación de abogados" id="pagination"></nav>
      </footer>
    </section>
  </main>
</div>

<!-- Modal 1: Registrar Abogado -->
<dialog id="modalRegistrarAbogado" class="custom-modal">
  <div class="modal-body-custom">
    <h2 class="font-serif modal-title">Registrar abogado</h2>
    <p class="modal-subtitle">Ingresa los datos generales y de adscripción para dar de alta al abogado en el sistema</p>
    <hr class="modal-divider">

    <form id="formRegistrarAbogado" class="modal-form-grid">
      <div class="form-group">
        <label for="regNombre">Nombre completo</label>
        <input type="text" id="regNombre" class="form-control" placeholder="Ej. Fernando Pérez" required>
      </div>

      <div class="form-group">
        <label for="regCorreo">Correo institucional</label>
        <input type="email" id="regCorreo" class="form-control" placeholder="Ej. fernandop@gob.mx" required>
      </div>

      <div class="form-group">
        <label for="regAcreditacion">Acreditación / Cédula</label>
        <input type="text" id="regAcreditacion" class="form-control" placeholder="Ej. CAP-1845285" required>
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
        <label for="regMesa">Mesa asignada</label>
        <select id="regMesa" class="form-select" required>
          <option value="Sin asignar">Sin asignar</option>
          <option value="M-01">M-01</option>
          <option value="M-02">M-02</option>
          <option value="M-03">M-03</option>
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

      <div class="info-banner full-width">
        <svg viewBox="0 0 20 20" fill="none" width="18" height="18" style="flex-shrink:0;">
          <circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="1.6"/>
          <path d="M10 9v5M10 6.5h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <span>El abogado quedará vinculado a la mesa seleccionada para dirigir y gestionar sus audiencias.</span>
      </div>

      <div class="modal-actions full-width">
        <button type="button" class="btn-link-reset text-decoration-none text-muted" id="btnCancelReg">Cancelar</button>
        <button type="submit" class="btn-auth-primary px-4 py-2">Registrar abogado</button>
      </div>
    </form>
  </div>
</dialog>

<!-- Modal 2: Editar Abogado -->
<dialog id="modalEditarAbogado" class="custom-modal">
  <div class="modal-body-custom">
    <h2 class="font-serif modal-title">Editar abogado</h2>
    <p class="modal-subtitle">Actualiza la información personal, acreditación, mesa o estado del abogado seleccionado</p>
    <hr class="modal-divider">

    <form id="formEditarAbogado" class="modal-form-grid">
      <input type="hidden" id="editAbogadoId">

      <div class="form-group">
        <label for="editNombre">Nombre completo</label>
        <input type="text" id="editNombre" class="form-control" required>
      </div>

      <div class="form-group">
        <label for="editCorreo">Correo institucional</label>
        <input type="email" id="editCorreo" class="form-control" required>
      </div>

      <div class="form-group">
        <label for="editAcreditacion">Acreditación / Cédula</label>
        <input type="text" id="editAcreditacion" class="form-control" required>
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
        <label for="editMesa">Mesa asignada</label>
        <select id="editMesa" class="form-select" required>
          <option value="Sin asignar">Sin asignar</option>
          <option value="M-01">M-01</option>
          <option value="M-02">M-02</option>
          <option value="M-03">M-03</option>
        </select>
      </div>

      <div class="form-group">
        <label>Estado del abogado</label>
        <div class="d-flex align-items-center gap-2 mt-1">
          <label class="switch-toggle">
            <input type="checkbox" id="editEstado">
            <span class="slider"></span>
          </label>
          <span class="text-sm fw-medium">Activo</span>
        </div>
      </div>

      <div class="info-banner full-width">
        <svg viewBox="0 0 20 20" fill="none" width="18" height="18" style="flex-shrink:0;">
          <circle cx="10" cy="10" r="8" stroke="currentColor" stroke-width="1.6"/>
          <path d="M10 9v5M10 6.5h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <span>Modificar la mesa o materia afectará el flujo de solicitudes y audiencias asignadas a este perfil.</span>
      </div>

      <div class="modal-actions full-width">
        <button type="button" class="btn-link-reset text-decoration-none text-muted" id="btnCancelEdit">Cancelar</button>
        <button type="submit" class="btn-auth-primary px-4 py-2">Guardar cambios</button>
      </div>
    </form>
  </div>
</dialog>

<script src="${pageContext.request.contextPath}/assets/js/abogados.js?v=1.0"></script>
</body>
</html>
