<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" uri="http://utez.edu.mx/tocas/format" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:page title="${greeting}, ${firstName}" subtitle="Este es el resumen de sus tocas, ponencias y avisos." active="home">
  <div class="row g-3 mb-3">
    <div class="col-6 col-lg-3"><div class="stat"><div class="stat-num">${assignedCount}</div><div class="stat-label">Tocas asignadas</div></div></div>
    <div class="col-6 col-lg-3"><div class="stat"><div class="stat-num">${hearingsIn15Days}</div><div class="stat-label">Ponencias en 15 días</div></div></div>
    <div class="col-6 col-lg-3"><div class="stat"><div class="stat-num">${draftCount}</div><div class="stat-label">Resolución en borrador</div></div></div>
    <div class="col-6 col-lg-3"><div class="stat"><div class="stat-num">${unreadCount}</div><div class="stat-label">Avisos sin leer</div></div></div>
  </div>
  <div class="row g-3">
    <div class="col-lg-5">
      <section class="panel h-100"><div class="panel-head"><h2>Próximas ponencias</h2></div>
        <div class="p-3 d-grid gap-2">
          <c:forEach var="h" items="${upcoming}">
            <t:hearingCard hearing="${h}" href="${ctx}/tocas/detail?id=${h.appealId}&tab=ponencias&hearing=${h.id}"/>
          </c:forEach>
          <c:if test="${empty upcoming}"><p class="text-muted m-0">No tiene ponencias próximas.</p></c:if>
          <a class="btn btn-outline-g" href="${ctx}/agenda">Ver agenda</a>
        </div>
      </section>
    </div>
    <div class="col-lg-7">
      <section class="panel h-100"><div class="panel-head"><h2>Avisos recientes</h2></div>
        <div class="notice-list">
          <c:forEach var="n" items="${notices}">
            <a class="notice ${n.read ? '' : 'unread'}" href="${ctx}/notifications/open?id=${n.id}">
              <t:icon name="${n.type == 'NEW_APPEAL' ? 'doc-plus' : n.type == 'FILE_RECEIVED' ? 'inbox' : 'calendar'}" size="20"/>
              <span class="notice-body"><strong><c:out value="${n.title}"/></strong><span><c:out value="${n.message}"/></span><small>${f:dateTime24(n.createdAt)}</small></span>
            </a>
          </c:forEach>
          <c:if test="${empty notices}"><p class="text-muted p-3 m-0">No tiene avisos.</p></c:if>
        </div>
        <div class="p-3"><a class="btn btn-outline-g" href="${ctx}/notifications">Ver todos los avisos</a></div>
      </section>
    </div>
  </div>
</t:page>
