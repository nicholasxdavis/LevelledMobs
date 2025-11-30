# Instructions for Submitting Mob Stacking Feature to LevelledMobs

## Option 1: Fork and Pull Request (Recommended)

1. **Fork the Repository**
   - Go to https://github.com/ArcanePlugins/LevelledMobs
   - Click "Fork" to create your own fork

2. **Clone Your Fork**
   ```bash
   git clone https://github.com/YOUR_USERNAME/LevelledMobs.git
   cd LevelledMobs
   ```

3. **Copy Your Changes**
   - Copy the modified files from your current directory to the cloned repository
   - Key files to copy:
     - `levelledmobs-plugin/src/main/kotlin/io/github/arcaneplugins/levelledmobs/managers/StackManager.kt`
     - Modified files in `levelledmobs-plugin/src/main/kotlin/io/github/arcaneplugins/levelledmobs/`
     - Modified `settings.yml`

4. **Create a New Branch**
   ```bash
   git checkout -b feature/mob-stacking
   ```

5. **Commit Your Changes**
   ```bash
   git add .
   git commit -m "Add mob stacking feature with performance optimizations

   - Added StackManager for intelligent mob stacking
   - Player proximity checks to reduce lag
   - Configurable stack display with ArmorStand
   - Only processes mobs near players (100 block radius)
   - Fully configurable via settings.yml"
   ```

6. **Push to Your Fork**
   ```bash
   git push origin feature/mob-stacking
   ```

7. **Create Pull Request**
   - Go to https://github.com/ArcanePlugins/LevelledMobs
   - Click "New Pull Request"
   - Select your fork and branch
   - Use the template from `PULL_REQUEST_TEMPLATE.md`
   - Mention that you recommend this as a separate plugin option

## Option 2: Direct Repository Setup

If you want to work directly with the official repo:

1. **Clone the Official Repository**
   ```bash
   git clone https://github.com/ArcanePlugins/LevelledMobs.git
   cd LevelledMobs
   ```

2. **Copy Your Changes**
   - Copy all modified/new files from your current directory

3. **Follow steps 4-7 from Option 1**

## Important Notes

1. **License**: Ensure your changes comply with AGPL-3.0 license
2. **Code Style**: Match existing LevelledMobs code style
3. **Documentation**: Include the `MOB_STACKING_FEATURE.md` in your PR description
4. **Recommendation**: Mention in the PR that you suggest this could be a separate plugin

## Files to Include in PR

### New Files:
- `levelledmobs-plugin/src/main/kotlin/io/github/arcaneplugins/levelledmobs/managers/StackManager.kt`

### Modified Files:
- `levelledmobs-plugin/src/main/kotlin/io/github/arcaneplugins/levelledmobs/LevelledMobs.kt`
- `levelledmobs-plugin/src/main/kotlin/io/github/arcaneplugins/levelledmobs/listeners/EntitySpawnListener.kt`
- `levelledmobs-plugin/src/main/kotlin/io/github/arcaneplugins/levelledmobs/listeners/EntityDeathListener.kt`
- `levelledmobs-plugin/src/main/kotlin/io/github/arcaneplugins/levelledmobs/misc/NamespacedKeys.kt`
- `levelledmobs-plugin/src/main/resources/settings.yml`

## PR Description Template

Use this in your pull request:

```markdown
## Mob Stacking Feature

This PR adds intelligent mob stacking functionality to reduce entity count and server lag.

### Features
- Stacks similar mobs together (configurable radius: 25 blocks)
- Only processes mobs near players (100 block radius) for performance
- Visual stack display using ArmorStand above mobs
- Fully configurable via settings.yml
- Can be completely disabled

### Performance
- Significantly reduces entity count on high-population servers
- Smart player proximity checks prevent unnecessary processing
- Configurable check intervals

### Recommendation
We recommend considering this as a separate optional plugin (`LevelledMobs-Stacking`) for better modularity, but it can also remain integrated as an optional feature.

See `MOB_STACKING_FEATURE.md` for complete documentation.
```

## After Submission

1. Monitor the PR for feedback
2. Be ready to make adjustments based on maintainer feedback
3. Consider creating a separate plugin repository if they prefer that approach

