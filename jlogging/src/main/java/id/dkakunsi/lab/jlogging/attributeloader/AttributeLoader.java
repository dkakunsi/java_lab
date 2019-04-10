package id.dkakunsi.lab.jlogging.attributeloader;

/**
 * Load attribute with the specified {@code key} from its context.
 * 
 * @author dkakunsi
 */
public interface AttributeLoader {

    /**
     * Load attribute from context with the give {@code key}.
     * 
     * @param key attribute key
     * @return attribute in K:V pair
     */
    Object load(String key);

    /**
     * Whether this loader can load attribute with the given {@code key}.
     * 
     * @param key attribute's key
     * @return true if it can load the attribute, false if it cannot
     */
    boolean contains(String key);

}