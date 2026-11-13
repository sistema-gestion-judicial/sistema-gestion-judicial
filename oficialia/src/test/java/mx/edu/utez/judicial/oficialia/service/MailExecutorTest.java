package mx.edu.utez.judicial.oficialia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

class MailExecutorTest {

    private static final Logger LOG = Logger.getLogger(MailExecutor.class.getName());

    private static final class Records extends Handler {

        private final List<LogRecord> records = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    }

    @Test
    void whenTheQueueIsFullTheMessageIsDroppedWithAWarning() throws Exception {
        Records logged = new Records();
        LOG.addHandler(logged);
        ThreadPoolExecutor pool = MailExecutor.create(1, 2);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger ran = new AtomicInteger();
        try {
            pool.execute(() -> {
                started.countDown();
                await(release);
                ran.incrementAndGet();
            });
            assertTrue(started.await(5, TimeUnit.SECONDS));
            pool.execute(ran::incrementAndGet);
            pool.execute(ran::incrementAndGet);

            pool.execute(ran::incrementAndGet);

            assertEquals(1, logged.records.size());
            assertEquals(Level.WARNING, logged.records.get(0).getLevel());
            release.countDown();
            pool.shutdown();
            assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
            assertEquals(3, ran.get(), "the fourth message was dropped");
        } finally {
            LOG.removeHandler(logged);
            release.countDown();
            pool.shutdownNow();
        }
    }

    @Test
    void afterShutdownNothingNewRunsAndNothingBreaks() {
        ThreadPoolExecutor pool = MailExecutor.create(1, 2);
        AtomicInteger ran = new AtomicInteger();

        MailExecutor.shutdown(pool);
        pool.execute(ran::incrementAndGet);

        assertTrue(pool.isShutdown());
        assertEquals(0, ran.get());
    }

    @Test
    void theThreadsAreDaemonsWithTheirOwnName() throws Exception {
        ThreadPoolExecutor pool = MailExecutor.create(1, 2);
        List<Thread> seen = new ArrayList<>();
        CountDownLatch done = new CountDownLatch(1);
        try {
            pool.execute(() -> {
                seen.add(Thread.currentThread());
                done.countDown();
            });
            assertTrue(done.await(5, TimeUnit.SECONDS));

            assertTrue(seen.get(0).isDaemon());
            assertTrue(seen.get(0).getName().startsWith("oficialia-mail-"));
        } finally {
            pool.shutdownNow();
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
