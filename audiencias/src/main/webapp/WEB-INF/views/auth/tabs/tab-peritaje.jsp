<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<section class="tab-panel" id="panel-peritaje" role="tabpanel" aria-labelledby="tab-peritaje" hidden>
  <div class="summary-layout">
    <div class="primary-column">
      <section class="panel agenda">
        <div class="agenda-heading" style="display:flex; justify-content:space-between; align-items:center;">
          <h2 class="panel-title" style="border:none; padding:0; background:transparent;">Peritaje de la audiencia</h2>
          <button class="btn-save" style="padding: 8px 16px;" onclick="document.getElementById('peritajeModal').showModal()">Solicitar peritaje</button>
        </div>
        <div class="table-scroll">
          <table style="width: 100%; text-align:left;">
            <thead>
            <tr>
              <th>Fecha</th>
              <th>Expediente peritaje</th>
              <th>Materia</th>
              <th>Estado</th>
              <th style="text-align:right;">Acciones</th>
            </tr>
            </thead>
            <tbody>
            <tr>
              <td><strong style="display:block;">05/10/2026</strong><span style="font-size:12px; color:var(--muted);">09:00 a.m.</span></td>
              <td>Ricardo Salgado</td>
              <td>Familiar</td>
              <td><span style="display:inline-block; padding:4px 10px; background:#e9f5ec; color:#2e7d32; border-radius:12px; font-size:12px; font-weight:600;">● En espera</span></td>
              <td style="text-align:right;">
                <button type="button" class="view-button">
                  <svg viewBox="0 0 20 20" fill="none" style="width:16px; margin-right:4px;"><circle cx="10" cy="10" r="3" stroke="currentColor" stroke-width="1.5"/><path d="M2.5 10c2.5-5 12.5-5 15 0-2.5 5-12.5 5-15 0z" stroke="currentColor" stroke-width="1.5"/></svg>
                  Ver
                </button>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
        <div style="padding: 20px; font-size:12px; color:var(--muted);">Mostrando 1 de 1 peritajes de este toca.</div>
      </section>
    </div>

    <aside class="panel">
      <h2 class="panel-title">Detalle de audiencia</h2>
      <div style="padding: 24px;">
        <div style="display:flex; gap:16px; align-items:center; margin-bottom: 24px;">
          <div style="background:#e9ece6; padding:12px; border-radius:8px; text-align:center; min-width:60px;">
            <span style="display:block; font-size:24px; font-weight:700; font-family:Georgia, serif; line-height:1;">26</span>
            <span style="display:block; font-size:11px; font-weight:700; text-transform:uppercase; margin-top:4px;">OCT</span>
          </div>
          <div>
            <strong style="display:block; font-size:14px; margin-bottom:4px;">PEN-2025-0891</strong>
            <span style="font-size:13px; color:var(--muted);">26/10/2026 · Próxima</span>
          </div>
        </div>
        <div style="display:flex; flex-direction:column; gap:16px; margin-bottom: 30px;">
          <div><span style="display:block; font-size:12px; color:var(--muted); margin-bottom:2px;">Hora</span><strong style="font-size:14px;">10:00 a.m.</strong></div>
          <div><span style="display:block; font-size:12px; color:var(--muted); margin-bottom:2px;">Materia</span><strong style="font-size:14px;">Familiar</strong></div>
          <div><span style="display:block; font-size:12px; color:var(--muted); margin-bottom:2px;">Mesa</span><strong style="font-size:14px;">M-06</strong></div>
        </div>
        <button type="button" class="page-button" style="width:100%; font-weight:600; padding:10px 0;">Ver agenda</button>
      </div>
    </aside>
  </div>
</section>

<dialog id="peritajeModal" class="custom-modal" aria-labelledby="peritajeModalTitle">
  <div class="modal-header">
    <h2 id="peritajeModalTitle">Solicitar Peritaje</h2>
    <p>Completa la información requerida para realizar la solicitud</p>
  </div>
  <form method="dialog" id="peritajeForm">
    <div class="modal-body">
      <div style="display:flex; gap:16px;">
        <div class="form-group" style="flex:1;">
          <label style="display:block; font-size:13px; font-weight:700; margin-bottom:6px;">Número de expediente</label>
          <input type="text" class="search-field" style="width:100%;" value="PEN-2025-0891" readonly>
        </div>
        <div class="form-group" style="flex:1;">
          <label style="display:block; font-size:13px; font-weight:700; margin-bottom:6px;">Especialidad requerida</label>
          <select class="search-field" style="width:100%;">
            <option>Seleccione una especialidad</option>
            <option>Psicología</option>
            <option>Grafoscopía</option>
          </select>
        </div>
      </div>
      <div class="form-group">
        <label style="display:block; font-size:13px; font-weight:700; margin-bottom:6px;">Razón o justificación del peritaje</label>
        <textarea rows="4" style="width:100%; border:1px solid var(--line); border-radius:6px; padding:12px; font-family:inherit; font-size:13px;" placeholder="Indica los hechos, documentos o elementos que deberán ser examinados..."></textarea>
        <span style="display:block; font-size:12px; color:var(--muted); margin-top:4px;">Describe con precisión el objeto del análisis pericial</span>
      </div>
      <div style="background:#eef4fb; padding:16px; border-radius:6px; font-size:13px; color:#1e3a5f;">
        El perito asignado puede ver toda la documentación creada para la audiencia.
      </div>
    </div>
    <div class="modal-footer">
      <button type="button" class="btn-cancel" onclick="document.getElementById('peritajeModal').close()">Cancelar</button>
      <button type="submit" class="btn-save">Solicitar</button>
    </div>
  </form>
</dialog>
