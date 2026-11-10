<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="hasError" value="${param.error != null || not empty error}" />
<!DOCTYPE html>
<html lang="es">
<head>
  <title>Recuperación de cuenta - Audiencias</title>
  <%@ include file="../layout/head.jspf" %>
</head>
<body>
<div class="container-fluid vh-100 p-0 overflow-hidden">
  <div class="row g-0 h-100">

    <!-- Panel Izquierdo (Logo, Título y Subtítulo de Recuperación) -->
    <div class="col-lg-6 bg-auth-left p-5 d-flex flex-column justify-content-between">
      <div>
        <div class="logo-container">
          <span class="logo-text">Logo</span>
        </div>
      </div>

      <div class="my-auto py-4">
        <span class="accent-line"></span>
        <h1 class="font-serif display-5 fw-bold mb-3">Sistema de Gestión de Audiencias</h1>
        <p class="text-white-50 fs-6 mb-0">Recuperación de cuenta</p>
      </div>

      <div class="border-top border-secondary border-opacity-50 pt-3">
        <p class="small text-white-50 mb-1">Acceso exclusivo para personal autorizado.</p>
        <p class="small text-white-50 mb-0">¿Tiene problemas para entrar? Contacte al administrador del sistema.</p>
      </div>
    </div>

    <!-- Panel Derecho (Formulario de Recuperación) -->
    <div class="col-lg-6 bg-auth-right d-flex align-items-center justify-content-center p-4">
      <div class="card auth-card p-4 p-md-5">
        <h2 class="font-serif fw-bold text-dark mb-3">Recuperación de cuenta</h2>
        <p class="text-muted small mb-4">
          Ingresa tu correo electrónico para enviar un enlace de recuperación.
        </p>

        <form id="recoverForm" action="${pageContext.request.contextPath}/recover-account" method="post">
          <div class="mb-2">
            <label for="email" class="form-label small text-secondary fw-semibold">Correo electrónico</label>
            <input type="email"
                   class="form-control py-2 ${hasError ? 'is-invalid' : ''}"
                   id="email"
                   name="email"
                   placeholder="mperez@utez.edu.mx"
                   required>

            <c:if test="${hasError}">
              <div class="error-msg d-inline-flex align-items-center gap-1 mt-2">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="currentColor" class="bi bi-exclamation-circle" viewBox="0 0 16 16">
                  <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
                  <path d="M7.002 11a1 1 0 1 0 2 0 1 1 0 0 0-2 0zM7.1 4.995a.905.905 0 1 1 1.8 0l-.35 3.507a.552.552 0 0 1-1.1 0L7.1 4.995z"/>
                </svg>
                Correo electrónico no registrado o inválido.
              </div>
            </c:if>
          </div>

          <button onC type="submit" class="btn btn-auth-primary w-100 py-2 mt-4 mb-4">Enviar enlace</button>

          <div class="text-center">
            <span class="text-muted small">¿Quieres iniciar sesión? </span>
            <a href="${pageContext.request.contextPath}/" class="link-auth fw-semibold">Iniciar sesión</a>
          </div>
        </form>
      </div>
    </div>

  </div>
</div>

<!-- Script externo JS -->
<script src="${pageContext.request.contextPath}/assets/js/recover-account.js"></script>
</body>
</html>
