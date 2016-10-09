import listeners.GifListener;
import listeners.MusicListener;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;


public class MusicBot {
    public static void main(String[] args) {
        IDiscordClient discordClient = DiscordClient.getClient();
        EventDispatcher discordDispatcher = discordClient.getDispatcher();

        discordDispatcher.registerListener(new MusicListener());
        discordDispatcher.registerListener(new GifListener());
    }
}
