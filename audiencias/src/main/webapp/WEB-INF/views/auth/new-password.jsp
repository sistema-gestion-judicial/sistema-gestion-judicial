<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Restablecer contraseña - Sistema de Gestión de Audiencias</title>

  <!-- Bootstrap 5 CSS e Iconos -->
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">

  <!-- Hoja de Estilos General -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
</head>
<body>
<div class="container-fluid p-0 vh-100">
  <div class="row g-0 h-100">

    <!-- Panel Izquierdo: Branding y Textura Morelos -->
    <div class="col-lg-6 bg-auth-left d-flex flex-column justify-content-between p-4 p-md-5">
      <div>
        <div class="logo-container mb-4">
          <span class="logo-text">Logo</span>
        </div>
      </div>

      <div class="my-auto py-4">
        <span class="accent-line"></span>
        <h1 class="display-5 font-serif fw-normal mb-2">Recupera tu cuenta de forma segura.</h1>
        <p class="text-white-50 fs-6 mb-0">Restablece tu contraseña.</p>
      </div>

      <div class="border-top border-secondary border-opacity-25 pt-3">
        <p class="small text-white-50 mb-1">Acceso exclusivo para personal autorizado.</p>
        <p class="small text-white-50 mb-0">¿Tiene problemas para entrar? Contacte al administrador del sistema.</p>
      </div>
    </div>

    <!-- Panel Derecho: Formulario Nueva Contraseña -->
    <div class="col-lg-6 bg-auth-right d-flex align-items-center justify-content-center p-4">
      <div class="card auth-card p-4 p-sm-5">
        <h2 class="font-serif fs-3 mb-2">Restablecer contraseña</h2>
        <p class="text-muted small mb-4">Ingresa tu contraseña nueva</p>

        <form id="newPasswordForm" action="index.jsp" method="GET" novalidate>
          <div class="mb-3">
            <label for="password" class="form-label">Contraseña nueva</label>
            <input type="password"
                   class="form-control"
                   id="password"
                   name="password"
                   placeholder="••••••••"
                   required>
          </div>

          <div class="mb-3">
            <label for="confirmPassword" class="form-label">Confirmar contraseña</label>
            <input type="password"
                   class="form-control"
                   id="confirmPassword"
                   name="confirmPassword"
                   placeholder="••••••••"
                   required>
            <div class="error-msg mt-2 d-none text-danger small" id="passwordError">
              <i class="bi bi-exclamation-circle me-1"></i> Las contraseñas no coinciden
            </div>
          </div>

          <button type="submit" class="btn btn-auth-primary w-100 py-2 mt-3">
            Cambiar contraseña
          </button>

          <div class="text-center mt-4">
            <span class="text-muted small">¿Ya recordaste tu contraseña?</span>
            <a href="index.jsp" class="link-auth ms-1">Iniciar sesión</a>
          </div>
        </form>
      </div>
    </div>

  </div>
</div>

<!-- Script de Lógica Externa -->
<script src="${pageContext.request.contextPath}/assets/js/new-password.js"></script>
</body>
</html>
