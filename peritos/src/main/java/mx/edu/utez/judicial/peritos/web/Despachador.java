package mx.edu.utez.judicial.peritos.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/** Busca la vista que corresponde a la ruta pedida y la muestra. */
final class Despachador {

    private Despachador() {
    }

    record Ruta(Pattern patron, String vista) {
    }

    static Ruta ruta(String expresion, String vista) {
        return new Ruta(Pattern.compile(expresion), vista);
    }

    static void mostrar(HttpServletRequest request, HttpServletResponse response, List<Ruta> rutas)
            throws ServletException, IOException {
        String rutaPedida = request.getPathInfo() == null ? "/" : request.getPathInfo();
        for (Ruta candidata : rutas) {
            if (candidata.patron().matcher(rutaPedida).matches()) {
                request.getRequestDispatcher(candidata.vista()).forward(request, response);
                return;
            }
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}
