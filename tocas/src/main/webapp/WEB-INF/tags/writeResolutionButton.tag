<%@ tag language="java" pageEncoding="UTF-8" body-content="empty" description="'Redactar resolución' button (creates the draft or opens the editor)" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="appeal" required="true" type="mx.edu.utez.judicial.tocas.model.Appeal" %>
<%@ attribute name="resolution" required="false" type="mx.edu.utez.judicial.tocas.model.Resolution" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:if test="${!appeal.concluded}">
  <c:choose>
    <c:when test="${empty resolution}">
      <form method="post" action="${ctx}/tocas/resolution/create" class="d-inline">
        <input type="hidden" name="id" value="${appeal.id}">
        <button type="submit" class="btn btn-dark-g">Redactar resolución</button>
      </form>
    </c:when>
    <c:when test="${resolution.draft}">
      <a class="btn btn-dark-g" href="${ctx}/tocas/resolution/edit?id=${appeal.id}">Redactar resolución</a>
    </c:when>
  </c:choose>
</c:if>
