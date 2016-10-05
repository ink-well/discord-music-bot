package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Property {
    private static final Logger logger = LoggerFactory.getLogger(Property.class);
    private static Map<String, String> propertiesMap = null;
    private static final String filename = "tokens.properties";
    private static Property instance;

    private Property() {
        Properties properties = new Properties();
        InputStream stream = getClass().getClassLoader().getResourceAsStream(filename);
        if (stream == null) {
            logger.warn("Unable to find " + filename);
        }

        try {
            properties.load(stream);
        } catch (IOException e) {
            logger.warn(e.getMessage());
        }

        propertiesMap = new HashMap<>();
        Enumeration<?> propertyNames = properties.propertyNames();
        while (propertyNames.hasMoreElements()) {
            String key = (String) propertyNames.nextElement();
            String value = properties.getProperty(key);
            propertiesMap.put(key, value);
        }
    }

    public static Property getInstance() {
        if (instance == null) {
            instance = new Property();
        }
        return instance;
    }

    public String getProperty(String key) {
        return propertiesMap.get(key);
    }
}
