<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Iniciar sesión" />
<c:set var="pageScript" value="login.js" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<body>
<div class="login-split">
  <section class="login-brand">
    <div class="app-brand-logo">SCD</div>
    <div class="login-brand-body">
      <div class="login-brand-rule"></div>
      <h1>Sistema de Control<br>de Demandas</h1>
      <div class="login-brand-area">Oficialía de Partes</div>
      <p class="fs-5 mb-0">Registre y califique solicitudes de demanda, y detecte duplicidades entre juzgados.</p>
    </div>
    <div class="login-brand-footer">
      Acceso exclusivo para personal autorizado.<br>
      ¿Tiene problemas para entrar? Contacte al administrador del sistema.
    </div>
  </section>

  <section class="login-panel">
    <div class="card-mor login-card">
      <h2 class="mb-4">Iniciar sesión</h2>
      <p class="text-secondary mb-4">
        Bienvenido al Sistema de Control de Demandas. Para ingresar, escriba su correo electrónico y contraseña.
      </p>

      <c:if test="${not empty infoMessage}">
        <div class="alert alert-mor-success py-2" role="status">
          <i class="bi bi-check-circle me-1"></i> <c:out value="${infoMessage}" />
        </div>
      </c:if>
      <c:if test="${not empty errorMessage}">
        <div class="alert alert-mor-warning py-2" role="alert">
          <i class="bi bi-exclamation-triangle me-1"></i> <c:out value="${errorMessage}" />
        </div>
      </c:if>

      <form method="post" action="${ctx}/login" novalidate>
        <div class="mb-3">
          <label for="email" class="form-label">Correo electrónico</label>
          <input type="email" class="form-control" id="email" name="email" autocomplete="username"
                 value="<c:out value='${email}' />" placeholder="nombre@utez.edu.mx" required autofocus>
        </div>

        <div class="mb-1">
          <label for="password" class="form-label">Contraseña</label>
          <div class="input-group">
            <input type="password" class="form-control border-end-0" id="password" name="password"
                   autocomplete="current-password" required>
            <button type="button" class="btn btn-link password-toggle" id="togglePassword">Mostrar</button>
          </div>
        </div>
        <div class="text-end mb-4">
          <span class="small text-decoration-underline text-secondary" title="Disponible en un siguiente avance">
            ¿Olvidó su contraseña?
          </span>
        </div>

        <button type="submit" class="btn btn-primary w-100">Entrar</button>
      </form>
    </div>
  </section>
</div>
<%@ include file="/WEB-INF/views/layout/scripts.jspf" %>
