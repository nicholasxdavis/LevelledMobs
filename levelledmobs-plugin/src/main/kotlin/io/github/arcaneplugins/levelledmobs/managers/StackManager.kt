package io.github.arcaneplugins.levelledmobs.managers

import io.github.arcaneplugins.levelledmobs.LevelledMobs
import io.github.arcaneplugins.levelledmobs.misc.NamespacedKeys
import io.github.arcaneplugins.levelledmobs.util.Log
import io.github.arcaneplugins.levelledmobs.wrappers.LivingEntityWrapper
import org.bukkit.Bukkit
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import java.util.function.Consumer

/**
 * Manages mob stacking functionality to reduce entity count
 * 
 * @author Auto (based on StackMob implementation)
 */
class StackManager {
    private val main: LevelledMobs
        get() = LevelledMobs.instance
    private var isEnabled = false
    private var maxStackSize = 64
    private var stackRadius = 25.0 // Increased to 25 blocks
    private var stackOnSpawn = true
    var skipDeathAnimation = false
        private set
    private var minStackSize = 2 // Only show stack if >= this value
    private var requirePlayerNearby = true // Only stack if player is nearby
    private var playerCheckRadius = 100.0 // Check for players within this radius
    private var stackDisplayFormat = "&7x%stack-size%" // Format for stack display
    
    // Track armor stands for stack display
    private val stackArmorStands = mutableMapOf<LivingEntity, ArmorStand>()
    
    companion object {
        @JvmStatic
        lateinit var instance: StackManager
            private set
    }
    
    init {
        instance = this
    }
    
    fun load() {
        isEnabled = main.helperSettings.getBoolean("mob-stacking.enabled", true)
        maxStackSize = main.helperSettings.getInt("mob-stacking.max-stack-size", 64)
        stackRadius = main.helperSettings.getDouble2("mob-stacking.stack-radius", 25.0) ?: 25.0
        stackOnSpawn = main.helperSettings.getBoolean("mob-stacking.stack-on-spawn", true)
        skipDeathAnimation = main.helperSettings.getBoolean("mob-stacking.skip-death-animation", false)
        minStackSize = main.helperSettings.getInt("mob-stacking.min-stack-size", 2)
        requirePlayerNearby = main.helperSettings.getBoolean("mob-stacking.require-player-nearby", true)
        playerCheckRadius = main.helperSettings.getDouble2("mob-stacking.player-check-radius", 100.0) ?: 100.0
        stackDisplayFormat = main.helperSettings.getString("mob-stacking.stack-display-format", "&7x%stack-size%") ?: "&7x%stack-size%"
        
        if (isEnabled) {
            Log.inf("Mob stacking enabled: max=$maxStackSize, radius=$stackRadius, requirePlayerNearby=$requirePlayerNearby")
        }
    }
    
    /**
     * Check if stacking is enabled
     */
    fun isStackingEnabled(): Boolean = isEnabled
    
    /**
     * Get the stack size of an entity
     */
    fun getStackSize(entity: LivingEntity): Int {
        synchronized(entity.persistentDataContainer) {
            return entity.persistentDataContainer.getOrDefault(
                NamespacedKeys.stackSize,
                PersistentDataType.INTEGER,
                1
            )
        }
    }
    
    /**
     * Set the stack size of an entity
     */
    fun setStackSize(entity: LivingEntity, size: Int) {
        if (size < 1) {
            removeStackData(entity)
            return
        }
        
        val actualSize = size.coerceAtMost(maxStackSize)
        synchronized(entity.persistentDataContainer) {
            entity.persistentDataContainer.set(
                NamespacedKeys.stackSize,
                PersistentDataType.INTEGER,
                actualSize
            )
        }
        
        // Update stack display (armorstand)
        updateStackDisplay(entity, actualSize)
    }
    
    /**
     * Increment stack size
     */
    fun incrementStackSize(entity: LivingEntity, amount: Int = 1) {
        val currentSize = getStackSize(entity)
        setStackSize(entity, currentSize + amount)
    }
    
    /**
     * Remove stack data from entity
     */
    fun removeStackData(entity: LivingEntity) {
        synchronized(entity.persistentDataContainer) {
            entity.persistentDataContainer.remove(NamespacedKeys.stackSize)
        }
        removeStackDisplay(entity)
    }
    
