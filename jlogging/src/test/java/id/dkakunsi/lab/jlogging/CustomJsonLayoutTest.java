package id.dkakunsi.lab.jlogging;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.dkakunsi.lab.jlogging.attributeloader.StacktraceAttributeLoader;
import id.dkakunsi.lab.jlogging.layout.CustomJsonLayout;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * CustomJsonLayout
 * 
 * @author dkakunsi
 */
public class CustomJsonLayoutTest {

    private static final Charset CHARSET = Charset.forName("UTF-8");

    private static final boolean RECURSIVE_STACKTRACE = false;

    private static final String TIMEZONE = "UTC";

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm'Z'";

    private static final String ATTRIBUTES_WITH_STACKTRACE = "correlationId,command,tid,principal,host,service,instance,version,thread,category,level,message,fault,stacktrace,payload";

    private static final String CORRELATION_ID = "0xoqwie1267sdh2";

    private static final String COMMAND = "fabricAction";

    private static final String TID = "tid";

    private static final String PRINCIPAL = "principal";

    private static final String SERVICE = "service";

    private static final String INSTANCE = "instance";

    private static final String VERSION = "version";

    private static final String CATEGORY = "category";

    private static final String FAULT = "fault";

    private static final String PAYLOAD = "payload";

    private static final Level LEVEL = Level.INFO;

    private static String HOST;

    private static String THREAD;

    private static ConfigurationFactory cf = new BasicConfigurationFactory();

    private static ObjectMapper mapper = new ObjectMapper();

    @BeforeClass
    public static void setup() throws UnknownHostException {
        ConfigurationFactory.setConfigurationFactory(cf);
        final LoggerContext ctx = LoggerContext.getContext();
        ctx.reconfigure();

        HOST = InetAddress.getLocalHost().getHostName();
        THREAD = Thread.currentThread().getName();
    }

    @After
    public void destroy() {
        ThreadContext.clearAll();
    }

    private void setContextValue() {
        ThreadContext.put("correlationId", CORRELATION_ID);
        ThreadContext.put("tid", TID);
        ThreadContext.put("principal", PRINCIPAL);
        ThreadContext.put("service", SERVICE);
        ThreadContext.put("instance", INSTANCE);
        ThreadContext.put("version", VERSION);
        ThreadContext.put("fault", FAULT);
        ThreadContext.put("payload", PAYLOAD);
        ThreadContext.put("command", COMMAND);
    }

    private void setContextValueWithoutCommand() {
        ThreadContext.put("correlationId", CORRELATION_ID);
        ThreadContext.put("tid", TID);
        ThreadContext.put("principal", PRINCIPAL);
        ThreadContext.put("service", SERVICE);
        ThreadContext.put("instance", INSTANCE);
        ThreadContext.put("version", VERSION);
        ThreadContext.put("fault", FAULT);
        ThreadContext.put("payload", PAYLOAD);
    }

    private String buildLogEntry(String message) throws JsonProcessingException {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("correlationId", CORRELATION_ID);
        map.put("command", COMMAND);
        map.put("tid", TID);
        map.put("principal", PRINCIPAL);
        map.put("host", HOST);
        map.put("service", SERVICE);
        map.put("instance", INSTANCE);
        map.put("version", VERSION);
        map.put("thread", THREAD);
        map.put("category", CATEGORY);
        map.put("level", LEVEL.name());
        map.put("message", message);
        map.put("fault", FAULT);
        map.put("payload", PAYLOAD);

        return mapper.writeValueAsString(map);
    }

    private String buildLogEntryWithException(String message, Exception exception, boolean recursive) throws JsonProcessingException {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("correlationId", CORRELATION_ID);
        map.put("command", COMMAND);
        map.put("tid", TID);
        map.put("principal", PRINCIPAL);
        map.put("host", HOST);
        map.put("service", SERVICE);
        map.put("instance", INSTANCE);
        map.put("version", VERSION);
        map.put("thread", THREAD);
        map.put("category", CATEGORY);
        map.put("level", LEVEL.name());
        map.put("message", message);
        map.put("fault", FAULT);
        map.put("stacktrace", StacktraceAttributeLoader.generateStackTrace(exception, recursive));
        map.put("payload", PAYLOAD);

        return mapper.writeValueAsString(map);
    }

    private String buildLogEntryWithWithoutCommand(String message) throws JsonProcessingException {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("correlationId", CORRELATION_ID);
        map.put("tid", TID);
        map.put("principal", PRINCIPAL);
        map.put("host", HOST);
        map.put("service", SERVICE);
        map.put("instance", INSTANCE);
        map.put("version", VERSION);
        map.put("thread", THREAD);
        map.put("category", CATEGORY);
        map.put("level", LEVEL.name());
        map.put("message", message);
        map.put("fault", FAULT);
        map.put("payload", PAYLOAD);

        return mapper.writeValueAsString(map);
    }

