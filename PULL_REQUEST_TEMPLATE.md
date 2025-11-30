# Mob Stacking Feature - Pull Request

## Description

This PR adds intelligent mob stacking functionality to LevelledMobs, designed to reduce entity count and server lag on high-population servers.

## Changes Summary

- ✅ Added `StackManager` class with complete stacking logic
- ✅ Integrated stacking into spawn and death events
- ✅ Added player proximity checks for performance optimization
- ✅ Implemented ArmorStand-based stack display above mobs
- ✅ Added comprehensive configuration options in `settings.yml`
- ✅ Optimized to only process mobs near players (100 block radius)

## Performance Improvements

- Reduces entity count by stacking similar mobs
- Only processes mobs within 100 blocks of players
- Skips entire worlds/chunks when no players present
- Configurable check intervals to balance performance vs responsiveness

## Configuration

All settings are in `settings.yml` under `mob-stacking` section. Feature can be completely disabled via `mob-stacking.enabled: false`.

## Testing

- ✅ Tested on high-population servers (100+ players)
- ✅ Verified compatibility with existing LevelledMobs features
- ✅ Tested on Paper 1.21+ servers
- ✅ Confirmed no breaking changes

## Recommendation

**Consider offering this as a separate optional plugin** (`LevelledMobs-Stacking`) for:
- Better modularity
- Optional feature for servers that don't need it
- Easier maintenance and updates
- User choice

If kept integrated, it's fully configurable and can be disabled.

## Files Changed

- `StackManager.kt` (new)
- `LevelledMobs.kt` (modified)
- `EntitySpawnListener.kt` (modified)
- `EntityDeathListener.kt` (modified)
- `NamespacedKeys.kt` (modified)
- `settings.yml` (modified)

## Breaking Changes

None - all changes are additive and optional.

