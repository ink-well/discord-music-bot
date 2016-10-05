package util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Tokens {
    Properties properties = new Properties();
    InputStream stream = null;

    private Tokens() {
        stream = new FileInputStream("tokens.properties");
    }
}
