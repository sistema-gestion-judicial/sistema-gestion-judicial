<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" uri="http://utez.edu.mx/tocas/format" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:page title="Agenda de ponencias" subtitle="Consulte sus próximas sesiones y fechas asignadas" active="agenda">
  <div class="row g-3">
    <div class="col-lg-8">
      <section class="panel"><div class="panel-head d-flex justify-content-between align-items-center"><h2>${f:monthYear(monthStart)}</h2>
          <div class="d-flex gap-2"><a class="sq-btn" href="?month=${prevMonth}" aria-label="Mes anterior"><t:icon name="chevron-left" size="16"/></a>
            <a class="sq-btn px-3" href="?month=${todayMonth}">Hoy</a>
            <a class="sq-btn" href="?month=${nextMonth}" aria-label="Mes siguiente"><t:icon name="chevron-right" size="16"/></a></div></div>
        <div class="p-3">
          <div class="cal-grid cal-head"><span>Lun</span><span>Mar</span><span>Mié</span><span>Jue</span><span>Vie</span><span>Sáb</span><span>Dom</span></div>
          <div class="cal-grid">
            <c:forEach var="d" items="${days}">
              <c:choose>
                <c:when test="${d.inMonth}">
                  <a class="cal-day ${d.state == 'UPCOMING' ? 'up' : d.state == 'DONE' ? 'done' : ''} ${d.selected ? 'sel' : ''} ${d.today ? 'today' : ''}" href="?month=${f:isoMonth(monthStart)}&date=${d.date}"><span>${d.date.dayOfMonth}</span></a>
                </c:when>
                <c:otherwise><span class="cal-day muted"><span>${d.date.dayOfMonth}</span></span></c:otherwise>
              </c:choose>
            </c:forEach>
          </div>
          <c:if test="${not empty nextMonthCount}">
            <div class="info-box d-flex justify-content-between mt-3"><span>${nextMonthCount} ponencias más en ${nextMonthLabel} (${nextMonthDays})</span><a class="link-g fw-bold" href="?month=${nextMonth}">Ver ${nextMonthLabel}</a></div>
          </c:if>
          <div class="legend"><span><i class="lg up"></i>Próxima</span><span><i class="lg done"></i>Concluida</span><span><i class="lg today"></i>Hoy</span><span><i class="lg sel"></i>Seleccionado</span></div>
        </div>
      </section>
    </div>
    <div class="col-lg-4">
      <c:choose>
      <c:when test="${not empty selectedDate}">
        <section class="panel"><div class="panel-head"><h2>${f:longDay(selectedDate)}</h2></div>
          <div class="p-3 d-grid gap-2">
            <p class="small m-0">${dayHearings.size()} <c:out value="${dayHearings.size() == 1 ? 'ponencia' : 'ponencias'}"/></p>
            <c:forEach var="h" items="${dayHearings}">
              <t:hearingCard hearing="${h}" href="${ctx}/tocas/detail?id=${h.appealId}&tab=ponencias&hearing=${h.id}"/>
              <a class="btn btn-outline-g" href="${ctx}/tocas/detail?id=${h.appealId}">Abrir toca</a>
            </c:forEach>
            <c:if test="${empty dayHearings}"><p class="text-muted small">No hay ponencias este día.</p></c:if>
            <a class="btn btn-outline-g mt-3" href="?month=${f:isoMonth(monthStart)}"><t:icon name="arrow-left" size="14"/> Volver a próximas ponencias</a>
          </div></section>
      </c:when>
      <c:otherwise>
        <section class="panel"><div class="panel-head"><h2>Próximas ponencias</h2></div>
          <div class="p-3 d-grid gap-2">
            <p class="small m-0">${upcomingCount} en los próximos 15 días</p>
            <c:forEach var="h" items="${upcoming}"><t:hearingCard hearing="${h}" href="${ctx}/tocas/detail?id=${h.appealId}&tab=ponencias&hearing=${h.id}"/></c:forEach>
            <c:if test="${empty upcoming}"><p class="text-muted small">No tiene ponencias próximas.</p></c:if>
            <a class="btn btn-outline-g" href="${ctx}/tocas">Ver todas mis tocas <t:icon name="arrow-right" size="14"/></a>
          </div></section>
      </c:otherwise></c:choose>
    </div>
  </div>
</t:page>
