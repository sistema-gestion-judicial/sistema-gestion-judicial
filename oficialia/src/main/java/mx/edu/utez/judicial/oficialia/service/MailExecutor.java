package mx.edu.utez.judicial.oficialia.service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * The threads that send the e-mails, apart from the pool every other task of the JVM uses. A send can wait for
 * the mail server up to its timeout (10 seconds), so a few slow sends must not leave other tasks without
 * threads. The pool is small and its queue has a limit: when the queue is full the message is dropped and a
 * warning is logged, instead of piling up without end.
 */
public final class MailExecutor {

    static final int THREADS = 2;
    static final int QUEUE_SIZE = 50;
    private static final long SHUTDOWN_WAIT_SECONDS = 5;
    private static final Logger LOG = Logger.getLogger(MailExecutor.class.getName());
    private static final ThreadPoolExecutor SHARED = create(THREADS, QUEUE_SIZE);

    private MailExecutor() {
    }

    /** The executor of the application. */
    public static Executor shared() {
        return SHARED;
    }

    /** Stops the shared threads when the application stops, so Tomcat does not keep them across redeploys. */
    public static void shutdown() {
        shutdown(SHARED);
    }

    static ThreadPoolExecutor create(int threads, int queueSize) {
        AtomicInteger counter = new AtomicInteger();
        ThreadPoolExecutor pool = new ThreadPoolExecutor(threads, threads, 30, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(queueSize),
            task -> {
                Thread thread = new Thread(task, "oficialia-mail-" + counter.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            },
            (task, executor) -> LOG.warning("A message was not sent: the mail queue is full or has been stopped"));
        // Idle threads end by themselves, so nothing stays alive when no message is being sent
        pool.allowCoreThreadTimeOut(true);
        return pool;
    }

    /** Lets the messages already running finish for a few seconds and drops the rest. */
    static void shutdown(ThreadPoolExecutor pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(SHUTDOWN_WAIT_SECONDS, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
