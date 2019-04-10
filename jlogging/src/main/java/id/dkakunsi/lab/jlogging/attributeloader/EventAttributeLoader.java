package id.dkakunsi.lab.jlogging.attributeloader;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.core.LogEvent;

import id.dkakunsi.lab.jlogging.layout.CustomJsonLayout;

/**
 * <p>
 * Load attribute from the {@link LogEvent}. Current supported attributes are:
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

    private static final String THREAD = "thread";

    private static final String CATEGORY = "category";

    private static final String LEVEL = "level";

    private static final String MESSAGE = "message";

    private static final List<String> SUPPORTED_ATTRIBUTES;

    static {
        SUPPORTED_ATTRIBUTES = Arrays.asList(new String[] { THREAD, CATEGORY, LEVEL, MESSAGE });
    }

    /**
     * The calling layout.
     */
    protected CustomJsonLayout layout;

    public EventAttributeLoader(CustomJsonLayout layout) {
        this.layout = layout;
    }

    @Override
    public Object load(String key) {
        switch (key) {
            case THREAD:
                return this.layout.getEvent().getThreadName();
            case CATEGORY:
                return this.layout.getEvent().getLoggerName();
            case LEVEL:
                return this.layout.getEvent().getLevel().getStandardLevel();
            case MESSAGE:
                return this.layout.getEvent().getMessage().getFormattedMessage();
            default:
                return null;
        }
    }

    @Override
    public boolean contains(String key) {
        return SUPPORTED_ATTRIBUTES.contains(key);
    }
}