    /**
     * Check if entity is stacked
     */
    fun isStacked(entity: LivingEntity): Boolean {
        return getStackSize(entity) > 1
    }
    
    /**
     * Check if two entities can stack together
     */
    fun canStack(entity1: LivingEntity, entity2: LivingEntity): Boolean {
        // Must be same type
        if (entity1.type != entity2.type) return false
        
        // Must be within radius
        val distanceSq = entity1.location.distanceSquared(entity2.location)
        val radiusSq = stackRadius * stackRadius
        if (distanceSq > radiusSq) {
            return false
        }
        
        // Check if entities are not dead and not maxed
        val stack1 = getStackSize(entity1)
        val stack2 = getStackSize(entity2)
        
        if (stack1 >= maxStackSize || stack2 >= maxStackSize) return false
        if (entity1.isDead || entity2.isDead) return false
        
        // Check if both are levellable (only stack levelled mobs with levelled mobs)
        val lm1 = LivingEntityWrapper.getInstance(entity1)
        val lm2 = LivingEntityWrapper.getInstance(entity2)
        
        try {
            val isLevelled1 = lm1.isLevelled
            val isLevelled2 = lm2.isLevelled
            
            // Both must be levelled or both unlevelled
            if (isLevelled1 != isLevelled2) return false
            
            // If both are levelled, allow stacking if levels are close (within 10 levels for more leniency)
            if (isLevelled1 && isLevelled2) {
                val level1 = main.levelInterface.getLevelOfMob(entity1)
                val level2 = main.levelInterface.getLevelOfMob(entity2)
                val levelDiff = kotlin.math.abs(level1 - level2)
                // Allow stacking if levels are within 10 levels
                if (levelDiff > 10) return false
            }
            
            return true
        } finally {
            lm1.free()
            lm2.free()
        }
    }
    
    /**
     * Merge two entities into one stack
     * Returns the entity that was kept (the one with larger stack or random if equal)
     */
    fun mergeStacks(entity1: LivingEntity, entity2: LivingEntity): LivingEntity? {
        if (!canStack(entity1, entity2)) {
            return null
        }
        
        val stack1 = getStackSize(entity1)
        val stack2 = getStackSize(entity2)
        val totalStack = stack1 + stack2
        
        // Determine which entity to keep (prefer the one with larger stack, or entity1 if equal)
        val keepEntity = if (stack1 >= stack2) entity1 else entity2
        val removeEntity = if (keepEntity == entity1) entity2 else entity1
        
        // Double-check entities are still valid
        if (!keepEntity.isValid || !removeEntity.isValid) return null
        if (keepEntity.isDead || removeEntity.isDead) return null
        
        if (totalStack > maxStackSize) {
            // Split the stack
            setStackSize(keepEntity, maxStackSize)
            // Don't spawn the remainder immediately, it will spawn on death
            removeEntity.remove()
            Log.inf("Mob stacking: Merged ${entity1.type} - kept $maxStackSize, removed ${totalStack - maxStackSize}")
            return keepEntity
        } else {
            // Merge completely
            setStackSize(keepEntity, totalStack)
            removeEntity.remove()
            Log.inf("Mob stacking: Merged ${entity1.type} - new stack size: $totalStack")
            return keepEntity
        }
    }
    
    /**
     * Handle stack splitting on death
     * Returns a Pair: (deathCount for drop/XP multiplication, shouldCancelDeath)
     */
    fun handleStackDeath(entity: LivingEntity): Pair<Int, Boolean> {
        val stackSize = getStackSize(entity)
        
        if (stackSize <= 1) {
            return Pair(1, false)
        }
        
        val deathCount = 1 // One mob dies
        val remainingStack = stackSize - deathCount
        
        if (remainingStack > 0) {
            if (skipDeathAnimation) {
                // Just reduce stack size without spawning new entity
                setStackSize(entity, remainingStack)
                // Cancel the death event's entity removal
                return Pair(deathCount, true)
            } else {
                // Spawn a new entity with remaining stack
                val newEntity = spawnStackedEntity(entity, remainingStack)
                if (newEntity != null) {
                    setStackSize(newEntity, remainingStack)
                    // Remove stack data from dying entity
                    removeStackData(entity)
                }
            }
        } else {
            removeStackData(entity)
        }
        
        return Pair(deathCount, false)
    }
    
