package mx.edu.utez.judicial.audiencias.api.controller;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import mx.edu.utez.judicial.audiencias.api.model.Usuario;
import mx.edu.utez.judicial.audiencias.api.service.ServiceAuth;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final static String VIEW= "WEB-INF/views/auth/login.jsp";


    private final ServiceAuth serviceAuth;

    public LoginServlet(ServiceAuth serviceAuth) {
        this.serviceAuth = serviceAuth;
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String correo = req.getParameter("email");
        String contra= req.getParameter("password");

        if(correo==null||correo.isBlank()||contra==null||contra.isBlank()){
            showError(req,resp,correo,"Introduzca un correo y contraseña para iniciar sesion");
        }
        Usuario usuario = serviceAuth.autenticar(correo,contra);

        if(usuario==null){
            showError(req,resp,correo,"Correo o contraseña incorrecta");
        }

        HttpSession user = req.getSession(false);
        user.setAttribute("Usuario",usuario);

        if(usuario.getRol().equals("ADMIN")){
            resp.sendRedirect("");
        }else {
            resp.sendRedirect("");
        }


    }

    private void showError(HttpServletRequest request, HttpServletResponse response, String email,
                           String message) throws ServletException, IOException {
        request.setAttribute("errorMessage", message);
        request.setAttribute("email", email);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
