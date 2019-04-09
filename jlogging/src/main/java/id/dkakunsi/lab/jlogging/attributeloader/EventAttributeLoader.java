package id.dkakunsi.lab.jlogging.attributeloader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.logging.log4j.core.LogEvent;

import id.dkakunsi.lab.jlogging.layout.CustomJsonLayout;

/**
 * <p>
 * Load attribute from the {@link LogEvent}. Current supported attribute are:
 * </p>
 * <ul>
 * <li>timestamp, which will return ISO-8601 date</li>
 * <li>thread, which will return thread name</li>
 * <li>category, which will return the logger name</li>
 * <li>level</li>
 * <li>message</li>
 * <li>stacktrace, which will return exception stacktrace, if exists</li>
 * </ul>
 * 
 * @author dkakunsi
 */
public class EventAttributeLoader implements AttributeLoader {

    private static final String TIMESTAMP = "timestamp";

    private static final String THREAD = "thread";

    private static final String CATEGORY = "category";

    private static final String LEVEL = "level";

    private static final String MESSAGE = "message";

    private static final String STACKTRACE = "stacktrace";

    private static final List<String> SUPPORTED_ATTRIBUTES;

    static {
        SUPPORTED_ATTRIBUTES = Arrays.asList(new String[] { TIMESTAMP, THREAD, CATEGORY, LEVEL, MESSAGE, STACKTRACE });
    }

    /**
     * Event context.
     */
    protected LogEvent event;

    /**
     * Set the context from which the attribute will be loaded from.
     * 
     * @param context the context
     * @throws IllegalArgumentException the given context is null.
     */
    public void setContext(LogEvent context) throws IllegalArgumentException {
        if (context == null) {
            throw new IllegalArgumentException("Cannot process null context.");
        }
        this.event = (LogEvent) context;
    }

    @Override
    public Object get(String key) {
        switch (key) {
        case TIMESTAMP:
            return getIsoDate();
        case THREAD:
            return this.event.getThreadName();
        case CATEGORY:
            return this.event.getLoggerName();
        case LEVEL:
            return this.event.getLevel().getStandardLevel();
        case MESSAGE:
            return this.event.getMessage().getFormattedMessage();
        case STACKTRACE:
            return event.getThrown() != null
                    ? generateStackTrace(event.getThrown(), CustomJsonLayout.isRecursiveStackTrace())
                    : null;
        default:
            return null;
        }
    }

    @Override
    public boolean contain(String key) {
        return SUPPORTED_ATTRIBUTES.contains(key);
    }

    private String getIsoDate() {
        TimeZone tz = TimeZone.getTimeZone(CustomJsonLayout.getTimezone());
        DateFormat df = new SimpleDateFormat(CustomJsonLayout.getDateFormat());
        df.setTimeZone(tz);

        return df.format(new Date(this.event.getTimeMillis()));
    }

    /**
     * <p>
     * Generate exception stack trace. The result is list of exception in custom
     * format.
     * </p>
     * The example is:
     * 
     * <pre>
     * {
     *      "exception": "java.lang.Exception",
     *      "message": "Exception message",
     *      "stack": "[LIST OF CALLING STACKTRACE]"
     * }
     * </pre>
     * 
     * To generate the stack element, please see
     * {@link #generateStackTrace(StackTraceElement[])}.
     * 
     * @param thrown    the exception
     * @param recursive whether to print the exception recursively. {@code false}
     *                  will just print the most inner exception.
     * @return list of custom-formatted exception data.
     */
    private List<Object> generateStackTrace(Throwable thrown, boolean recursive) {
        List<Object> stack = new ArrayList<>();

        if (recursive) {
            if (thrown.getCause() != null) {
                stack.addAll(generateStackTrace(thrown.getCause(), recursive));
            }
        } else {
            thrown = getInnerException(thrown);
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("exception", thrown.getClass().getName());
        map.put("message", thrown.getMessage());
        map.put("stack", generateStackTrace(thrown.getStackTrace()));

        stack.add(map);

        return stack;
    }

    /**
     * Get the most inner exception.
     * 
     * @param thrown exception
     * @return the most inner exception
     */
    private Throwable getInnerException(Throwable thrown) {
        while (thrown.getCause() != null) {
            thrown = thrown.getCause();
        }
        return thrown;
    }

    /**
     * <p>
     * Generate calling stacktrace of an exception. The result of list of calling
     * stacktrace in custom format.
     * </p>
     * The format is:
     * 
     * <pre>
     * {
     *      "file": "App.java",
     *      "method": "main",
     *      "line": 20
     * }
     * </pre>
     * 
     * @param stacktraces stacktrace element
     * @return list of custom-formatted stacktrace
     */
    private List<Object> generateStackTrace(StackTraceElement[] stacktraces) {
        List<Object> stack = new ArrayList<>();

        Map<String, Object> map;
        for (StackTraceElement stacktrace : stacktraces) {
            map = new LinkedHashMap<>();
            map.put("file", stacktrace.getFileName());
            map.put("method", stacktrace.getMethodName());
            map.put("line", stacktrace.getLineNumber());

            stack.add(map);
        }

        return stack;
    }
}
