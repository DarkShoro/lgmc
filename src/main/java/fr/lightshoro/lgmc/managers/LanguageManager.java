package fr.lightshoro.lgmc.managers;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import fr.lightshoro.lgmc.Lgmc;

/**
 * Gestionnaire de langue pour supporter plusieurs langues
 */
@SuppressWarnings("deprecation")
public class LanguageManager {
    private final Lgmc plugin;
    private FileConfiguration langConfig;
    private String currentLang;
    private final Map<String, String> messageCache = new HashMap<>();

    public LanguageManager(Lgmc plugin, String language) {
        this.plugin = plugin;
        this.currentLang = language;
        exportAllLanguageFiles(); // Exporter tous les fichiers de langue disponibles
        loadLanguage();
    }

    /**
     * Exporte tous les fichiers de langue disponibles dans le plugin
     */
    public void exportAllLanguageFiles() {
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        // Liste des langues disponibles
        String[] availableLanguages = {"fr", "en"};

        for (String lang : availableLanguages) {
            File langFile = new File(langFolder, lang + ".yml");

            // N'exporter que si le fichier n'existe pas déjà
            if (!langFile.exists()) {
                InputStream defLangStream = plugin.getResource("lang/" + lang + ".yml");
                if (defLangStream != null) {
                    try {
                        java.nio.file.Files.copy(defLangStream, langFile.toPath());
                        plugin.getLogger().info("Fichier de langue " + lang + ".yml exporté");
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "Erreur lors de l'export du fichier de langue " + lang + ".yml", e);
                    }
                } else {
                    plugin.getLogger().warning("Fichier de langue " + lang + ".yml non trouvé dans les ressources");
                }
            }
        }
    }

    /**
     * Charge le fichier de langue
     */
    public void loadLanguage() {
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        File langFile = new File(langFolder, currentLang + ".yml");

        // Créer le fichier de langue s'il n'existe pas
        if (!langFile.exists()) {
            InputStream defLangStream = plugin.getResource("lang/" + currentLang + ".yml");
            if (defLangStream != null) {
                try {
                    java.nio.file.Files.copy(defLangStream, langFile.toPath());
                    plugin.getLogger().info("Fichier de langue " + currentLang + ".yml créé");
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Erreur lors de la création du fichier de langue", e);
                    // Fallback vers français
                    currentLang = "fr";
                    langFile = new File(langFolder, "fr.yml");
                }
            } else {
                plugin.getLogger().warning("Langue " + currentLang + " non trouvée, utilisation du français par défaut");
                currentLang = "fr";
                langFile = new File(langFolder, "fr.yml");
                if (!langFile.exists()) {
                    defLangStream = plugin.getResource("lang/fr.yml");
                    if (defLangStream != null) {
                        try {
                            java.nio.file.Files.copy(defLangStream, langFile.toPath());
                        } catch (Exception e) {
                            plugin.getLogger().log(Level.SEVERE, "Erreur critique lors de la création du fichier de langue", e);
                        }
                    }
                }
            }
        }

        // Charger la configuration
        langConfig = YamlConfiguration.loadConfiguration(langFile);

        // Charger la configuration par défaut depuis les ressources
        InputStream defLangStream = plugin.getResource("lang/" + currentLang + ".yml");
        if (defLangStream != null) {
            FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defLangStream));

            // Vérifier et ajouter les clés manquantes
            boolean modified = false;
            for (String key : defaultConfig.getKeys(true)) {
                // Ignorer les sections (on veut uniquement les clés finales)
                if (!defaultConfig.isConfigurationSection(key)) {
                    if (!langConfig.contains(key)) {
                        langConfig.set(key, defaultConfig.get(key));
                        modified = true;
                        plugin.getLogger().info("Clé manquante ajoutée: " + key);
                    }
                }
            }

            // Sauvegarder si des modifications ont été apportées
            if (modified) {
                try {
                    langConfig.save(langFile);
                    plugin.getLogger().info("Fichier de langue mis à jour avec les nouvelles clés");
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Erreur lors de la sauvegarde des nouvelles clés", e);
                }
            }
        }

        // Vider le cache
        messageCache.clear();

        plugin.getLogger().info("Langue chargée: " + currentLang);
    }

    /**
     * Récupère un message traduit
     */
    public String getMessage(String path) {
        // Vérifier le cache
        if (messageCache.containsKey(path)) {
            return messageCache.get(path);
        }

        // Si, pour n'importe quelle raison, le path a un _, le remplacer par un -
        path = path.replace("_", "-");
        // Je déteste les _ dans les clés de config

        String message = langConfig.getString(path);
        if (message == null) {
            plugin.getLogger().warning("Message manquant pour la clé: " + path);
            return path;
        }

        // Traduire les codes couleur
        message = ChatColor.translateAlternateColorCodes('&', message);

        // Résoudre les références internes (ex: {role.loup-garou.name})
        message = resolveInternalReferences(message);

        // Mettre en cache
        messageCache.put(path, message);

        return message;
    }

    /**
     * Résout les références internes dans un message
     * Remplace les patterns {key.path} par leurs valeurs correspondantes
     */
    private String resolveInternalReferences(String message) {
        if (message == null) {
            return null;
        }

        // Pattern pour détecter les références: {roles.xxx.xxx}, {actions.xxx}, etc.
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{(roles\\.[^}]+|actions\\.[^}]+|vote\\.[^}]+|gui\\.[^}]+|death\\.[^}]+|phases\\.[^}]+|general\\.[^}]+)\\}");
        java.util.regex.Matcher matcher = pattern.matcher(message);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String refPath = matcher.group(1);
            String refValue = langConfig.getString(refPath);

            if (refValue != null) {
                // Traduire les codes couleur de la référence
                refValue = ChatColor.translateAlternateColorCodes('&', refValue);
                // Récursivement résoudre les références dans la valeur référencée
                refValue = resolveInternalReferences(refValue);
                matcher.appendReplacement(result, java.util.regex.Matcher.quoteReplacement(refValue));
            } else {
                // Si la référence n'existe pas, la laisser telle quelle
                matcher.appendReplacement(result, java.util.regex.Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Récupère un message traduit avec des placeholders
     */
    public String getMessage(String path, Map<String, String> placeholders) {
        String message = getMessage(path);

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        return message;
    }

    /**
     * Récupère un message traduit avec un seul placeholder
     */
    public String getMessage(String path, String placeholder, String value) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(placeholder, value);
        return getMessage(path, placeholders);
    }

    /**
     * Récupère une liste de messages traduits
     */
    public java.util.List<String> getMessageList(String path) {
        // Si, pour n'importe quelle raison, le path a un _, le remplacer par un -
        path = path.replace("_", "-");

        java.util.List<String> messages = langConfig.getStringList(path);
        if (messages == null || messages.isEmpty()) {
            plugin.getLogger().warning("Liste de messages manquante pour la clé: " + path);
            return java.util.Arrays.asList(path);
        }

        // Traduire les codes couleur pour chaque message
        java.util.List<String> translatedMessages = new java.util.ArrayList<>();
        for (String message : messages) {
            String translated = ChatColor.translateAlternateColorCodes('&', message);
            translated = resolveInternalReferences(translated);
            translatedMessages.add(translated);
        }

        return translatedMessages;
    }

    /**
     * Change la langue
     */
    public void setLanguage(String language) {
        this.currentLang = language;
        loadLanguage();
    }

    /**
     * Récupère la langue actuelle
     */
    public String getCurrentLanguage() {
        return currentLang;
    }

    /**
     * Recharge le fichier de langue
     */
    public void reload() {
        loadLanguage();
    }

    // Méthodes utilitaires pour des messages fréquents

    public String getPrefix() {
        return getMessage("general.prefix");
    }

    // plurar default false
    public String getRoleName(String role, boolean plural, boolean colorFormat) {
        if (plural) {
            String pluralName = getMessage("roles." + role + ".plural");
            String colorLessName = ChatColor.stripColor(pluralName);
            if (colorFormat == false) {
                return colorLessName;
            }

            return pluralName;
        }

        String name = getMessage("roles." + role + ".name");
        String colorLessName = ChatColor.stripColor(name);
        if (colorFormat == false) {
            return colorLessName;
        }

        return name;
    }

    public String getRoleName(String role) {
        return getRoleName(role, false, true);
    }

    public String getRoleName(String role, boolean plural) {
        return getRoleName(role, plural, true);
    }

    public String getRoleTitle(String role) {
        return getMessage("roles." + role + ".title");
    }

    public String getRoleSubtitle(String role) {
        return getMessage("roles." + role + ".subtitle");
    }

    public String getGuiTitle(String gui) {
        return getMessage("gui." + gui);
    }

    public String getDeathMessage(String deathType) {
        return getMessage("death." + deathType);
    }

    public String getVictoryTitle(String team) {
        return getMessage("victory." + team + ".title");
    }

    public String getVictorySubtitle(String team) {
        return getMessage("victory." + team + ".subtitle");
    }

    public String getCommandMessage(String command, String key) {
        return getMessage("commands." + command + "." + key);
    }

    public String getErrorMessage(String error) {
        return getMessage("errors." + error);
    }

    public String getActionMessage(String role, String action) {
        return getMessage("actions." + role + "." + action);
    }

    public String getVoteMessage(String type, String key) {
        return getMessage("vote." + type + "." + key);
    }

    public String getPhaseMessage(String phase) {
        return getMessage("phases." + phase);
    }
}

