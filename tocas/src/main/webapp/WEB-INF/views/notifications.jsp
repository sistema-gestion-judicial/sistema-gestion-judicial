<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" uri="http://utez.edu.mx/tocas/format" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:page title="Notificaciones" subtitle="Avisos sobre sus tocas, ponencias y expedientes recibidos." active="notifications">
  <jsp:attribute name="actions">
    <form method="post" action="${ctx}/notifications/read-all"><button class="btn btn-outline-g" type="submit" ${countUnread == 0 ? 'disabled' : ''}>Marcar todas como leídas</button></form>
  </jsp:attribute>
  <jsp:attribute name="tabs">
    <div class="tabs-row"><nav class="tabs-bar">
      <a class="${filter == 'all' ? 'active' : ''}" href="${ctx}/notifications?filter=all">Todas (${countAll})</a>
      <a class="${filter == 'unread' ? 'active' : ''}" href="${ctx}/notifications?filter=unread">Sin leer (${countUnread})</a>
      <a class="${filter == 'read' ? 'active' : ''}" href="${ctx}/notifications?filter=read">Leídas (${countRead})</a>
    </nav></div>
  </jsp:attribute>
  <jsp:body>
    <section class="panel">
      <c:forEach var="n" items="${notifications}">
        <div class="notice-row ${n.read ? '' : 'unread'}">
          <t:icon name="${n.type == 'NEW_APPEAL' ? 'doc-plus' : n.type == 'FILE_RECEIVED' ? 'inbox' : 'calendar'}" size="20"/>
          <div class="notice-body"><strong><c:out value="${n.title}"/></strong><span><c:out value="${n.message}"/></span><small>${f:dateTime24(n.createdAt)}</small></div>
          <c:if test="${!n.read}"><span class="tag tag-blue tag-dot">Nueva</span></c:if>
          <a class="btn btn-outline-g btn-sm" href="${ctx}/notifications/open?id=${n.id}">Ver toca</a>
        </div>
      </c:forEach>
      <c:if test="${empty notifications}"><p class="text-muted text-center p-5 m-0">No hay avisos en esta sección.</p></c:if>
    </section>
  </jsp:body>
</t:page>
