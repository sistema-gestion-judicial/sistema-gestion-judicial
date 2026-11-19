<%@ tag language="java" pageEncoding="UTF-8" body-content="scriptless" description="Layout of the appeal detail screens (title, badge, tabs)" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" uri="http://utez.edu.mx/tocas/format" %>
<%@ attribute name="appeal" required="true" type="mx.edu.utez.judicial.tocas.model.Appeal" %>
<%@ attribute name="tab" required="true" %>
<%@ attribute name="actions" fragment="true" required="false" %>
<%@ attribute name="scripts" fragment="true" required="false" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:page title="Toca ${appeal.number}" subtitle="${appeal.parties} · ${appeal.originCourt}" active="tocas">
  <jsp:attribute name="titleExtra">
    <span class="tag ${appeal.concluded ? 'tag-green' : 'tag-blue'} tag-dot align-middle">${f:appealStatus(appeal.status)}</span>
  </jsp:attribute>
  <jsp:attribute name="actions"><c:if test="${not empty actions}"><jsp:invoke fragment="actions"/></c:if></jsp:attribute>
  <jsp:attribute name="tabs">
    <div class="tabs-row">
      <nav class="tabs-bar" aria-label="Secciones de la toca">
        <a class="${tab == 'resumen' ? 'active' : ''}" href="${ctx}/tocas/detail?id=${appeal.id}&tab=resumen">Resumen</a>
        <a class="${tab == 'expediente' ? 'active' : ''}" href="${ctx}/tocas/detail?id=${appeal.id}&tab=expediente">Expediente completo</a>
        <a class="${tab == 'ponencias' ? 'active' : ''}" href="${ctx}/tocas/detail?id=${appeal.id}&tab=ponencias">Ponencias</a>
        <a class="${tab == 'resolucion' ? 'active' : ''}" href="${ctx}/tocas/detail?id=${appeal.id}&tab=resolucion">Resolución</a>
        <a class="${tab == 'bitacora' ? 'active' : ''}" href="${ctx}/tocas/detail?id=${appeal.id}&tab=bitacora">Bitácora</a>
      </nav>
      <a class="back-link" href="${ctx}/tocas"><t:icon name="arrow-left" size="16"/> Volver a Mis tocas</a>
    </div>
  </jsp:attribute>
  <jsp:attribute name="scripts"><c:if test="${not empty scripts}"><jsp:invoke fragment="scripts"/></c:if></jsp:attribute>
  <jsp:body><jsp:doBody/></jsp:body>
</t:page>