    /**
     * Spawn a new stacked entity at the location of the old one
     */
    private fun spawnStackedEntity(original: LivingEntity, stackSize: Int): LivingEntity? {
        try {
            val location = original.location
            val entityType = original.type
            
            // Spawn new entity
            val newEntity = location.world.spawnEntity(location, entityType) as? LivingEntity
                ?: return null
            
            // Copy level data if original was levelled
            if (main.levelInterface.isLevelled(original)) {
                val level = main.levelInterface.getLevelOfMob(original)
                val lmNew = LivingEntityWrapper.getInstance(newEntity)
                try {
                    // Apply level to new entity
                    main.levelManager.applyLevelToMob(lmNew, level, false, false, null)
                } finally {
                    lmNew.free()
                }
            }
            
            // Set initial stack size
            setStackSize(newEntity, stackSize)
            
            return newEntity
        } catch (e: Exception) {
            Log.war("Failed to spawn stacked entity: ${e.message}")
            return null
        }
    }
    
    /**
     * Update stack display using ArmorStand above the mob
     */
    private fun updateStackDisplay(entity: LivingEntity, stackSize: Int) {
        if (stackSize < minStackSize) {
            // Remove display if below threshold
            removeStackDisplay(entity)
            return
        }
        
        if (!entity.isValid || entity.isDead) {
            removeStackDisplay(entity)
            return
        }
        
        // Get or create armorstand
        var armorStand = stackArmorStands[entity]
        
        if (armorStand == null || !armorStand.isValid) {
            // Create new armorstand above the mob
            val location = entity.location.clone().add(0.0, entity.height + 0.5, 0.0)
            
            // Format stack display text
            val displayText = formatStackDisplay(stackSize)
            
            armorStand = entity.world.spawn(location, ArmorStand::class.java) { stand ->
                stand.isVisible = false
                stand.isMarker = true
                stand.isSmall = true
                stand.setGravity(false)
                stand.setBasePlate(false)
                stand.setArms(false)
                @Suppress("DEPRECATION")
                stand.customName = displayText
                stand.isCustomNameVisible = true
                stand.setCollidable(false)
                stand.isInvulnerable = true
                stand.setCanPickupItems(false)
            }
            
            // Always use teleport tracking to keep armorstand above mob
            startArmorStandTracking(armorStand, entity)
            
            stackArmorStands[entity] = armorStand
        } else {
            // Update existing armorstand
            val displayText = formatStackDisplay(stackSize)
            @Suppress("DEPRECATION")
            armorStand.customName = displayText
            armorStand.isCustomNameVisible = true
            
            // Update position immediately
            if (entity.isValid && !entity.isDead) {
                val newLoc = entity.location.clone().add(0.0, entity.height + 0.5, 0.0)
                armorStand.teleport(newLoc)
            }
        }
    }
    
    /**
     * Format the stack display text using the configured format
     */
    private fun formatStackDisplay(stackSize: Int): String {
        var formatted = stackDisplayFormat
        formatted = formatted.replace("%stack-size%", stackSize.toString())
        formatted = formatted.replace("&", "§") // Convert & color codes to §
        return formatted
    }
    
    /**
     * Remove stack display armorstand
     */
    private fun removeStackDisplay(entity: LivingEntity) {
        val armorStand = stackArmorStands.remove(entity)
        if (armorStand != null && armorStand.isValid) {
            armorStand.remove()
        }
    }
    
    /**
     * Remove stack display on entity death
     */
    fun removeStackDisplayOnDeath(entity: LivingEntity) {
        removeStackDisplay(entity)
    }
    
