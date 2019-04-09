package id.dkakunsi.lab.jlogging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final Logger LOG = LogManager.getLogger(App.class);

    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        LOG.info("Run Log");

        try {
            catchException();
        } catch (Exception ex) {
            LOG.error("Exception", ex);
        }
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
