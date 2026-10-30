<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Registrar solicitud de demanda" />
<c:set var="activeNav" value="new-request" />
<c:set var="pageScript" value="demand-request-new.js" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>

<header class="app-topbar mb-0">
  <div>
    <a class="back-link" href="${ctx}/demand-requests"><i class="bi bi-arrow-left"></i> Volver a Solicitudes</a>
    <h1 class="page-title">Registrar solicitud de demanda</h1>
    <p class="page-subtitle">
      Primero capture los datos mínimos para verificar que la demanda no esté activa en otro juzgado.
    </p>
  </div>
  <%@ include file="/WEB-INF/views/layout/user-chip.jspf" %>
</header>

<ol class="stepper">
  <li class="stepper-item ${verified ? 'is-done' : 'is-active'}">
    <span class="stepper-number">
      <c:choose><c:when test="${verified}"><i class="bi bi-check-lg"></i></c:when><c:otherwise>1</c:otherwise></c:choose>
    </span>
    Verificar duplicidad
  </li>
  <li class="stepper-line" aria-hidden="true"></li>
  <li class="stepper-item ${verified ? 'is-active' : ''}">
    <span class="stepper-number">2</span> Completar registro
  </li>
</ol>

<div class="row g-4">
  <div class="col-xl-8">

    <c:if test="${verified and empty continuedWithWarning}">
      <div class="alert alert-mor-success d-flex gap-2 align-items-start" role="status">
        <i class="bi bi-check-circle mt-1"></i>
        <span>Verificación completada: no se encontró una demanda activa con estos datos en ningún juzgado del estado.
          Ya puede completar el registro.</span>
      </div>
    </c:if>
    <c:if test="${not empty continuedWithWarning}">
      <div class="alert alert-mor-warning d-flex gap-2 align-items-start" role="status">
        <i class="bi bi-exclamation-triangle mt-1"></i>
        <span>Se guardó su decisión de continuar pese a la advertencia
          (<c:out value="${continuedWithWarning.courtName}" />, <c:out value="${continuedWithWarning.matter}" />).
          La solicitud quedará marcada como registrada con advertencia.</span>
      </div>
    </c:if>

    <form id="duplicityForm" class="card-mor p-4" method="post" action="${ctx}/demand-requests/new" novalidate>
      <fieldset ${verified ? 'disabled' : ''}>
        <h2 class="card-title fs-5 mb-3"><i class="bi bi-search me-2"></i>Datos para verificar duplicidad</h2>

        <div class="row g-3 pb-4 border-bottom">
          <div class="col-md-4">
            <label for="matter" class="form-label">Materia *</label>
            <select id="matter" name="matter" class="form-select ${not empty errors.matter ? 'is-invalid' : ''}">
              <option value="">Seleccione</option>
              <c:forEach var="matter" items="${matters}">
                <option value="${matter}" ${form.matter == matter ? 'selected' : ''}><c:out value="${matter}" /></option>
              </c:forEach>
            </select>
            <div class="invalid-feedback"><c:out value="${errors.matter}" /></div>
          </div>
          <div class="col-md-4">
            <label for="receivedDate" class="form-label">Fecha de recepción</label>
            <input id="receivedDate" class="form-control" value="${receivedDate}" readonly>
          </div>
          <div class="col-md-4">
            <label for="court" class="form-label">Juzgado</label>
            <input id="court" class="form-control" value="${fn:escapeXml(sessionScope.currentUser.courtName)}" readonly>
          </div>
        </div>

        <h3 class="fs-5 fw-bold mt-4 mb-3">Demandante (actor)</h3>
        <div class="row g-3" data-party="plaintiff">
          <div class="col-md-4">
            <label for="plaintiffType" class="form-label">Tipo de persona</label>
            <select id="plaintiffType" name="plaintiffType" class="form-select" data-party-field>
              <option value="INDIVIDUAL" ${form.plaintiffType == 'INDIVIDUAL' ? 'selected' : ''}>Persona física</option>
              <option value="COMPANY" ${form.plaintiffType == 'COMPANY' ? 'selected' : ''}>Persona moral</option>
            </select>
          </div>
          <div class="col-md-4">
            <label for="plaintiffName" class="form-label">Nombre o razón social</label>
            <input id="plaintiffName" name="plaintiffName" data-party-field
                   class="form-control ${not empty errors.plaintiffName ? 'is-invalid' : ''}"
                   value="${fn:escapeXml(form.plaintiffName)}">
            <div class="invalid-feedback"><c:out value="${errors.plaintiffName}" /></div>
          </div>
          <div class="col-md-4">
            <label for="plaintiffId" class="form-label">RFC o CURP</label>
            <input id="plaintiffId" name="plaintiffId" data-party-field data-uppercase maxlength="18"
                   class="form-control ${not empty errors.plaintiffId ? 'is-invalid' : ''}"
                   value="${fn:escapeXml(form.plaintiffId)}">
            <div class="invalid-feedback"><c:out value="${errors.plaintiffId}" /></div>
          </div>
          <div class="col-12">
            <div class="form-check">
              <input class="form-check-input" type="checkbox" id="plaintiffAnonymous" name="plaintiffAnonymous"
                     data-party-toggle="plaintiff" ${form.plaintiffAnonymous ? 'checked' : ''}>
              <label class="form-check-label" for="plaintiffAnonymous">Demanda anónima (no se identifica al demandante)</label>
            </div>
          </div>
        </div>

        <h3 class="fs-5 fw-bold mt-4 pt-4 border-top mb-3">Demandado</h3>
        <div class="row g-3" data-party="defendant">
          <div class="col-md-4">
            <label for="defendantType" class="form-label">Tipo de persona</label>
            <select id="defendantType" name="defendantType" class="form-select" data-party-field>
              <option value="INDIVIDUAL" ${form.defendantType == 'INDIVIDUAL' ? 'selected' : ''}>Persona física</option>
              <option value="COMPANY" ${form.defendantType == 'COMPANY' ? 'selected' : ''}>Persona moral</option>
            </select>
          </div>
          <div class="col-md-4">
            <label for="defendantName" class="form-label">Nombre o razón social</label>
            <input id="defendantName" name="defendantName" data-party-field
                   class="form-control ${not empty errors.defendantName ? 'is-invalid' : ''}"
                   value="${fn:escapeXml(form.defendantName)}">
            <div class="invalid-feedback"><c:out value="${errors.defendantName}" /></div>
          </div>
          <div class="col-md-4">
            <label for="defendantId" class="form-label">RFC o CURP</label>
            <input id="defendantId" name="defendantId" data-party-field data-uppercase maxlength="18"
                   class="form-control ${not empty errors.defendantId ? 'is-invalid' : ''}"
                   value="${fn:escapeXml(form.defendantId)}">
            <div class="invalid-feedback"><c:out value="${errors.defendantId}" /></div>
          </div>
          <div class="col-12">
            <div class="form-check">
              <input class="form-check-input" type="checkbox" id="defendantUnknown" name="defendantUnknown"
                     data-party-toggle="defendant" ${form.defendantUnknown ? 'checked' : ''}>
              <label class="form-check-label" for="defendantUnknown">Se desconoce el nombre del demandado</label>
            </div>
          </div>
        </div>

        <c:if test="${not empty errors.parties}">
          <div class="alert alert-mor-warning d-flex gap-2 align-items-start mt-4 mb-0" role="alert">
            <i class="bi bi-exclamation-triangle mt-1"></i> <span><c:out value="${errors.parties}" /></span>
          </div>
        </c:if>
        <div class="alert alert-mor-info d-flex gap-2 align-items-start mt-4 mb-0">
          <i class="bi bi-info-circle mt-1"></i>
          <span>Capture al menos una parte con su nombre y su RFC o CURP. La duplicidad se verifica con el RFC
            o la CURP, porque dos personas pueden llamarse igual.</span>
        </div>
      </fieldset>
    </form>

    <section class="card-mor p-4 mt-4 ${verified ? '' : 'is-locked'}">
      <div class="d-flex justify-content-between align-items-center mb-2">
        <h2 class="card-title fs-5 mb-0"><i class="bi bi-file-earmark-text me-2"></i>Datos complementarios</h2>
        <c:choose>
          <c:when test="${verified}">
            <span class="badge-mor status-admissible"><i class="bi bi-unlock"></i> Habilitado</span>
          </c:when>
          <c:otherwise>
            <span class="badge-mor is-neutral"><i class="bi bi-lock"></i> Bloqueado</span>
          </c:otherwise>
        </c:choose>
      </div>
      <p class="mb-0 text-secondary">
        Domicilio, teléfono y correo de las partes, representante legal, tipo de presentación y anexos.
        <c:choose>
          <c:when test="${verified}">Esta parte del registro se presenta en el siguiente avance.</c:when>
          <c:otherwise>Se habilitan cuando termine la verificación de duplicidad.</c:otherwise>
        </c:choose>
      </p>
    </section>
  </div>

  <div class="col-xl-4">
    <aside class="card-mor p-4 mb-4">
      <h2 class="card-title fs-5">Cómo funciona</h2>
      <p class="text-secondary mb-0">
        El sistema busca en todos los juzgados del estado una demanda activa en la que coincida el RFC o la CURP
        del demandante o del demandado, junto con la materia. Si hay coincidencia verá el juzgado y la materia,
        y podrá decidir si continúa.
      </p>
    </aside>
    <div class="card-mor p-4 d-grid gap-3">
      <c:choose>
        <c:when test="${verified}">
          <button type="button" class="btn btn-primary" disabled>Registrar solicitud</button>
          <a class="btn btn-outline-primary" href="${ctx}/demand-requests">Volver a Solicitudes</a>
        </c:when>
        <c:otherwise>
          <button type="submit" class="btn btn-primary" form="duplicityForm" name="action" value="verify">
            Verificar duplicidad
          </button>
          <a class="btn btn-outline-primary" href="${ctx}/demand-requests">Cancelar</a>
        </c:otherwise>
      </c:choose>
    </div>
  </div>
