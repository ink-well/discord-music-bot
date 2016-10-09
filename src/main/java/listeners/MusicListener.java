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
import util.ConcUtil;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.util.stream.Collectors;


public class MusicListener {
    private final static Logger logger = LoggerFactory.getLogger(MusicListener.class);

    @EventSubscriber
    public void onMessageRecievedEvent(MessageReceivedEvent event) {
        IMessage message = event.getMessage();
        String content = message.getContent();

        if (content.equalsIgnoreCase("!summon")) {
            ConcUtil.processCommand(() -> summonCommand(event), logger);
        }
        if (content.startsWith("!play ")) {
            ConcUtil.processCommand(() -> playCommand(event), logger);
        }
        if (content.equalsIgnoreCase("!skip")) {
            ConcUtil.processCommand(() -> skipCommand(event), logger);
        }
        if (content.equalsIgnoreCase("!stop")) {
            ConcUtil.processCommand(() -> stopCommand(event), logger);
        }
    }

    private void skipCommand(MessageReceivedEvent event) {
        IMessage message = event.getMessage();
        AudioPlayer audioPlayer = AudioPlayer.getAudioPlayerForGuild(message.getGuild());
        try {
            message.getChannel().sendMessage("Skipping current song");
        } catch (MissingPermissionsException | DiscordException | RateLimitException e) {
            logger.warn(e.getMessage());
        }
        audioPlayer.skip();
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

    private void stopCommand(MessageReceivedEvent event) {
        IMessage message = event.getMessage();
        AudioPlayer audioPlayer = AudioPlayer.getAudioPlayerForGuild(message.getGuild());

        audioPlayer.clear();
        try {
            message.getChannel().sendMessage("Stopped the playback");
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
                channel.sendMessage("Dude, I don't respond to private messages!");
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
        InputStream stream = null;
        try {
            Process process = builder.start();
            try {
                stream = process.getInputStream();
                player.queue(AudioSystem.getAudioInputStream(stream));
                return true;
            } catch (UnsupportedAudioFileException e) {
                logger.warn("Could not queue audio", e);
                process.destroyForcibly();
            }
        } catch (EOFException e) {
            String result = new BufferedReader(new InputStreamReader(stream))
                    .lines().collect(Collectors.joining("\n"));
            logger.warn("EOF Reached!\n", result);
        } catch (IOException e) {
            logger.warn("Could not start process", e);
        }
        return false;
    }
}
