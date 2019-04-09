package id.dkakunsi.lab.jlogging.attributeloader;

import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LogEvent;

/**
 * <p>
 * Load attributes from {@link ThreadContext} inside {@link LogEvent}. Each key
 * will be supported as long as it exists in the context. {@code null} will be
 * return if it is not exists.
 * </p>
 * 
 * @author dkakunsi
 */
public class ContextAttributeLoader extends EventAttributeLoader {

    @Override
    public Object get(String key) {
        return this.event.getContextData().getValue(key);
    }

    @Override
    public boolean contain(String key) {
        return true;
    }
}
