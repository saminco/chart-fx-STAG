package de.gsi.chart.viewer.event;

import de.gsi.dataset.event.EventSource;

/**
 * Event issued before DataViewWindow is being restored
 *
 * @see de.gsi.chart.viewer.DataViewWindow
 * @see de.gsi.chart.viewer.event.WindowUpdateEvent
 * @author rstein
 */
public class WindowRestoringEvent extends WindowUpdateEvent {
    private static final long serialVersionUID = 2846294413532027952L;

    public WindowRestoringEvent(final EventSource evtSource) {
        super(evtSource, Type.WINDOW_RESTORING);
    }

    public WindowRestoringEvent(final EventSource evtSource, final String msg) {
        super(evtSource, msg, Type.WINDOW_RESTORING);
    }

    public WindowRestoringEvent(final EventSource evtSource, final String msg, final Object obj) {
        super(evtSource, msg, obj, Type.WINDOW_RESTORING);
    }
}
