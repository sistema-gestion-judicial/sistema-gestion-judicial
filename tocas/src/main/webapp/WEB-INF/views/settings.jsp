<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:page title="Configuración" subtitle="Datos de la cuenta con la que inició sesión." active="settings" crumb="Configuración">
  <div class="row g-3">
    <div class="col-lg-7">
      <section class="panel"><div class="panel-head"><h2>Datos del magistrado</h2></div>
        <div class="p-3">
          <div class="kv-row"><span>Nombre</span><strong><c:out value="${sessionScope.user.fullName}"/></strong></div>
          <div class="kv-row"><span>Correo electrónico</span><strong><c:out value="${sessionScope.user.email}"/></strong></div>
          <div class="kv-row"><span>Usuario</span><strong><c:out value="${sessionScope.user.username}"/></strong></div>
          <div class="kv-row"><span>Cargo</span><strong><c:out value="${sessionScope.user.roleTitle}"/></strong></div>
          <div class="kv-row"><span>Sala</span><strong><c:out value="${empty sessionScope.user.roomName ? 'Sin sala asignada' : sessionScope.user.roomName}"/></strong></div>
          <div class="kv-row"><span>Ubicación</span><strong>${location}</strong></div>
        </div></section>
    </div>
    <div class="col-lg-5">
      <section class="panel"><div class="panel-head"><h2>Sesión</h2></div>
        <div class="p-3">
          <div class="info-box mb-3">Para cambiar sus datos o su contraseña, contacte al administrador del sistema.</div>
          <form method="post" action="${ctx}/logout"><button class="btn btn-outline-g w-100" type="submit">Cerrar sesión</button></form>
        </div></section>
    </div>
  </div>
</t:page>
