package de.gsi.dataset.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Small utility class to measure ns-level processing delays
 * 
 * @author rstein
 *
 */
public final class ProcessingProfiler { // NOPMD nomen est omen

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessingProfiler.class);
    /**
     * boolean flag controlling whether diagnostics time-marks are taken or the routine to be skipped
     */
    protected static boolean debugState = false;
    /**
     * boolean flag controlling whether the statistics/time differences are output to the logger/console or not
     */
    protected static boolean verboseOutput = true;
    /**
     * boolean flag controlling whether the statistics/time differences are output to the logger/console or not
     */
    protected static boolean loggerOutput = false;

    private ProcessingProfiler() {
    }

    /**
     * @param recursionDepth 0 being the calling function
     * @return the 'class::function(line:xxx)' string
     */
    public static List<String> getCallingClassMethod(int... recursionDepth) {
        ArrayList<String> list = new ArrayList<>();
        final StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();

        for (int i : recursionDepth) {
            // this number needs to be corrected if this class is refactored
            final int nLast = 2 + i;
            if (nLast >= stacktrace.length) {
                break;
            }
            final StringBuilder strBuilder = new StringBuilder();
            final StackTraceElement stackTraceElement = stacktrace[nLast];
            final String fullClassName = stackTraceElement.getClassName();
            final String simpleClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
            final String methodName = stackTraceElement.getMethodName();
            final int lineNumer = stackTraceElement.getLineNumber();

            strBuilder.append(simpleClassName).append("::").append(methodName).append("(line:").append(lineNumer).append(')');
            list.add(strBuilder.toString());
        }

        return list;
    }

    protected static String getCallingClassMethod(String msg) {
        final StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        // this number needs to be corrected if this class is refactored
        final int nLast = msg == null ? 4 : 3;
        final StackTraceElement stackTraceElement = stacktrace[nLast];
        final String fullClassName = stackTraceElement.getClassName();
        final String simpleClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
        final String methodName = stackTraceElement.getMethodName();
        final int lineNumer = stackTraceElement.getLineNumber();

        return simpleClassName + "::" + methodName + "(line:" + lineNumer + ")";
    }

    /**
     * @return boolean flag controlling whether diagnostics time-marks are taken or the routine to be skipped
     */
    public static boolean getDebugState() {
        return debugState;
    }

    /**
     * @return boolean flag controlling whether the statistics/time differences are output to the logger/console or not
     */
    public static boolean getLoggerOutputState() {
        return loggerOutput;
    }

    /**
     * @param lastStamp reference time stamp
     * @return actual delay
     */
    public static long getTimeDiff(final long lastStamp) {
        return ProcessingProfiler.getTimeDiff(lastStamp, null);
    }

    /**
     * @param lastStamp reference time stamp
     * @param msg custom string message that should be printed alongside the time stamp
     * @return actual delay
     */
    public static long getTimeDiff(final long lastStamp, final String msg) {
        if (!(ProcessingProfiler.debugState)) {
            return 0;
        }
        final long now = System.nanoTime();
        final double diff = TimeUnit.NANOSECONDS.toMillis(now - lastStamp);
        if (ProcessingProfiler.verboseOutput) {
            final String compoundName = getCallingClassMethod(msg);
            final String message = String.format("%-55s - time diff = %8.3f [ms] msg: '%s'", compoundName, diff,
                    msg == null ? "" : msg);
            if (ProcessingProfiler.loggerOutput) {
                ProcessingProfiler.LOGGER.info(message);
            } else {
                System.out.println(message); // #NOPMD, this System.out is
                        // on purpose
            }
        }
        // TODO: log statistic to HashMap/histogram, etc.
        return now;
    }

    /**
     * Returns the current value of the running Java Virtual Machine's high-resolution time source, in nanoseconds.
     * <p>
     * This method can only be used to measure elapsed time and is not related to any other notion of system or
     * wall-clock time. The value returned represents nanoseconds since some fixed but arbitrary <i>origin</i> time.
     * <p>
     * the overhead of taking the time stamp is disabled via #debugProperty()
     *
     * @return nanoSecond resolution time stam
     */
    public static long getTimeStamp() {
        if (ProcessingProfiler.debugState) {
            return System.nanoTime();
        }
        return 0;
    }

    /**
     * @return boolean flag controlling whether the statistics/time differences are output to the logger/console or not
     */
    public static boolean getVerboseOutputState() {
        return verboseOutput;
    }

    /**
     * boolean flag controlling whether diagnostics time-marks are taken or the routine to be skipped
     * 
     * @param state true: enable
     */
    public static void setDebugState(final boolean state) {
        debugState = state;
    }

    /**
     * boolean flag controlling whether the statistics/time differences are output to the logger/console or not
     * 
     * @param state true: enable
     */
    public static void setLoggerOutputState(final boolean state) {
        loggerOutput = state;
    }

    /**
     * boolean flag controlling whether the statistics/time differences are output to the logger/console or not
     * 
     * @param state true: enable
     */
    public static void setVerboseOutputState(final boolean state) {
        verboseOutput = state;
    }
}