</div>

<c:if test="${not empty duplicityMatch}">
  <div class="modal fade" id="duplicityModal" tabindex="-1" aria-labelledby="duplicityModalTitle"
       data-bs-backdrop="static" data-show-on-load>
    <div class="modal-dialog modal-dialog-centered modal-lg">
      <div class="modal-content p-3" style="border-radius: var(--mor-radius);">
        <div class="modal-header border-0">
          <div class="d-flex align-items-center gap-3">
            <span class="d-grid rounded-circle"
                  style="width:48px;height:48px;place-items:center;background:var(--mor-secondary-surface);color:var(--mor-warning-text);">
              <i class="bi bi-exclamation-triangle fs-4"></i>
            </span>
            <h2 class="modal-title fs-4 fw-bold font-title" id="duplicityModalTitle">Posible demanda duplicada</h2>
          </div>
        </div>
        <div class="modal-body pt-0">
          <p>Se encontró una demanda activa en otro juzgado en la que coincide el
            <c:out value="${duplicityMatch.matchText}" />, junto con la materia.</p>
          <div class="alert-mor-warning p-3 mb-3">
            <div class="fw-bold mb-2">Demanda activa encontrada</div>
            <div class="row g-3">
              <div class="col-6"><div class="small">Juzgado</div><div class="fw-bold"><c:out value="${duplicityMatch.courtName}" /></div></div>
              <div class="col-6"><div class="small">Materia</div><div class="fw-bold"><c:out value="${duplicityMatch.matter}" /></div></div>
              <div class="col-6"><div class="small">Instancia</div><div class="fw-bold"><c:out value="${duplicityMatch.instance}" /></div></div>
              <div class="col-6"><div class="small">Coincidencia</div><div class="fw-bold"><c:out value="${duplicityMatch.matchText}" /></div></div>
            </div>
          </div>
          <div class="alert alert-mor-info d-flex gap-2 align-items-start mb-0">
            <i class="bi bi-info-circle mt-1"></i>
            <span>Si continúa, podrá capturar los datos complementarios y anexos. Después podrá calificar la
              solicitud como no procedente por duplicidad.</span>
          </div>
        </div>
        <div class="modal-footer border-0 d-grid gap-3" style="grid-template-columns: 1fr 1fr;">
          <button type="submit" class="btn btn-outline-primary m-0" form="duplicityForm" name="action" value="cancel">
            Cancelar registro
          </button>
          <button type="submit" class="btn btn-primary m-0" form="duplicityForm" name="action" value="continue">
            Continuar con el registro
          </button>
        </div>
      </div>
    </div>
  </div>
</c:if>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
