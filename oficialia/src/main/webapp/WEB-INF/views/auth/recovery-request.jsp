<%--
  Created by IntelliJ IDEA.
  User: aleja
  Date: 10/11/2026
  Time: 12:15 p.m.
  To change this template use File | Settings | File Templates.
--%>
<%-- session="false": viewing this page must not create a session that could later look expired. --%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Recuperar contraseña" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<body>
<div class="login-split">
  <%@ include file="/WEB-INF/views/layout/login-brand.jspf" %>

  <section class="login-panel">
    <div class="card-mor login-card">
      <h2 class="mb-4">Recuperar contraseña</h2>
      <p class="text-secondary mb-4">
        Escriba el correo electrónico de su cuenta. Si está registrado, recibirá un código de verificación.
      </p>

      <form method="post" action="${ctx}/password-recovery" novalidate>
        <div class="mb-4">
          <label for="email" class="form-label">Correo electrónico *</label>
          <input type="email" class="form-control ${not empty errors.email ? 'is-invalid' : ''}" id="email"
                 name="email" autocomplete="username" value="<c:out value='${email}' />"
                 placeholder="nombre@utez.edu.mx" required autofocus>
          <c:if test="${not empty errors.email}">
            <div class="invalid-feedback"><c:out value="${errors.email}" /></div>
          </c:if>
        </div>

        <button type="submit" class="btn btn-primary w-100 mb-3">Enviar código</button>
        <div class="text-center">
          <a class="small text-secondary" href="${ctx}/login">Volver a iniciar sesión</a>
        </div>
      </form>
    </div>
  </section>
</div>
<%@ include file="/WEB-INF/views/layout/scripts.jspf" %>
