package listeners;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;


public class MusicListeners {
    @EventSubscriber
    public void onMessageRecievedEvent(MessageReceivedEvent event) {
        IMessage message = event.getMessage();
        String content = message.getContent();
        IVoiceChannel channel;

        try {
            if (content.equalsIgnoreCase("!summon")) {
                channel = message.getAuthor().getConnectedVoiceChannels().get(0);
                channel.join();
            }
            if (content.startsWith("!play ")) {
                playCommand(event);
            }
        } catch (MissingPermissionsException | DiscordException | RateLimitException e) {
            e.printStackTrace();
        }
    }

    private void playCommand(MessageReceivedEvent event) throws MissingPermissionsException, DiscordException, RateLimitException {
        IMessage message = event.getMessage();
        String content = message.getContent();
        IChannel channel = message.getChannel();

        if (channel.isPrivate()) {
            channel.sendMessage("Dude, fuck off");
            return;
        }

        String urlString = content.trim().split(" ", 2)[1].trim();
        if (urlString.isEmpty()) {
            channel.sendMessage("You must provide valid URL");
            return;
        }

        if (urlString.contains("youtube")) {
            channel.sendMessage("youtube");
        }

        if (urlString.contains("soundcloud")) {
            channel.sendMessage("souncloud");
        }

        //AudioPlayer audioPlayer = AudioPlayer.getAudioPlayerForGuild(channel.getGuild());
    }
}
