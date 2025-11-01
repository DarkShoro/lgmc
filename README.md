# LGMC - Werewolf Game Plugin

[![Version](https://img.shields.io/badge/version-3.4.1--BETA-blue.svg)](https://github.com/DarkShoro/lgmc/releases)
[![Minecraft](https://img.shields.io/badge/minecraft-1.21-green.svg)](https://www.minecraft.net/)
[![Paper](https://img.shields.io/badge/paper-required-orange.svg)](https://papermc.io/)
[![Java](https://img.shields.io/badge/java-21-red.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

A feature-complete Minecraft implementation of the classic Werewolf social deduction game with full automation, intuitive GUIs, and multilingual support.

## Quick Links

- [Installation](#installation)
- [Quick Start](#quick-start)
- [Commands](#commands)
- [Roles](#roles)
- [Configuration](#configuration)
- [Development](#development)

---

## Features

### Core Gameplay
- **4-12 Players** - Balanced role distribution
- **7 Unique Roles** - Werewolf, Seer, Witch, Hunter, Cupid, Little Girl, Villager
- **Day/Night Cycles** - Automated phase management with visual effects
- **Captain System** - Election, tie-breaking, succession
- **Lovers Mechanic** - Linked fate and private communication
- **Multiple Victory Conditions** - Village, Werewolves, Lovers, or Stalemate

### User Interface
- **Graphical Menus** - All actions through intuitive GUIs
- **Boss Bar Timers** - Clear countdown for each phase
- **Auto-Advancement** - Phases progress when all players act
- **Spectator Mode** - Dead players can observe and chat

### Communication
- **Role-Specific Chats** - Private channels for werewolves and lovers
- **Spectator Chat** - Separate channel for eliminated players
- **Multilingual** - French and English support with hot-reload

### Administration
- **Hot Reload** - Update config without restart
- **Easy Setup** - Simple location configuration
- **Debug Tools** - Built-in testing commands
- **WebSocket API** - External integration support

---

## Installation

### Requirements
- **Server**: Paper 1.21 or newer
- **Java**: JDK 21+
- **Players**: 4-12 for optimal gameplay

### Steps

1. Download `lgmc-3.4.1-BETA.jar` from [releases](https://github.com/DarkShoro/lgmc/releases)
2. Place in your server's `plugins/` folder
3. Restart server
4. Configure locations with `/lg setup`
5. Start game with `/lg start`

The plugin auto-generates all configuration files on first run.

---

## Quick Start

### 1. Setup Locations

```
/lg setup spawn <role>    # Set spawn points
/lg setup campfire        # Set gathering location
/lg setup vote            # Set voting location
```

**Required spawns**: `villageois`, `loup-garou`, `voyante`, `sorciere`, `chasseur`, `cupidon`, `petite-fille`

### 2. Start Game

```
/lg start
```

Players are teleported, roles assigned automatically, and game begins.

### 3. Game Flow

1. **Captain Election** - Vote via GUI
2. **Night Phases** - Each role acts in sequence
3. **Day Phase** - Village discusses and votes
4. **Repeat** - Until victory condition met

### 4. End Game

```
/lg stop    # Manual stop
```

Or game ends automatically when a team wins.

---

## Commands

### Main Command

```
/lg <subcommand> [args]
```

| Subcommand | Description | Permission |
|------------|-------------|------------|
| `start` | Begin new game | `lgmc.start` |
| `stop` | End current game | `lgmc.stop` |
| `reload` | Reload configuration | `lgmc.reload` |
| `setup <type> [role]` | Configure locations | `lgmc.setup` |
| `gui` | Open debug GUIs | `lgmc.debug` |
| `help` | Show help | `lgmc.help` |

### Utility Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/love <message>` | Lovers private chat | - |
| `/goodGuys` | Debug: Show good count | `lgmc.debug` |
| `/linkDiscord` | Show Discord link | - |

### Tab Completion

All commands support tab completion for easier use.

---

## Roles

### Werewolf Team

**🐺 Werewolf (Loup-Garou)**
- Eliminate one villager each night
- Win when equal to or outnumber villagers
- Private chat during night phase

### Village Team

**👁️ Seer (Voyante)**
- Investigate one player's role each night
- Guides village with information

**🧪 Witch (Sorcière)**
- **Life Potion**: Save tonight's victim (once per game)
- **Death Potion**: Kill any player (once per game)

**🏹 Hunter (Chasseur)**
- Shoot one player when eliminated
- Can turn the tide with strategic final shot

**💘 Cupid (Cupidon)**
- Choose two lovers on first night
- Creates special win condition

**👧 Little Girl (Petite Fille)**
- Spy on werewolves during night (anonymous)
- Risk/reward information gathering

**👤 Villager (Villageois)**
- No special ability
- Rely on deduction and discussion

### Special Role

**👑 Captain**
- Elected by village vote at game start
- Vote counts double
- Breaks ties in voting
- Names successor via GUI when dying
- Wears blue helmet (visual indicator)

### Lovers

- Chosen by Cupid on first night
- Share private `/love` chat
- If one dies, both die
- **Win Condition**: Last two survivors
- Can be any roles (even werewolf + villager)

---

## Configuration

### Main Config (`config.yml`)

```yaml
# Game settings
min-players: 4
max-players: 12
enable-captain: true
enable-lovers: true

# Phase durations (seconds)
timers:
  night: 60
  day: 300
  vote: 120
  
# Role assignment
roles:
  werewolf: auto    # Or specific count
  seer: 1
  witch: 1
  # ...
```

### Language Files (`lang/fr.yml`, `lang/en.yml`)

Hot-reloadable translation files with automatic key migration.

```yaml
roles:
  loup-garou:
    name: "&cLoup-Garou"
    description: "Éliminez les villageois"
```

Change with `/lg reload` without restart.

---

## Game Phases

### Night Sequence

1. **Cupid** (first night only) - Choose lovers
2. **Lovers Meet** (first night only) - Reveal each other
3. **Seer** - Investigate player
4. **Werewolves** - Designate victim
5. **Little Girl** - Spy on werewolves
6. **Witch** - Use potions

### Day Sequence

1. **Death Announcement** - Reveal night victims
2. **Discussion** - Free chat period
3. **Voting** - Eliminate suspect via GUI
4. **Captain Tie-Break** (if needed)
5. **Execution** - Reveal role of eliminated
6. **Hunter Shot** (if hunter eliminated)

### Victory Conditions

- **Villagers**: All werewolves eliminated
- **Werewolves**: Equal or outnumber villagers
- **Lovers**: Last two survivors
- **Stalemate**: No valid moves remain

---

## Development

### Building

```bash
# Clone repository
git clone https://github.com/DarkShoro/lgmc.git
cd lgmc

# Build JAR
./gradlew shadowJar          # Linux/Mac
gradlew.bat shadowJar        # Windows

# Output: build/libs/lgmc-3.4.1-BETA.jar
```

### Project Structure

```
src/main/java/fr/DarkShoro/lgmc/
├── Lgmc.java              # Main plugin class
├── models/                # Data models (Role, GamePlayer)
├── managers/              # Game logic (GameManager, TimerManager, etc.)
├── gui/                   # Inventory interfaces
├── listeners/             # Event handlers
├── commands/              # Command executors
└── tasks/                 # Background tasks
```

### Architecture

- **Manager Pattern** - Separation of concerns
- **Event-Driven** - Bukkit event system
- **State Machine** - Game phases as states
- **GUI Framework** - InventoryFramework for consistency

### Tech Stack

- **Paper API** 1.21
- **InventoryFramework** 0.11.5
- **Gradle** 8.x + Shadow
- **Java** 21

### Adding Features

**New Role**:
1. Add to `Role.java` enum
2. Create handler in `RoleFinishers.java`
3. Add GUI if needed
4. Update language files

**New Language**:
1. Copy `lang/en.yml` to `lang/xx.yml`
2. Translate all keys
3. Set in config or use `/lg reload`

---

## Contributing

Contributions welcome! Please:

1. Fork the repository
2. Create feature branch
3. Make changes with clear commits
4. Test thoroughly
5. Submit pull request

Report bugs via [GitHub Issues](https://github.com/DarkShoro/lgmc/issues).

---

## License

MIT License - see [LICENSE](LICENSE) file for details.

---

## Links

- **Issues**: [Report bugs](https://github.com/DarkShoro/lgmc/issues)
- **Wiki**: [Full documentation](https://github.com/DarkShoro/lgmc/wiki)

---

**Made with ❤️ by [DarkShoro](https://lightshoro.fr)** & [MisterNox](https://misternox.fr)

