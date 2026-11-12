package mx.edu.utez.judicial.common.service;

import mx.edu.utez.judicial.tocas.dao.UserDao;
import mx.edu.utez.judicial.tocas.model.User;

import java.util.regex.Pattern;

public class AuthService {
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final String DUMMY_SALT = PasswordHasher.newSalt();

    private final UserDao userDao = new UserDao();

    /**
     * Validates the form and checks the credentials.
     *
     * @throws BusinessException with the message to show under the form
     */
    public User authenticate(String email, String password) {
        String cleanEmail = email == null ? "" : email.trim().toLowerCase();
        if (cleanEmail.isEmpty()) {
            throw new BusinessException("Ingrese su correo electrónico.");
        }
        if (!EMAIL.matcher(cleanEmail).matches()) {
            throw new BusinessException("Ingrese un correo electrónico válido.");
        }
        if (password == null || password.isEmpty()) {
            throw new BusinessException("Ingrese su contraseña.");
        }
        User user = userDao.findByEmail(cleanEmail);
        if (user == null) {
            PasswordHasher.hash(password, DUMMY_SALT); // keep timing similar
            throw new BusinessException("Correo o contraseña incorrectos.");
        }
        if (!PasswordHasher.matches(password, user.getSalt(), user.getPasswordHash())) {
            throw new BusinessException("Correo o contraseña incorrectos.");
        }
        return user;
    }
}
