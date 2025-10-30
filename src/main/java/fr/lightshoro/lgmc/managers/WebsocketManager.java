package fr.lightshoro.lgmc.managers;

import com.google.gson.JsonArray;
import org.bukkit.entity.Player;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.net.URISyntaxException;
import fr.lightshoro.lgmc.Lgmc;
import java.util.logging.Logger;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WebsocketManager {

    private WebSocketClient socket;
    private final String uri;
    private final String secret;
    private final Lgmc plugin;
    private final Logger logger;

    public WebsocketManager(String websocketUri, String sharedSecret, Lgmc plugin) {
        this.uri = websocketUri;
        this.secret = sharedSecret;
        this.plugin = plugin;
        this.logger = this.plugin.getLogger();
        connect();
    }

    public WebsocketManager(boolean enable, Lgmc plugin) {
        this.uri = null;
        this.secret = null;
        this.plugin = plugin;
        this.logger = this.plugin.getLogger();
        this.socket = null;
        logger.info("Not connecting to WebSocket as it is disabled in config.");
    }

    private void connect() {
        try {
            socket = new WebSocketClient(new URI(uri)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    logger.info("✅ Connected to Discord WebSocket, authenticating...");
                    authenticate();
                }

                @Override
                public void onMessage(String message) {
                    logger.info("WebSocket message received: " + message);
                    // Handle incoming messages if needed
                    // Parse message as Json

                    try {
                        JsonObject jsonMessage = JsonParser.parseString(message).getAsJsonObject();
                        String type = jsonMessage.get("type").getAsString();

                        switch (type) {
                            case "auth_ok":
                                logger.info("🔐 WebSocket authentication successful");
                                break;
                            case "auth_failed":
                                logger.severe("🔐 WebSocket authentication failed");
                                break;
                            case "link_ok":
                                String username = jsonMessage.get("username").getAsString();
                                logger.info("🔗 Discord account linked successfully for Minecraft user: " + username);
                                Player linkedPlayer = plugin.getServer().getPlayerExact(username);
                                if (linkedPlayer != null && linkedPlayer.isOnline()) {
                                    linkedPlayer.sendMessage(plugin.getLanguageManager().getMessage("commands.linkDiscord.linked"));
                                }
                                break;
                            case "link_failed_unknown":
                                String usernameFail = jsonMessage.get("username").getAsString();
                                logger.warning("🔗 Discord account link failed (unknown UUID) for Minecraft user: " + usernameFail);
                                Player failedPlayer = plugin.getServer().getPlayerExact(usernameFail);
                                if (failedPlayer != null && failedPlayer.isOnline()) {
                                    failedPlayer.sendMessage(plugin.getLanguageManager().getMessage("commands.linkDiscord.uuid-invalid"));
                                }
                                break;

                        }
                    } catch (Exception e) {
                        logger.severe("Failed to parse WebSocket message: " + e.getMessage());
                    }

                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    logger.warning("WebSocket closed: " + reason + " - Reconnecting in 10s...");
                    attemptreconnect();
                }

                @Override
                public void onError(Exception ex) {
                    logger.severe("WebSocket error: " + ex.getMessage());
                    // Attempt to reconnect on error
                    attemptreconnect();
                }
            };

            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void authenticate() {
        if (this.isDisabled()) return;
        if (socket != null && socket.isOpen()) {
            String authMessage = String.format("{\"type\":\"auth\",\"secret\":\"%s\"}", secret);
            socket.send(authMessage);
            logger.info("🔐 Authentication message sent");
        }
    }

    private void attemptreconnect() {
        if (this.isDisabled()) return;
        Integer interval = plugin.getConfigManager().getWebsocketReconnectInterval();
        // Interval to ticks
        plugin.getServer().getScheduler().runTaskLater(plugin, this::connect, interval * 20L);
    }

    public void sendAction(String action) {
        if (this.isDisabled()) return;
        if (socket != null && socket.isOpen()) {
            String msg = String.format("{\"action\":\"%s\"}", action);
            socket.send(msg);
        }
    }

    public void sendPlayerDied(String playerName) {
        if (this.isDisabled()) return;
        if (socket != null && socket.isOpen()) {
            String msg = String.format("{\"action\":\"player_died\",\"player\":\"%s\"}", playerName);
            socket.send(msg);
        }
    }

    public void sendMuteAll() {
        sendAction("muteall");
    }

    public void sendDemuteAll() {
        sendAction("demuteall");
    }

    public void sendGameStart() {
        sendAction("gamestart");
    }

    public void sendGameOver() {
        sendAction("gameover");
    }

    public void sendLink(String uuid, String username) {
        if (this.isDisabled()) return;
        if (socket != null && socket.isOpen()) {
            String msg = String.format("{\"action\":\"link\",\"uuid\":\"%s\",\"username\":\"%s\"}", uuid, username);
            socket.send(msg);
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isOpen();
    }

    public void close() {
        if (socket != null) socket.close();
    }

    public boolean isDisabled() {
        return !plugin.getConfigManager().isWebsocketEnabled();
    }
}
