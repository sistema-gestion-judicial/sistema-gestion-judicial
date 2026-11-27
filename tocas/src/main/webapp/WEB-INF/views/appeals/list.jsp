<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" uri="http://utez.edu.mx/tocas/format" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:page title="Mis tocas" subtitle="Aquí puede consultar y gestionar las tocas asignadas a su sala." active="tocas">
  <form class="filter-bar" method="get" action="${ctx}/tocas">
    <label class="search-box"><t:icon name="search" size="18"/><input type="search" name="q" value="<c:out value='${q}'/>" placeholder="Buscar por toca, juzgado o partes"></label>
    <select class="field filter-select" name="status" aria-label="Estado">
      <option value="" ${status == '' ? 'selected' : ''}>Estado: Todos</option>
      <option value="NEW" ${status == 'NEW' ? 'selected' : ''}>Estado: Nueva</option>
      <option value="CONCLUDED" ${status == 'CONCLUDED' ? 'selected' : ''}>Estado: Concluida</option>
    </select>
    <div class="dropdown">
      <button type="button" class="field filter-select text-start" data-bs-toggle="dropdown" data-bs-auto-close="outside" aria-expanded="false">
        <c:choose><c:when test="${not empty from or not empty to}">${f:dateNum(from)} – ${f:dateNum(to)}</c:when><c:otherwise>Rango de fechas</c:otherwise></c:choose>
        <t:icon name="chevron-down" size="16"/></button>
      <div class="dropdown-menu p-3 range-menu">
        <label class="mini-label" for="from">Recepción desde</label><input class="field mb-2" type="date" id="from" name="from" value="${from}">
        <label class="mini-label" for="to">Recepción hasta</label><input class="field" type="date" id="to" name="to" value="${to}">
      </div>
    </div>
    <a class="link-g ms-lg-auto" href="${ctx}/tocas">Limpiar filtros</a>
    <button class="btn btn-dark-g" type="submit"><t:icon name="filter" size="16"/> Aplicar filtros</button>
  </form>

  <section class="panel table-panel">
    <div class="panel-head d-flex justify-content-between align-items-start">
      <div><h2 class="caps">Expedientes asignados</h2><p class="m-0 small">Consulta y seguimiento de las tocas de su sala.</p></div>
      <small>${result.total} registros</small>
    </div>
    <div class="table-responsive">
    <table class="grid-table">
      <thead><tr><th>Toca</th><th>Partes</th><th>Fechas</th><th>Estado</th><th class="text-end">Acciones</th></tr></thead>
      <tbody>
      <c:forEach var="a" items="${result.items}">
        <tr>
          <td><strong>${a.number}</strong><small><c:out value="${a.originCourt}"/></small></td>
          <td><c:out value="${a.plaintiff}"/><small>vs. <c:out value="${a.defendant}"/></small></td>
          <td><c:choose><c:when test="${not empty a.nextHearing}">${f:dateShort(a.nextHearing.date)}</c:when><c:otherwise><span class="text-muted">Sin ponencia</span></c:otherwise></c:choose>
              <small>Recepción: ${f:dateShort(a.receivedDate)}</small></td>
          <td><span class="tag ${a.concluded ? 'tag-green' : 'tag-blue'} tag-dot">${f:appealStatus(a.status)}</span></td>
          <td class="text-end"><a class="row-action" href="${ctx}/tocas/detail?id=${a.id}"><t:icon name="eye" size="16"/> Ver</a></td>
        </tr>
      </c:forEach>
      </tbody>
    </table>
    </div>
    <c:if test="${result.total == 0}">
      <div class="empty-state"><t:icon name="search" size="36"/><h3>No hay tocas con esos filtros</h3>
        <p>Pruebe con otro número de toca, juzgado o parte, o limpie los filtros.</p>
        <a class="btn btn-outline-g" href="${ctx}/tocas">Limpiar filtros</a></div>
    </c:if>
    <div class="table-foot">
      <span>Mostrando <c:choose><c:when test="${result.total == 0}">0 de 0</c:when><c:otherwise>${result.firstIndex}–${result.lastIndex} de ${result.total}</c:otherwise></c:choose></span>
      <c:if test="${result.total > 0}">
      <nav class="pager" aria-label="Paginación">
        <a class="pg ${result.page <= 1 ? 'disabled' : ''}" href="?${queryBase}&page=${result.page - 1}" aria-label="Anterior"><t:icon name="chevron-left" size="16"/></a>
        <c:forEach var="p" items="${result.window}">
          <c:choose>
            <c:when test="${p == -1}"><span class="pg-gap">…</span></c:when>
            <c:otherwise><a class="pg ${p == result.page ? 'active' : ''}" href="?${queryBase}&page=${p}">${p}</a></c:otherwise>
          </c:choose>
        </c:forEach>
        <a class="pg ${result.page >= result.pages ? 'disabled' : ''}" href="?${queryBase}&page=${result.page + 1}" aria-label="Siguiente"><t:icon name="chevron-right" size="16"/></a>
      </nav></c:if>
    </div>
  </section>
</t:page>
