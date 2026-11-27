<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="unread" value="${not empty requestScope.unreadCount ? requestScope.unreadCount : requestScope.countUnread}"/>
<c:forTokens var="w" items="${sessionScope.user.fullName}" delims=" " varStatus="s">
  <c:if test="${empty first}"><c:set var="first" value="${w.substring(0, 1)}"/></c:if>
  <c:if test="${!s.first}"><c:set var="last" value="${w.substring(0, 1)}"/></c:if>
</c:forTokens>
<header class="topbar">
  <a class="brand" href="${ctx}/home"><strong>SSCT</strong><small>Segunda instancia</small></a>
  <nav class="crumbs" aria-label="Ruta de navegación">
    <a href="${ctx}/home">Inicio</a> <t:icon name="chevron-right" size="12"/>
    <strong><c:out value="${not empty param.crumb ? param.crumb : param.title}"/></strong>
  </nav>
  <div class="topbar-right">
    <a class="notif-pill" href="${ctx}/notifications">
      <span class="ico"><t:icon name="bell" size="16"/></span>
      <span><strong>Notificaciones</strong><c:if test="${not empty unread}"><small>${unread} ${unread == 1 ? 'nueva' : 'nuevas'}</small></c:if></span>
    </a>
    <div class="dropdown user-menu">
      <button class="user-btn" type="button" data-bs-toggle="dropdown" aria-expanded="false">
        <span class="avatar"><c:out value="${first}${last}"/></span>
        <span class="user-text"><strong><c:out value="${sessionScope.user.fullName}"/></strong><small><c:out value="${sessionScope.user.roleTitle}"/></small></span>
        <t:icon name="chevron-down" size="18"/>
      </button>
      <ul class="dropdown-menu dropdown-menu-end">
        <li><form method="post" action="${ctx}/logout"><button class="dropdown-item" type="submit">Cerrar sesión</button></form></li>
      </ul>
    </div>
  </div>
</header>
