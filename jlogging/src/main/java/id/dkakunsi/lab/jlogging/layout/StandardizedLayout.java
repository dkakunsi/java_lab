package id.dkakunsi.lab.jlogging.layout;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

/**
 * <p>
 * Serialize {@link LogEvent} into a custom-formatted log entry, with each entry
 * is a valid JSON. But, the full log file is not guaranteed to be a valid JSON
 * file.
 * </p>
 * <p>
 * There are 2 types of attributes, which are common attributes from
 * {@link LogEvent} and custom attributes from {@link ThreadContext}.
 * </p>
 * The format is:
 * 
 * <pre>
 * {
 *      "timestamp": "When the event created",
 *      "correlationId": "Custom field loaded from {@code ThreadContext}",
 *      "tid": "Custom field loaded from {@code ThreadContext}",
 *      "principal": "Custom field loaded from {@code ThreadContext}",
 *      "host": "Host from {@link InetAddress}",
 *      "service": "Custom field loaded from {@code ThreadContext}",
 *      "instance": "Custom field loaded from {@code ThreadContext}",
 *      "version": "Custom field loaded from {@code ThreadContext}",
 *      "thread": "Thread that generate the event",
 *      "category": "Logger name",
 *      "level": "Log level",
 *      "message": "Log message",
 *      "fault": "Custom field loaded from {@code ThreadContext}",
 *      "stacktrace": "Stacktrace of an exception. Only exist when there is an exception",
 *      "payload": "Custom field loaded from {@code ThreadContext}"
 * }
 * </pre>
 * <p>
 * The {@code stacktrace} can be either printed recursively or just the most
 * inner exception. This behaviour controlled by {@link #RECURSIVE_STACKTRACE}
 * value.
 * </p>
 * 
 * @author dkakunsi
 */
@Plugin(name = "StandardizedLayout", category = "Core", elementType = "layout", printObject = true)
public class StandardizedLayout extends AbstractStringLayout {

    private String dateFormat;

    private boolean recursiveStackTrace;

    private String timezone;

    private ObjectMapper mapper;

    protected StandardizedLayout(Charset charset, boolean recursiveStackTrace, String timezone, String dateFormat) {
        super(charset);
        this.recursiveStackTrace = recursiveStackTrace;
        this.timezone = timezone;
        this.dateFormat = dateFormat;

        this.mapper = new ObjectMapper();
    }

    @PluginFactory
    public static StandardizedLayout createLayout(
            @PluginAttribute(value = "charset", defaultString = "UTF-8") Charset charset,
            @PluginAttribute(value = "recursiveStackTrace", defaultBoolean = true) boolean recursiveStacktrace,
            @PluginAttribute(value = "timeZone", defaultString = "UTC") String timezone,
            @PluginAttribute(value = "dateFormat", defaultString = "yyyy-MM-dd'T'HH:mm'Z'") String dateFormat) {
        return new StandardizedLayout(charset, recursiveStacktrace, timezone, dateFormat);
    }

    @Override
    public String toSerializable(LogEvent event) {
        Map<String, Object> format = new LinkedHashMap<>();
        format.put("timestamp", getIsoDate(event.getTimeMillis()));
        format.put("correlationId", event.getContextData().getValue("correlationId"));
        format.put("tid", event.getContextData().getValue("tid"));
        format.put("principal", event.getContextData().getValue("principal"));
        format.put("host", getHostname());
        format.put("service", event.getContextData().getValue("service"));
        format.put("instance", event.getContextData().getValue("instance"));
        format.put("version", event.getContextData().getValue("version"));
        format.put("thread", event.getThreadName());
        format.put("category", event.getLoggerName());
        format.put("level", event.getLevel().getStandardLevel());
        format.put("message", event.getMessage().getFormattedMessage());
        format.put("fault", event.getContextData().getValue("fault"));
        format.put("stacktrace",
                event.getThrown() != null ? generateStackTrace(event.getThrown(), recursiveStackTrace) : null);
        format.put("payload", event.getContextData().getValue("payload"));

        try {
            return mapper.writeValueAsString(format);
        } catch (JsonProcessingException ex) {
            return ex.getMessage();
        }
    }

    private String getIsoDate(long millis) {
        if (millis == 0) {
            millis = new Date().getTime();
        }

        TimeZone tz = TimeZone.getTimeZone(this.timezone);
        DateFormat df = new SimpleDateFormat(this.dateFormat);
        df.setTimeZone(tz);

        return df.format(new Date());
    }

    private String getHostname() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostName();
        } catch (UnknownHostException e) {
            return e.getMessage();
        }
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
