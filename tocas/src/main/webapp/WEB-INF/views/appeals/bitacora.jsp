<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" uri="http://utez.edu.mx/tocas/format" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:appealPage appeal="${appeal}" tab="bitacora">
  <jsp:body>
  <section class="panel"><div class="panel-head d-flex justify-content-between"><h2>Historial de la toca</h2><small>Solo lectura</small></div>
    <div class="table-responsive"><table class="grid-table">
      <thead><tr><th>Fecha y hora</th><th>Usuario</th><th>Acción</th><th>Detalle</th></tr></thead>
      <tbody><c:forEach var="e" items="${audit}">
        <tr><td><strong>${f:dateNum(e.timestamp.toLocalDate())}</strong><small>${f:time12(e.timestamp.toLocalTime())}</small></td>
          <td><c:out value="${e.username}"/></td><td><strong><c:out value="${e.action}"/></strong></td><td><c:out value="${e.detail}"/></td></tr>
      </c:forEach></tbody></table></div>
    <div class="table-foot"><span>Mostrando ${audit.size()} movimientos de esta toca, del más reciente al más antiguo.</span></div>
  </section>
  </jsp:body>
</t:appealPage>
