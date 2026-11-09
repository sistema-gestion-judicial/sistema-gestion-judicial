package mx.edu.utez.judicial.peritos.web;

import static mx.edu.utez.judicial.peritos.web.Despachador.ruta;

import java.util.List;

/** Rutas de Admin 0 a 6 (inicio, peritos y solicitudes). */
final class RutasAdminBloque1 {

    private static final String VISTAS = "/WEB-INF/views/admin/";
    private static final String ID = "[^/]+";

    static final List<Despachador.Ruta> RUTAS = List.of(
            ruta("/", VISTAS + "inicio.jsp"),
            ruta("/peritos", VISTAS + "peritos/list.jsp"),
            ruta("/peritos/nuevo", VISTAS + "peritos/form.jsp"),
            ruta("/peritos/" + ID + "/editar", VISTAS + "peritos/form.jsp"),
            ruta("/solicitudes", VISTAS + "solicitudes/list.jsp"),
            ruta("/solicitudes/" + ID, VISTAS + "solicitudes/detail.jsp"));

    private RutasAdminBloque1() {
    }
}
