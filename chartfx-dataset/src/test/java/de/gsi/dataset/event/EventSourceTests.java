package de.gsi.dataset.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gsi.dataset.utils.AggregateException;

/**
 * Tests the default implementation of the EventSource interface
 *
 * @author rstein
 */
class EventSourceTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventSourceTests.class);

    @Test
    void basicTests() {
        final TestEventSource evtSource = new TestEventSource();
        final AtomicInteger updateCount = new AtomicInteger();
        final Object payLoad = new Object();
        final UpdateEvent updateEvent = new UpdateEvent(evtSource, "evtMsg", payLoad);
        final EventListener specialEventListener = event -> {
            assertEquals(updateEvent, event, "event equivalency");
            assertEquals("evtMsg", event.getMessage(), "event msg equivalency");
            assertEquals(payLoad, event.getPayLoad(), "event payLoad equivalency");
        };

        // empty invoke listener
        evtSource.invokeListener(updateEvent, true);

        // add named listener
        evtSource.addListener(specialEventListener);
        assertEquals(1, evtSource.eventListener.size(), "event specialEventListener count");
        evtSource.addListener(specialEventListener);
        assertEquals(1, evtSource.eventListener.size(), "event specialEventListener count");
        evtSource.invokeListener(updateEvent, true);
        evtSource.removeListener(specialEventListener);
        assertEquals(0, evtSource.eventListener.size(), "event specialEventListener count");

        // add three anonymous listener
        for (int i = 0; i < 3; i++) {
            evtSource.addListener(evt -> updateCount.incrementAndGet());
        }

        assertEquals(3, evtSource.eventListener.size(), "event listener count");

        evtSource.invokeListener();
        assertEquals(3, updateCount.get(), "invokeListener()");

        evtSource.invokeListener(updateEvent, false);
        assertEquals(6, updateCount.get(), "invokeListener()");

        // check autonotification
        assertTrue(evtSource.isAutoNotification(), "initial autonotification()");
        evtSource.autoNotification.set(false);
        assertFalse(evtSource.isAutoNotification(), "false autonotification()");
        evtSource.invokeListener(updateEvent, false);
        // N.B. notification count should not increase
        assertEquals(6, updateCount.get(), "invokeListener()");
        evtSource.autoNotification.set(true);

        // clear event listener and add exception throwing listener
        evtSource.eventListener.clear();
        evtSource.addListener(evt -> {
            throw new IllegalStateException("bad bad exception #1");
        });
        evtSource.addListener(evt -> exceptionThrowingFunctionA());
        evtSource.addListener(evt -> exceptionThrowingFunctionB());
        assertThrows(AggregateException.class, evtSource::invokeListener);
        try {
            // check exception handling for parallel execution
            evtSource.invokeListener(updateEvent, true);
        } catch (AggregateException e) {
            assertEquals(3, e.getThrowableList().size());
        }

        try {
            // check exception handling for non-parallel execution
            evtSource.invokeListener(updateEvent, false);
        } catch (AggregateException e) {
            assertEquals(3, e.getThrowableList().size());
        }

        try {
            // check stack-trace printout
            evtSource.invokeListener(updateEvent, false);
        } catch (AggregateException e) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(os);
            e.printStackTrace(ps);
        }

        // corrupt event listener list (test failure case)
        assertTrue(evtSource.isAutoNotification(), "initial autonotification()");
        evtSource.eventListener = null;
        evtSource.invokeListener(updateEvent, false);
    }

    protected void exceptionThrowingFunctionA() {
        throw new IllegalStateException("bad bad exception #2");
    }

    protected void exceptionThrowingFunctionB() {
        throw new IllegalStateException("bad bad exception #3");
    }
}