    /**
     * Start tracking the mob with the armorstand (for Spigot or fallback)
     */
    private fun startArmorStandTracking(armorStand: ArmorStand, mob: LivingEntity) {
        val task = Runnable {
            if (!mob.isValid || mob.isDead) {
                removeStackDisplay(mob)
                return@Runnable
            }
            
            if (!armorStand.isValid) {
                stackArmorStands.remove(mob)
                return@Runnable
            }
            
            // Teleport armorstand above mob
            val location = mob.location.clone().add(0.0, mob.height + 0.5, 0.0)
            armorStand.teleport(location)
        }
        
        // Run every tick to keep armorstand above mob
        if (main.ver.isRunningFolia) {
            val consumer: Consumer<io.papermc.paper.threadedregions.scheduler.ScheduledTask?> = Consumer { _ ->
                task.run()
            }
            Bukkit.getRegionScheduler().runAtFixedRate(
                main, mob.location, consumer, 1, 1
            )
        } else {
            Bukkit.getScheduler().runTaskTimer(main, task, 1, 1)
        }
    }
    
    /**
     * Check if entity type can be stacked
     */
    fun canStackType(entityType: EntityType): Boolean {
        // Only stack the hostile mobs we specified
        val allowedTypes = setOf(
            EntityType.ENDER_DRAGON,
            EntityType.WITHER,
            EntityType.GUARDIAN,
            EntityType.ELDER_GUARDIAN,
            EntityType.SPIDER,
            EntityType.CAVE_SPIDER,
            EntityType.BLAZE,
            EntityType.CREEPER,
            EntityType.DROWNED,
            EntityType.ENDERMITE,
            EntityType.EVOKER,
            EntityType.GHAST,
            EntityType.HOGLIN,
            EntityType.HUSK,
            EntityType.MAGMA_CUBE,
            EntityType.PHANTOM,
            EntityType.PIGLIN_BRUTE,
            EntityType.PILLAGER,
            EntityType.RAVAGER,
            EntityType.SHULKER,
            EntityType.SILVERFISH,
            EntityType.SKELETON,
            EntityType.STRAY,
            EntityType.WITHER_SKELETON,
            EntityType.SLIME,
            EntityType.VEX,
            EntityType.VINDICATOR,
            EntityType.WARDEN,
            EntityType.WITCH,
            EntityType.ZOGLIN,
            EntityType.ZOMBIE,
            EntityType.ZOMBIE_VILLAGER
        )
        
        // Check for 1.21+ mobs
        try {
            val breeze = EntityType.valueOf("BREEZE")
            val bogged = EntityType.valueOf("BOGGED")
            val creaking = EntityType.valueOf("CREAKING")
            return allowedTypes.contains(entityType) || 
                   entityType == breeze || 
                   entityType == bogged || 
                   entityType == creaking
        } catch (e: IllegalArgumentException) {
            // These mobs don't exist in this version
            return allowedTypes.contains(entityType)
        }
    }
    
    /**
     * Check if there's a player nearby within the configured radius
     */
    private fun hasPlayerNearby(entity: LivingEntity): Boolean {
        if (!requirePlayerNearby) return true
        
        val location = entity.location
        for (player in Bukkit.getOnlinePlayers()) {
            if (player.world != location.world) continue
            if (player.location.distance(location) <= playerCheckRadius) {
                return true
            }
        }
        return false
    }
    
    /**
     * Try to stack a newly spawned entity with nearby entities
     */
    fun tryStackOnSpawn(entity: LivingEntity) {
        if (!stackOnSpawn || !isEnabled) return
        if (!canStackType(entity.type)) return
        if (entity.isDead || !entity.isValid) return
        
        // Only stack if player is nearby
        if (!hasPlayerNearby(entity)) {
            return
        }
        
        val nearbyEntities = entity.getNearbyEntities(stackRadius, stackRadius, stackRadius)
        
        for (nearby in nearbyEntities) {
            if (nearby !is LivingEntity) continue
            if (nearby == entity) continue
            if (!canStackType(nearby.type)) continue
            if (nearby.isDead || !nearby.isValid) continue
            
            if (canStack(entity, nearby)) {
                val merged = mergeStacks(entity, nearby)
                if (merged != null) {
                    // Successfully merged, stop searching
                    return
                }
            }
        }
        
        // No merge happened, set stack size to 1
        setStackSize(entity, 1)
    }
    
