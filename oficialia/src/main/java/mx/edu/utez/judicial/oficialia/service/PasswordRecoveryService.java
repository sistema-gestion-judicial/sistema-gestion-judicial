package mx.edu.utez.judicial.oficialia.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import mx.edu.utez.judicial.oficialia.config.AuthSettings;
import mx.edu.utez.judicial.oficialia.dao.InMemoryRecoveryCodeDao;
import mx.edu.utez.judicial.oficialia.dao.InMemoryUserDao;
import mx.edu.utez.judicial.oficialia.dao.RecoveryCodeDao;
import mx.edu.utez.judicial.oficialia.dao.UserDao;
import mx.edu.utez.judicial.oficialia.exception.BusinessRuleException;
import mx.edu.utez.judicial.oficialia.exception.ServiceUnavailableException;
import mx.edu.utez.judicial.oficialia.model.RecoveryCode;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserAccount;
import mx.edu.utez.judicial.oficialia.util.EmailValidator;

/**
 * Recovers a forgotten password with a verification code sent by e-mail (RF 1.3).
 *
 * <p>The code expires after {@link AuthSettings#RECOVERY_CODE_MINUTES} minutes and can be used once. Nothing here
 * reveals whether an e-mail is registered: requesting a code never fails, the e-mail is sent in the background,
 * and every wrong, expired or used code gets the same message. Only active accounts receive a code.
 */
public class PasswordRecoveryService {

    private static final Logger LOG = Logger.getLogger(PasswordRecoveryService.class.getName());
    private static final Pattern CODE_FORMAT = Pattern.compile("^\\d{" + AuthSettings.RECOVERY_CODE_LENGTH + "}$");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final EmailService SHARED_EMAIL_SERVICE = new EmailService();
    private static final String INVALID_CODE_MESSAGE =
        "El código no es correcto, venció o ya fue utilizado. Solicite uno nuevo.";

    private final UserDao userDao;
    private final RecoveryCodeDao recoveryCodeDao;
    private final EmailService emailService;
    private final LoginAttemptTracker attemptTracker;
    private final Clock clock;
    private final Supplier<String> codeGenerator;
    private final Executor mailExecutor;

    public PasswordRecoveryService() {
        this(InMemoryUserDao.getInstance(), InMemoryRecoveryCodeDao.getInstance(), SHARED_EMAIL_SERVICE,
            LoginAttemptTracker.getInstance(), Clock.systemDefaultZone(), PasswordRecoveryService::randomCode,
            ForkJoinPool.commonPool());
    }

    /** The executor runs the e-mail sending; tests pass one that runs it right away. */
    public PasswordRecoveryService(UserDao userDao, RecoveryCodeDao recoveryCodeDao, EmailService emailService,
                                   LoginAttemptTracker attemptTracker, Clock clock, Supplier<String> codeGenerator, Executor mailExecutor) {
        this.userDao = userDao;
        this.recoveryCodeDao = recoveryCodeDao;
        this.emailService = emailService;
        this.attemptTracker = attemptTracker;
        this.clock = clock;
        this.codeGenerator = codeGenerator;
        this.mailExecutor = mailExecutor;
    }

    /** True when the text has the shape of a code, which is checked before asking the service about it. */
    public static boolean isWellFormedCode(String code) {
        return code != null && CODE_FORMAT.matcher(code).matches();
    }

    /**
     * Sends a new code to the e-mail when it belongs to an active account, and forgets the previous one.
     * Does nothing for any other e-mail and never says which case it was.
     */
    public synchronized void requestCode(String email) {
        String normalized = EmailValidator.normalize(email);
        Optional<UserAccount> account = userDao.findByEmail(normalized).filter(UserAccount::active);
        if (account.isEmpty()) {
            return;
        }
        String code = codeGenerator.get();
        LocalDateTime expiresAt = LocalDateTime.now(clock).plusMinutes(AuthSettings.RECOVERY_CODE_MINUTES);
        recoveryCodeDao.save(new RecoveryCode(normalized, code, expiresAt));
        User user = account.get().user();
        // In the background, so the answer takes the same time for every e-mail and a mail failure is not shown
        ClassLoader webAppLoader = Thread.currentThread().getContextClassLoader();
        mailExecutor.execute(() -> deliver(user, code, webAppLoader));
    }

    /**
     * Checks a code without using it, so the user can then type the new password.
     *
     * @throws BusinessRuleException when the code is wrong, expired or already used
     */
    public synchronized void verifyCode(String email, String code) {
        checkCode(EmailValidator.normalize(email), code);
    }

    /**
     * Saves the new password, uses up the code and lifts any sign in lock, since the user proved to own the
     * e-mail. The password format is checked by the caller.
     *
     * @throws BusinessRuleException when the code is wrong, expired or already used
     */
    public synchronized void resetPassword(String email, String code, String newPassword) {
        String normalized = EmailValidator.normalize(email);
        checkCode(normalized, code);
        userDao.findByEmail(normalized).filter(UserAccount::active)
            .orElseThrow(() -> new BusinessRuleException(INVALID_CODE_MESSAGE));
        userDao.updatePasswordHash(normalized, PasswordHasher.hash(newPassword));
        recoveryCodeDao.delete(normalized);
        attemptTracker.reset(normalized);
    }

    private void checkCode(String email, String code) {
        RecoveryCode stored = recoveryCodeDao.findByEmail(email)
            .orElseThrow(() -> new BusinessRuleException(INVALID_CODE_MESSAGE));
        if (stored.isExpired(LocalDateTime.now(clock))) {
            recoveryCodeDao.delete(email);
            throw new BusinessRuleException(INVALID_CODE_MESSAGE);
        }
        if (!stored.matches(code)) {
            if (stored.registerFailedAttempt() >= AuthSettings.RECOVERY_MAX_FAILED_ATTEMPTS) {
                recoveryCodeDao.delete(email);
            }
            throw new BusinessRuleException(INVALID_CODE_MESSAGE);
        }
    }

    /** Runs in another thread, which needs the web application's class loader to find the mail providers. */
    private void deliver(User user, String code, ClassLoader webAppLoader) {
        Thread thread = Thread.currentThread();
        ClassLoader previous = thread.getContextClassLoader();
        thread.setContextClassLoader(webAppLoader);
        try {
            emailService.sendPasswordRecoveryCode(user.getEmail(), user.getFullName(), code,
                AuthSettings.RECOVERY_CODE_MINUTES);
        } catch (ServiceUnavailableException e) {
            LOG.warning("The recovery code was not delivered: " + e.getMessage());
        } finally {
            thread.setContextClassLoader(previous);
        }
    }

    private static String randomCode() {
        return String.format("%0" + AuthSettings.RECOVERY_CODE_LENGTH + "d",
            RANDOM.nextInt((int) Math.pow(10, AuthSettings.RECOVERY_CODE_LENGTH)));
    }
}
