<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Everything goes through the /mesas servlet, which checks the session and issues the CSRF token --%>
<c:redirect url="/mesas"/>
