<%-- session="false": viewing this page must not create a session that could later look expired. --%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Iniciar sesión" />
<c:set var="pageScript" value="login.js" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<body>
<div class="login-split">
  <%@ include file="/WEB-INF/views/layout/login-brand.jspf" %>

  <section class="login-panel">
    <div class="card-mor login-card">
      <h2>Iniciar sesión</h2>
      <p class="login-intro">
        Bienvenido al Sistema de Control de Demandas. Para ingresar, escriba su correo electrónico y contraseña.
      </p>

      <c:if test="${not empty infoMessage}">
        <div class="alert alert-mor-success py-2" role="status">
          <i class="bi bi-check-circle me-1"></i> <c:out value="${infoMessage}" />
        </div>
      </c:if>
      <c:if test="${not empty expiredMessage}">
        <div class="alert alert-mor-info py-2" role="status">
          <i class="bi bi-clock-history me-1"></i> <c:out value="${expiredMessage}" />
        </div>
      </c:if>

      <%-- A rejected sign-in marks both fields and shows the reason under the password (Figma "Login | Error") --%>
      <c:set var="signInFailed" value="${not empty errorMessage}" />
      <form method="post" action="${ctx}/login" novalidate>
        <div class="mb-4">
          <label for="email" class="form-label">Correo electrónico</label>
          <input type="email" class="form-control ${not empty emailError or signInFailed ? 'is-invalid' : ''}"
                 id="email" name="email" autocomplete="username" value="<c:out value='${email}' />"
                 placeholder="nombre@utez.edu.mx" required autofocus
                 ${signInFailed ? 'aria-describedby="signInError"' : ''}>
          <c:if test="${not empty emailError}">
            <div class="invalid-feedback"><c:out value="${emailError}" /></div>
          </c:if>
        </div>

        <div>
          <label for="password" class="form-label">Contraseña</label>
          <div class="input-group">
            <input type="password" class="form-control border-end-0 ${signInFailed ? 'is-invalid' : ''}"
                   id="password" name="password" autocomplete="current-password" required
                   ${signInFailed ? 'aria-describedby="signInError"' : ''}>
            <button type="button" class="btn btn-link password-toggle" id="togglePassword">Mostrar</button>
          </div>
        </div>
        <div class="login-row">
          <c:if test="${signInFailed}">
            <span class="login-error" id="signInError" role="alert"><c:out value="${errorMessage}" /></span>
          </c:if>
          <a href="${ctx}/password-recovery">¿Olvidó su contraseña?</a>
        </div>

        <button type="submit" class="btn btn-primary w-100">Entrar</button>
      </form>
    </div>
  </section>
</div>
<%@ include file="/WEB-INF/views/layout/scripts.jspf" %>
