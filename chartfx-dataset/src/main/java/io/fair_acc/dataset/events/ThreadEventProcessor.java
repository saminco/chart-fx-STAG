package io.fair_acc.dataset.events;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.tuple.Pair;

/**
 * An event processor class which processes dataset events independent of the UI thread of the chart.
 * All datasets added to this processor will be processed whenever they are invalidated.
 * There is an optional RateLimiting, which delays the final update in case of bursts by a predefined time.
 * Data processing can either be added to a separate EventProcessor or be handled inside the event processing of
 * the chartfx-chart package, eg as a member of a plugin which will perform the update during the plugin's preLayout phase.
 * <p>
 * TODO:
 * - implement rate limiting
 * - distribute work on multiple threads/thread-pool
 */
public class ThreadEventProcessor implements EventProcessor, Runnable {
    private static final AtomicReference<ThreadEventProcessor> INSTANCE = new AtomicReference<>();
    private static EventProcessor userInstance;

    private final Object changeLock = new Object();
    private final BitState state = BitState.initDirtyMultiThreaded(this, ChartBits.DataSetMask)
                                           .addChangeListener((src, bits) -> notifyChanged());
    private final List<Pair<BitState, Runnable>> actions = new CopyOnWriteArrayList<>();

    public static EventProcessor getUserInstance() {
        return userInstance != null ? userInstance : getInstance();
    }

    public static void setUserInstance(EventProcessor customProcessor) {
        userInstance = customProcessor;
    }

    public static ThreadEventProcessor getInstance() {
        ThreadEventProcessor result = INSTANCE.get();
        if (result != null) {
            return result;
        }
        // probably does not exist yet, but initialise in thread safe way
        result = new ThreadEventProcessor();
        if (INSTANCE.compareAndSet(null, result)) {
            return result;
        } else {
            return INSTANCE.get();
        }
    }

    ThreadEventProcessor() {
        // TODO: use a thread pool instead of a single thread
        var thread = new Thread(this, "ChartFx event processor");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        // noinspection InfiniteLoopStatement
        while (true) {
            boolean isDirty = state.clear() != 0;
            if (isDirty) {
                for (final var action : actions) {
                    if (action.getLeft().isDirty(ChartBits.DataSetMask)) {
                        action.getLeft().clear();
                        try {
                            action.getRight().run();
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
            // Todo: add optional rate limiting
            waitForChanges();
        }
    }

    private void notifyChanged() {
        synchronized (changeLock) {
            changeLock.notifyAll();
        }
    }

    private void waitForChanges() {
        synchronized (changeLock) {
            if (state.isClean()) {
                try {
                    changeLock.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    public BitState getBitState() {
        return state;
    }

    @Override
    public void addAction(final BitState obj, final Runnable action) {
        obj.addInvalidateListener(state);
        actions.add(Pair.of(obj, action));
    }
}
