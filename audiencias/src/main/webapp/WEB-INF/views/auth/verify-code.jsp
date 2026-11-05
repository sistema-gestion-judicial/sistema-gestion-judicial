<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="hasError" value="${param.error != null || not empty error}" />
<!DOCTYPE html>
<html lang="es">
<head>
  <title>Confirmar código - Audiencias</title>
  <%@ include file="../layout/head.jspf" %>
</head>
<body>
<div class="container-fluid vh-100 p-0 overflow-hidden">
  <div class="row g-0 h-100">

    <!-- Panel Izquierdo -->
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
      </div>
    </div>

    <!-- Panel Derecho -->
    <div class="col-lg-6 bg-auth-right d-flex align-items-center justify-content-center p-4">
      <div class="card auth-card p-4 p-md-5">
        <h2 class="font-serif fw-bold text-dark mb-3">Confirmar código</h2>
        <p class="text-muted small mb-4">
          Ingresa el código de 6 dígitos a continuación.
        </p>

        <form id="codeForm" action="${pageContext.request.contextPath}/verify-code" method="post">
          <input type="hidden" id="fullCode" name="code" value="">

          <!-- 6 casillas de entrada -->
          <div class="code-input-container mb-2">
            <input type="text" maxlength="1" class="code-input ${hasError ? 'is-invalid' : ''}" required autocomplete="off">
            <input type="text" maxlength="1" class="code-input ${hasError ? 'is-invalid' : ''}" required autocomplete="off">
            <input type="text" maxlength="1" class="code-input ${hasError ? 'is-invalid' : ''}" required autocomplete="off">
            <input type="text" maxlength="1" class="code-input ${hasError ? 'is-invalid' : ''}" required autocomplete="off">
            <input type="text" maxlength="1" class="code-input ${hasError ? 'is-invalid' : ''}" required autocomplete="off">
            <input type="text" maxlength="1" class="code-input ${hasError ? 'is-invalid' : ''}" required autocomplete="off">
          </div>

          <!-- Mensaje de error con ícono -->
          <c:if test="${hasError}">
            <div id="errorCodeMsg" class="error-msg d-flex align-items-center gap-1 mb-3">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="currentColor" class="bi bi-exclamation-circle" viewBox="0 0 16 16">
                <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
                <path d="M7.002 11a1 1 0 1 0 2 0 1 1 0 0 0-2 0zM7.1 4.995a.905.905 0 1 1 1.8 0l-.35 3.507a.552.552 0 0 1-1.1 0L7.1 4.995z"/>
              </svg>
              Código incorrecto.
            </div>
          </c:if>

          <button type="submit" class="btn btn-auth-primary w-100 py-2 mt-4 mb-4">Enviar código</button>

          <div class="text-center">
            <span class="text-muted small">¿No recibiste código? </span>
            <a href="#" class="link-auth fw-semibold">Reenviar código</a>
          </div>
        </form>
      </div>
    </div>

  </div>
</div>

<script src="${pageContext.request.contextPath}/assets/js/verify-code.js"></script>
</body>
</html>
