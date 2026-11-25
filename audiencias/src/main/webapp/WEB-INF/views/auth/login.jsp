<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="hasError" value="${param.error != null || not empty error}" />
<!DOCTYPE html>
<html lang="es">
<head>
  <title>Iniciar Sesión - Audiencias</title>
  <%@ include file="../layout/head.jspf" %>
</head>
<body>
<div class="container-fluid vh-100 p-0 overflow-hidden">
  <div class="row g-0 h-100">

    <!-- Panel Izquierdo (Logo, Título y Nota de pie) -->
    <div class="col-lg-6 bg-auth-left p-5 d-flex flex-column justify-content-between">
      <div>
        <div class="logo-container">
          <span class="logo-text">Logo</span>
        </div>
      </div>

      <div class="my-auto py-4">
        <span class="accent-line"></span>
        <h1 class="font-serif display-5 fw-bold mb-3">Sistema de Gestión de Audiencias</h1>
        <p class="text-white-50 fs-6 mb-0">Audiencias</p>
      </div>

      <div class="border-top border-secondary border-opacity-50 pt-3">
        <p class="small text-white-50 mb-1">Acceso exclusivo para personal autorizado.</p>
        <p class="small text-white-50 mb-0">¿Tiene problemas para entrar? Contacte al administrador del sistema.</p>
        <a href="inicioabogados.jsp" class="small text-white-50 mb-0">inicio</a>
      </div>
    </div>

    <!-- Panel Derecho (Formulario de Login) -->
    <div class="col-lg-6 bg-auth-right d-flex align-items-center justify-content-center p-4">
      <div class="card auth-card p-4 p-md-5">
        <h2 class="font-serif fw-bold text-dark mb-3">Iniciar sesión</h2>
        <p class="text-muted small mb-4">
          Bienvenido al Sistema de Gestión de Audiencias.<br>
          Para ingresar, escriba su correo electrónico y contraseña.
        </p>

        <form action="${pageContext.request.contextPath}/login" method="post">
          <div class="mb-3">
            <label for="email" class="form-label small text-secondary fw-semibold">Correo electrónico</label>
            <input type="email"
                   class="form-control py-2 ${hasError ? 'is-invalid' : ''}"
                   id="email"
                   name="email"
                   placeholder="mperez@utez.edu.mx"
                   required>
          </div>

          <div class="mb-2">
            <label for="password" class="form-label small text-secondary fw-semibold">Contraseña</label>
            <div class="position-relative">
              <input type="password"
                     class="form-control py-2 pe-5 ${hasError ? 'is-invalid' : ''}"
                     id="password"
                     name="password"
                     required>
              <button type="button" id="btnTogglePassword" class="btn btn-link position-absolute end-0 top-50 translate-middle-y text-dark text-decoration-none small me-2" onclick="togglePassword()">
                Mostrar
              </button>
            </div>
          </div>

          <!-- Fila inferior: Mensaje de Error + Enlace Olvidó contraseña -->
          <div class="d-flex justify-content-between align-items-center mb-4 mt-2">
            <div>
              <c:if test="${hasError}">
                <span class="error-msg d-inline-flex align-items-center gap-1">
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="currentColor" class="bi bi-exclamation-circle" viewBox="0 0 16 16">
                    <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
                    <path d="M7.002 11a1 1 0 1 0 2 0 1 1 0 0 0-2 0zM7.1 4.995a.905.905 0 1 1 1.8 0l-.35 3.507a.552.552 0 0 1-1.1 0L7.1 4.995z"/>
                  </svg>
                  Correo o contraseña incorrectos.
                </span>
              </c:if>
            </div>
            <a href="${pageContext.request.contextPath}/recover.jsp" class="link-auth">¿Olvidó su contraseña?</a>
          </div>

          <button type="submit" class="btn btn-auth-primary w-100 py-2">Entrar</button>
        </form>
      </div>
    </div>

  </div>
</div>

<script src="${pageContext.request.contextPath}/assets/js/login.js"></script>
</body>
</html>
