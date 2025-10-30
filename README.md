# 🐺 LGMC - Werewolf Minecraft Plugin

[![Version](https://img.shields.io/badge/version-2.7.1--SNAPSHOT-blue.svg)](https://github.com/lightshoro/lgmc)
[![Minecraft](https://img.shields.io/badge/minecraft-1.21-green.svg)](https://www.minecraft.net/)
[![Paper](https://img.shields.io/badge/paper-required-orange.svg)](https://papermc.io/)

A complete Minecraft plugin that recreates the Werewolf game with an immersive and automated experience.

---

## 📋 Table of Contents

- [About](#-about)
- [Features](#-features)
- [Installation](#-installation)
- [Quick Start Guide](#-quick-start-guide)
- [Commands](#-commands)
- [Configuration](#-configuration)
- [Available Roles](#-available-roles)
- [Plugin Architecture](#-plugin-architecture)
- [Compilation](#-compilation)
- [License](#-license)

---

## 🎮 About

**LGMC** is a complete Java recreation of the social game Werewolf for Minecraft. The plugin automatically manages the entire game, from role distribution to victory conditions, including day/night phases and voting.

### Key Features

- ✅ **Supports 4 to 12 players** for balanced games
- ✅ **7 roles** implemented with unique powers
- ✅ **Intuitive graphical interface** for all actions
- ✅ **Automatic management** of game phases
- ✅ **Voting system** with tie-breaking by the captain
- ✅ **Multilingual** (French and English) with automatic translation updates
- ✅ **Flexible configuration** with automatic migration
- ❌ **Production ready** - The project is still under active development
- ❌ **Customizable** - Some configuration currently have no effects

---

## ✨ Features

### Core Gameplay

- **Automatic role distribution** based on the number of players
- **Day/night phases** with automatic alternation and Minecraft time management
- **Timer with boss bar** displaying remaining time
- **Action queue** sequenced for each phase
- **3 victory conditions**: Village, Werewolves, or Lovers
- **Death system** with 6 different reasons (vote, wolves, hunter, poison, love, famine)
- **Visibility management** and blindness effects during the night
- **Automatic spectator mode** for eliminated players

### Special Mechanics

- **Lovers system**: joint death if one dies
- **Captain**: blue helmet, breaks voting ties, can appoint a successor
- **Will**: the captain can choose their successor before dying
- **Automatic tie-breaking** in case of voting ties
- **Automatic check** when all players have voted
- **Custom messages** for each type of death

### Graphical Interfaces (GUIs)

- 🔮 **SeerGUI** - Probe a player to know their role
- 🐺 **WerewolfGUI** - Choose a victim (unanimous vote required)
- 🧪 **WitchGUI** - Use life or death potions
- ☠️ **WitchPoisonGUI** - Poison a player
- 👑 **CaptainVoteGUI** - Elect the village captain
- 🗳️ **VoteGUI** - Vote to eliminate a player
- 💕 **CupidGUI** - Choose two lovers
- 🔜 **WillGUI** - Appoint a successor before dying with a dedicated menu instead of a command.

---

## 📦 Installation

### Prerequisites

- **Minecraft Server**: Paper or Spigot 1.21+
- **Java**: Version 21 or higher

### Steps

1. Download the JAR file from [Releases](https://github.com/DarkShoro/lgmc/releases) or compile it yourself
2. Place the `lgmc-2.7.1-SNAPSHOT.jar` file in your server's `plugins/` folder
3. Start or restart the server
4. Configuration files will be automatically generated in `plugins/lgmc/`

---

## 🚀 Quick Start Guide

### 1. Configure Locations

Before playing, you need to set the required locations:

```
/lgsetup campfire          # Sets the game center (campfire)
/lgsetup chasseurtp        # Sets where the hunter is teleported to shoot
/lgsetup spawn 1           # First spawn point
/lgsetup spawn 2           # Second spawn point
/lgsetup spawn 3           # Etc. (one for each maximum player)
...
```

**Tip**: Position yourself at the desired location before executing the command.

### 2. Verify Configuration

```
/lgsetup info              # Displays all configured locations
```

### 3. Start a Game

```
/lgstart
```

**Requirements to Start**:
- Between **4 and 12 players** connected
- Configured locations (campfire, chasseurtp, and enough spawns)
- No ongoing game

The game starts with:
- A 10-second countdown
- Automatic role distribution
- Beginning of the first night

### 4. During the Game

**For all players**:
- Follow the instructions displayed in the chat
- GUIs open automatically when it's your turn to act
- Voting is done via clickable interfaces

**For the captain**:
If you are in danger of dying and wish to appoint a successor:
```
/testament <player_name>
```

### 5. Stop a Game

```
/lgstop                    # Stops the game and resets all settings
```

---

## 📜 Commands

| Command | Aliases | Description | Permission |
|----------|---------|-------------|------------|
| `/lgstart` | `/lggo`, `/startlg` | Starts a Werewolf game | `lgmc.start` |
| `/lgstop` | `/stoplg` | Stops the ongoing game | `lgmc.stop` |
| `/lgreload` | `/lgrl`, `/reloadlg` | Reloads the configuration | `lgmc.reload` |
| `/lgsetup <type> [number]` | - | Configures locations | `lgmc.setup` |
| `/testament <player>` | - | The captain appoints their successor | `lgmc.testament` |
| `/goodGuys` | - | Displays the number of good players (debug) | `lgmc.debug` |

### Types for /lgsetup

- `campfire` - Game center
- `chasseurtp` - Hunter's shooting location
- `spawn <number>` - Player spawn points (1, 2, 3, etc.)
- `info` - Displays all configured locations

---

## ⚙️ Configuration

### config.yml File

The main file is located in `plugins/lgmc/config.yml`:

### Multilingual System

Language files are located in `plugins/lgmc/lang/`:

- `fr.yml` - French 🇫🇷
- `en.yml` - English 🇬🇧

**To change the language**:
1. Change `language: fr` to `language: en` in `config.yml`
2. Execute `/lgreload`

**To customize messages**:
Edit the `.yml` files directly in the `lang/` folder.

**Automatic Translation Updates**:
When updating the plugin, if new translation keys are added:
- ✅ Your customizations are **automatically preserved**
- ✅ Missing new keys are **automatically added** from the default file
- ✅ A log message indicates each added key

No need to delete your language files during updates!

### Automatic Migration

When updating the plugin:
- ✅ Your custom values are **automatically preserved**
- ✅ New options are added with default values
- ✅ A backup is created: `config_backup_vX.yml`

Simply reload with `/lgreload` after the update!

---

## 🎭 Available Roles

### 🐺 Werewolf
- **Team**: Werewolves
- **Power**: Chooses a victim each night (unanimous vote required)
- **Objective**: Eliminate all villagers

### 👤 Villager
- **Team**: Village
- **Power**: No special power
- **Objective**: Eliminate all werewolves

### 🔮 Seer
- **Team**: Village
- **Power**: Can probe a player each night to know their role
- **Objective**: Help the village find the werewolves

### 🧪 Witch
- **Team**: Village
- **Power**: Has two potions (usable once each)
  - Life Potion: Resurrects the wolves' victim
  - Death Potion: Poisons a player
- **Objective**: Protect the village

### 🎯 Hunter
- **Team**: Village
- **Power**: Can kill a player upon dying
- **Objective**: Strategically use their last shot

### 💘 Cupid
- **Team**: Variable (depends on their lovers)
- **Power**: Chooses two lovers at the start of the game
- **Note**: If the lovers die, Cupid wins alone

### 👧 Little Girl
- **Team**: Village
- **Power**: Can spy on the werewolves at night (see their actions)
- **Objective**: Gather information for the village

### 👑 Captain (Additional Role)
- **Acquisition**: Elected by vote at the start of the game
- **Privilege**: Wears a blue helmet, breaks voting ties
- **Will**: Can appoint a successor with `/testament`

---

## 🏗️ Plugin Architecture

The plugin uses a modular and clean architecture:

```
src/main/java/fr/lightshoro/lgmc/
├── Lgmc.java                          # Main class
├── models/
│   ├── Role.java                      # Role enumeration
│   └── GamePlayer.java                # Represents a player in the game
├── managers/
│   ├── GameManager.java               # Main game logic
│   ├── TimerManager.java              # Timer and phase management
│   ├── LocationManager.java           # Location management
│   ├── LanguageManager.java           # Multilingual system with auto-update
│   └── RoleFinishers.java             # Role action processing
├── gui/
│   ├── SeerGUI.java
│   ├── WerewolfGUI.java
│   ├── WitchGUI.java
│   ├── WitchPoisonGUI.java
│   ├── CaptainVoteGUI.java
│   ├── VoteGUI.java
│   └── CupidGUI.java
├── listeners/
│   ├── GameListener.java              # Game events
│   └── VoteListener.java              # Voting events
├── commands/
│   ├── LGStartCommand.java
│   ├── LGStopCommand.java
│   ├── LGSetupCommand.java
│   ├── LGReloadCommand.java
│   ├── TestamentCommand.java
│   └── GoodGuysCommand.java
└── tasks/
    └── VoteCheckTask.java             # Automatic vote checking
```

### Technologies Used

- **Spigot/Paper API** 1.21
- **InventoryFramework** 0.11.5 (GUIs)
- **Gradle** + Shadow plugin (build)
- **Java** 21

---

## 🔨 Compilation

### Prerequisites
- JDK 21+
- Git (optional)

### Commands

```bash
# Clone the project (or download the ZIP)
git clone https://github.com/lightshoro/lgmc.git
cd lgmc

# Compile with Gradle
./gradlew shadowJar        # Linux/Mac
gradlew.bat shadowJar      # Windows

# The JAR will be generated in:
# build/libs/lgmc-2.7.1-SNAPSHOT.jar
```

### Automatic Build

The project uses the Shadow plugin to automatically include all dependencies in the final JAR.

---

## 📊 Project Statistics

- **Files**: 24 Java files
- **Lines of Code**: ~3500+ lines
- **Development Time**: Complete and tested project
- **Status**: Production Ready ✅

---

## 🤝 Contributions

Contributions are welcome! Feel free to:
- Open an issue to report a bug
- Suggest new features
- Submit a pull request

---

## 📝 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## 👤 Author

**LightShoro**

---

## 🙏 Acknowledgments

- The Minecraft community for inspiration
- The creators of the original Werewolf game
- All contributors and testers

---

**Enjoy the game! 🐺🌙**
