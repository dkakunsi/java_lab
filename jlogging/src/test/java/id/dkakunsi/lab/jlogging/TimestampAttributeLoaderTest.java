package id.dkakunsi.lab.jlogging;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import id.dkakunsi.lab.jlogging.attributeloader.TimestampAttributeLoader;

import org.junit.Test;

/**
 * TimestampAttributeLoaderTest
 */
public class TimestampAttributeLoaderTest {

    private static final String REGEX_PATTERN = "\\d{4}-[0-1]\\d-[0-3]\\dT[0-2]\\d:[0-5]\\d:[0-6]\\d.\\d{3}(\\+|\\-)\\d{4}";

    @Test
    public void testTimestamp() {
        Date date = new Date();
        Object timestamp = TimestampAttributeLoader.load(date, null, null);
        Matcher matcher = createMatcher((String) timestamp);

        assertTrue(matcher.matches());
    }

    private static Matcher createMatcher(String input) {
        Pattern pattern = Pattern.compile(REGEX_PATTERN);
        return pattern.matcher(input);
    }
}
