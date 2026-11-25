<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="theme-color" content="#e9e9e2">
  <title>SGA Audiencias | Notificaciones</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/abogados.css?v=<%= System.currentTimeMillis() %>">
</head>
<body>
<div class="page">

  <header class="topbar">
    <a class="brand" href="${pageContext.request.contextPath}/inicioabogados.jsp" aria-label="SGA Audiencias, inicio">
      <span class="logo">Logo</span>
      <span>
        <span class="brand-name">SGA</span>
        <span class="brand-subtitle" style="display:block">Audiencias</span>
      </span>
    </a>
    <nav class="main-nav" aria-label="Navegación principal">
      <a class="nav-link" href="${pageContext.request.contextPath}/inicioabogados.jsp">Inicio</a>
      <a class="nav-link" href="${pageContext.request.contextPath}/agendamientoAbogados.jsp">Agendamiento</a>
      <a class="nav-link" href="${pageContext.request.contextPath}/inicioAudienciasAbogados.jsp">Audiencias</a>
      <a class="nav-link" href="#" aria-current="page">Notificaciones</a>
    </nav>
    <div class="profile-wrap">
      <button class="profile" id="profileButton" aria-expanded="false" type="button">
        <span class="avatar" aria-hidden="true">
          <svg viewBox="0 0 36 36" fill="none"><circle cx="18" cy="11" r="7" stroke="currentColor" stroke-width="2"/><path d="M5.5 32c.9-7.3 5.2-11 12.5-11s11.6 3.7 12.5 11" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
        </span>
        <span>
          <span class="profile-name">Ricardo Salgado</span>
          <span class="profile-role" style="display:block">Abogado de audiencias</span>
        </span>
        <svg class="profile-chevron" viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="M5 7.5L10 12.5L15 7.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </button>
      <span class="profile-menu" id="profileMenu" role="menu">
        <button type="button" role="menuitem">Mi perfil</button>
        <button type="button" role="menuitem">Cerrar sesión</button>
      </span>
    </div>
  </header>

  <main>
    <section class="case-heading" style="align-items: center;">
      <div class="heading-title">
        <div class="heading-rule"></div>
        <h1>Notificaciones</h1>
        <p class="intro" style="margin-bottom: 0;">Avisos sobre sus tocas, ponencias y expedientes recibidos.</p>
      </div>
      <div class="heading-actions" style="margin-top: 0;">
        <button class="page-button" type="button" style="font-weight: 600; padding: 0 20px; height: 42px;">
          Marcar todas como leídas
        </button>
      </div>
    </section>

    <nav class="tabs" role="tablist" aria-label="Filtro de notificaciones" style="margin-bottom: 20px; border-bottom: none;">
      <button class="tab-button" type="button" role="tab" aria-selected="true">Todas (6)</button>
      <button class="tab-button" type="button" role="tab" aria-selected="false">Sin leer (2)</button>
      <button class="tab-button" type="button" role="tab" aria-selected="false">Leídas (4)</button>
    </nav>

    <section class="panel" style="padding: 16px 24px;">

      <article style="display: flex; gap: 16px; align-items: center; padding: 16px 20px; background: #e9ece6; border-radius: 8px; margin-bottom: 12px;">
        <svg viewBox="0 0 24 24" fill="none" style="width: 24px; height: 24px; color: var(--muted); flex-shrink: 0;"><rect x="3" y="4" width="18" height="18" rx="2" ry="2" stroke="currentColor" stroke-width="1.5"/><line x1="16" y1="2" x2="16" y2="6" stroke="currentColor" stroke-width="1.5"/><line x1="8" y1="2" x2="8" y2="6" stroke="currentColor" stroke-width="1.5"/><line x1="3" y1="10" x2="21" y2="10" stroke="currentColor" stroke-width="1.5"/></svg>
        <div style="flex: 1; min-width: 0;">
          <h3 style="margin: 0; font-size: 14px; font-weight: 700; color: var(--ink);">Audiencia agendada</h3>
          <p style="margin: 4px 0 0; font-size: 13px; color: var(--muted);">La audiencia del expediente PEN-2025-0891 fue agendada para el 27/10/2026.</p>
          <span style="display: block; margin-top: 4px; font-size: 12px; color: var(--muted);">24/10/2026 · 08:30</span>
        </div>
        <div style="display: flex; align-items: center; gap: 16px;">
          <span style="color: #2563eb; font-size: 13px; font-weight: 700; display: flex; align-items: center; gap: 6px;">
            <span style="font-size: 18px;">&bull;</span> Nueva
          </span>
          <button class="page-button" type="button" style="font-weight: 600; padding: 0 16px; height: 36px;">Ver Audiencia</button>
        </div>
      </article>

      <article style="display: flex; gap: 16px; align-items: center; padding: 16px 20px; background: #e9ece6; border-radius: 8px; margin-bottom: 12px;">
        <svg viewBox="0 0 24 24" fill="none" style="width: 24px; height: 24px; color: var(--muted); flex-shrink: 0;"><rect x="3" y="4" width="18" height="18" rx="2" ry="2" stroke="currentColor" stroke-width="1.5"/><line x1="16" y1="2" x2="16" y2="6" stroke="currentColor" stroke-width="1.5"/><line x1="8" y1="2" x2="8" y2="6" stroke="currentColor" stroke-width="1.5"/><line x1="3" y1="10" x2="21" y2="10" stroke="currentColor" stroke-width="1.5"/></svg>
        <div style="flex: 1; min-width: 0;">
          <h3 style="margin: 0; font-size: 14px; font-weight: 700; color: var(--ink);">Audiencia próxima</h3>
          <p style="margin: 4px 0 0; font-size: 13px; color: var(--muted);">La audiencia del expediente PEN-2025-0891 es en 3 días (26/10/2026).</p>
          <span style="display: block; margin-top: 4px; font-size: 12px; color: var(--muted);">23/10/2026 · 12:10</span>
        </div>
        <div style="display: flex; align-items: center; gap: 16px;">
          <span style="color: #2563eb; font-size: 13px; font-weight: 700; display: flex; align-items: center; gap: 6px;">
            <span style="font-size: 18px;">&bull;</span> Nueva
          </span>
          <button class="page-button" type="button" style="font-weight: 600; padding: 0 16px; height: 36px;">Ver Audiencia</button>
        </div>
      </article>

      <article style="display: flex; gap: 16px; align-items: center; padding: 16px 20px; border-bottom: 1px solid var(--line);">
        <svg viewBox="0 0 24 24" fill="none" style="width: 24px; height: 24px; color: var(--muted); flex-shrink: 0;"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="1.5"/><polyline points="14 2 14 8 20 8" stroke="currentColor" stroke-width="1.5"/><line x1="12" y1="18" x2="12" y2="12" stroke="currentColor" stroke-width="1.5"/><line x1="9" y1="15" x2="15" y2="15" stroke="currentColor" stroke-width="1.5"/></svg>
        <div style="flex: 1; min-width: 0;">
          <h3 style="margin: 0; font-size: 14px; font-weight: 700; color: var(--ink);">Nueva audiencia a su mesa</h3>
          <p style="margin: 4px 0 0; font-size: 13px; color: var(--muted);">PEN-2025-0891</p>
          <span style="display: block; margin-top: 4px; font-size: 12px; color: var(--muted);">05/10/2026 · 09:24</span>
        </div>
        <div style="display: flex; align-items: center;">
          <button class="page-button" type="button" style="font-weight: 600; padding: 0 16px; height: 36px;">Ver Audiencia</button>
        </div>
      </article>

      <article style="display: flex; gap: 16px; align-items: center; padding: 16px 20px; border-bottom: 1px solid var(--line);">
        <svg viewBox="0 0 24 24" fill="none" style="width: 24px; height: 24px; color: var(--muted); flex-shrink: 0;"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" stroke="currentColor" stroke-width="1.5"/><polyline points="22,6 12,13 2,6" stroke="currentColor" stroke-width="1.5"/></svg>
        <div style="flex: 1; min-width: 0;">
          <h3 style="margin: 0; font-size: 14px; font-weight: 700; color: var(--ink);">Solución de peritaje recibido</h3>
          <p style="margin: 4px 0 0; font-size: 13px; color: var(--muted);">PEN-2025-0891 / PER-2025-1546-8794</p>
          <span style="display: block; margin-top: 4px; font-size: 12px; color: var(--muted);">05/10/2026 · 09:15</span>
        </div>
        <div style="display: flex; align-items: center;">
          <button class="page-button" type="button" style="font-weight: 600; padding: 0 16px; height: 36px;">Ver Audiencia</button>
        </div>
      </article>

      <article style="display: flex; gap: 16px; align-items: center; padding: 16px 20px; border-bottom: 1px solid var(--line);">
        <svg viewBox="0 0 24 24" fill="none" style="width: 24px; height: 24px; color: var(--muted); flex-shrink: 0;"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="1.5"/><polyline points="14 2 14 8 20 8" stroke="currentColor" stroke-width="1.5"/><line x1="12" y1="18" x2="12" y2="12" stroke="currentColor" stroke-width="1.5"/><line x1="9" y1="15" x2="15" y2="15" stroke="currentColor" stroke-width="1.5"/></svg>
        <div style="flex: 1; min-width: 0;">
          <h3 style="margin: 0; font-size: 14px; font-weight: 700; color: var(--ink);">Nueva audiencia asignada a su mesa</h3>
          <p style="margin: 4px 0 0; font-size: 13px; color: var(--muted);">Exp. FAM-1230-4561</p>
          <span style="display: block; margin-top: 4px; font-size: 12px; color: var(--muted);">30/09/2026 · 16:20</span>
        </div>
        <div style="display: flex; align-items: center;">
          <button class="page-button" type="button" style="font-weight: 600; padding: 0 16px; height: 36px;">Ver Audiencia</button>
        </div>
      </article>

      <article style="display: flex; gap: 16px; align-items: center; padding: 16px 20px;">
        <svg viewBox="0 0 24 24" fill="none" style="width: 24px; height: 24px; color: var(--muted); flex-shrink: 0;"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="1.5"/><polyline points="14 2 14 8 20 8" stroke="currentColor" stroke-width="1.5"/><line x1="12" y1="18" x2="12" y2="12" stroke="currentColor" stroke-width="1.5"/><line x1="9" y1="15" x2="15" y2="15" stroke="currentColor" stroke-width="1.5"/></svg>
        <div style="flex: 1; min-width: 0;">
          <h3 style="margin: 0; font-size: 14px; font-weight: 700; color: var(--ink);">Nueva audiencia asignada a su mesa</h3>
          <p style="margin: 4px 0 0; font-size: 13px; color: var(--muted);">Exp. PEN-2025-7894-1542</p>
          <span style="display: block; margin-top: 4px; font-size: 12px; color: var(--muted);">28/09/2026 · 10:05</span>
        </div>
        <div style="display: flex; align-items: center;">
          <button class="page-button" type="button" style="font-weight: 600; padding: 0 16px; height: 36px;">Ver Audiencia</button>
        </div>
      </article>

    </section>
  </main>
</div>

<script>
  document.addEventListener("DOMContentLoaded", () => {
    const profileButton = document.getElementById("profileButton");
    const profileMenu = document.getElementById("profileMenu");
    if (profileButton && profileMenu) {
      profileButton.addEventListener("click", () => {
        const expanded = profileButton.getAttribute("aria-expanded") === "true";
        profileButton.setAttribute("aria-expanded", String(!expanded));
        profileMenu.classList.toggle("is-open", !expanded);
      });
      document.addEventListener("click", (e) => {
        if (!profileButton.contains(e.target) && !profileMenu.contains(e.target)) {
          profileButton.setAttribute("aria-expanded", "false");
          profileMenu.classList.remove("is-open");
        }
      });
    }

    const tabs = document.querySelectorAll(".tab-button");
    tabs.forEach(tab => {
      tab.addEventListener("click", () => {
        tabs.forEach(t => t.setAttribute("aria-selected", "false"));
        tab.setAttribute("aria-selected", "true");
      });
    });
  });
</script>
</body>
</html>
