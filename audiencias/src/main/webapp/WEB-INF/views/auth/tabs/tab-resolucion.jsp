<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<section class="tab-panel" id="panel-resolucion" role="tabpanel" aria-labelledby="tab-resolucion" hidden>
  <div class="summary-layout">
    <div class="primary-column">
      <div style="margin-bottom: 24px; background: #e9ece6; padding: 20px 24px; border-radius: 8px;">
        <h2 style="font-family: Georgia, serif; font-size: 22px; margin: 0 0 6px 0; font-weight: normal;">Resolución final del expediente</h2>
        <p style="margin: 0; font-size: 14px; color: var(--muted);">Revisa el resultado registrado en la(s) audiencia(s) e integral el documento final cuando corresponda</p>
      </div>

      <section class="panel">
        <div style="padding: 24px;">
          <h3 style="font-family: Georgia, serif; font-size: 20px; font-weight: normal; margin: 0 0 4px 0;">Resultado registrado en Verbatim</h3>
          <p style="margin: 0 0 20px 0; font-size: 13px; color: var(--muted);">Esta información es de consulta y no vuelve a capturarse en esta sección</p>

          <div style="border: 1px solid var(--line); border-radius: 8px; overflow: hidden; background: #fdfdfc;">
            <div style="display:flex; border-bottom: 1px solid var(--line);">
              <div style="flex:1; padding:16px; border-right: 1px solid var(--line);">
                <span style="display:block; font-size:12px; color:var(--muted); margin-bottom:4px;">Resultado de la audiencia</span>
                <strong style="font-size:15px;">Acuerdo total</strong>
              </div>
              <div style="flex:1; padding:16px;">
                <span style="display:block; font-size:12px; color:var(--muted); margin-bottom:4px;">Estado resultante</span>
                <span style="display:inline-block; padding:4px 10px; background:#e9f5ec; color:#2e7d32; border-radius:12px; font-size:12px; font-weight:600;">● Resuelto</span>
              </div>
            </div>
            <div style="display:flex;">
              <div style="flex:1; padding:16px; border-right: 1px solid var(--line);">
                <span style="display:block; font-size:12px; color:var(--muted); margin-bottom:4px;">Motivo del término</span>
                <p style="margin:0; font-size:13px; line-height:1.4;">Las partes alcanzaron un convenio definitivo respecto de la totalidad de las prestaciones reclamadas</p>
              </div>
              <div style="flex:1; padding:16px;">
                <span style="display:block; font-size:12px; color:var(--muted); margin-bottom:4px;">Registrado</span>
                <strong style="font-size:14px;">10 jun 2025 - 10:12h</strong>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>

    <aside class="panel" style="background: #e9ece6;">
      <div class="panel-title" style="flex-direction:column; align-items:flex-start; justify-content:center; padding: 16px 20px; background: transparent; border-bottom: none;">
        <span style="display:block;">Expediente final consolado</span>
        <span style="font-size:13px; color:var(--muted); font-family:Inter, sans-serif; font-weight:normal; margin-top:4px;">Adjunta la versión íntegra que quedará resguardada como documento de cierre.</span>
      </div>
      <div style="padding: 0 20px 24px 20px;">
        <div class="dropzone" style="background: #fff; margin-bottom: 12px; padding: 40px 20px;">
          <input type="file" class="file-input" accept=".pdf">
          <div class="dropzone-content">
            <svg viewBox="0 0 24 24" width="28" height="28" stroke="#728bae" stroke-width="2" fill="none"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="17 8 12 3 7 8"></polyline><line x1="12" y1="3" x2="12" y2="15"></line></svg>
            <div class="dropzone-text" style="font-size:13px;">
              <strong style="color:var(--ink);">Arrastra tus evidencias aquí</strong><br>
              <span style="font-size:11px;">o selecciona desde tu equipo PDF</span>
            </div>
          </div>
        </div>
        <p style="font-size:11px; color:var(--muted); margin:0 0 20px 0;">Solo PDF - Máximo 20MB</p>

        <div style="display:flex; justify-content:space-between; align-items:center;">
          <button type="button" class="btn-cancel" style="font-weight:600; font-size:13px; color:var(--ink);">Guardar borrador</button>
          <button type="button" class="btn-save">Cerrar expediente</button>
        </div>
      </div>
    </aside>
  </div>
</section>
