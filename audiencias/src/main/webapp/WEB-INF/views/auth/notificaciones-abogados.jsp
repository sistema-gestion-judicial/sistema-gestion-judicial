<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="theme-color" content="#e9e9e2">
  <title>SGA Audiencias | Notificaciones</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/abogados.css?v=<%= System.currentTimeMillis() %>">
</head>
<body>
<div class="page layout-figma">

  <!-- Barra Lateral Institucional -->
  <aside class="sidebar" aria-label="Menú lateral">
    <div>
      <a class="sidebar-brand" href="${pageContext.request.contextPath}/inicioabogados.jsp" aria-label="SGA Audiencias, inicio">
        <svg width="46" height="46" viewBox="0 0 48 48" fill="none" aria-hidden="true">
          <path d="M24 7v33M15 40h18M11 15h26" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
          <path d="M11 15 6 26h10l-5-11ZM37 15l-5 11h10l-5-11Z" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round"/>
          <path d="M6 26c0 2.8 2.2 4.5 5 4.5s5-1.7 5-4.5M32 26c0 2.8 2.2 4.5 5 4.5s5-1.7 5-4.5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
        </svg>
        <span class="sidebar-brand-title">Audiencias</span>
      </a>

      <nav class="sidebar-nav" aria-label="Navegación principal">
        <a href="${pageContext.request.contextPath}/inicioabogados.jsp" class="sidebar-link">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M3 10.5 12 3l9 7.5V20a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1v-9.5Z" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round"/>
          </svg>
          <span>Inicio</span>
        </a>

        <a href="${pageContext.request.contextPath}/agendamientoAbogados.jsp" class="sidebar-link">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <rect x="3" y="4" width="18" height="17" rx="2.5" stroke="currentColor" stroke-width="1.7"/>
            <path d="M8 2.5v3M16 2.5v3M3 9.5h18M8 14h3v3H8v-3Z" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>Agendamiento</span>
        </a>

        <a href="${pageContext.request.contextPath}/inicioAudienciasAbogados.jsp" class="sidebar-link">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <rect x="3" y="4" width="18" height="17" rx="2.5" stroke="currentColor" stroke-width="1.7"/>
            <path d="M8 2.5v3M16 2.5v3M3 9.5h18M8 14h8M8 17.5h5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
          </svg>
          <span>Audiencias</span>
        </a>

        <a href="#" class="sidebar-link active-link" aria-current="page">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M18 8a6 6 0 1 0-12 0c0 7-3 9-3 9h18s-3-2-3-9ZM13.73 21a2 2 0 0 1-3.46 0" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>Notificaciones</span>
        </a>
      </nav>
    </div>

    <div class="sidebar-footer">
      <a href="${pageContext.request.contextPath}/login.jsp" class="sidebar-logout">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span>Cerrar sesión</span>
      </a>
    </div>
  </aside>

  <!-- Columna Derecha: Topbar + Contenido -->
  <div class="content-shell">

    <!-- Topbar Flotante (sin flecha en perfil) -->
    <header class="topbar">
      <div class="topbar-left">
        <div class="topbar-brand-compact">
          <span class="brand-name">SGA</span>
          <span class="brand-subtitle">Audiencias</span>
        </div>

        <span class="topbar-divider" aria-hidden="true"></span>

        <nav class="breadcrumb-nav" aria-label="Ruta de navegación">
          <a href="${pageContext.request.contextPath}/inicioabogados.jsp">Inicio</a>
          <span aria-hidden="true">&rsaquo;</span>
          <span class="breadcrumb-current">Notificaciones</span>
        </nav>
      </div>

      <div class="topbar-right">
        <a href="#" class="notif-pill" aria-current="page">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M18 8a6 6 0 1 0-12 0c0 7-3 9-3 9h18s-3-2-3-9ZM13.73 21a2 2 0 0 1-3.46 0" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span>Notificaciones</span>
        </a>

        <div class="profile-wrap">
          <button class="profile" id="profileButton" aria-expanded="false" aria-haspopup="true" aria-controls="profileMenu" type="button">
            <span class="avatar" aria-hidden="true">RS</span>
            <span class="profile-text">
              <span class="profile-name">Ricardo Salgado</span>
              <span class="profile-role">Abogado de audiencias</span>
            </span>
          </button>
          <span class="profile-menu" id="profileMenu" role="menu">
            <button type="button" role="menuitem">Mi perfil</button>
            <button type="button" role="menuitem">Cerrar sesión</button>
          </span>
        </div>
      </div>
    </header>

    <main>
      <section class="case-heading hero-banner-justice" style="display: flex; justify-content: space-between; align-items: center; gap: 16px; flex-wrap: wrap;">
        <div class="heading-title">
          <div class="heading-rule"></div>
          <h1 id="page-title">Notificaciones</h1>
          <p class="intro" style="margin-bottom: 0;">Avisos sobre sus tocas, ponencias y expedientes recibidos.</p>
        </div>
        <div class="heading-actions" style="margin-top: 0;">
          <button class="page-button" type="button" style="font-weight: 600; padding: 0 20px; height: 42px;">
            Marcar todas como leídas
          </button>
        </div>
      </section>

      <nav class="tabs tabs-navigation" role="tablist" aria-label="Filtro de notificaciones" style="margin-top: 18px; margin-bottom: 20px; background: transparent; padding: 0;">
        <button class="tab-button" type="button" role="tab" aria-selected="true">Todas (6)</button>
        <button class="tab-button" type="button" role="tab" aria-selected="false">Sin leer (2)</button>
        <button class="tab-button" type="button" role="tab" aria-selected="false">Leídas (4)</button>
      </nav>

      <section class="panel notif-panel" style="padding: 18px 24px;">

        <article style="display: flex; gap: 16px; align-items: center; padding: 16px 20px; background: #e9ece6; border-radius: 10px; margin-bottom: 12px;">
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
            <button class="page-button" type="button" data-expediente="PEN-2025-0891" style="font-weight: 600; padding: 0 16px; height: 36px;">Ver Audiencia</button>
          </div>
        </article>

        <article style="display: flex; gap: 16px; align-items: center; padding: 16px 20px; background: #e9ece6; border-radius: 10px; margin-bottom: 12px;">
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
            <button class="page-button" type="button" data-expediente="PEN-2025-0891" style="font-weight: 600; padding: 0 16px; height: 36px;">Ver Audiencia</button>
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
            <button class="page-button" type="button" data-expediente="PEN-2025-0891" style="font-weight: 600; padding: 0 16px; height: 36px;">Ver Audiencia</button>
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
            <button class="page-button" type="button" data-expediente="PEN-2025-0891" style="font-weight: 600; padding: 0 16px; height: 36px;">Ver Audiencia</button>
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
            <button class="page-button" type="button" data-expediente="FAM-1230-4561" style="font-weight: 600; padding: 0 16px; height: 36px;">Ver Audiencia</button>
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
            <button class="page-button" type="button" data-expediente="PEN-2025-7894-1542" style="font-weight: 600; padding: 0 16px; height: 36px;">Ver Audiencia</button>
          </div>
        </article>

      </section>
    </main>
  </div>
