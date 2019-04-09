package id.dkakunsi.lab.jlogging.layout;

import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

import id.dkakunsi.lab.jlogging.attributeloader.AttributeLoader;
import id.dkakunsi.lab.jlogging.attributeloader.ContextAttributeLoader;
import id.dkakunsi.lab.jlogging.attributeloader.EnvironmentAttributeLoader;
import id.dkakunsi.lab.jlogging.attributeloader.EventAttributeLoader;

/**
 * <p>
 * Serialize {@link LogEvent} into a custom-formatted log entry, with each entry
 * is a valid JSON. But, the full log file is not guaranteed to be a valid JSON
 * file.
 * </p>
 * <p>
 * There are 3 types of attributes, which are event attributes from
 * {@link LogEvent}, context attributes from {@link ThreadContext}, and
 * environment attributes from application or system environment. Each attribute
 * has it's own {@link AttributeLoader}. The loader priority is as follow:
 * <ol>
 * <li>{@link EventAttributeLoader}</li>
 * <li>{@link EnvironmentAttributeLoader}</li>
 * <li>{@link ContextAttributeLoader}</li>
 * </ol>
 * </p>
 * <p>
 * The log entry is in JSON format which can be specified as {@code attribute}
 * in log4j configuration under {@code <CustomJsonLayout>} element. If not
 * specified, the layout will use the {@link #DEFAULT_ATTRIBUTES}. The following
 * is a layout with specific attribute value:
 * </p>
 * 
 * <pre>
 *      {@code <CustomJsonLayout attributes=
"timestamp,correlationId,tid,principal,host,service,instance,version,thread,category,level,message,fault,stacktrace,payload" />}
 * </pre>
 * 
 * which will produce the following JSON:
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
 * inner exception. This behaviour controlled by {@link #IS_RECURSIVE_STACKTRACE}
 * value and can be specified as {@code isRecursiveStackTrace} value under
 * {@code <CustomJsonLayout>} config element.
 * </p>
 * 
 * <p>
 * The other configurable attribute under {@code <CustomJsonLayout>} are:
 * <ul>
 * <li>timezone, default ot "UTC"</li>
 * <li>dateFormat, default to ISO-8601</li>
 * </ul>
 * </p>
 * @author dkakunsi
 */
@Plugin(name = "CustomJsonLayout", category = "Core", elementType = "layout", printObject = true)
public class CustomJsonLayout extends AbstractStringLayout {

    private static final String DEFAULT_ATTRIBUTES = "timestamp,category,level,message";

    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm'Z'";

    private static final String DEFAULT_TIMEZONE = "UTC";

    private static String DATE_FORMAT;

    private static String TIMEZONE;

    private static boolean IS_RECURSIVE_STACKTRACE;

    private String attributes;

    private ObjectMapper mapper;

    private EventAttributeLoader eventAttributeLoader;

    private EventAttributeLoader contextAttributeLoader;

    private AttributeLoader environmentAttributeLoader;

    protected CustomJsonLayout(Charset charset, boolean isRecursiveStackTrace, String timezone, String dateFormat,
            String attributes) {
        super(charset);
        this.attributes = attributes;
        this.mapper = new ObjectMapper();
        this.eventAttributeLoader = new EventAttributeLoader();
        this.contextAttributeLoader = new ContextAttributeLoader();
        this.environmentAttributeLoader = new EnvironmentAttributeLoader();

        IS_RECURSIVE_STACKTRACE = isRecursiveStackTrace;
        TIMEZONE = timezone;
        DATE_FORMAT = dateFormat;
    }

    @PluginFactory
    public static CustomJsonLayout createLayout(
            @PluginAttribute(value = "charset", defaultString = "UTF-8") Charset charset,
            @PluginAttribute(value = "isRecursiveStackTrace", defaultBoolean = true) boolean recursiveStacktrace,
            @PluginAttribute(value = "timezone", defaultString = DEFAULT_TIMEZONE) String timezone,
            @PluginAttribute(value = "dateFormat", defaultString = DEFAULT_DATE_FORMAT) String dateFormat,
            @PluginAttribute(value = "attributes", defaultString = DEFAULT_ATTRIBUTES) String attributes) {
        return new CustomJsonLayout(charset, recursiveStacktrace, timezone, dateFormat, attributes);
    }

    public static boolean isRecursiveStackTrace() {
        return IS_RECURSIVE_STACKTRACE;
    }

    public static String getTimezone() {
        return TIMEZONE;
    }

    public static String getDateFormat() {
        return DATE_FORMAT;
    }

    @Override
    public String toSerializable(LogEvent event) {
        this.eventAttributeLoader.setContext(event);
        this.contextAttributeLoader.setContext(event);

        Map<String, Object> format = new LinkedHashMap<>();

        for (String attribute : getAttributes()) {
            format.put(attribute, selectLoader(attribute).get(attribute));
        }

        try {
            return mapper.writeValueAsString(format);
        } catch (JsonProcessingException ex) {
            return ex.getMessage();
        }
    }

    private String[] getAttributes() {
        return this.attributes.split(",");
    }

    private AttributeLoader selectLoader(String key) {
        if (this.eventAttributeLoader.contain(key)) {
            return this.eventAttributeLoader;
        } else if (this.environmentAttributeLoader.contain(key)) {
            return this.environmentAttributeLoader;
        } else {
            return this.contextAttributeLoader;
        }
    }
}
