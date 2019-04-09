package id.dkakunsi.lab.jlogging.attributeloader;

/**
 * Load attribute with the specified {@code key} from the given {@code context}.
 * 
 * @author dkakunsi
 */
public interface AttributeLoader {

    /**
     * Get attribute from context with the give {@code key}.
     * 
     * @param key attribute key
     * @return attribute in K:V pair
     */
    Object get(String key);

    /**
     * Whether this loader can load attribute with the given {@code key}.
     * 
     * @param key attribute's key
     * @return true if it can load the attribute, false it cannot
     */
    boolean contain(String key);

}