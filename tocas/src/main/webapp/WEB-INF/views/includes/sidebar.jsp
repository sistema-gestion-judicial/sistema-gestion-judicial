<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="active" value="${param.active}"/>
<aside class="sidebar" aria-label="Navegación principal">
  <a class="sb-brand" href="${ctx}/home" aria-label="Tocas, inicio">
    <svg viewBox="0 0 80 72" fill="none" stroke="currentColor" stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
      <path d="M40 6v52M28 62h24M40 6l-1.5-2.5h3zM12 16h56"/>
      <path d="M14 16 4 40a10 10 0 0 0 20 0zM66 16 56 40a10 10 0 0 0 20 0z"/><path d="M4 40h20M56 40h20"/>
    </svg>
    <span>Tocas</span>
  </a>
  <nav class="sb-nav">
    <a class="sb-link ${active == 'home' ? 'active' : ''}" href="${ctx}/home"><t:icon name="home" size="20"/> Inicio</a>
    <a class="sb-link ${active == 'tocas' ? 'active' : ''}" href="${ctx}/tocas"><t:icon name="gavel" size="20"/> Mis tocas</a>
    <a class="sb-link ${active == 'agenda' ? 'active' : ''}" href="${ctx}/agenda"><t:icon name="calendar" size="20"/> Calendario</a>
    <a class="sb-link ${active == 'notifications' ? 'active' : ''}" href="${ctx}/notifications"><t:icon name="bell" size="20"/> Notificaciones</a>
    <a class="sb-link ${active == 'settings' ? 'active' : ''}" href="${ctx}/settings"><t:icon name="settings" size="20"/> Configuración</a>
  </nav>
  <div class="sb-foot">
    <form method="post" action="${ctx}/logout"><button class="sb-link" type="submit"><t:icon name="logout" size="20"/> Cerrar sesión</button></form>
  </div>
</aside>
