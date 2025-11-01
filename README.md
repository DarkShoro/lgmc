# 🐺 LGMC - Werewolf Minecraft Plugin

[![Version](https://img.shields.io/badge/version-3.3.1--BETA-blue.svg)](https://github.com/DarkShoro/lgmc/releases)
[![Minecraft](https://img.shields.io/badge/minecraft-1.21-green.svg)](https://www.minecraft.net/)
[![Paper](https://img.shields.io/badge/paper-required-orange.svg)](https://papermc.io/)
[![Java](https://img.shields.io/badge/java-21-red.svg)](https://www.oracle.com/java/)

A complete Minecraft plugin that recreates the classic Werewolf social deduction game with an immersive and fully automated experience.

---

## 📋 Table of Contents

- [About](#-about)
- [Features](#-features)
- [Installation](#-installation)
- [Quick Start Guide](#-quick-start-guide)
- [Commands](#-commands)
- [Chat Systems](#-chat-systems)
- [Available Roles](#-available-roles)
- [Configuration](#-configuration)
- [Game Phases](#-game-phases)
- [Development](#-development)
- [License](#-license)

---

## 🎮 About

**LGMC** is a comprehensive Java implementation of the classic Werewolf (Loup-Garou) social deduction game for Minecraft. The plugin fully automates game management, from role distribution to victory conditions, including day/night phases, voting systems, and special role abilities.

### Key Highlights

- ✅ **4-12 Players** - Balanced gameplay for small to medium groups
- ✅ **7 Unique Roles** - Each with distinct abilities and win conditions
- ✅ **Intuitive GUIs** - Graphical interfaces for all player actions
- ✅ **Automated Phases** - Seamless day/night cycle management
- ✅ **Advanced Chat Systems** - Role-specific and spectator chat channels
- ✅ **Multilingual Support** - French and English with auto-updating translations
- ✅ **Flexible Configuration** - Hot-reloadable config with automatic migration
- ✅ **Modern Command System** - Unified `/lg` command with tab completion

---

## ✨ Features

### Core Gameplay

- **Intelligent Role Distribution** - Automatically balanced based on player count
- **Day/Night Cycle** - Synchronized with Minecraft time and visual effects
- **Boss Bar Timer** - Clear countdown display for each phase
- **Action Queue System** - Orchestrated role actions in proper sequence
- **Multiple Victory Conditions** - Village, Werewolves, or Lovers can win
- **Comprehensive Death System** - 6 death types with custom messages
- **Visibility Management** - Dynamic player visibility and blindness effects
- **Automatic Spectator Mode** - Dead players can chat and observe

### Special Mechanics

- **Lovers System** - Cupid creates a couple; if one dies, both die
- **Captain Role** - Blue helmet, tie-breaking vote, can name successor
- **Testament Command** - Captain chooses successor before dying
- **Automatic Tie Resolution** - Captain breaks voting ties
- **Early Vote Completion** - Phase advances when all players vote
- **Detailed Death Messages** - Different messages for each death type
- **Campfire Ambience** - Auto-lighting/extinguishing based on day/night

### Advanced Chat Features

- **Werewolf Chat** - Private communication during werewolf phase
  - Werewolves see: `PlayerName >> message` (red, bold)
  - Little Girl sees: `Loup-Garou >> message` (anonymous)
  - Other players attempting to chat receive "cannot chat" message

- **Lover Chat** - Private `/love <message>` command for lovers
  - Romantic purple formatting with hearts: `♥ PlayerName >> message ♥`
  - Works day and night, only visible between the two lovers

- **Spectator Chat** - Separate channel for dead/spectating players
  - Gray, italic formatting: `[Spectator] PlayerName >> message`
  - Only visible to other spectators
  - Can chat anytime without interfering with game

- **Night Restrictions** - Living players cannot chat during night phases
  - Prevents accidental information leaks
  - Exceptions for role-specific chats (werewolves, lovers)

### Graphical Interfaces

- 🔮 **SeerGUI** - Investigate a player to learn their role
- 🐺 **WerewolfGUI** - Vote for tonight's victim (requires consensus)
- 🧪 **WitchGUI** - Choose to use life or death potion
- ☠️ **WitchPoisonGUI** - Select a poison target
- 👑 **CaptainVoteGUI** - Elect the village captain
- 🗳️ **VoteGUI** - Daily elimination vote
- 💕 **CupidGUI** - Choose two players to become lovers
- 📜 **WillGUI** - Captain names successor before death

---

## 📦 Installation

### Prerequisites

- **Server Software**: PaperMC 1.21+ (recommended) or Spigot 1.21+
- **Java Runtime**: JRE 21 or higher
- **Player Count**: 4-12 players for proper gameplay

### Installation Steps

1. **Download** the latest release from [GitHub Releases](https://github.com/DarkShoro/lgmc/releases)
2. **Place** `lgmc-3.3.1-BETA.jar` in your server's `plugins/` folder
3. **Start** or restart your server
4. **Verify** installation - check `plugins/lgmc/` for generated config files

The plugin will automatically:
- Create configuration files (`config.yml`)
- Generate language files (`lang/fr.yml`, `lang/en.yml`)
- Set max server slots to 12
- Load custom server icon

---

## 🚀 Quick Start Guide

### 1. Initial Setup

Configure essential game locations before your first game:

```bash
/lg setup campfire       # Set central gathering point (campfire)
/lg setup chasseurtp     # Set hunter's shooting location
/lg setup spawn 1        # First player spawn point
/lg setup spawn 2        # Second player spawn point
# ... continue for up to 12 spawns
```

**Tip**: Stand at the desired location before executing each command.

### 2. Verify Configuration

```bash
/lg setup info           # Display all configured locations
```

Ensure you have:
- ✅ Campfire location set
- ✅ Chasseur teleport location set
- ✅ Enough spawn points for your player count

### 3. Start Your First Game

```bash
/lg start
```

**Starting Requirements**:
- **4-12 players** connected to the server
- **All locations** properly configured
- **No game** currently in progress

**Starting Sequence**:
1. 10-second countdown with action bar notifications
2. Players teleported to spawn points
3. Roles randomly distributed
4. First night phase begins

### 4. Playing the Game

**For All Players**:
- Watch chat for phase announcements
- GUIs open automatically when it's your turn
- Click items in GUI to perform actions
- Vote during day phases via the voting GUI

**For Werewolves**:
- Use werewolf chat during your turn
- Vote in WerewolfGUI for a victim
- Coordinate with other werewolves

**For Lovers**:
- Use `/love <message>` to communicate privately
- Remember: if one dies, both die!

**For Captain**:
- Your vote counts double
- Break ties in voting
- Use `/testament <player>` if dying

**For Dead Players**:
- Automatic spectator mode
- Chat with other spectators only
- Watch the game unfold!

### 5. End the Game

```bash
/lg stop                 # Manually end and reset the game
```

Games also end automatically when:
- All werewolves are eliminated (Village wins)
- Werewolves equal or outnumber villagers (Werewolves win)
- Only the two lovers remain alive (Lovers win)

---

## 📜 Commands

### Main Command: `/lg`

The unified command system with tab completion (Mojang-style):

| Subcommand | Description | Permission | Aliases |
|------------|-------------|------------|---------|
| `/lg start` | Start a new Werewolf game | `lgmc.start` | `/loupgarou start`, `/werewolf start` |
| `/lg stop` | End the current game | `lgmc.stop` | `/loupgarou stop`, `/werewolf stop` |
| `/lg reload` | Reload configuration and language files | `lgmc.reload` | `/loupgarou reload`, `/werewolf reload` |
| `/lg setup <type> [args]` | Configure game locations | `lgmc.setup` | `/loupgarou setup`, `/werewolf setup` |
| `/lg help` | Display command help | - | `/loupgarou help`, `/werewolf help` |

#### Setup Subcommands

| Setup Type | Usage | Description |
|------------|-------|-------------|
| `campfire` | `/lg setup campfire` | Set central campfire location |
| `chasseurtp` | `/lg setup chasseurtp` | Set hunter teleport location |
| `spawn <#>` | `/lg setup spawn <1-12>` | Set player spawn point (1-12) |
| `info` | `/lg setup info` | Display all configured locations |

### Additional Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/testament <player>` | Captain names successor before death | `lgmc.testament` |
| `/love <message>` | Lovers communicate privately | - |
| `/goodGuys` | Display good guys count (debug) | `lgmc.debug` |
| `/lgopengui` | Open debug GUIs | `lgmc.debug` |

### Tab Completion

The command system supports intelligent tab completion:

- `/lg <TAB>` → Shows: `start`, `stop`, `reload`, `setup`, `help`
- `/lg setup <TAB>` → Shows: `campfire`, `chasseurtp`, `spawn`, `info`
- `/lg setup spawn <TAB>` → Shows: `1`, `2`, `3`, ..., `12`

---

## 💬 Chat Systems

### Werewolf Chat
**When**: During the werewolf phase at night  
**Who**: Werewolves and Little Girl only  
**Format**:
- Werewolves see: `PlayerName >> message` (red, bold)
- Little Girl sees: `Loup-Garou >> message` (anonymous)
- Others: Cannot chat (blocked with message)

### Lover Chat
**Command**: `/love <message>` or `/amour <message>`  
**When**: Anytime (day or night)  
**Who**: Only the two lovers  
**Format**: `♥ PlayerName >> message ♥` (light purple, bold)  
**Requirement**: Must be in a couple formed by Cupid

### Spectator Chat
**When**: Anytime after death/elimination  
**Who**: Dead players and non-participants  
**Format**: `[Spectator] PlayerName >> message` (gray, italic)  
**Visibility**: Only other spectators can see

### Night Chat Restrictions
- Living players cannot chat during night phases
- Prevents accidental information leaks
- Exceptions: Werewolf chat, Lover chat
- Dead players can always chat (spectator channel)

---

## 🎭 Available Roles

### Werewolves Team (Evil)

#### 🐺 Werewolf (Loup-Garou)
- **Count**: 1-2 (2 if 9+ players)
- **Ability**: Vote each night to eliminate a villager
- **Win Condition**: Eliminate all villagers or equal their number

### Village Team (Good)

#### 👨‍🌾 Villager (Villageois)
- **Count**: Remaining players
- **Ability**: None (pure deduction)
- **Win Condition**: Eliminate all werewolves

#### 🔮 Seer (Voyante)
- **Count**: 1
- **Ability**: Each night, learn one player's role
- **Win Condition**: Help village eliminate werewolves

#### 🧪 Witch (Sorcière)
- **Count**: 1
- **Ability**: Two one-time potions
  - Life Potion: Save the werewolves' victim
  - Death Potion: Kill any player
- **Win Condition**: Help village eliminate werewolves

#### 🏹 Hunter (Chasseur)
- **Count**: 1
- **Ability**: When dying, shoot and kill one player
- **Special**: If hunter kills everyone in tie, game ends in stalemate
- **Win Condition**: Help village eliminate werewolves

#### 💘 Cupid (Cupidon)
- **Count**: 1 (only if 9+ players)
- **Ability**: First night only - choose two lovers
- **Special**: If lovers are different teams, they have unique win condition
- **Win Condition**: Help village eliminate werewolves

#### 👧 Little Girl (Petite Fille)
- **Count**: 1
- **Ability**: Can see werewolf chat during werewolf phase (anonymized)
- **Win Condition**: Help village eliminate werewolves

### Special Roles

#### 👑 Captain (Capitaine)
- **Selection**: Voted by all players first day
- **Perks**:
  - Blue helmet indicator
  - Vote counts double
  - Breaks voting ties
  - Can name successor via `/testament`
- **Succession**: Chosen player becomes new captain

#### 💑 Lovers (Amoureux)
- **Selection**: Chosen by Cupid on first night
- **Special**: If one dies, both die
- **Unique Win**: If only the two lovers remain alive
- **Communication**: Private `/love` chat command

---

## ⚙️ Configuration

### Configuration File: `config.yml`

Located in `plugins/lgmc/config.yml`:

```yaml
# Configuration version (auto-managed)
config-version: 3

# Language selection
language: en  # Options: 'en' or 'fr'

# Game settings
game:
  min-players: 4
  max-players: 12
  two-wolves-threshold: 9
  cupidon-enabled: true

# Phase timers (seconds)
timers:
  night: 300
  day: 300
  vote: 300
  role-action: 60

# Visual effects
effects:
  night-blindness: true
  freeze-during-night: true

# WebSocket integration (optional)
websocket:
  enabled: false
  url: "ws://localhost:8080"
  secret: "your-secret-here"
```

### Language Files

Language files in `plugins/lgmc/lang/`:

- **French**: `fr.yml` 🇫🇷
- **English**: `en.yml` 🇬🇧

**Change Language**:
1. Edit `language: en` in `config.yml`
2. Run `/lg reload`

**Customize Messages**:
- Directly edit `.yml` files in `lang/` folder
- All messages support color codes (`&a`, `&c`, etc.)
- Supports placeholders (`{player}`, `{role}`, etc.)

**Auto-Update System**:
- ✅ Your customizations are preserved on plugin updates
- ✅ New translation keys auto-added from defaults
- ✅ Missing keys logged in console
- ✅ Old config backed up before migration

### Location Storage

Locations saved in `plugins/lgmc/locations.yml`:

```yaml
locations:
  campfire:
    world: world
    x: 0.0
    y: 64.0
    z: 0.0
  chasseur-tp:
    world: world
    x: 10.0
    y: 64.0
    z: 10.0
  spawns:
    1: { world: world, x: 5.0, y: 64.0, z: 5.0 }
    2: { world: world, x: -5.0, y: 64.0, z: 5.0 }
    # ... up to 12
```

---

## 🌙 Game Phases

### Night Phase

1. **Broadcast**: "Night X" announcement
2. **Visual**: World time set to 18000 (midnight)
3. **Effect**: Campfire extinguished
4. **Restriction**: Living players cannot chat (except in role chats)
5. **Action Queue** (in order):
   - **Cupid** (Night 1 only) - Choose two lovers
   - **Seer** - Investigate one player
   - **Werewolves** - Vote to kill a villager
   - **Witch** - Use life/death potions
6. **Transition**: All players get blindness effect before day

### Day Phase

1. **Broadcast**: "Day X" announcement
2. **Visual**: World time set to 6000 (noon)
3. **Effect**: Campfire lit
4. **Restriction**: None (everyone can chat)
5. **Action Queue** (in order):
   - **Death Reveal** - Announce night victims
   - **Captain Vote** (Day 1 only) - Elect captain
   - **Village Vote** - Vote to eliminate a player
   - **Hunter Shot** (if hunter died) - Hunter chooses target
   - **Captain Succession** (if captain died) - New captain chosen
6. **Transition**: Deaths processed, victory checked

### Action Queue System

The plugin uses a sophisticated action queue to manage phase progression:

- Actions execute sequentially (no overlap)
- GUIs open automatically at the right time
- Timers adjust based on action type
- Early completion when all players acted
- Skip mechanism for unavailable actions (e.g., dead role)

### Victory Check

Checked after every death:

1. **Lovers Win**: Only 2 lovers remain alive
2. **Village Wins**: All werewolves eliminated
3. **Werewolves Win**: Werewolves ≥ Villagers OR only werewolves remain
4. **Stalemate**: Hunter kills everyone in final vote (rare)

Victory triggers:
- Title screen announcement
- Detailed broadcast with statistics
- Automatic game reset
- WebSocket notification (if enabled)

---

## 🛠️ Development

### Building from Source

**Prerequisites**:
- JDK 21+
- Gradle 8.0+

**Build Steps**:
```bash
git clone https://github.com/DarkShoro/lgmc.git
cd lgmc
./gradlew build
```

**Output**: `build/libs/lgmc-3.3.1-BETA.jar`

### Project Structure

```
lgmc/
├── src/main/
│   ├── java/fr/lightshoro/lgmc/
│   │   ├── commands/          # Command handlers
│   │   ├── gui/               # Inventory GUIs
│   │   ├── listeners/         # Event listeners
│   │   ├── managers/          # Core game logic
│   │   ├── models/            # Data models
│   │   └── tasks/             # Async tasks
│   └── resources/
│       ├── config.yml         # Default config
│       ├── plugin.yml         # Plugin metadata
│       ├── server-icon.png    # Custom server icon
│       └── lang/              # Language files
│           ├── fr.yml
│           └── en.yml
├── build.gradle               # Gradle build config
└── README.md
```

### Key Classes

- **Lgmc.java** - Main plugin class, manager initialization
- **GameManager.java** - Core game logic, phase management
- **ChatManager.java** - Chat system (werewolf, lover, spectator)
- **RoleFinishers.java** - Role action completion handlers
- **LGCommand.java** - Unified command system with tab completion
- **ConfigManager.java** - Config loading with auto-migration
- **LanguageManager.java** - Multi-language support

### Contributing

This project is currently under active development. Contributions, bug reports, and feature requests are welcome!

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- Based on the classic **Werewolf (Mafia)** party game
- Inspired by the original Skript version
- Built for the **Minecraft Paper** ecosystem
- Community feedback and testing

---

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/DarkShoro/lgmc/issues)
- **Discord**: [Join our server](#) *(link to be added)*
- **Documentation**: This README + in-game `/lg help`

---

**Made with ❤️ by LightShoro**
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
│   ├── RoleFinishers.java             # Role action processing
│   └── ChatManager.java               # Chat system management
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
│   ├── VoteListener.java              # Voting events
│   └── ChatListener.java              # Chat events
├── commands/
│   ├── DiscordLinkCommand.java
│   ├── LGStartCommand.java
│   ├── LGStopCommand.java
│   ├── LGSetupCommand.java
│   ├── LGReloadCommand.java
│   ├── TestamentCommand.java
│   ├── WSActionCommand.java
│   ├── OpenGuiCommand.java
│   └── GoodGuysCommand.java
└── tasks/
    ├── VisibilityTask.java
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
# build/libs/lgmc-3.3-BETA.jar
```

### Automatic Build

The project uses the Shadow plugin to automatically include all dependencies in the final JAR.


## 🤝 Contributions

Contributions are welcome! Feel free to:
- Open an issue to report a bug
- Suggest new features
- Submit a pull request

---

## 📝 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## 👤 Authors

[LightShoro](https://lightshoro.fr) &
[MisterNox](https://misternox.net/)

---

## 🙏 Acknowledgments

- The Minecraft community for inspiration
- The creators of the original Werewolf game
- All contributors and testers

---

**Enjoy the game! 🐺🌙**
