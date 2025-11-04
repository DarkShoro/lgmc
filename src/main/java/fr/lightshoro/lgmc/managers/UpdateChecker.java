package fr.lightshoro.lgmc.managers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.lightshoro.lgmc.Lgmc;

@SuppressWarnings("deprecation")
public class UpdateChecker {
    private final Lgmc plugin;
    private final String RELEASES_FEED_URL = "https://github.com/DarkShoro/lgmc/releases.atom";
    private final String CURRENT_VERSION;
    private String feedContent = null;
    
    private String latestVersion = null;
    private String latestVersionUrl = null;
    private boolean isPreRelease = false;
    private boolean updateAvailable = false;
    
    public UpdateChecker(Lgmc plugin) {
        this.plugin = plugin;
        this.CURRENT_VERSION = plugin.getDescription().getVersion();
    }
    
    /**
     * Check for updates asynchronously
     */
    public void checkForUpdates() {
        if (!plugin.getConfigManager().isUpdateCheckerEnabled()) {
            return;
        }
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                this.feedContent = fetchFeed();
                if (feedContent != null) {
                    parseReleases(feedContent);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
            }
            
            // Print update info after checking
            Bukkit.getScheduler().runTask(plugin, this::printUpdateInfo);
        });
    }
    
    /**
     * Fetch the Atom feed from GitHub
     */
    private String fetchFeed() {
        try {
            URL url = new URL(RELEASES_FEED_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "LGMC-Plugin");
            
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder content = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                
                reader.close();
                return content.toString();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error fetching releases feed: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Parse the Atom feed and extract release information
     */
    private void parseReleases(String feedContent) {
        boolean includePrereleases = plugin.getConfigManager().isUpdateCheckerIncludePrereleases();
        
        // Pattern to match entries in the Atom feed
        Pattern entryPattern = Pattern.compile("<entry>(.*?)</entry>", Pattern.DOTALL);
        Pattern titlePattern = Pattern.compile("<title>(.*?)</title>");
        Pattern linkPattern = Pattern.compile("<link\\s+href=\"(.*?)\"");
        Pattern categoryPattern = Pattern.compile("<category\\s+term=\"(.*?)\"");
        // Pattern to extract version number from title (e.g., "Release v5.0.0" or "v1.2.3-beta" or "5.0.2")
        Pattern versionPattern = Pattern.compile("v?(\\d+\\.\\d+(?:\\.\\d+)?(?:[.-]\\w+)*)");
        
        Matcher entryMatcher = entryPattern.matcher(feedContent);
        
        while (entryMatcher.find()) {
            String entry = entryMatcher.group(1);
            
            // Extract title
            Matcher titleMatcher = titlePattern.matcher(entry);
            String title = null;
            if (titleMatcher.find()) {
                title = titleMatcher.group(1).trim();
            }
            
            if (title == null || title.isEmpty()) {
                continue;
            }
            
            // Extract version number from title
            Matcher versionMatcher = versionPattern.matcher(title);
            String version = null;
            if (versionMatcher.find()) {
                version = versionMatcher.group(1); // Get the version without 'v' prefix
            }
            
            if (version == null || version.isEmpty()) {
                continue;
            }
            
            // Extract link
            Matcher linkMatcher = linkPattern.matcher(entry);
            String link = null;
            if (linkMatcher.find()) {
                link = linkMatcher.group(1);
            }
            
            // Check if it's a pre-release
            Matcher categoryMatcher = categoryPattern.matcher(entry);
            boolean isPreRelease = false;
            while (categoryMatcher.find()) {
                if ("pre-release".equals(categoryMatcher.group(1))) {
                    isPreRelease = true;
                    break;
                }
            }
            
            // Skip pre-releases if not enabled
            if (isPreRelease && !includePrereleases) {
                continue;
            }
            
            // This is the latest release we care about
            latestVersion = version;
            latestVersionUrl = link;
            this.isPreRelease = isPreRelease;
            
            // Compare versions
            updateAvailable = !version.equals(CURRENT_VERSION) && isNewerVersion(version, CURRENT_VERSION);
            break;
        }
    }
    
    /**
     * Simple version comparison (assumes semantic versioning)
     */
    private boolean isNewerVersion(String latest, String current) {
        try {
            // Remove 'v' prefix if present
            String latestClean = latest.replaceFirst("^v", "");
            String currentClean = current.replaceFirst("^v", "");
            
            // Split by dots and compare
            String[] latestParts = latestClean.split("[.-]");
            String[] currentParts = currentClean.split("[.-]");
            
            int maxLength = Math.max(latestParts.length, currentParts.length);
            
            for (int i = 0; i < maxLength; i++) {
                int latestPart = i < latestParts.length ? parseVersionPart(latestParts[i]) : 0;
                int currentPart = i < currentParts.length ? parseVersionPart(currentParts[i]) : 0;
                
                if (latestPart > currentPart) {
                    return true;
                } else if (latestPart < currentPart) {
                    return false;
                }
            }
            
            return false; // Versions are equal
        } catch (Exception e) {
            // If parsing fails, assume it's newer to be safe
            return true;
        }
    }
    
    /**
     * Parse a version part to integer, ignoring non-numeric suffixes
     */
    private int parseVersionPart(String part) {
        try {
            // Extract only digits from the beginning
            Matcher matcher = Pattern.compile("^(\\d+)").matcher(part);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Determine if a given version is known to the git repository
     * @return true if the version exists in the git tags/releases, false otherwise
     */

    public boolean isVersionKnown(String version) {
        if (feedContent == null) {
            return false;
        }

        // Pattern to extract version number from title
        Pattern versionPattern = Pattern.compile("v?(\\d+\\.\\d+(?:\\.\\d+)?(?:[.-]\\w+)*)");
        Pattern titlePattern = Pattern.compile("<title>(.*?)</title>");
        Matcher titleMatcher = titlePattern.matcher(feedContent);
        
        while (titleMatcher.find()) {
            String title = titleMatcher.group(1).trim();
            Matcher versionMatcher = versionPattern.matcher(title);
            if (versionMatcher.find()) {
                String foundVersion = versionMatcher.group(1);
                if (foundVersion.equals(version)) {
                    return true;
                }
            }
        }

        return false;
    }
    
    /**
     * Print update information to console
     */
    public void printUpdateInfo() {
        if (!plugin.getConfigManager().isUpdateCheckerEnabled()) {
            return;
        }
        
        plugin.getLogger().info("========================================");
        plugin.getLogger().info("LGMC Version Checker");
        plugin.getLogger().info("Current version: " + CURRENT_VERSION);
        
        if (latestVersion != null) {
            plugin.getLogger().info("Latest version: " + latestVersion + (isPreRelease ? " (pre-release)" : ""));
            
            if (!isVersionKnown(CURRENT_VERSION)) {
                plugin.getLogger().warning("You are running a custom, dev or unknown version!");
            }
            else if (updateAvailable) {
                plugin.getLogger().warning("A new version is available!");
                if (latestVersionUrl != null) {
                    plugin.getLogger().info("Download: " + latestVersionUrl);
                }
            } else {
                plugin.getLogger().info("You are running the latest version!");
            }
        } else {
            plugin.getLogger().warning("Could not check for updates.");
        }
        
        plugin.getLogger().info("========================================");
    }
    
    /**
     * Send update notification to a player (admin)
     */
    public void notifyPlayer(Player player) {
        if (!plugin.getConfigManager().isUpdateCheckerEnabled()) {
            return;
        }
        
        if (!player.hasPermission("lgmc.admin") && !player.isOp()) {
            return;
        }
        
        if (latestVersion == null) {
            return;
        }

        if (!isVersionKnown(CURRENT_VERSION) || !updateAvailable) {
            return;
        }
        
        player.sendMessage("§8§m                                                  ");
        player.sendMessage("§6§lLGMC Update Checker");
        player.sendMessage("§7Current version: §f" + CURRENT_VERSION);
        player.sendMessage("§7Latest version: §f" + latestVersion + (isPreRelease ? " §e(pre-release)" : ""));
        
        if (updateAvailable) {
            player.sendMessage("§a§lA new version is available!");
            if (latestVersionUrl != null) {
                player.sendMessage("§7Download: §b" + latestVersionUrl);
            }
        }
    }
    
    public boolean isUpdateAvailable() {
        return updateAvailable;
    }
    
    public String getLatestVersion() {
        return latestVersion;
    }
    
    public String getLatestVersionUrl() {
        return latestVersionUrl;
    }
}
