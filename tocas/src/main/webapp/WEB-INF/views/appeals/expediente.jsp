<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" uri="http://utez.edu.mx/tocas/format" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:appealPage appeal="${appeal}" tab="expediente">
  <jsp:attribute name="scripts"><script src="${ctx}/assets/js/viewer.js"></script></jsp:attribute>
  <jsp:body>
  <div class="row g-3">
    <div class="col-lg-4">
      <section class="panel"><div class="panel-head"><h2>Actuaciones</h2></div>
        <div class="p-3">
          <p class="small">Mostrando las ${docs.size()} actuaciones · ${totalPages} páginas</p>
          <c:forEach var="group" items="FIRST,APPEAL">
            <p class="group-label"><strong><c:choose><c:when test="${group == 'FIRST'}">Primera instancia · <c:out value="${appeal.originCourt}"/></c:when><c:otherwise>Apelación · Segunda instancia</c:otherwise></c:choose></strong></p>
            <c:forEach var="d" items="${docs}"><c:if test="${d.instance == group}">
              <a class="act-item ${d.id == selectedDoc.id ? 'selected' : ''}" href="${ctx}/tocas/detail?id=${appeal.id}&tab=expediente&doc=${d.id}">
                <t:icon name="doc" size="18"/><span><strong>${d.name}</strong><small>PDF · ${d.pages} páginas · ${f:dateNum(d.date)}</small></span></a>
            </c:if></c:forEach>
          </c:forEach>
        </div>
      </section>
    </div>
    <div class="col-lg-8">
      <section class="panel" id="viewer" data-pages="${selectedDoc.pages}" data-title="${selectedDoc.name}" data-file="${appeal.originFile}">
        <div class="panel-head"><h2>${selectedDoc.name}</h2></div>
        <div class="p-3">
          <p class="small">PDF · ${selectedDoc.pages} páginas · ${f:dateNum(selectedDoc.date)} · <c:out value="${selectedDoc.court}"/></p>
          <div class="viewer-bar">
            <div class="d-flex align-items-center gap-2">
              <button type="button" class="sq-btn" id="prevPage" aria-label="Página anterior"><t:icon name="chevron-left" size="16"/></button>
              <span class="small fw-bold" id="pageLabel">Página 1 de ${selectedDoc.pages}</span>
              <button type="button" class="sq-btn" id="nextPage" aria-label="Página siguiente"><t:icon name="chevron-right" size="16"/></button>
            </div>
            <div class="d-flex align-items-center gap-2">
              <button type="button" class="sq-btn" id="zoomOut" aria-label="Alejar"><t:icon name="minus" size="16"/></button>
              <span class="small fw-bold" id="zoomLabel">100 %</span>
              <button type="button" class="sq-btn" id="zoomIn" aria-label="Acercar"><t:icon name="plus" size="16"/></button>
            </div>
          </div>
          <div class="viewer-stage"><div class="viewer-sheet" id="sheet"></div></div>
        </div>
      </section>
    </div>
  </div>
  </jsp:body>
</t:appealPage>
