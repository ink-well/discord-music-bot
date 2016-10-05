import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;
import util.Property;


public class DiscordClient {
    private final static Logger log = LoggerFactory.getLogger(DiscordClient.class);
    private static IDiscordClient client;
    private final static String TOKEN = Property.getInstance().getProperty("discord");
    private DiscordClient() {
    }

    public static IDiscordClient getClient() {
        if (client == null) {
            try {
                client = createClient();
            } catch (DiscordException e) {
                log.warn(e.getErrorMessage());
            }
        }
        return client;
    }

    private static IDiscordClient createClient() throws DiscordException {
        ClientBuilder builder = new ClientBuilder();
        builder.withToken(TOKEN);
        return builder.login();
    }
}