    @Test
    public void testNormal() throws JsonProcessingException {
        setContextValue();

        final String message = "Hello, World";

        final CustomJsonLayout layout = CustomJsonLayout.createLayout(CHARSET, RECURSIVE_STACKTRACE, TIMEZONE,
                DATE_FORMAT, ATTRIBUTES_WITH_STACKTRACE);

        final LogEvent event = Log4jLogEvent.newBuilder()
                .setLoggerName(CATEGORY)
                .setLoggerFqcn(CATEGORY)
                .setLevel(LEVEL)
                .setMessage(new SimpleMessage(message))
                .build();

        String expected = buildLogEntry(message);
        String actual = layout.toSerializable(event);

        assertEquals(expected, actual);
    }

    @Test
    public void testEscapedDoubleQuote() throws JsonProcessingException {
        setContextValue();

        final String message = "\"Hello\", World";

        final CustomJsonLayout layout = CustomJsonLayout.createLayout(CHARSET, RECURSIVE_STACKTRACE, TIMEZONE,
                DATE_FORMAT, ATTRIBUTES_WITH_STACKTRACE);

        final LogEvent event = Log4jLogEvent.newBuilder()
                .setLoggerName(CATEGORY)
                .setLoggerFqcn(CATEGORY)
                .setLevel(LEVEL)
                .setMessage(new SimpleMessage(message))
                .build();

        String expected = buildLogEntry(message);
        String actual = layout.toSerializable(event);

        assertEquals(expected, actual);
    }

    @Test
    public void testEscapedBackslash() throws JsonProcessingException {
        setContextValue();

        final String message = "\\Hello, World";

        final CustomJsonLayout layout = CustomJsonLayout.createLayout(CHARSET, RECURSIVE_STACKTRACE, TIMEZONE,
                DATE_FORMAT, ATTRIBUTES_WITH_STACKTRACE);

        final LogEvent event = Log4jLogEvent.newBuilder()
                .setLoggerName(CATEGORY)
                .setLoggerFqcn(CATEGORY)
                .setLevel(LEVEL)
                .setMessage(new SimpleMessage(message))
                .build();

        String expected = buildLogEntry(message);
        String actual = layout.toSerializable(event);

        assertEquals(expected, actual);
    }

    @Test
    public void testEscapedNewLine() throws JsonProcessingException {
        setContextValue();

        final String message = String.format("Hello, %s World", System.getProperty("line.separator"));

        final CustomJsonLayout layout = CustomJsonLayout.createLayout(CHARSET, RECURSIVE_STACKTRACE, TIMEZONE,
                DATE_FORMAT, ATTRIBUTES_WITH_STACKTRACE);

        final LogEvent event = Log4jLogEvent.newBuilder()
                .setLoggerName(CATEGORY)
                .setLoggerFqcn(CATEGORY)
                .setLevel(LEVEL)
                .setMessage(new SimpleMessage(message))
                .build();

        String expected = buildLogEntry(message);
        String actual = layout.toSerializable(event);

        assertEquals(expected, actual);
    }

    @Test
    public void testWithoutCommand_NullValueNotAppear() throws JsonProcessingException {
        setContextValueWithoutCommand();

        final String message = "Hello, World";

        final CustomJsonLayout layout = CustomJsonLayout.createLayout(CHARSET, RECURSIVE_STACKTRACE, TIMEZONE,
                DATE_FORMAT, ATTRIBUTES_WITH_STACKTRACE);

        final LogEvent event = Log4jLogEvent.newBuilder()
                .setLoggerName(CATEGORY)
                .setLoggerFqcn(CATEGORY)
                .setLevel(LEVEL)
                .setMessage(new SimpleMessage(message))
                .build();

        String expected = buildLogEntryWithWithoutCommand(message);
        String actual = layout.toSerializable(event);

        assertEquals(expected, actual);
    }

    @Test
    public void testWithStacktrace() throws JsonProcessingException {
        Exception exception = null;
        try {
            throwException();
        } catch (Exception ex) {
            exception = ex;
        }

        setContextValue();

        final String message = "Hello, World";

        final CustomJsonLayout layout = CustomJsonLayout.createLayout(CHARSET, RECURSIVE_STACKTRACE, TIMEZONE,
                DATE_FORMAT, ATTRIBUTES_WITH_STACKTRACE);

        final LogEvent event = Log4jLogEvent.newBuilder()
                .setLoggerName(CATEGORY)
                .setLoggerFqcn(CATEGORY)
                .setLevel(LEVEL)
                .setMessage(new SimpleMessage(message))
                .setThrown(exception)
                .build();

        String expected = buildLogEntryWithException(message, exception, true);
        String actual = layout.toSerializable(event);

        assertEquals(expected, actual);
    }

    private void throwException() throws Exception {
        throw new Exception("This is exception");
    }
}
