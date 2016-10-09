package listeners;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import util.ConcUtil;
import util.Property;

import java.util.Random;

public class GifListener {
    private static final Logger logger = LoggerFactory.getLogger(GifListener.class);
    private static final String GIF_TOKEN = Property.getInstance().getProperty("giphy");

    @EventSubscriber
    public void onMessageRecievedEvent(MessageReceivedEvent event) {
        IMessage message = event.getMessage();
        String content = message.getContent();

        if (content.startsWith("!gif ")) {
            ConcUtil.processCommand(() -> gifCommand(event), logger);
        }
    }

    private void gifCommand(MessageReceivedEvent event) {
        String content = event.getMessage().getContent();
        String requestString = content.trim().split(" ", 2)[1].trim();
        IChannel channel = event.getMessage().getChannel();
        IUser user = event.getMessage().getAuthor();
        HttpResponse<JsonNode> jsonResponse = null;

        try {
            jsonResponse = Unirest.get("http://api.giphy.com/v1/gifs/search?q={q}&api_key={api_key}")
                    .routeParam("q", requestString)
                    .routeParam("api_key", GIF_TOKEN)
                    .asJson();
        } catch (UnirestException e) {
            logger.warn(e.getMessage());
        }

        JSONArray array = (JSONArray) jsonResponse.getBody().getObject().get("data");
        JSONObject randomGif = (JSONObject) array.get(new Random(12).nextInt(array.length()));
        JSONObject imagesUrl = (JSONObject) randomGif.get("images");
        JSONObject oneGif = (JSONObject) imagesUrl.get("original");
        String gifUrl = oneGif.getString("url");

        try {
            channel.sendMessage(user.mention() + " " + gifUrl);
        } catch (MissingPermissionsException | DiscordException | RateLimitException e) {
            logger.warn(e.getMessage());
        }
    }
}