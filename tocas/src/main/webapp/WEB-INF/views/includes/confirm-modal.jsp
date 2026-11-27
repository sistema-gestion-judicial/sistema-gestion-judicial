<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<div class="modal fade" id="confirmModal" tabindex="-1" aria-labelledby="confirmTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content app-modal">
      <div class="modal-head"><h2 id="confirmTitle" class="caps">CONFIRMAR</h2>
        <button type="button" class="modal-x" data-bs-dismiss="modal" aria-label="Cerrar"><t:icon name="close" size="16"/></button></div>
      <div class="modal-body-g">
        <p class="confirm-question" id="confirmQuestion"></p>
        <dl class="confirm-rows" id="confirmRows"></dl>
        <div class="warn-box" id="confirmWarning" hidden></div>
      </div>
      <div class="modal-foot end">
        <button type="button" class="btn btn-outline-g" data-bs-dismiss="modal">Cancelar</button>
        <button type="button" class="btn btn-dark-g" id="confirmOk">Confirmar</button>
      </div>
    </div>
  </div>
</div>
