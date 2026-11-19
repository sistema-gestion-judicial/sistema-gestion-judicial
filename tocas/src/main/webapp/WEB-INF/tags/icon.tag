<%@ tag language="java" pageEncoding="UTF-8" body-content="empty" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="size" required="false" %>
<c:set var="sz" value="${empty size ? 18 : size}"/>
<svg class="icon" width="${sz}" height="${sz}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
<c:choose>
<c:when test="${name == 'search'}"><circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/></c:when>
<c:when test="${name == 'eye'}"><path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12z"/><circle cx="12" cy="12" r="3"/></c:when>
<c:when test="${name == 'chevron-right'}"><path d="m9 6 6 6-6 6"/></c:when>
<c:when test="${name == 'chevron-left'}"><path d="m15 6-6 6 6 6"/></c:when>
<c:when test="${name == 'chevron-down'}"><path d="m6 9 6 6 6-6"/></c:when>
<c:when test="${name == 'calendar'}"><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M8 3v4M16 3v4M3 10h18"/></c:when>
<c:when test="${name == 'clock'}"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></c:when>
<c:when test="${name == 'doc'}"><path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8z"/><path d="M14 3v5h5M9 13h6M9 17h6"/></c:when>
<c:when test="${name == 'doc-plus'}"><path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8z"/><path d="M14 3v5h5M12 11v6M9 14h6"/></c:when>
<c:when test="${name == 'inbox'}"><path d="M3 13h5l1 3h6l1-3h5"/><path d="M5 5h14l2 8v6H3v-6z"/></c:when>
<c:when test="${name == 'filter'}"><path d="M3 5h18l-7 8v6l-4-2v-4z"/></c:when>
<c:when test="${name == 'user'}"><circle cx="12" cy="8" r="4"/><path d="M4 21c1-4 4-6 8-6s7 2 8 6"/></c:when>
<c:when test="${name == 'close'}"><path d="M6 6l12 12M18 6 6 18"/></c:when>
<c:when test="${name == 'upload'}"><path d="M12 16V4M7 9l5-5 5 5M4 20h16"/></c:when>
<c:when test="${name == 'arrow-left'}"><path d="M19 12H5M11 6l-6 6 6 6"/></c:when>
<c:when test="${name == 'arrow-right'}"><path d="M5 12h14M13 6l6 6-6 6"/></c:when>
<c:when test="${name == 'alert'}"><circle cx="12" cy="12" r="9"/><path d="M12 7v6M12 16.5v.5"/></c:when>
<c:when test="${name == 'plus'}"><path d="M12 5v14M5 12h14"/></c:when>
<c:when test="${name == 'minus'}"><path d="M5 12h14"/></c:when>
<c:when test="${name == 'check-filled'}"><circle cx="12" cy="12" r="10" fill="currentColor" stroke="none"/><path d="m7.5 12.2 3 3 6-6.4" stroke="#fff" stroke-width="2.2"/></c:when>
<c:when test="${name == 'ring'}"><circle cx="12" cy="12" r="8.5" stroke-width="2.2"/></c:when>
<c:when test="${name == 'ring-dot'}"><circle cx="12" cy="12" r="8.5" stroke-width="2.2"/><circle cx="12" cy="12" r="3.2" fill="currentColor" stroke="none"/></c:when>
</c:choose>
</svg>
