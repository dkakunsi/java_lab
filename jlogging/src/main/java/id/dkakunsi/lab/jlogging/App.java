package id.dkakunsi.lab.jlogging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final Logger LOG = LogManager.getLogger(App.class);

    public static void main( String[] args )
    {
        ThreadContext.put("correlationId", "0xgdtrsd2345634heu");
        ThreadContext.put("service", "Service A");

        System.out.println( "Hello World!" );
        LOG.info("Run Log");

        try {
            catchException();
        } catch (Exception ex) {
            LOG.error("Exception", ex);
        }

        ThreadContext.clearAll();
    }

    public static void catchException() throws Exception {
        try {
            throwException();
        } catch (Exception ex) {
            throw new Exception(ex);
        }
    }

    public static void throwException() {
        throw new ArithmeticException("ArithmeticException");
    }
}
