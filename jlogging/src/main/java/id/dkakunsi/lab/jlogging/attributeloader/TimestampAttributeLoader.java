package id.dkakunsi.lab.jlogging.attributeloader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import id.dkakunsi.lab.jlogging.layout.CustomJsonLayout;

/**
 * Loader for ISO-8601.
 * 
 * @author dkakunsi
 */
public class TimestampAttributeLoader extends EventAttributeLoader {

    private static final String TIMESTAMP = "timestamp";

    public TimestampAttributeLoader(CustomJsonLayout layout) {
        super(layout);
    }

    @Override
    public Object load(String key) {
        if (!contains(key)) {
            return null;
        }

        TimeZone tz = TimeZone.getTimeZone(this.layout.getTimezone());
        DateFormat df = new SimpleDateFormat(this.layout.getDateFormat());
        df.setTimeZone(tz);

        return df.format(new Date(this.layout.getEvent().getTimeMillis()));
    }

    @Override
    public boolean contains(String key) {
        return TIMESTAMP.equals(key);
    }
}
