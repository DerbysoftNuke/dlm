package com.derbysoft.nuke.dlm.server.config;

import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;

import java.util.*;

public class PropertiesUtils {

    public static Map<String, String> getProperties(PropertySources sources) {
        Map<String, String> result = new HashMap<>();
        for (PropertySource item : sources) {
            Object source = item.getSource();
            if (source instanceof Map) {
                result.putAll((Map<? extends String, ? extends String>) source);
            } else if (source instanceof AbstractEnvironment) {
                result.putAll(getProperties(((AbstractEnvironment) source).getPropertySources()));
            }
        }
        return result;
    }

    public static Set<String> getKeys(PropertySources properties, String prefix) {
        Set<String> keys = new HashSet<String>();
        for (PropertySource item : properties) {
            Object source = item.getSource();
            if (source instanceof Map) {
                Map<String, ?> map = Map.class.cast(source);
                for (Map.Entry<String, ?> entry : map.entrySet()) {
                    if (entry.getKey().startsWith(prefix)) {
                        keys.add(entry.getKey());
                    }
                }
            } else if (source instanceof AbstractEnvironment) {
                keys.addAll(getKeys(((AbstractEnvironment) source).getPropertySources(), prefix));
            }
        }
        return keys;
    }

    public static List<String> getList(PropertyResolver resolver, String key) {
        if (!resolver.containsProperty(key)) {
            return null;
        }

        String stringValue = resolver.getProperty(key);
        List<String> values = new ArrayList<String>();
        values.addAll(Arrays.asList(stringValue.split(",")));
        return values;
    }

}
