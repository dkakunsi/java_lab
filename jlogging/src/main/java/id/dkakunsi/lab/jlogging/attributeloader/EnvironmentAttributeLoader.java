package id.dkakunsi.lab.jlogging.attributeloader;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * <p>
 * Load attribute from application or system environment.
 * 
 * The supported attribut are:
 * <ul>
 * <li>hostname, which will produce {@link InetAddress} hostname</li>
 * <li>host, which will produce {@link InetAddress} hostname</li>
 * </ul>
 * </p>
 * 
 * @author dkakunsi
 */
public class EnvironmentAttributeLoader implements AttributeLoader {

    private static final String HOSTNAME = "hostname";

    private static final String HOST = "host";

    @Override
    public Object get(String key) {
        switch (key) {
        case HOSTNAME:
            return getHostname();
        case HOST:
            return getHostname();
        default:
            return null;
        }
    }

    @Override
    public boolean contain(String key) {
        return HOSTNAME.equals(key) || HOST.equals(key);
    }

    private String getHostname() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostName();
        } catch (UnknownHostException e) {
            return e.getMessage();
        }
    }
}