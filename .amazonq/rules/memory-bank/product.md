# Woodlanders - Product Overview

## Project Purpose

Woodlanders is a 2D multiplayer adventure game that serves as an ambitious experiment in AI-assisted game development. Built entirely through conversational AI within the Kiro IDE, this project demonstrates zero-code game development - creating a fully functional multiplayer game using only natural language prompts and specifications.

## Value Proposition

- **AI Development Showcase**: Proof of concept that complex software can be built through conversation, with every feature, bug fix, and optimization coming from natural language descriptions
- **Complete Multiplayer Experience**: Fully functional 2D adventure game with real-time networking, infinite world exploration, and synchronized gameplay
- **Educational Resource**: Demonstrates modern game development patterns, networking architecture, and Java/libGDX best practices

## Key Features

### World & Exploration
- **Infinite Procedurally Generated World**: Endless exploration with chunk-based rendering and dynamic terrain generation
- **Multiple Biomes**: Grass and sand biomes with distinct visual styles and generation patterns
- **Dynamic Weather System**: Random rain events that follow the player (120s duration, 2-8 minute intervals)
- **Compass Navigation**: Visual compass pointing toward spawn point or custom target locations
- **World Persistence**: Complete save/load system with separate singleplayer/multiplayer saves

### Character & Survival
- **Animated Player Character**: Smooth walking animations with directional sprites and idle states
- **Health & Hunger System**: Dual survival mechanics with health damage and hunger accumulation (1% per 60s)
- **Consumable Items**: Apples restore 10% health, bananas reduce 5% hunger
- **Visual Feedback**: Unified health bar showing both health (red) and hunger (blue) status
- **Death & Respawn**: Automatic respawn at spawn point when health reaches zero or hunger reaches 100%

### Resources & Crafting
- **6 Tree Types**: Small trees, regular trees, apple trees, banana trees, bamboo trees, and coconut trees
- **Combat System**: Attack and destroy trees with visual health bars and damage feedback
- **Health Regeneration**: Damaged trees slowly recover health over time
- **Resource Drops**: Trees drop specific items when destroyed (apples, bananas, wood, bamboo)
- **Environmental Hazards**: Cacti that damage players on contact
- **Planting System**: Plant baby bamboo on sand tiles; grows into harvestable bamboo trees (120s growth time)
- **Resource Respawn**: Destroyed resources respawn after 15 minutes with visual indicators

### Inventory & Items
- **6-Slot Inventory System**: Separate inventories for singleplayer and multiplayer modes
- **Item Types**: Apples, bananas, baby bamboo, bamboo stacks, wood stacks, pebbles
- **Manual Consumption**: Select items with number keys, consume with spacebar
- **Automatic Pickup**: Items collected when walking near them (within 32 pixels)
- **Tile Targeting System**: Visual targeting indicator for precise item placement with WASD controls
- **Network Synchronization**: Inventory synchronized across multiplayer sessions

### Multiplayer
- **Dedicated Server**: Standalone server with configurable port and max clients
- **Real-time Synchronization**: 22+ message types for complete world and player state sync
- **Connection Quality Indicator**: Visual network status display
- **Separate Positions**: Independent player positions for singleplayer and multiplayer
- **Graceful Disconnect/Reconnect**: Connection handling with last server memory
- **Server-Authoritative**: Server validates all actions to prevent cheating

### User Interface
- **In-Game Menu System**: Wooden plank themed menus with ESC key access
- **Player Customization**: Set custom player names (3-15 characters)
- **HUD Elements**: Health bar, inventory display, compass, connection status
- **World Management**: Save, load, and manage multiple world saves with timestamps
- **Multi-Language Support**: English, Polish, Portuguese, Dutch, German with auto-detection
- **Custom Fonts**: Retro pixel fonts for authentic game feel

## Target Users

### Primary Audience
- **Game Developers**: Learning multiplayer game architecture and networking patterns
- **AI Development Enthusiasts**: Exploring AI-assisted software development capabilities
- **Java/libGDX Developers**: Reference implementation for 2D game development

### Secondary Audience
- **Casual Gamers**: Enjoying a simple multiplayer adventure experience
- **Educators**: Teaching game development, networking, or AI-assisted coding
- **Open Source Contributors**: Contributing features and improvements to the project

## Use Cases

### Development & Learning
- Study multiplayer networking architecture and synchronization patterns
- Learn libGDX framework for 2D game development
- Understand chunk-based world generation and rendering optimization
- Explore AI-assisted development workflows and methodologies

### Gaming & Entertainment
- Play solo or with friends in infinite procedurally generated worlds
- Survive by managing health and hunger while gathering resources
- Explore different biomes and discover various tree types
- Build and customize worlds with planting and resource management

### Research & Experimentation
- Analyze AI-generated code quality and architecture decisions
- Test multiplayer networking under various conditions
- Experiment with game mechanics and feature additions
- Contribute to open source game development

## Technical Highlights

- **Performance Optimized**: Chunk-based rendering, spatial partitioning, texture atlases
- **Network Efficient**: Message batching, delta-time synchronization, heartbeat system
- **Cross-Platform**: Runs on Windows, macOS, and Linux
- **Modular Architecture**: Clean separation of concerns with dedicated packages for each system
- **Comprehensive Testing**: JUnit 5 and Mockito test infrastructure
