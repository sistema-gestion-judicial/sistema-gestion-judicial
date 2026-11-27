document.addEventListener('DOMContentLoaded', () => {
  const inputs = document.querySelectorAll('.code-input');
  const hiddenFullCode = document.getElementById('fullCode');

  // Autofoco en la primera casilla al cargar la pantalla (Estado Focus de Figma)
  if (inputs.length > 0 && !document.querySelector('.code-input.is-invalid')) {
    inputs[0].focus();
  }

  inputs.forEach((input, index) => {
    // Al escribir un número
    input.addEventListener('input', (e) => {
      const value = e.target.value;

      // Filtrar solo dígitos
      if (!/^\d*$/.test(value)) {
        e.target.value = '';
        return;
      }

      // Limpiar visualmente los errores al comenzar a interactuar
      inputs.forEach(inp => inp.classList.remove('is-invalid'));
      const errorMsg = document.getElementById('errorCodeMsg');
      if (errorMsg) errorMsg.style.display = 'none';

      // Salto automático al siguiente campo
      if (value.length === 1 && index < inputs.length - 1) {
        inputs[index + 1].focus();
      }

      updateFullCode();
    });

    // Borrar y retroceder casilla
    input.addEventListener('keydown', (e) => {
      if (e.key === 'Backspace' && !input.value && index > 0) {
        inputs[index - 1].focus();
      }
    });

    // Soporte para pegar código de 6 dígitos
    input.addEventListener('paste', (e) => {
      e.preventDefault();
      const pastedData = (e.clipboardData || window.clipboardData).getData('text').trim();
      if (/^\d{6}$/.test(pastedData)) {
        pastedData.split('').forEach((char, i) => {
          if (inputs[i]) inputs[i].value = char;
        });
        inputs[inputs.length - 1].focus();
        updateFullCode();
      }
    });
  });

  function updateFullCode() {
    if (hiddenFullCode) {
      hiddenFullCode.value = Array.from(inputs).map(inp => inp.value).join('');
    }
  }
});
