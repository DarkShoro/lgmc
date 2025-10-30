# 🐺 LGMC - Loup-Garou Minecraft Plugin

[![Version](https://img.shields.io/badge/version-2.7.1--SNAPSHOT-blue.svg)](https://github.com/lightshoro/lgmc)
[![Minecraft](https://img.shields.io/badge/minecraft-1.21-green.svg)](https://www.minecraft.net/)
[![Paper](https://img.shields.io/badge/paper-required-orange.svg)](https://papermc.io/)

Un plugin Minecraft complet qui recrée le jeu du Loup-Garou avec une expérience immersive et automatisée.

---

## 📋 Table des matières

- [À propos](#-à-propos)
- [Fonctionnalités](#-fonctionnalités)
- [Installation](#-installation)
- [Guide de démarrage rapide](#-guide-de-démarrage-rapide)
- [Commandes](#-commandes)
- [Configuration](#-configuration)
- [Rôles disponibles](#-rôles-disponibles)
- [Architecture du plugin](#-architecture-du-plugin)
- [Compilation](#-compilation)
- [Licence](#-licence)

---

## 🎮 À propos

**LGMC** est une recréation complète en Java du jeu social Loup-Garou pour Minecraft. Le plugin gère automatiquement l'intégralité d'une partie, de la distribution des rôles aux conditions de victoire, en passant par les phases jour/nuit et les votes.

### Caractéristiques principales

- ✅ **Support de 4 à 12 joueurs** pour des parties équilibrées
- ✅ **7 rôles** implémentés avec leurs pouvoirs uniques
- ✅ **Interface graphique** intuitive pour toutes les actions
- ✅ **Gestion automatique** des phases de jeu
- ✅ **Système de vote** avec départage par le capitaine
- ✅ **Multilingue** (Français et Anglais) avec mise à jour automatique des traductions
- ✅ **Configuration flexible** avec migration automatique
- ✅ **Production ready** - Testé et stable

---

## ✨ Fonctionnalités

### Core du jeu

- **Distribution automatique des rôles** selon le nombre de joueurs
- **Phases jour/nuit** avec alternance automatique et gestion du temps Minecraft
- **Timer avec barre de boss** affichant le temps restant
- **File d'attente d'actions** séquencées pour chaque phase
- **3 conditions de victoire** : Village, Loups-Garous, ou Amoureux
- **Système de mort** avec 6 raisons différentes (vote, loups, chasseur, poison, amour, famine)
- **Gestion de la visibilité** et effets de cécité pendant la nuit
- **Spectateur automatique** pour les joueurs éliminés

### Mécaniques spéciales

- **Système d'amoureux** : mort conjointe si l'un meurt
- **Capitaine** : casque bleu, départage les votes, peut nommer un successeur
- **Testament** : le capitaine peut choisir son successeur avant de mourir
- **Départage automatique** en cas d'égalité lors des votes
- **Vérification automatique** quand tous les joueurs ont voté
- **Messages personnalisés** pour chaque type de mort

### Interfaces graphiques (GUIs)

- 🔮 **VoyanteGUI** - Sonder un joueur pour connaître son rôle
- 🐺 **LoupGarouGUI** - Choisir une victime (vote unanime requis)
- 🧪 **SorciereGUI** - Utiliser les potions de vie ou de mort
- ☠️ **SorcierePoisonGUI** - Empoisonner un joueur
- 👑 **CapitaineVoteGUI** - Élire le capitaine du village
- 🗳️ **VoteGUI** - Voter pour éliminer un joueur
- 💕 **CupidonGUI** - Choisir deux amoureux

---

## 📦 Installation

### Prérequis

- **Serveur Minecraft** : Paper ou Spigot 1.21+
- **Java** : Version 21 ou supérieure

### Étapes

1. Téléchargez le fichier JAR depuis [Releases](https://github.com/lightshoro/lgmc/releases) ou compilez-le vous-même
2. Placez le fichier `lgmc-2.7.1-SNAPSHOT.jar` dans le dossier `plugins/` de votre serveur
3. Démarrez ou redémarrez le serveur
4. Les fichiers de configuration seront générés automatiquement dans `plugins/lgmc/`

---

## 🚀 Guide de démarrage rapide

### 1. Configuration des emplacements

Avant de pouvoir jouer, vous devez définir les emplacements nécessaires :

```
/lgsetup campfire          # Définit le centre du jeu (feu de camp)
/lgsetup chasseurtp        # Définit où le chasseur est téléporté pour tirer
/lgsetup spawn 1           # Premier point d'apparition
/lgsetup spawn 2           # Deuxième point d'apparition
/lgsetup spawn 3           # Etc. (un pour chaque joueur maximum)
...
```

**Astuce** : Positionnez-vous à l'endroit désiré avant d'exécuter la commande.

### 2. Vérifier la configuration

```
/lgsetup info              # Affiche tous les emplacements configurés
```

### 3. Démarrer une partie

```
/lgstart
```

**Prérequis pour démarrer** :
- Entre **4 et 12 joueurs** connectés
- Emplacements configurés (campfire, chasseurtp, et assez de spawns)
- Aucune partie en cours

Le jeu démarre avec :
- Un compte à rebours de 10 secondes
- Distribution automatique des rôles
- Début de la première nuit

### 4. Pendant la partie

**Pour tous les joueurs** :
- Suivez les instructions affichées dans le chat
- Les GUIs s'ouvrent automatiquement quand c'est votre tour d'agir
- Les votes se font via des interfaces cliquables

**Pour le capitaine** :
Si vous êtes en danger de mort et souhaitez nommer un successeur :
```
/testament <nom_du_joueur>
```

### 5. Arrêter une partie

```
/lgstop                    # Arrête la partie et reset tous les paramètres
```

---

## 📜 Commandes

| Commande | Aliases | Description | Permission |
|----------|---------|-------------|------------|
| `/lgstart` | `/lggo`, `/startlg` | Démarre une partie de Loup-Garou | `lgmc.start` |
| `/lgstop` | `/stoplg` | Arrête la partie en cours | `lgmc.stop` |
| `/lgreload` | `/lgrl`, `/reloadlg` | Recharge la configuration | `lgmc.reload` |
| `/lgsetup <type> [numéro]` | - | Configure les emplacements | `lgmc.setup` |
| `/testament <joueur>` | - | Le capitaine nomme son successeur | `lgmc.testament` |
| `/goodGuys` | - | Affiche le nombre de bons joueurs (debug) | `lgmc.debug` |

### Types pour /lgsetup

- `campfire` - Centre du jeu
- `chasseurtp` - Emplacement du tir du chasseur
- `spawn <numéro>` - Points d'apparition des joueurs (1, 2, 3, etc.)
- `info` - Affiche tous les emplacements configurés

---

## ⚙️ Configuration

### Fichier config.yml

Le fichier principal se trouve dans `plugins/lgmc/config.yml` :

```yaml
config-version: 2      # Version de configuration (ne pas modifier)
language: fr           # Langue : 'fr' ou 'en'

# Timers (en secondes)
timers:
  day: 300            # Durée du jour (5 minutes)
  night: 180          # Durée de la nuit (3 minutes)
  vote: 180           # Durée du vote (3 minutes)
  
# Nombre de joueurs par rôle
roles:
  villageois: 2       # Villageois de base
  loupgarou: 1        # Loups-Garous
  voyante: 1          # Voyantes
  sorciere: 1         # Sorcières
  chasseur: 1         # Chasseurs
  cupidon: 1          # Cupidons
  petitefille: 1      # Petites Filles
```

### Système multilingue

Les fichiers de langue se trouvent dans `plugins/lgmc/lang/` :

- `fr.yml` - Français 🇫🇷
- `en.yml` - English 🇬🇧

**Pour changer de langue** :
1. Modifiez `language: fr` en `language: en` dans `config.yml`
2. Exécutez `/lgreload`

**Pour personnaliser les messages** :
Éditez directement les fichiers `.yml` dans le dossier `lang/`.

**Mise à jour automatique des traductions** :
Lors d'une mise à jour du plugin, si de nouvelles clés de traduction sont ajoutées :
- ✅ Vos personnalisations sont **automatiquement préservées**
- ✅ Les nouvelles clés manquantes sont **ajoutées automatiquement** depuis le fichier par défaut
- ✅ Un message dans les logs indique chaque clé ajoutée

Plus besoin de supprimer vos fichiers de langue lors des mises à jour !

### Migration automatique

Lors d'une mise à jour du plugin :
- ✅ Vos valeurs personnalisées sont **automatiquement préservées**
- ✅ Les nouvelles options sont ajoutées avec leurs valeurs par défaut
- ✅ Une sauvegarde est créée : `config_backup_vX.yml`

Rechargez simplement avec `/lgreload` après la mise à jour !

---

## 🎭 Rôles disponibles

### 🐺 Loup-Garou
- **Camp** : Loups-Garous
- **Pouvoir** : Choisit une victime chaque nuit (vote unanime requis)
- **Objectif** : Éliminer tous les villageois

### 👤 Villageois
- **Camp** : Village
- **Pouvoir** : Aucun pouvoir spécial
- **Objectif** : Éliminer tous les loups-garous

### 🔮 Voyante
- **Camp** : Village
- **Pouvoir** : Peut sonder un joueur chaque nuit pour connaître son rôle
- **Objectif** : Aider le village à trouver les loups

### 🧪 Sorcière
- **Camp** : Village
- **Pouvoir** : Possède deux potions (utilisables une fois chacune)
  - Potion de vie : Ressuscite la victime des loups
  - Potion de mort : Empoisonne un joueur
- **Objectif** : Protéger le village

### 🎯 Chasseur
- **Camp** : Village
- **Pouvoir** : Peut tuer un joueur en mourant
- **Objectif** : Utiliser stratégiquement son dernier tir

### 💘 Cupidon
- **Camp** : Variable (dépend de ses amoureux)
- **Pouvoir** : Choisit deux amoureux en début de partie
- **Note** : Si les amoureux meurent tous les deux, Cupidon gagne seul

### 👧 Petite Fille
- **Camp** : Village
- **Pouvoir** : Peut espionner les loups-garous la nuit (voir leurs actions)
- **Objectif** : Glaner des informations pour le village

### 👑 Capitaine (Rôle additionnel)
- **Obtention** : Élu par vote au début de la partie
- **Privilège** : Porte un casque bleu, départage les votes en cas d'égalité
- **Testament** : Peut nommer son successeur avec `/testament`

---

## 🏗️ Architecture du plugin

Le plugin utilise une architecture modulaire et propre :

```
src/main/java/fr/lightshoro/lgmc/
├── Lgmc.java                          # Classe principale
├── models/
│   ├── Role.java                      # Énumération des rôles
│   └── GamePlayer.java                # Représente un joueur en partie
├── managers/
│   ├── GameManager.java               # Logique principale du jeu
│   ├── TimerManager.java              # Gestion des timers et phases
│   ├── LocationManager.java           # Gestion des emplacements
│   ├── LanguageManager.java           # Système multilingue avec mise à jour auto
│   └── RoleFinishers.java             # Traitement des actions de rôles
├── gui/
│   ├── VoyanteGUI.java
│   ├── LoupGarouGUI.java
│   ├── SorciereGUI.java
│   ├── SorcierePoisonGUI.java
│   ├── CapitaineVoteGUI.java
│   ├── VoteGUI.java
│   └── CupidonGUI.java
├── listeners/
│   ├── GameListener.java              # Événements de jeu
│   └── VoteListener.java              # Événements de vote
├── commands/
│   ├── LGStartCommand.java
│   ├── LGStopCommand.java
│   ├── LGSetupCommand.java
│   ├── LGReloadCommand.java
│   ├── TestamentCommand.java
│   └── GoodGuysCommand.java
└── tasks/
    └── VoteCheckTask.java             # Vérification automatique des votes
```

### Technologies utilisées

- **Spigot/Paper API** 1.21
- **InventoryFramework** 0.11.5 (GUIs)
- **Gradle** + Shadow plugin (build)
- **Java** 21

---

## 🔨 Compilation

### Prérequis
- JDK 21+
- Git (optionnel)

### Commandes

```bash
# Cloner le projet (ou télécharger le ZIP)
git clone https://github.com/lightshoro/lgmc.git
cd lgmc

# Compiler avec Gradle
./gradlew shadowJar        # Linux/Mac
gradlew.bat shadowJar      # Windows

# Le JAR sera généré dans :
# build/libs/lgmc-2.7.1-SNAPSHOT.jar
```

### Build automatique

Le projet utilise le plugin Shadow pour inclure automatiquement toutes les dépendances dans le JAR final.

---

## 📊 Statistiques du projet

- **Fichiers** : 24 fichiers Java
- **Lignes de code** : ~3500+ lignes
- **Temps de développement** : Projet complet et testé
- **Status** : Production Ready ✅

---

## 🤝 Contributions

Les contributions sont les bienvenues ! N'hésitez pas à :
- Ouvrir une issue pour signaler un bug
- Proposer de nouvelles fonctionnalités
- Soumettre une pull request

---

## 📝 Licence

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

---

## 👤 Auteur

**LightShoro**

---

## 🙏 Remerciements

- La communauté Minecraft pour l'inspiration
- Les créateurs du jeu Loup-Garou original
- Tous les contributeurs et testeurs

---

**Bon jeu ! 🐺🌙**

