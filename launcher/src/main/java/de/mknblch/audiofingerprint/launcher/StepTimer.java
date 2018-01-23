package de.mknblch.audiofingerprint.launcher;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author mknblch
 */
public class StepTimer implements AutoCloseable {

    private final Logger logger;
    private final int printTimeout;

    private long nextLog = 0;
    private int steps = 0;
    private double expected = -1;
    private long startTime = 0;
    private long endTime = 0;
    private int phase = 0;


    public StepTimer() {
        this(getLogger(StepTimer.class));
    }

    public StepTimer(Logger logger) {
        this(logger, 3000);
    }

    public StepTimer(Logger logger, int printTimeout) {
        this.logger = logger;
        this.printTimeout = printTimeout;
    }

    public StepTimer withExpectedSteps(int expected) {
        this.expected = expected;
        return this;
    }

    public synchronized void step() {
        final long millis = System.currentTimeMillis();
        if (steps == 0) {
            startTime = millis;
            nextLog = millis + printTimeout;
        }
        steps++;
        phase++;
        if (millis < nextLog) {
            return;
        }
        nextLog = millis + printTimeout;
        final long duration = millis - startTime;
        if (expected > 0) {
            logger.info(String.format("#%d, %2.0f%%, %s, %.0f steps/s",
                    steps,
                    steps * 100.0 / expected,
                    format(duration),
                    phase / (printTimeout / 1000d)
            ));
        } else {
            logger.info(String.format("#%d, %s, %.0f steps/s",
                    steps,
                    format(duration),
                    phase / (printTimeout / 1000d)
            ));
        }
        phase = 0;
    }

    public void end() {

        endTime = System.currentTimeMillis();
        logger.info(String.format("#%d (%s) finished",
                steps,
                format(endTime - startTime)
        ));
    }

    @Override
    public void close() throws Exception {
        end();
    }

    public String format(long duration) {

        final int k = (int) (duration / 1000);
        final int sec = k % 60;
        final int min = (k % 3600) / 60;
        final int hour = k / 3600;

        if (hour > 0) {
            return String.format("%dh:%dm:%ds", hour, min, sec);
        }
        if (min > 0) {
            return String.format("%dm:%ds", min, sec);
        }
        return String.format("%ds", sec);
    }
}
