package id.dkakunsi.lab.jlogging.layout;

import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

import id.dkakunsi.lab.jlogging.attributeloader.AttributeLoader;
import id.dkakunsi.lab.jlogging.attributeloader.ContextAttributeLoader;
import id.dkakunsi.lab.jlogging.attributeloader.HostAttributeLoader;
import id.dkakunsi.lab.jlogging.attributeloader.StacktraceAttributeLoader;
import id.dkakunsi.lab.jlogging.attributeloader.TimestampAttributeLoader;
import id.dkakunsi.lab.jlogging.attributeloader.EventAttributeLoader;

/**
 * <p>
 * Serialize {@link LogEvent} into a custom-formatted log entry, with each entry
 * is a valid JSON. But, the full log file is not guaranteed to be a valid JSON
 * file.
 * </p>
 * <p>
 * There are several configuration attributes under {@code <CustomJsonLayout>} element, which are:
 * <ul>
 * <li>{@link #attributes}, list off JSON attributes to be printed.</li>
 * <li>{@link #isRecursiveStackTrace}, whether the stacktrace will be printed recursively.</li>
 * <li>{@link #timezone}, default to "UTC"</li>
 * <li>{@link #dateFormat}, default to ISO-8601</li>
 * </ul>
 * The following is a layout with specific attributes value:
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
 * 
 * @author dkakunsi
 */
@Plugin(name = "CustomJsonLayout", category = "Core", elementType = "layout", printObject = true)
public class CustomJsonLayout extends AbstractStringLayout {

    private static final String DEFAULT_ATTRIBUTES = "timestamp,category,level,message";

    private String dateFormat;

    private String timezone;

    private boolean isRecursiveStackTrace;

    private String attributes;

    private ObjectMapper mapper;

    private AttributeLoader[] attributeLoaders;

    private LogEvent event;

    protected CustomJsonLayout(Charset charset, boolean isRecursiveStackTrace, String timezone, String dateFormat,
            String attributes) {
        super(charset);
        this.mapper = new ObjectMapper();

        this.attributeLoaders = new AttributeLoader[5];
        this.attributeLoaders[0] = new HostAttributeLoader();
        this.attributeLoaders[1] = new EventAttributeLoader(this);
        this.attributeLoaders[2] = new TimestampAttributeLoader(this);
        this.attributeLoaders[3] = new StacktraceAttributeLoader(this);
        this.attributeLoaders[4] = new ContextAttributeLoader(this);

        this.attributes = attributes;
        this.isRecursiveStackTrace = isRecursiveStackTrace;
        this.timezone = timezone;
        this.dateFormat = dateFormat;
    }

    @PluginFactory
    public static CustomJsonLayout createLayout(
            @PluginAttribute(value = "charset", defaultString = "UTF-8") Charset charset,
            @PluginAttribute(value = "isRecursiveStackTrace", defaultBoolean = true) boolean recursiveStacktrace,
            @PluginAttribute(value = "timezone") String timezone,
            @PluginAttribute(value = "dateFormat") String dateFormat,
            @PluginAttribute(value = "attributes", defaultString = DEFAULT_ATTRIBUTES) String attributes) {
        return new CustomJsonLayout(charset, recursiveStacktrace, timezone, dateFormat, attributes);
    }

    public boolean isRecursiveStackTrace() {
        return this.isRecursiveStackTrace;
    }

    public String getTimezone() {
        return this.timezone;
    }

    public String getDateFormat() {
        return this.dateFormat;
    }

    public LogEvent getEvent() {
        return this.event;
    }

    @Override
    public String toSerializable(LogEvent event) {
        this.event = event;

        Map<String, Object> format = new LinkedHashMap<>();
        for (String attribute : getAttributes()) {

            Object attributeValue = selectLoader(attribute).load(attribute);
            if (attributeValue != null) {
                format.put(attribute, attributeValue);
            }
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
        for (AttributeLoader attributeLoader : this.attributeLoaders) {
            if (attributeLoader.contains(key)) {
                return attributeLoader;
            }
        }

        return this.attributeLoaders[0];
    }
}