</div>

<script>
  window.APP_CONTEXT = "${pageContext.request.contextPath}";

  document.addEventListener("DOMContentLoaded", function () {
    const profileButton = document.getElementById("profileButton");
    const profileMenu = document.getElementById("profileMenu");
    if (profileButton && profileMenu) {
      profileButton.addEventListener("click", function () {
        const expanded = profileButton.getAttribute("aria-expanded") === "true";
        profileButton.setAttribute("aria-expanded", String(!expanded));
        profileMenu.classList.toggle("is-open", !expanded);
      });
      document.addEventListener("click", function (e) {
        if (!profileButton.contains(e.target) && !profileMenu.contains(e.target)) {
          profileButton.setAttribute("aria-expanded", "false");
          profileMenu.classList.remove("is-open");
        }
      });
    }

    const tabs = document.querySelectorAll(".tab-button");
    tabs.forEach(function (tab) {
      tab.addEventListener("click", function () {
        tabs.forEach(function (t) {
          t.setAttribute("aria-selected", "false");
        });
        tab.setAttribute("aria-selected", "true");
      });
    });

    document.querySelectorAll("[data-expediente]").forEach(function (btn) {
      btn.addEventListener("click", function () {
        var exp = encodeURIComponent(btn.getAttribute("data-expediente"));
        window.location.href = window.APP_CONTEXT + "/resumenAudiencia.jsp?expediente=" + exp;
      });
    });
  });
</script>
</body>
</html>
