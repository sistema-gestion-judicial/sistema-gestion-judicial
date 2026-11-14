<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Cambiar contraseña" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<body>
<div class="login-split">
  <section class="login-brand">
    <div class="app-brand-logo">SCD</div>
    <div class="login-brand-body">
      <div class="login-brand-rule"></div>
      <h1>Sistema de Control<br>de Demandas</h1>
      <div class="login-brand-area">Oficialía de Partes</div>
      <p class="fs-5 mb-0">Es su primer acceso: elija una contraseña propia para continuar.</p>
    </div>
    <div class="login-brand-footer">
      Acceso exclusivo para personal autorizado.
    </div>
  </section>

  <section class="login-panel">
    <div class="card-mor login-card">
      <h2 class="mb-3">Cambiar contraseña</h2>
      <p class="text-secondary mb-4">
        Entró con una contraseña temporal. Escriba la nueva contraseña: debe tener al menos 8 caracteres,
        con letras y números.
      </p>

      <form method="post" action="${ctx}/password/change" novalidate>
        <div class="mb-3">
          <label for="newPassword" class="form-label">Nueva contraseña</label>
          <input type="password" id="newPassword" name="newPassword" autocomplete="new-password" autofocus required
                 class="form-control ${not empty errors.newPassword ? 'is-invalid' : ''}">
          <c:if test="${not empty errors.newPassword}">
            <div class="invalid-feedback"><c:out value="${errors.newPassword}" /></div>
          </c:if>
        </div>
        <div class="mb-4">
          <label for="confirmation" class="form-label">Confirmar contraseña</label>
          <input type="password" id="confirmation" name="confirmation" autocomplete="new-password" required
                 class="form-control ${not empty errors.confirmation ? 'is-invalid' : ''}">
          <c:if test="${not empty errors.confirmation}">
            <div class="invalid-feedback"><c:out value="${errors.confirmation}" /></div>
          </c:if>
        </div>
        <button type="submit" class="btn btn-primary w-100 mb-3">Guardar contraseña</button>
      </form>
      <form method="post" action="${ctx}/logout" class="text-center">
        <button type="submit" class="btn btn-link text-secondary">Cerrar sesión</button>
      </form>
    </div>
  </section>
</div>
<%@ include file="/WEB-INF/views/layout/scripts.jspf" %>
