<%-- session="false": the flow reads its session in the servlet, never from this page. --%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Nueva contraseña" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<body>
<div class="login-split">
  <%@ include file="/WEB-INF/views/layout/login-brand.jspf" %>

  <section class="login-panel">
    <div class="card-mor login-card">
      <h2 class="mb-4">Nueva contraseña</h2>
      <p class="text-secondary mb-4">
        El código es correcto. Escriba su nueva contraseña: al menos ${passwordMinLength} caracteres, con letras
        y números.
      </p>

      <form method="post" action="${ctx}/password-recovery/reset" novalidate>
        <div class="mb-3">
          <label for="newPassword" class="form-label">Nueva contraseña *</label>
          <input type="password" class="form-control ${not empty errors.newPassword ? 'is-invalid' : ''}"
                 id="newPassword" name="newPassword" autocomplete="new-password" required autofocus>
          <c:if test="${not empty errors.newPassword}">
            <div class="invalid-feedback"><c:out value="${errors.newPassword}" /></div>
          </c:if>
        </div>

        <div class="mb-4">
          <label for="confirmation" class="form-label">Confirmar nueva contraseña *</label>
          <input type="password" class="form-control ${not empty errors.confirmation ? 'is-invalid' : ''}"
                 id="confirmation" name="confirmation" autocomplete="new-password" required>
          <c:if test="${not empty errors.confirmation}">
            <div class="invalid-feedback"><c:out value="${errors.confirmation}" /></div>
          </c:if>
        </div>

        <button type="submit" class="btn btn-primary w-100 mb-3">Guardar contraseña</button>
        <div class="text-center">
          <a class="small text-secondary" href="${ctx}/login">Cancelar</a>
        </div>
      </form>
    </div>
  </section>
</div>
<%@ include file="/WEB-INF/views/layout/scripts.jspf" %>
