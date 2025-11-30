# Mob Stacking Feature for LevelledMobs

## Overview

This feature adds intelligent mob stacking functionality to LevelledMobs, significantly reducing entity count and server lag on high-population servers (100+ players). The implementation is optimized to only process mobs near players, preventing unnecessary server load.

## Key Features

### 1. **Intelligent Mob Stacking**
- Stacks similar mobs together into a single entity
- Only stacks mobs within 25 blocks of each other (configurable)
- Respects LevelledMobs level system - only stacks mobs with similar levels (within 10 levels)
- Maximum stack size: 64 mobs (configurable)

### 2. **Performance Optimizations**
- **Player Proximity Checks**: Only stacks mobs if a player is within 100 blocks (configurable)
- **Smart Processing**: Skips entire worlds/chunks if no players are present
- **Reduced Entity Scanning**: Only processes entities near active players
- **Configurable Check Period**: Periodic stacking checks every 3 seconds (configurable)

### 3. **Visual Stack Display**
- Uses invisible ArmorStand above mobs to show stack count
- Customizable display format (supports color codes and placeholders)
- Appears as a second floating text line above the mob's nametag
- Only shows when stack size >= 2 (configurable minimum)

### 4. **Stack Management**
- Automatically stacks on spawn (configurable)
- Handles stack splitting on death with proper drop/XP multiplication
- Optional death animation skipping for better performance
- Clean removal of stack displays when mobs die or unstack

## Files Modified/Created

### New Files:
- `levelledmobs-plugin/src/main/kotlin/io/github/arcaneplugins/levelledmobs/managers/StackManager.kt`
  - Complete stacking logic implementation
  - Player proximity checks
  - Stack display management
  - Periodic stacking task

### Modified Files:
- `levelledmobs-plugin/src/main/kotlin/io/github/arcaneplugins/levelledmobs/LevelledMobs.kt`
  - Added StackManager initialization

- `levelledmobs-plugin/src/main/kotlin/io/github/arcaneplugins/levelledmobs/listeners/EntitySpawnListener.kt`
  - Added stacking attempt on spawn

- `levelledmobs-plugin/src/main/kotlin/io/github/arcaneplugins/levelledmobs/listeners/EntityDeathListener.kt`
  - Integrated stack death handling with drop/XP multiplication

- `levelledmobs-plugin/src/main/kotlin/io/github/arcaneplugins/levelledmobs/misc/NamespacedKeys.kt`
  - Added `stackSize` NamespacedKey for PDC storage

- `levelledmobs-plugin/src/main/resources/settings.yml`
  - Added complete mob-stacking configuration section

## Configuration

All settings are in `settings.yml` under the `mob-stacking` section:

```yaml
mob-stacking:
  enabled: true                          # Enable/disable stacking
  max-stack-size: 64                     # Maximum mobs per stack
  stack-radius: 25.0                     # Radius to search for nearby mobs
  stack-on-spawn: true                   # Auto-stack on spawn
  skip-death-animation: false            # Skip animation when splitting
  min-stack-size: 2                      # Minimum size to show display
  check-period: 3                        # Periodic check interval (seconds)
  require-player-nearby: true            # Only stack near players
  player-check-radius: 100.0            # Player detection radius
  stack-display-format: "&7x%stack-size%" # Display format
```

### Stack Display Format Options:
- `%stack-size%` - Placeholder for the number of mobs
- Color codes using `&` (e.g., `&7`, `&c`, `&a`, `&e`)
- Examples:
  - `"&7x%stack-size%"` - Gray "x64" (default)
  - `"&e[%stack-size%]"` - Yellow brackets
  - `"&cStack: &f%stack-size%"` - Red "Stack: " with white number

## Performance Impact

### Before:
- 100+ players loading mobs = thousands of entities
- High server lag and TPS drops
- Excessive entity processing

### After:
- Same mobs stacked into fewer entities
- Only processes mobs near players (100 block radius)
- Significantly reduced entity count
- Minimal performance overhead

## Compatibility

- Works with all LevelledMobs features
- Compatible with Paper, Spigot, and Folia
- Respects existing mob leveling system
- No breaking changes to existing functionality

## Testing

Tested on:
- High-population servers (100+ concurrent players)
- Various mob types (creepers, zombies, skeletons, etc.)
- Different server configurations
- Paper 1.21+ servers

## Recommendation

**Consider offering this as a separate plugin** (`LevelledMobs-Stacking` or similar) for the following reasons:

1. **Modularity**: Some servers may not need stacking functionality
2. **Optional Feature**: Keeps core LevelledMobs lightweight
3. **Easier Maintenance**: Separate plugin allows independent updates
4. **User Choice**: Server owners can choose to enable/disable stacking

If kept integrated, it's fully configurable and can be disabled via `mob-stacking.enabled: false`.

## Technical Details

### Stack Storage
- Uses Persistent Data Container (PDC) to store stack size
- NamespacedKey: `levelledmobs:stackSize`

### Stack Display
- Invisible ArmorStand (marker mode, no gravity)
- Teleports above mob every tick to maintain position
- Automatically cleaned up on entity death/removal

### Stacking Logic
- Only stacks same entity type
- Only stacks if both mobs are levelled or both unlevelled
- Level difference must be <= 10
- Distance must be <= stack-radius
- Player must be within player-check-radius (if enabled)

## License

This feature follows the same AGPL-3.0 license as LevelledMobs.

## Credits

Implementation inspired by StackMob plugin architecture, adapted for LevelledMobs integration.

