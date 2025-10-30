package fr.lightshoro.lgmc.managers;

import fr.lightshoro.lgmc.Lgmc;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire centralisé pour les packs de ressources
 * Gère le téléchargement et l'application des packs en fonction de la version du client
 */
public class ResourcePackManager implements Listener {
    private final Lgmc plugin;
    private final Map<Integer, String> protocolVersionMap;
    private static final String RESOURCE_PACK_URL_TEMPLATE = "https://cdn.eradium.fr/lgrsp/public/%s.zip";
    private static final String RESOURCE_PACK_HASH_URL_TEMPLATE = "https://cdn.eradium.fr/lgrsp/public/%s.txt";

    public ResourcePackManager(Lgmc plugin) {
        this.plugin = plugin;
        this.protocolVersionMap = initializeProtocolVersionMap();
    }

    /**
     * Initialise la correspondance entre version de protocole et version Minecraft
     */
    private Map<Integer, String> initializeProtocolVersionMap() {
        Map<Integer, String> map = new HashMap<>();
        map.put(767, "1.21");
        map.put(768, "1.21.1");
        map.put(769, "1.21.2");
        map.put(770, "1.21.3");
        map.put(771, "1.21.4");
        map.put(772, "1.21.5");
        map.put(773, "1.21.6");
        map.put(774, "1.21.7");
        map.put(775, "1.21.8");
        map.put(776, "1.21.9");
        map.put(777, "1.21.10");
        return map;
    }

    /**
     * Configure le pack de ressources pour un joueur
     */
    public void applyResourcePack(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String minecraftVersion = getPlayerMinecraftVersion(player);
                String packUrl = String.format(RESOURCE_PACK_URL_TEMPLATE, minecraftVersion);
                String hashUrl = String.format(RESOURCE_PACK_HASH_URL_TEMPLATE, minecraftVersion);

                String sha1Hash = downloadHash(hashUrl);
                if (sha1Hash == null || sha1Hash.isEmpty()) {
                    plugin.getLogger().warning("Impossible de récupérer le hash SHA1 pour la version " + minecraftVersion);
                    return;
                }

                // Appliquer le pack sur le thread principal
                Bukkit.getScheduler().runTask(plugin, () -> player.setResourcePack(packUrl, sha1Hash));
                plugin.getLogger().info("Pack de ressources appliqué au joueur " + player.getName() + " (version " + minecraftVersion + ")");
            } catch (Exception e) {
                plugin.getLogger().warning("Impossible de configurer le pack de ressources pour le joueur " + player.getName() + ": " + e.getMessage());
            }
        });
    }

    /**
     * Récupère la version Minecraft du joueur en fonction de sa version de protocole
     */
    private String getPlayerMinecraftVersion(Player player) throws Exception {
        int protocolVersion = getProtocolVersion(player);
        return protocolVersionMap.getOrDefault(protocolVersion, "1.21");
    }

    /**
     * Récupère la version de protocole du joueur via réflexion
     */
    private int getProtocolVersion(Player player) throws Exception {
        Class<?> craftPlayerClass = getCraftPlayerClass();
        Object craftPlayer = craftPlayerClass.cast(player);
        Object entityPlayer = craftPlayerClass.getMethod("getHandle").invoke(craftPlayer);
        Object playerConnection = entityPlayer.getClass().getField("c").get(entityPlayer);
        return (int) playerConnection.getClass().getField("b").get(playerConnection);
    }

    /**
     * Récupère la classe CraftPlayer avec gestion de la version du serveur
     */
    private Class<?> getCraftPlayerClass() throws ClassNotFoundException {
        String packageName = plugin.getServer().getClass().getPackage().getName();
        String[] parts = packageName.split("\\.");
        String cbVersion = parts.length >= 4 ? parts[3] : "";
        String className = "org.bukkit.craftbukkit" + (cbVersion.isEmpty() ? "" : "." + cbVersion) + ".entity.CraftPlayer";
        return Class.forName(className);
    }

    /**
     * Télécharge le hash SHA1 depuis l'URL fournie
     */
    private String downloadHash(String hashUrl) {
        try {
            URI hashFileUri = new URI(hashUrl);
            Path tempFile = Files.createTempFile("hash", ".txt");
            Files.copy(hashFileUri.toURL().openStream(), tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            String hash = Files.readString(tempFile).trim();
            Files.deleteIfExists(tempFile);
            return hash;
        } catch (Exception e) {
            plugin.getLogger().warning("Impossible de télécharger le hash depuis " + hashUrl + ": " + e.getMessage());
            return null;
        }
    }

    @EventHandler
    public void onPlayerResourcePackStatus(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();
        PlayerResourcePackStatusEvent.Status status = event.getStatus();

        switch (status) {
            case SUCCESSFULLY_LOADED:
                plugin.getLogger().info("Le pack de ressources a été appliqué avec succès pour le joueur " + player.getName());
                break;
            case FAILED_DOWNLOAD:
                plugin.getLogger().warning("Échec du téléchargement du pack de ressources pour le joueur " + player.getName());
                kickPlayer(player, "Impossible de télécharger le pack de ressources requis.");
                break;
            case ACCEPTED:
                plugin.getLogger().info("Le joueur " + player.getName() + " a accepté le pack de ressources");
                break;
            case DECLINED:
                plugin.getLogger().info("Le joueur " + player.getName() + " a décliné le pack de ressources - Expulsion en cours...");
                kickPlayer(player, "Le pack de ressources est obligatoire pour rejoindre ce serveur.");
                break;
        }
    }

    /**
     * Expulse un joueur du serveur avec un message personnalisé
     */
    private void kickPlayer(Player player, String reason) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.kick(Component.text(reason));
            plugin.getLogger().warning("Le joueur " + player.getName() + " a été expulsé : " + reason);
        });
    }
}
