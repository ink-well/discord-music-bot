package listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.audio.AudioPlayer;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;


public class MusicListeners {
    private final static Logger logger = LoggerFactory.getLogger(MusicListeners.class);

    public static CompletableFuture<Void> processCommand(Runnable runnable) {
        return CompletableFuture.runAsync(runnable)
                .exceptionally(t -> {
                    logger.warn("Could not complete command", t);
                    return null;
                });
    }

    @EventSubscriber
    public void onMessageRecievedEvent(MessageReceivedEvent event) {
        IMessage message = event.getMessage();
        String content = message.getContent();

        if (content.equalsIgnoreCase("!summon")) {
            processCommand(() -> summonCommand(event));
        }
        if (content.startsWith("!play ")) {
            processCommand(() -> playCommand(event));
        }
    }

    private void summonCommand(MessageReceivedEvent event) {
        IMessage message = event.getMessage();
        IVoiceChannel channel = message.getAuthor().getConnectedVoiceChannels().get(0);
        try {
            channel.join();
            message.getChannel().sendMessage("Joined '" + channel.getName() + "'.");
        } catch (MissingPermissionsException | DiscordException | RateLimitException e) {
            logger.warn(e.getMessage());
        }
    }

    private void playCommand(MessageReceivedEvent event) {
        IMessage message = event.getMessage();
        String content = message.getContent();
        IChannel channel = message.getChannel();
        AudioPlayer audioPlayer = AudioPlayer.getAudioPlayerForGuild(message.getGuild());

        try {
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
                if (playFromYoutube(urlString, audioPlayer)) {
                    channel.sendMessage("Added to the queue: " + urlString);
                }
            }
        } catch (MissingPermissionsException | DiscordException | RateLimitException e) {
            logger.warn(e.getMessage());
        }
    }

    private boolean playFromYoutube(String url, AudioPlayer player) {
        String osName = System.getProperty("os.name").contains("Windows") ? "youtube-dl.exe" : "youtube-dl";
        ProcessBuilder builder = new ProcessBuilder(osName, "-q", "-f", "worstaudio",
                "--exec", "ffmpeg -hide_banner -nostats -loglevel panic -y -i {} -vn -q:a 5 -f mp3 pipe:1", "-o",
                "%(id)s", "--", url);
        try {
            Process process = builder.start();
            try {
                player.queue(AudioSystem.getAudioInputStream(process.getInputStream()));
                return true;
            } catch (UnsupportedAudioFileException e) {
                logger.warn("Could not queue audio", e);
                process.destroyForcibly();
            }
        } catch (IOException e) {
            logger.warn("Could not start process", e);
        }
        return false;
    }
}
