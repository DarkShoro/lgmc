# LGMC - Werewolf Game Plugin

[![Version](https://img.shields.io/badge/version-5.1.2-blue.svg)](https://github.com/DarkShoro/lgmc/releases)
[![Minecraft](https://img.shields.io/badge/minecraft-1.21-green.svg)](https://www.minecraft.net/)
[![Paper](https://img.shields.io/badge/paper-required-orange.svg)](https://papermc.io/)
[![Java](https://img.shields.io/badge/java-21-red.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

A feature-complete Minecraft implementation of the classic Werewolf social deduction game with full automation, intuitive GUIs, visual skin transformations, and multilingual support.

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
- **9 Unique Roles** - Werewolf, Psychic, Witch, Hunter, Cupid, Insomniac, Thief, Angel, Villager
- **Day/Night Cycles** - Automated phase management with visual effects
- **Captain System** - Election, tie-breaking, succession
- **Lovers Mechanic** - Linked fate and private communication with `<3` prefix
- **Multiple Victory Conditions** - Village, Werewolves, Lovers, Angel, or Stalemate

### Visual Integration
- **SkinsRestorer Support** - Werewolves transform during night phase
- **Role Helmets** - Colored indicators for each role
- **Boss Bar Timers** - Clear countdown for each phase
- **Particle Effects** - Death animations and visual feedback
- **Spectator Mode** - Dead players can observe and chat

### User Interface
- **Graphical Menus** - All actions through intuitive GUIs
- **Click-to-Vote** - Direct player interaction voting system
- **Auto-Advancement** - Phases progress when all players act
- **Testament System** - Last words before execution

### Communication
- **Role-Specific Chats** - Private channels for werewolves
- **Lover Chat** - Type `<3 message` to chat with your partner
- **Spectator Chat** - Separate channel for eliminated players
- **Multilingual** - French and English support with hot-reload

### Administration
- **Hot Reload** - Update config without restart
- **Easy Setup** - Simple location configuration
- **Debug Tools** - Built-in testing commands for roles and skins
- **WebSocket API** - External integration support

---

## Installation

### Requirements
- **Server**: Paper 1.21 or newer
- **Java**: JDK 21+
- **Players**: 4-12 for optimal gameplay
- **Optional**: [SkinsRestorer](https://skinsrestorer.net/) for werewolf skin transformations

### Steps

1. Download `lgmc-5.1.2.jar` from [releases](https://github.com/DarkShoro/lgmc/releases)
2. (Optional) Install SkinsRestorer for visual transformations
3. Place in your server's `plugins/` folder
4. Restart server
5. Configure locations with `/lg setup`
6. Configure skin integration in `config.yml` if using SkinsRestorer
7. Start game with `/lg start`

The plugin auto-generates all configuration files on first run.

---

## Quick Start

### 1. Setup Locations

```
/lg setup spawn <#number>    # Set spawn points
/lg setup campfire        # Set gathering location
/lg setup chasseurtp            # Set chasseur (hunter) location
```

### 2. Start Game

```
/lg start
```

Players are teleported, roles assigned automatically, and game begins.

### 3. Game Flow

1. **Night Phases** - Each role acts in sequence
2. **Day Phase** - Village discusses and votes
3. **Captain Election** - Vote on first day
4. **Execution** - Player with most votes is eliminated
5. **Repeat** - Until victory condition met

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
| `neighbors <player>` | Show player neighbors | `lgmc.debug` |
| `testskin <apply\|restore> [player]` | Test skin transformations | `lgmc.debug` |
| `help` | Show help | `lgmc.help` |

### Utility Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/love <message>` | Lovers private chat (or use `<3` prefix) | - |
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
- **Visual transformation**: Skin changes during night phase (with SkinsRestorer)

### Village Team

**👁️ Psychic (Voyante)**
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
- Enabled with 9+ players (configurable)

**👧 Insomniac (Petite Fille)**
- Spy on werewolves during night (anonymous)
- Risk/reward information gathering

**🎭 Thief (Voleur)**
- Appears randomly (50% chance by default)
- Steals a role from another player during first night
- Adds unpredictability to role distribution

**😇 Angel (Ange)**
- **Special Win Condition**: Get executed on first day vote
- If survives first vote, becomes regular villager
- Enabled with 9+ players (configurable)
- Creates interesting first-day dynamics

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
- Share private `/love` chat or use `<3` prefix in normal chat
- **QoL Feature**: Type `<3 message` anywhere to send to love chat
  - Example: `<3 I think it's Dark` sends only to your lover
- If one dies, both die
- **Win Condition**: Last two survivors
- Can be any roles (even werewolf + villager)

---

## Configuration

### Main Config (`config.yml`)

```yaml
# Language
language: en

# Update checker
update-checker:
  enabled: true                # Enable update checking
  include-prereleases: false   # Include pre-releases in update checks

# Game settings
game:
  min-players: 4
  two-wolves-threshold: 9    # Minimum players for 2 werewolves
  cupidon-threshold: 9       # Minimum players for Cupidon
  ange-threshold: 9          # Minimum players for Ange
  voleur-threshold: 6        # Minimum players for Voleur
  cupidon-enabled: false     # Enable Cupidon role
  ange-enabled: true         # Enable Ange role
  voleur-enabled: true       # Enable Voleur role
  voleur-chance: 0.5         # 50% chance for Thief to appear

# Phase durations (seconds)
timers:
  voleur: 60
  cupidon: 60
  voyante: 30
  loups-garous: 30
  sorciere: 30
  vote-capitaine: 60
  debat: 300
  chasseur: 30
  
# SkinsRestorer integration
skinsrestorer:
  enabled: true
  werewolf-skin-url: "https://your-skin-url.png"  # Or player name
```

### SkinsRestorer Configuration

The plugin integrates with SkinsRestorer to transform werewolf skins during night phase:

1. Install [SkinsRestorer](https://skinsrestorer.net/) plugin
2. Enable in config: `skinsrestorer.enabled: true`
3. Set werewolf skin URL or player name: `werewolf-skin-url: "YourPlayerName"`
4. Skins automatically change during werewolf turn and restore after

**Supported formats:**
- Minecraft player name: `"Notch"`
- Skin URL: `"https://mineskin.eu/..."`
- Custom uploaded skins

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

1. **Thief** (first night only, if present) - Steal a role
2. **Cupid** (first night only) - Choose lovers
3. **Lovers Meet** (first night only) - Reveal each other
4. **Psychic** - Investigate player
5. **Werewolves** - Designate victim (skins transform with SkinsRestorer)
6. **Insomniac** - Spy on werewolves
7. **Witch** - Use potions

### Day Sequence

1. **Death Announcement** - Reveal night victims
2. **Discussion** - Free chat period
3. **Voting** - Eliminate suspect via GUI or click
4. **Captain Tie-Break** (if needed)
5. **Testament** - Last words before execution
6. **Execution** - Reveal role of eliminated
7. **Angel Victory Check** - If Angel voted out on day 1
8. **Hunter Shot** (if hunter eliminated)

### Victory Conditions

- **Villagers**: All werewolves eliminated
- **Werewolves**: Equal or outnumber villagers
- **Lovers**: Last two survivors
- **Angel**: Executed on first day vote
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

# Output: build/libs/lgmc-5.1.2.jar
```

### Project Structure

```
src/main/java/fr/DarkShoro/lgmc/
├── Lgmc.java              # Main plugin class
├── models/                # Data models (Role, GamePlayer)
├── managers/              # Game logic (GameManager, TimerManager, SkinManager, etc.)
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
- **SkinsRestorer API** (optional integration)
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

