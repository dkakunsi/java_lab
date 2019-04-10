package id.dkakunsi.lab.jlogging.attributeloader;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * <p>
 * Load attribute from application or system environment.
 * 
 * The supported attributes are:
 * <ul>
 * <li>hostname, which will produce {@link InetAddress} hostname</li>
 * <li>host, which will produce {@link InetAddress} hostname</li>
 * </ul>
 * </p>
 * 
 * @author dkakunsi
 */
public class HostAttributeLoader implements AttributeLoader {

    private static final String HOSTNAME = "hostname";

    private static final String HOST = "host";

    @Override
    public Object load(String key) {
        if (!contains(key)) {
            return null;
        }

        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostName();
        } catch (UnknownHostException e) {
            return e.getMessage();
        }
    }

    @Override
    public boolean contains(String key) {
        return HOSTNAME.equals(key) || HOST.equals(key);
    }
}
