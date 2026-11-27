<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<section class="tab-panel" id="panel-acta" role="tabpanel" aria-labelledby="tab-acta" hidden>
  <div class="summary-layout">
    <div class="primary-column">
      <section class="panel" style="margin-bottom: 24px;">
        <div class="panel-title" style="flex-direction:column; align-items:flex-start; justify-content:center; padding: 14px 20px;">
          <span style="display:block;">Acta firmada</span>
          <span style="font-size:13px; color:var(--muted); font-family:Inter, sans-serif; font-weight:normal; margin-top:4px;">Adjunta la digitalización íntegra del documento firmado</span>
        </div>
        <div style="padding: 24px;">
          <div class="dropzone" style="margin-bottom: 12px;">
            <input type="file" class="file-input" accept=".pdf">
            <div class="dropzone-content">
              <svg viewBox="0 0 24 24" width="24" height="24" stroke="#728bae" stroke-width="2" fill="none"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="17 8 12 3 7 8"></polyline><line x1="12" y1="3" x2="12" y2="15"></line></svg>
              <div class="dropzone-text" style="font-size:13px;">
                <strong>Arrastra tus evidencias aquí</strong><br>
                <span style="font-size:11px;">o selecciona desde tu equipo PDF</span>
              </div>
            </div>
          </div>
          <p style="font-size:12px; color:var(--muted); margin:0 0 20px 0;">Solo se admite formato PDF. Tamaño máximo permitido 20MB.</p>
          <div style="display:flex; justify-content:flex-end; gap:12px;">
            <button type="button" class="page-button" style="font-weight:600; padding:0 20px;">Guardar borrador</button>
            <button type="button" class="btn-save">Vincular acta</button>
          </div>
        </div>
      </section>

      <section class="panel">
        <div class="panel-title" style="flex-direction:column; align-items:flex-start; justify-content:center; padding: 14px 20px;">
          <span style="display:block;">Resultado y término de la audiencia</span>
          <span style="font-size:13px; color:var(--muted); font-family:Inter, sans-serif; font-weight:normal; margin-top:4px;">Registre cómo terminó la sesión para definir el siguiente paso del expediente</span>
        </div>
        <div style="padding: 24px;">
          <div class="radio-cards-container" style="display:grid; grid-template-columns: 1fr 1fr; gap:16px; margin-bottom: 24px;">
            <label class="radio-card">
              <input type="radio" name="resultadoAudiencia" value="total">
              <span class="radio-content">
                <span class="radio-title">Acuerdo total</span>
                <span class="radio-desc">El asunto queda listo para concluir</span>
              </span>
            </label>
            <label class="radio-card">
              <input type="radio" name="resultadoAudiencia" value="parcial">
              <span class="radio-content">
                <span class="radio-title">Acuerdo parcial</span>
                <span class="radio-desc">Quedan puntos pendientes</span>
              </span>
            </label>
            <label class="radio-card">
              <input type="radio" name="resultadoAudiencia" value="sin_acuerdo">
              <span class="radio-content">
                <span class="radio-title">Sin acuerdo</span>
                <span class="radio-desc">Las partes no alcanzaron convenio</span>
              </span>
            </label>
            <label class="radio-card">
              <input type="radio" name="resultadoAudiencia" value="inconclusa">
              <span class="radio-content">
                <span class="radio-title">Audiencia inconclusa</span>
                <span class="radio-desc">Se requiere continuar la sesión</span>
              </span>
            </label>
          </div>
          <div class="form-group" style="margin-bottom: 24px;">
            <label style="display:block; font-size:14px; font-weight:700; margin-bottom:8px;">Descripción del término</label>
            <textarea rows="4" style="width:100%; border:1px solid var(--line); border-radius:6px; padding:12px; font-family:inherit; font-size:13px;" placeholder="Describe las circunstancias y los puntos que quedaron resueltos o pendientes."></textarea>
          </div>
          <div style="display:flex; justify-content:flex-end;">
            <button type="button" class="btn-save">Guardar</button>
          </div>
        </div>
      </section>
    </div>

    <aside class="panel">
      <div class="panel-title" style="flex-direction:column; align-items:flex-start; justify-content:center; padding: 14px 20px;">
        <span style="display:block;">Solicitud de nueva audiencia</span>
        <span style="font-size:12px; color:var(--muted); font-family:Inter, sans-serif; font-weight:normal; margin-top:4px;">Propón una fecha, la asignación final dependerá de la disponibilidad de la mesa</span>
      </div>
      <div style="padding: 20px;">
        <div class="form-group" style="margin-bottom: 16px;">
          <label style="display:block; font-size:13px; font-weight:700; margin-bottom:6px;">Expediente</label>
          <input type="text" class="search-field" style="width:100%;" value="PEN-2025-0891" readonly>
        </div>
        <div class="form-group" style="margin-bottom: 16px;">
          <label style="display:block; font-size:13px; font-weight:700; margin-bottom:6px;">Tipo de audiencia</label>
          <select class="search-field" style="width:100%;">
            <option>Continuación de audiencia inicial</option>
            <option>Audiencia intermedia</option>
          </select>
        </div>
        <div class="form-group" style="margin-bottom: 16px;">
          <label style="display:block; font-size:13px; font-weight:700; margin-bottom:6px;">Fecha</label>
          <input type="date" class="search-field" style="width:100%;">
        </div>
        <div class="form-group" style="margin-bottom: 24px;">
          <label style="display:block; font-size:13px; font-weight:700; margin-bottom:6px;">Hora</label>
          <input type="time" class="search-field" style="width:100%;">
        </div>
        <div style="background:#eef4fb; padding:16px; border-radius:6px; margin-bottom:24px; font-size:12px; color:#1e3a5f;">
          Al enviar, la solicitud aparecerá en <strong>Agendamiento - Por reagendar</strong> con la referencia al expediente.
        </div>
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <button type="button" class="btn-cancel">Cancelar</button>
          <button type="button" class="btn-save">Reagendar</button>
        </div>
      </div>
    </aside>
  </div>
</section>
