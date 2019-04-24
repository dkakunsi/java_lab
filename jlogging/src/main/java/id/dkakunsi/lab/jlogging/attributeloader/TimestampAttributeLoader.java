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

    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public TimestampAttributeLoader(CustomJsonLayout layout) {
        super(layout);
    }

    @Override
    public Object load(String key) {
        if (!contains(key)) {
            return null;
        }
        return load(new Date(this.layout.getEvent().getTimeMillis()), this.layout.getTimezone(),
                this.layout.getDateFormat());
    }

    public static Object load(Date date, String timezone, String dateFormat) {
        if (dateFormat == null) {
            dateFormat = DEFAULT_DATE_FORMAT;
        }
        DateFormat df = new SimpleDateFormat(dateFormat);

        TimeZone tz = TimeZone.getDefault();
        if (timezone != null) {
            tz = TimeZone.getTimeZone(timezone);
        }
        df.setTimeZone(tz);

        return df.format(date);
    }

    @Override
    public boolean contains(String key) {
        return TIMESTAMP.equals(key);
    }
}
