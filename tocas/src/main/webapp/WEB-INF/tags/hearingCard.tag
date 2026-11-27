<%@ tag language="java" pageEncoding="UTF-8" body-content="empty" description="Hearing card (guide: Ponencia | Tarjeta)" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="f" uri="http://utez.edu.mx/tocas/format" %>
<%@ attribute name="hearing" required="true" type="mx.edu.utez.judicial.tocas.model.Hearing" %>
<%@ attribute name="href" required="true" %>
<a class="hearing-card" href="${href}">
  <span class="hc-date"><strong>${f:dayNum(hearing.date)}</strong><small>${f:monthAbbr(hearing.date)}</small></span>
  <span class="hc-body">
    <span class="hc-top"><strong>${hearing.appeal.number}</strong>
      <span class="tag ${hearing.done ? 'tag-green' : 'tag-tan'}">${f:hearingStatus(hearing.status)}</span></span>
    <span>${hearing.appeal.originCourt}</span>
    <span>${hearing.appeal.parties}</span>
  </span>
  <t:icon name="chevron-right" size="16"/>
</a>
