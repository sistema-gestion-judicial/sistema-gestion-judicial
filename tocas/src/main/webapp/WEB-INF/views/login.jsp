<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Iniciar sesión · SSCT</title>
  <link href="https://fonts.googleapis.com/css2?family=Merriweather:wght@400;700&family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="${ctx}/webjars/bootstrap/5.3.3/css/bootstrap.min.css">
  <link rel="stylesheet" href="${ctx}/assets/css/app.css">
</head>
<body class="login-body">
<div class="login-split">
  <aside class="login-left">
    <span class="logo-box logo-lg">Logo</span>
    <div class="login-hero">
      <span class="accent-line"></span>
      <h2>Sistema de Seguimiento y Control de Tocas</h2>
      <small>Segunda instancia</small>
      <p>Consulte sus tocas, ponencias y documentos en un solo lugar.</p>
    </div>
    <div class="login-foot">
      <p>Acceso exclusivo para personal autorizado.<br>¿Tiene problemas para entrar? Contacte al administrador del sistema.</p>
    </div>
  </aside>
  <section class="login-right">
    <div class="login-card">
      <h1>Iniciar sesión</h1>
      <p class="login-intro">Bienvenido al Sistema de Seguimiento y Control de Tocas. Para ingresar, escriba su correo electrónico y contraseña.</p>
      <form method="post" action="${ctx}/login" novalidate>
        <label class="mini-label" for="email">Correo electrónico</label>
        <input class="field ${not empty error ? 'is-error' : ''}" type="email" id="email" name="email" value="<c:out value='${email}'/>" autocomplete="username" autofocus>
        <label class="mini-label mt-3" for="password">Contraseña</label>
        <div class="pw-wrap">
          <input class="field ${not empty error ? 'is-error' : ''}" type="password" id="password" name="password" autocomplete="current-password">
          <button type="button" class="pw-toggle" data-toggle-password="#password">Mostrar</button>
        </div>
        <div class="login-row">
          <c:if test="${not empty error}"><span class="login-error"><t:icon name="alert" size="15"/> <c:out value="${error}"/></span></c:if>
          <a href="#" class="ms-auto link-g" data-bs-toggle="modal" data-bs-target="#forgotModal">¿Olvidó su contraseña?</a>
        </div>
        <button type="submit" class="btn btn-dark-g w-100 mt-2">Entrar</button>
      </form>
    </div>
  </section>
</div>
<div class="modal fade" id="forgotModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered"><div class="modal-content app-modal">
    <div class="modal-head"><h2>Recuperar contraseña</h2><button type="button" class="modal-x" data-bs-dismiss="modal" aria-label="Cerrar"><t:icon name="close" size="16"/></button></div>
    <div class="modal-body-g">Para restablecer su contraseña, contacte al administrador del sistema.</div>
    <div class="modal-foot"><button type="button" class="btn btn-dark-g" data-bs-dismiss="modal">Entendido</button></div>
  </div></div>
</div>
<script src="${ctx}/webjars/bootstrap/5.3.3/js/bootstrap.bundle.min.js"></script>
<script src="${ctx}/assets/js/app.js"></script>
</body>
</html>
