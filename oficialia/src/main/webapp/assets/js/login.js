// Shows or hides the password typed in the login form.
document.addEventListener("DOMContentLoaded", () => {
  const passwordInput = document.getElementById("password");
  const toggleButton = document.getElementById("togglePassword");
  if (!passwordInput || !toggleButton) {
    return;
  }
  toggleButton.addEventListener("click", () => {
    const isHidden = passwordInput.type === "password";
    passwordInput.type = isHidden ? "text" : "password";
    toggleButton.textContent = isHidden ? "Ocultar" : "Mostrar";
  });
});
