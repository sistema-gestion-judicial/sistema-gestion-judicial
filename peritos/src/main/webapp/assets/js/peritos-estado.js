// Fills the "change status" modal of the peritos list with the perito the user chose.
(function () {
  "use strict";

  var modal = document.getElementById("estadoPeritoModal");
  if (!modal) {
    return;
  }

  var form = modal.querySelector("form");
  var activarInput = form.querySelector("input[name='activar']");

  modal.addEventListener("show.bs.modal", function (event) {
    var trigger = event.relatedTarget;
    var isActive = trigger.getAttribute("data-perito-active") === "true";
    var cases = parseInt(trigger.getAttribute("data-perito-cases"), 10) || 0;
    var verb = isActive ? "Desactivar" : "Activar";

    form.action = form.getAttribute("data-action-template").replace("__ID__", trigger.getAttribute("data-perito-id"));
    activarInput.value = isActive ? "false" : "true";

    modal.querySelector("[data-perito-name-target]").textContent = trigger.getAttribute("data-perito-name");
    modal.querySelector("[data-perito-cases-target]").textContent = cases;
    modal.querySelector("[data-perito-cases-alert]").classList.toggle("d-none", !(isActive && cases > 0));
    modal.querySelector("#estadoPeritoTitle").textContent = verb + " perito";
    modal.querySelector("button[type='submit']").textContent = verb + " perito";
  });
})();
