# CYBER RELIC ARCADE

## *PLEASE DOWNLOAD THE V7 FILE COMPLETELY*

## Project Overview

**Cyber Relic Arcade** is a synthwave-styled arcade game built entirely in Java using Swing and AWT. The game combines classic catcher mechanics with modern roguelike elements, featuring combo multipliers, boss battles, power-ups, and a persistent user progression system with coins and a shop.

### What the Project Does

Cyber Relic Arcade is an interactive arcade-style game where players:
- **Catch falling objects** using a controllable paddle at the bottom of the screen
- **Earn points** by successfully catching relics, cubes, energy cells, and power-ups
- **Build combos** to multiply their point rewards (multiplier increases every 3 consecutive catches)
- **Face hazards** that damage the player if missed or caught
- **Battle bosses** when reaching score milestones (every 750+ points)
- **Earn coins** based on their final score (coins = score ÷ 15) redeemable in the shop
- **Log in as registered users** to persist high scores and coin balances
- **Play as guest** for casual runs without account requirements

### Key Features

- **User Authentication System**: Register, login, and track individual progress
- **Persistent Progression**: High scores and coins saved per user
- **Dynamic Gameplay**: Increasing difficulty with level progression
- **Boss Encounters**: Fight bosses with unique attack patterns
- **Power-ups**: 
  - **Shield**: Blocks one hit
  - **Invincibility**: 5-second complete protection (1% spawn rate)
  - **Bonus Lives**: Extra lives earned from boss defeats
- **Visual Effects**: CRT scanlines, vignette effects, particle explosions, floating score text
- **Combo System**: Multiplier rewards for consecutive catches
- **Leaderboard**: View top 10 players' high scores on game over screen
- **Shop System**: A shop menu for future cosmetics and upgrades (coins display ready)

---

## How to Run the Program

### Prerequisites

- **Java Development Kit (JDK) 11 or higher** must be installed
- Verify installation by running `javac -version` in your terminal

### Compilation

**Windows (Batch):**
```bash
cd CyberRelicArcade
compile.bat
```

**Windows (PowerShell):**
```powershell
cd CyberRelicArcade
.\compile.ps1
```

**macOS / Linux:**
```bash
cd CyberRelicArcade
javac -source 11 -target 11 -d bin src/*.java
```

### Running the Game

**Windows (Batch):**
```bash
run.bat
```

**macOS / Linux / Manual:**
```bash
java -cp bin CyberRelicArcade
```

---

## Project Goals & Purpose

### Objectives

1. **Create an engaging arcade experience** that blends classic mechanics with modern game design patterns
2. **Demonstrate Java Swing proficiency** through custom rendering, animations, and event handling
3. **Implement a complete game loop** with proper state management and entity systems
4. **Build persistent user systems** with authentication and progression tracking
5. **Deliver polished visual feedback** through particle effects, screen shake, and CRT aesthetics

### Educational Value

This project showcases:
- Object-oriented design with inheritance (FallingObject → BossProjectile)
- Game loop architecture with fixed timestep updates
- State machine pattern for screen management
- File I/O for user persistence
- Graphics rendering and animation
- Collision detection and physics

---

## Installation & Setup Instructions

### 1. **Extract the Project**
   ```
   Extract V6.zip to your desired location
   ```

### 2. **Navigate to the Project Directory**
   ```bash
   cd CyberRelicArcade
   ```

### 3. **Compile All Java Files**
   - Use the provided compile script (see "How to Run the Program" section above)
   - Or manually compile with: `javac -source 11 -target 11 -d bin src/*.java`

### 4. **Verify Compilation**
   - Check that a `bin/` folder was created with `.class` files
   - If compilation fails, ensure JDK 11+ is installed

### 5. **Run the Game**
   - Use the provided run script or execute: `java -cp bin CyberRelicArcade`

### 6. **First Launch**
   - You'll see the login screen
   - Create a new account or play as a guest
   - Accounts are stored in `users.txt` file

---

## Project Structure

```
CyberRelicArcade/
├── compile.bat              # Windows batch compilation script
├── compile.ps1              # Windows PowerShell compilation script
├── run.bat                  # Windows run script
├── README.md                # This file
├── users.txt                # User database (auto-created)
├── src/
│   ├── CyberRelicArcade.java       # Main entry point
│   ├── GamePanel.java              # Core game loop & rendering
│   ├── LoginPanel.java             # Login/signup UI
│   ├── ShopPanel.java              # Shop menu (framework)
│   ├── Catcher.java                # Player paddle
│   ├── FallingObject.java          # Falling items (relics, cubes, etc.)
│   ├── BossProjectile.java         # Boss bullets (extends FallingObject)
│   ├── Boss.java                   # Boss enemy
│   ├── Particle.java               # Visual effects
│   ├── TextPop.java                # Floating text
│   ├── UserManager.java            # Authentication & persistence
│   └── SoundEngine.java            # (Optional) Audio synthesis
└── bin/                     # Compiled .class files (auto-generated)
```

