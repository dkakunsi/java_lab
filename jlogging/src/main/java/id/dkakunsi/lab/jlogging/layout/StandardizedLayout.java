package id.dkakunsi.lab.jlogging.layout;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
 * StandardizedLayout
 */
@Plugin(name = "StandardizedLayout", category = "Core", elementType = "layout", printObject = true)
public class StandardizedLayout extends AbstractStringLayout {

    private static final boolean recursiveStacktrace = true;

    private ObjectMapper mapper;

    protected StandardizedLayout(Charset charset) {
        super(charset);
        mapper = new ObjectMapper();
    }

    @PluginFactory
    public static StandardizedLayout createLayout(
            @PluginAttribute(value = "charset", defaultString = "UTF-8") Charset charset) {
        return new StandardizedLayout(charset);
    }

    @Override
    public String toSerializable(LogEvent event) {
        Map<String, Object> format = new HashMap<>();
        format.put("timestamp", getIsoDate(new Date()));
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
                event.getThrown() != null ? generateStackTrace(event.getThrown(), recursiveStacktrace) : null);
        format.put("payload", event.getContextData().getValue("payload"));

        try {
            return mapper.writeValueAsString(format);
        } catch (JsonProcessingException ex) {
            return ex.getMessage();
        }
    }

    private static String getIsoDate(Date date) {
        if (date == null) {
            date = new Date();
        }

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
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

    private List<Object> generateStackTrace(Throwable thrown, boolean recursive) {
        List<Object> stack = new ArrayList<>();

        if (recursive) {
            if (thrown.getCause() != null) {
                stack.addAll(generateStackTrace(thrown.getCause(), recursive));
            }
        } else {
            thrown = getInnerException(thrown);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("exception", thrown.getClass().getName());
        map.put("message", thrown.getMessage());
        map.put("stack", generateStackTrace(thrown.getStackTrace()));

        stack.add(map);

        return stack;
    }

    private Throwable getInnerException(Throwable thrown) {
        while (thrown.getCause() != null) {
            thrown = thrown.getCause();
        }
        return thrown;
    }

    private List<Object> generateStackTrace(StackTraceElement[] stacktraces) {
        List<Object> stack = new ArrayList<>();

        Map<String, Object> map;
        for (StackTraceElement stacktrace : stacktraces) {
            map = new HashMap<>();
            map.put("class", stacktrace.getFileName());
            map.put("method", stacktrace.getMethodName());
            map.put("line", stacktrace.getLineNumber());

            stack.add(map);
        }

        return stack;
    }
}
