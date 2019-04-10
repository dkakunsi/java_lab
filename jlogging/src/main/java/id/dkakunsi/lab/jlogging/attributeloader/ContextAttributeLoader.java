package id.dkakunsi.lab.jlogging.attributeloader;

import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LogEvent;

import id.dkakunsi.lab.jlogging.layout.CustomJsonLayout;

/**
 * <p>
 * Load attributes from {@link ThreadContext} inside {@link LogEvent}. Each key
 * will be supported as long as it exists in the context. {@code null} will be
 * returned if it is not exists.
 * </p>
 * 
 * @author dkakunsi
 */
public class ContextAttributeLoader extends EventAttributeLoader {

    public ContextAttributeLoader(CustomJsonLayout layout) {
        super(layout);
    }

    @Override
    public Object load(String key) {
        return this.layout.getEvent().getContextData().getValue(key);
    }

    @Override
    public boolean contains(String key) {
        return load(key) != null;
    }
}
