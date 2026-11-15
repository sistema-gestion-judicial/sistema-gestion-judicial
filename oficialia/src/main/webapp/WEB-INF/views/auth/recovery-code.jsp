<%-- session="false": the flow reads its session in the servlet, never from this page. --%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Código de verificación" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<body>
<div class="login-split">
  <%@ include file="/WEB-INF/views/layout/login-brand.jspf" %>

  <section class="login-panel">
    <div class="card-mor login-card">
      <h2 class="mb-4">Código de verificación</h2>

      <div class="alert alert-mor-info d-flex gap-2 align-items-start" role="status">
        <i class="bi bi-envelope"></i>
        <div>
          Si el correo <strong><c:out value="${email}" /></strong> está registrado, recibirá un código de
          verificación. El código vence en ${codeMinutes} minutos y solo puede usarse una vez.
          Si no llega, espere ${cooldownSeconds} segundos antes de solicitar otro.
        </div>
      </div>
      <c:if test="${not empty errorMessage}">
        <div class="alert alert-mor-warning py-2" role="alert">
          <i class="bi bi-exclamation-triangle me-1"></i> <c:out value="${errorMessage}" />
        </div>
      </c:if>

      <form method="post" action="${ctx}/password-recovery/code" novalidate>
        <div class="mb-4">
          <label for="code" class="form-label">Código de verificación *</label>
          <input type="text" class="form-control ${not empty errors.code ? 'is-invalid' : ''}" id="code"
                 name="code" inputmode="numeric" pattern="[0-9]*" maxlength="${codeLength}"
                 autocomplete="one-time-code" placeholder="${codeLength} dígitos" required autofocus>
          <c:if test="${not empty errors.code}">
            <div class="invalid-feedback"><c:out value="${errors.code}" /></div>
          </c:if>
        </div>

        <button type="submit" class="btn btn-primary w-100 mb-3">Verificar código</button>
        <div class="d-flex justify-content-between">
          <a class="small text-secondary" href="${ctx}/password-recovery">Solicitar un código nuevo</a>
          <a class="small text-secondary" href="${ctx}/login">Volver a iniciar sesión</a>
        </div>
      </form>
    </div>
  </section>
</div>
<%@ include file="/WEB-INF/views/layout/scripts.jspf" %>
