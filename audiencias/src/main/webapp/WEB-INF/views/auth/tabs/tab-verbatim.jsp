<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<section class="tab-panel" id="panel-verbatim" role="tabpanel" aria-labelledby="tab-verbatim" hidden>
  <div class="summary-layout">
    <div class="primary-column">
      <section class="panel">
        <div class="panel-title documents-heading">
          <span>Editor Verbatim</span>
          <span style="font-size:13px; color:var(--muted); font-weight:normal;">Borrador · versión 3</span>
        </div>
        <div style="padding: 12px 20px; display: flex; gap: 8px; border-bottom: 1px solid var(--line); background:#fcfcfc;">
          <button class="page-button" style="height:32px; min-width:32px; font-weight:bold;">B</button>
          <button class="page-button" style="height:32px; min-width:32px; font-style:italic;">I</button>
          <button class="page-button" style="height:32px; min-width:32px; text-decoration:underline;">U</button>
          <button class="page-button" style="height:32px; padding:0 12px;">Lista</button>
          <button class="page-button" style="height:32px; padding:0 12px;">Alinear</button>
          <div style="flex-grow:1;"></div>
          <button class="page-button" style="height:32px; padding:0 12px;">Deshacer</button>
          <button class="page-button" style="height:32px; padding:0 12px;">Rehacer</button>
        </div>
        <div style="background: #e9ece6; padding: 30px; min-height: 400px; display: flex; justify-content: center;">
          <div style="background: #fff; width: 100%; max-width: 600px; padding: 40px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); border-radius: 4px;">
            <h2 style="text-align:center; font-family:Georgia, serif; margin-bottom: 5px;">Dictamen</h2>
            <p style="text-align:center; font-size:12px; color:var(--muted); margin-top:0; margin-bottom:30px;">PEN-2025-0891<br>Paloma vs Alejandro · Juzgado de Zapata</p>
            <h3 style="font-size:13px; font-weight:700; margin-bottom:10px;">RESULTANDO</h3>
            <div style="height:6px; background:#e0e0e0; border-radius:3px; margin-bottom:8px;"></div>
            <div style="height:6px; background:#e0e0e0; border-radius:3px; margin-bottom:8px; width:90%;"></div>
            <h3 style="font-size:13px; font-weight:700; margin-top:24px; margin-bottom:10px;">CONSIDERANDO</h3>
            <div style="height:6px; background:#e0e0e0; border-radius:3px; margin-bottom:8px;"></div>
            <div style="height:6px; background:#e0e0e0; border-radius:3px; margin-bottom:8px; width:85%;"></div>
            <div style="height:6px; background:#e0e0e0; border-radius:3px; margin-bottom:8px; width:95%;"></div>
          </div>
        </div>
        <div style="padding: 16px 20px; display:flex; justify-content:space-between; align-items:center; border-top:1px solid var(--line);">
          <span style="font-size:12px; color:var(--muted);">Guardado automáticamente hoy a las 04:35 p.m.</span>
          <div style="display:flex; gap:12px;">
            <button type="button" class="page-button" style="font-weight:600; padding:0 20px;">Guardar borrador</button>
            <button type="button" class="btn-save">Imprimir/Descargar</button>
          </div>
        </div>
      </section>
    </div>

    <aside class="panel">
      <h2 class="panel-title">Evidencias</h2>
      <div style="padding: 20px;">
        <div class="form-group" style="margin-bottom: 16px;">
          <label style="display:block; font-size:13px; font-weight:600; margin-bottom:6px;">Nombre / Descripción de anexo</label>
          <input type="text" class="search-field" style="width:100%;">
        </div>
        <div class="form-group" style="margin-bottom: 24px;">
          <label style="display:block; font-size:13px; font-weight:600; margin-bottom:6px;">Tipo de evidencia</label>
          <input type="text" class="search-field" style="width:100%;">
        </div>
        <div class="form-group">
          <label style="display:block; font-size:13px; font-weight:600; margin-bottom:6px;">Archivos de evidencia</label>
          <p style="font-size:12px; color:var(--muted); margin-top:0; margin-bottom:12px;">Puedes seleccionar uno o varios archivos relacionados</p>
          <div class="dropzone">
            <input type="file" class="file-input" accept=".pdf,.png,.jpg,.jpeg" multiple>
            <div class="dropzone-content">
              <svg viewBox="0 0 24 24" width="24" height="24" stroke="#728bae" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="17 8 12 3 7 8"></polyline><line x1="12" y1="3" x2="12" y2="15"></line></svg>
              <div class="dropzone-text" style="font-size:12px;">
                <strong>Arrastra tus evidencias aquí</strong><br>
                <span style="font-size:10px;">o selecciona desde tu equipo PDF, PNG, JPG, JPEG</span>
              </div>
            </div>
          </div>
        </div>
        <div style="display:flex; justify-content:flex-end; gap:12px; margin-top:30px;">
          <button type="button" class="btn-cancel">Cancelar</button>
          <button type="button" class="btn-save">Adjuntar al expediente</button>
        </div>
      </div>
    </aside>
  </div>
</section>
