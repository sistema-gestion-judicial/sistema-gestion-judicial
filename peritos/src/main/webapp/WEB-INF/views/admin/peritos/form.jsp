<%-- Admin 2 · Registrar o editar perito: personal data, subjects, certification and documents. --%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Registrar perito" />
<c:set var="navRole" value="admin" />
<c:set var="activeNav" value="peritos" />
<%@ include file="/WEB-INF/views/layout/head.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-start.jspf" %>
<c:set var="pageHeading" value="${empty perito ? 'Registrar perito' : 'Editar perito'}" />
<c:set var="pageDescription" value="Llene los datos del perito. Los campos marcados con * son obligatorios." />
<%@ include file="/WEB-INF/views/layout/page-header.jspf" %>

<a class="d-inline-block mt-3 fw-semibold" href="${ctx}/admin/peritos"><i class="bi bi-arrow-left"></i> Volver a Peritos</a>

<form method="post" action="${ctx}/admin/peritos" enctype="multipart/form-data" class="mt-3">
  <input type="hidden" name="id" value="${perito.id}">

  <section class="card-mor p-4 mb-4">
    <h2 class="page-section-title mb-3">Datos generales</h2>
    <div class="row g-3">
      <div class="col-md-4">
        <label class="form-label" for="nombres">Nombre(s) *</label>
        <input id="nombres" name="nombres" class="form-control" placeholder="Ej. Ana" value="${perito.firstName}" required>
      </div>
      <div class="col-md-4">
        <label class="form-label" for="apellidos">Apellidos *</label>
        <input id="apellidos" name="apellidos" class="form-control" placeholder="Ej. López Ruiz" value="${perito.lastName}" required>
      </div>
      <div class="col-md-4">
        <label class="form-label" for="correo">Correo electrónico *</label>
        <input id="correo" type="email" name="correo" class="form-control" placeholder="correo@ejemplo.com" value="${perito.email}" required>
      </div>
      <div class="col-md-4">
        <label class="form-label" for="telefono">Teléfono</label>
        <input id="telefono" type="tel" name="telefono" class="form-control" placeholder="777 000 0000" value="${perito.phone}">
      </div>
      <div class="col-md-4">
        <label class="form-label" for="cedula">Cédula profesional *</label>
        <input id="cedula" name="cedula" class="form-control" placeholder="Número de cédula" value="${perito.professionalLicense}" required>
      </div>
      <div class="col-md-4">
        <label class="form-label" for="especialidad">Especialidad *</label>
        <select id="especialidad" name="especialidad" class="form-select" required>
          <option value="">Ej. Contabilidad</option>
          <c:forEach var="especialidad" items="${especialidades}">
            <option value="${especialidad.id}" ${perito.specialtyId == especialidad.id ? 'selected' : ''}><c:out value="${especialidad.name}" /></option>
          </c:forEach>
        </select>
      </div>
    </div>

    <h2 class="page-section-title mt-4 mb-3">Materias y certificación</h2>
    <div class="row g-3">
      <div class="col-md-3">
        <label class="form-label" for="materias">Materias que atiende *</label>
        <select id="materias" name="materias" class="form-select" multiple required>
          <c:forEach var="materia" items="${materias}">
            <option value="${materia.id}"><c:out value="${materia.name}" /></option>
          </c:forEach>
        </select>
      </div>
      <div class="col-md-3">
        <label class="form-label" for="certificacion">Certificación</label>
        <select id="certificacion" name="certificacion" class="form-select">
          <option value="VIGENTE">Vigente</option>
          <option value="EN_REVISION">En revisión</option>
          <option value="VENCIDA">Vencida</option>
        </select>
      </div>
      <div class="col-md-3">
        <label class="form-label" for="vigencia">Vigencia de certificación</label>
        <input id="vigencia" type="date" name="vigencia" class="form-control" value="${perito.certificationExpiresOn}">
      </div>
      <div class="col-md-3">
        <label class="form-label" for="estado">Estado del perito</label>
        <select id="estado" name="estado" class="form-select">
          <option value="ACTIVO" ${perito.active ? 'selected' : ''}>Activo</option>
          <option value="INACTIVO" ${perito.active ? '' : 'selected'}>Inactivo</option>
        </select>
      </div>
    </div>
  </section>

  <section class="card-mor p-4 mb-4">
    <h2 class="page-section-title mb-1">Documentos</h2>
    <p class="text-muted mb-3">Adjunte la cédula y la certificación en PDF.</p>
    <label class="upload-dropzone w-100 text-center p-5 rounded-3 border border-2 border-dashed">
      <i class="bi bi-upload fs-3 d-block mb-2"></i>
      <span class="fw-semibold d-block">Arrastre o seleccione archivos</span>
      <span class="small text-muted">PDF · máximo 10 MB</span>
      <input type="file" name="documentos" accept="application/pdf" multiple class="visually-hidden">
    </label>
  </section>

  <div class="d-flex justify-content-end gap-3">
    <a class="btn btn-outline-secondary" href="${ctx}/admin/peritos">Cancelar</a>
    <button type="submit" class="btn btn-primary">Guardar perito</button>
  </div>
</form>

<%@ include file="/WEB-INF/views/layout/app-end.jspf" %>