    /**
     * Periodic task to check for stacking opportunities
     * This runs every few seconds to merge nearby mobs
     */
    fun startStackingTask() {
        if (!isEnabled) {
            Log.inf("Mob stacking task not started - stacking is disabled")
            return
        }
        
        val main = LevelledMobs.instance
        val period = main.helperSettings.getInt("mob-stacking.check-period", 3).toLong() // Check every 3 seconds
        
        Log.inf("Starting mob stacking task - checking every $period seconds")
        
        val runnable = Runnable {
            checkForStackingOpportunities()
        }
        
        if (main.ver.isRunningFolia) {
            val task = java.util.function.Consumer { _: io.papermc.paper.threadedregions.scheduler.ScheduledTask? -> 
                runnable.run()
            }
            Bukkit.getAsyncScheduler().runAtFixedRate(
                main, task, period, period, java.util.concurrent.TimeUnit.SECONDS
            )
        } else {
            Bukkit.getScheduler().runTaskTimer(main, runnable, 20 * period, 20 * period)
        }
    }
    
    /**
     * Check all loaded entities for stacking opportunities
     * More aggressive - finds all nearby mobs and merges them into the largest stack
     */
    private fun checkForStackingOpportunities() {
        if (!isEnabled) return
        
        val checkedEntities = mutableSetOf<LivingEntity>()
        var stacksCreated = 0
        
        // Get all online players and their locations
        val playerLocations = mutableListOf<org.bukkit.Location>()
        for (player in Bukkit.getOnlinePlayers()) {
            if (player.isOnline && !player.isDead) {
                playerLocations.add(player.location)
            }
        }
        
        // If no players online and requirePlayerNearby is true, skip entirely
        if (requirePlayerNearby && playerLocations.isEmpty()) {
            return
        }
        
        // Only check worlds that have players
        val worldsWithPlayers = playerLocations.map { it.world }.distinct()
        
        for (world in worldsWithPlayers) {
            if (!world.isChunkLoaded(0, 0)) continue // Skip if world not ready
            
            // Get entities near players only
            val entitiesToCheck = mutableSetOf<LivingEntity>()
            
            for (playerLoc in playerLocations) {
                if (playerLoc.world != world) continue
                
                // Get entities within player check radius + stack radius
                val searchRadius = (playerCheckRadius + stackRadius).toInt()
                val nearbyEntities = world.getNearbyEntities(
                    playerLoc, 
                    searchRadius.toDouble(), 
                    searchRadius.toDouble(), 
                    searchRadius.toDouble()
                ) { it is LivingEntity && canStackType(it.type) }
                
                for (entity in nearbyEntities) {
                    if (entity is LivingEntity && !entity.isDead && entity.isValid) {
                        // Double check player proximity
                        if (!requirePlayerNearby || hasPlayerNearby(entity)) {
                            entitiesToCheck.add(entity)
                        }
                    }
                }
            }
            
            // Now process only entities near players
            for (entity in entitiesToCheck) {
                if (checkedEntities.contains(entity)) continue
                if (!canStackType(entity.type)) continue
                if (entity.isDead || !entity.isValid) continue
                
                val processed = mutableSetOf<LivingEntity>()
                val toRemove = mutableListOf<LivingEntity>()
                var totalStack = getStackSize(entity)
                
                // Find all nearby stackable entities (within stack radius)
                val nearbyEntities = entity.getNearbyEntities(stackRadius, stackRadius, stackRadius)
                
                for (nearby in nearbyEntities) {
                    if (nearby !is LivingEntity) continue
                    if (nearby == entity) continue
                    if (processed.contains(nearby)) continue
                    if (checkedEntities.contains(nearby)) continue
                    if (!canStackType(nearby.type)) continue
                    if (nearby.isDead || !nearby.isValid) continue
                    
                    if (canStack(entity, nearby)) {
                        val nearbyStack = getStackSize(nearby)
                        totalStack += nearbyStack
                        toRemove.add(nearby)
                        processed.add(nearby)
                    }
                }
                
                if (totalStack > maxStackSize) {
                    totalStack = maxStackSize
                }
                
                if (toRemove.isNotEmpty()) {
                    setStackSize(entity, totalStack)
                    
                    // Remove merged entities and their displays
                    for (removeEntity in toRemove) {
                        removeStackDisplay(removeEntity)
                        removeEntity.remove()
                        checkedEntities.add(removeEntity)
                    }
                    
                    checkedEntities.add(entity)
                    processed.add(entity)
                } else {
                    processed.add(entity)
                }
            }
        }
    }
}

