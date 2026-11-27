function togglePassword() {
  const input = document.getElementById('password');
  const btn = document.getElementById('btnTogglePassword');
  if (input.type === 'password') {
    input.type = 'text';
    btn.textContent = 'Ocultar';
  } else {
    input.type = 'password';
    btn.textContent = 'Mostrar';
  }
}

"use strict";

document.addEventListener("DOMContentLoaded", () => {
  const form = document.querySelector("form");
  const password = document.getElementById("password");
  const toggle = document.getElementById("btnTogglePassword");

  if (password && toggle) {
    toggle.addEventListener("click", () => {
      const show = password.type === "password";
      password.type = show ? "text" : "password";
      toggle.textContent = show ? "Ocultar" : "Mostrar";
      toggle.setAttribute("aria-pressed", String(show));
    });
  }

  // La verificación de la contraseña tarda un instante: evita enviar el formulario dos veces
  if (form) {
    form.addEventListener("submit", () => {
      const submit = form.querySelector('[type="submit"]');
      if (submit) submit.disabled = true;
    });
  }
});

// Si el servidor responde con error, el navegador vuelve a cargar la página y el botón queda activo.
// Al volver con "atrás" desde caché se restablece también.
window.addEventListener("pageshow", (event) => {
  if (event.persisted) {
    const submit = document.querySelector('form [type="submit"]');
    if (submit) submit.disabled = false;
  }
});

