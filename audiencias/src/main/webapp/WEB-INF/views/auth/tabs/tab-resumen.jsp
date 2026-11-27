<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<section class="tab-panel" id="panel-resumen" role="tabpanel" aria-labelledby="tab-resumen">
  <div class="summary-layout">
    <div class="primary-column">
      <section class="panel" aria-labelledby="general-title">
        <h2 class="panel-title" id="general-title">Datos generales</h2>
        <div class="general-content">
          <dl class="data-grid">
            <div class="data-item"><dt>Expediente</dt><dd id="detailExpediente">PEN-2025-0891</dd></div>
            <div class="data-item"><dt>Materia</dt><dd id="detailMateria">Familiar</dd></div>
            <div class="data-item"><dt>Fecha de recepción</dt><dd id="detailRecepcion">02/10/2026</dd></div>
            <div class="data-item"><dt>Juzgado de origen</dt><dd id="detailJuzgado">Juzgado de Zapata</dd></div>
            <div class="data-item"><dt>Mesa</dt><dd id="detailMesa">M-06</dd></div>
            <div class="data-item"><dt>Fecha de audiencia</dt><dd id="detailFecha">26/10/2026</dd></div>
          </dl>
        </div>
      </section>

      <section class="panel" aria-labelledby="documents-title">
        <div class="panel-title documents-heading">
          <span id="documents-title">Documentos del expediente</span>
          <button class="all-files-link" id="viewAllFiles" type="button">
            Ver expediente completo
            <svg viewBox="0 0 16 16" fill="none" aria-hidden="true"><path d="M2 8h12m-4-4 4 4-4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </button>
        </div>
        <div class="document-list" id="documentList">
          <article class="document-row">
            <svg class="document-icon" viewBox="0 0 16 18" fill="none" aria-hidden="true"><path d="M3 1.5h6l4 4v11H3v-15z" stroke="currentColor" stroke-width="1.2"/><path d="M9 1.5v4h4M5 9h6M5 12h6" stroke="currentColor" stroke-width="1.1" stroke-linecap="round"/></svg>
            <div class="document-info"><div class="document-name">Auto de admisión de la apelación</div><div class="document-meta">PDF · 2 páginas · 05/10/2026</div></div>
            <button class="open-document" type="button" data-document="Auto de admisión de la apelación">Abrir</button>
          </article>
          <article class="document-row">
            <svg class="document-icon" viewBox="0 0 16 18" fill="none" aria-hidden="true"><path d="M3 1.5h6l4 4v11H3v-15z" stroke="currentColor" stroke-width="1.2"/><path d="M9 1.5v4h4M5 9h6M5 12h6" stroke="currentColor" stroke-width="1.1" stroke-linecap="round"/></svg>
            <div class="document-info"><div class="document-name">Expresión de agravios</div><div class="document-meta">PDF · 9 páginas · 22/09/2026</div></div>
            <button class="open-document" type="button" data-document="Expresión de agravios">Abrir</button>
          </article>
          <article class="document-row">
            <svg class="document-icon" viewBox="0 0 16 18" fill="none" aria-hidden="true"><path d="M3 1.5h6l4 4v11H3v-15z" stroke="currentColor" stroke-width="1.2"/><path d="M9 1.5v4h4M5 9h6M5 12h6" stroke="currentColor" stroke-width="1.1" stroke-linecap="round"/></svg>
            <div class="document-info"><div class="document-name">Sentencia recurrida</div><div class="document-meta">PDF · 14 páginas · 28/08/2026</div></div>
            <button class="open-document" type="button" data-document="Sentencia recurrida">Abrir</button>
          </article>
        </div>
        <div class="documents-note">Mostrando 3 de 7 actuaciones. El expediente completo incluye todas, desde la primera instancia.</div>
      </section>
    </div>

    <aside class="panel timeline-panel" aria-labelledby="timeline-title">
      <h2 class="panel-title" id="timeline-title">Seguimiento</h2>
      <ol class="timeline">
        <li class="timeline-item is-complete">
          <span class="timeline-marker" aria-hidden="true"></span>
          <div class="timeline-title">Audiencia recibida</div>
          <div class="timeline-date">02/10/2026</div>
        </li>
        <li class="timeline-item is-complete">
          <span class="timeline-marker" aria-hidden="true"></span>
          <div class="timeline-title">Audiencia agendada</div>
          <div class="timeline-date">05/10/2026</div>
        </li>
        <li class="timeline-item is-current">
          <span class="timeline-marker" aria-hidden="true"></span>
          <div class="timeline-title">Audiencia en curso</div>
          <div class="timeline-date" id="timelineNextDate">26/10/2026</div>
          <span class="timeline-tag">Próxima</span>
        </li>
        <li class="timeline-item">
          <span class="timeline-marker" aria-hidden="true"></span>
          <div class="timeline-title">Resolución</div>
          <div class="timeline-date">Pendiente</div>
        </li>
      </ol>
    </aside>
  </div>
</section>