---

## Controls

### Gameplay

| Key | Action |
|-----|--------|
| **← / →** | Move paddle left/right |
| **A / D** | Move paddle (alternative) |
| **SPACE** | Activate pulse (8 second cooldown) |
| **P** | Pause / Resume |
| **R** | Restart (from Game Over) |
| **ESC** | Quit |

### Menus

| Key | Action |
|-----|--------|
| **ENTER** | Select / Continue |
| **← / →** | Navigate options |
| **M** | Return to menu (shop screen) |

---

## Game Mechanics

### Scoring & Combo System

- **Base Points by Object Type:**
  - Relic: 10 points
  - Cube: 20 points
  - Energy Cell: 50 points
  - Invincibility: 500 points
  - Hazard: 0 points (damage instead)

- **Combo Multiplier**: Increases by 1× every 3 consecutive catches (max 8×)
- **Combo Duration**: 3 seconds since last catch
- **Coins Earned**: `score ÷ 15` (integer division)

### Object Spawn Rates

- 40% Relic
- 25% Cube
- 13% Energy Cell
- 21% Hazard
- **1% Invincibility Power-up** (rare)

### Boss Encounters

- Trigger at: 750, 2000, 4000+ points
- Boss appears with health bar
- Defeat bosses to earn bonus lives (max 3)
- Boss projectiles can be dodged or blocked by shields

### Power-ups & Protection

1. **Shield** (Auto at 5000+ point milestones)
   - Blocks one hazard hit
   - Displays "SHIELD" on HUD

2. **Invincibility** (1% spawn rate)
   - 5-second complete protection
   - Negates hazards and boss projectiles
   - Prevents life loss on missed objects
   - Displays countdown timer and purple aura

3. **Bonus Lives** (Boss defeats)
   - Awarded for defeating bosses
   - Max 3 bonus lives
   - Used before losing regular lives

---

## User System

### Account Features

- **Registration**: Create new accounts with username & password
- **Persistent High Scores**: Tracked per user
- **Coin Balance**: Accumulated coins from gameplay
- **Shop Purchases**: Cosmetics/upgrades unique to each user
- **Guest Play**: Anonymous sessions without account

### Data Storage

- Users stored in `users.txt` (CSV format)
- Format: `username,password_hash,high_score,coins,shop_items`
- Local file storage (no external servers)

---

## Graphics & Aesthetics

### Visual Style

- **Theme**: Synthwave (black background, neon cyan/red accents)
- **Color Scheme**: Black & Blue (updated from original purple)
- **Effects**: 
  - CRT scanlines overlay
  - Vignette (darkened edges)
  - Screen shake on damage
  - Particle explosions
  - Floating score text

### UI Elements

- **Rounded Input Fields**: Soft aesthetic in login panel
- **Gradient Effects**: Combo bar with linear gradients
- **Glowing Text**: Boss names and combat text
- **Neon Borders**: Subtle blue border on panels

---

## Additional Notes & Documentation

### Known Limitations

- Audio synthesis is basic (no external sound files)
- Shop is a framework ready for item implementation
- Single-player and multiplayer modes both playable
- No network multiplayer (local split-screen only)

### Future Enhancements

1. **Shop Implementation**: Add purchasable cosmetics (skins, sound packs)
2. **Leaderboard**: Online score submission
3. **Achievements**: Badge system for milestones
4. **Mobile Port**: Touch controls for mobile devices
5. **Difficulty Modes**: Easy/Normal/Hard settings
6. **Sound System**: Full music and SFX integration

### Troubleshooting

**Q: "javac: command not found"**
- A: Install Java Development Kit (JDK) from oracle.com and add to PATH

**Q: "Cannot find symbol" compilation error**
- A: Ensure all .java files are in the `src/` folder, then recompile

**Q: Game window won't open**
- A: Verify Java is installed correctly. Try: `java -version`

**Q: Users.txt not found**
- A: Game creates it automatically on first login. Check file permissions.

### References & Resources

- [Java Swing Documentation](https://docs.oracle.com/javase/tutorial/uiswing/)
- [Java 2D Graphics](https://docs.oracle.com/javase/tutorial/2d/)
- [Game Loop Architecture](https://gameprogrammingpatterns.com/game-loop.html)
- [JDK Download](https://www.oracle.com/java/technologies/downloads/)

---

## License

This project is created for educational purposes. Feel free to modify and distribute as needed.

---

**Last Updated**: May 21, 2026
**Version**: 6.0
**Status**: Actively maintained

Enjoy the game! 🎮✨
