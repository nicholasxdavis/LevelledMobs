# How to Push Your Changes

## Step 1: Fork the Repository (if you haven't already)

1. Go to https://github.com/ArcanePlugins/LevelledMobs
2. Click the "Fork" button in the top right
3. This creates your own copy at `https://github.com/YOUR_USERNAME/LevelledMobs`

## Step 2: Add Your Fork as Remote

Replace `YOUR_USERNAME` with your GitHub username:

```bash
cd "C:\Users\lifa2\OneDrive\Desktop\LevelledMobs-master"
git remote add origin https://github.com/YOUR_USERNAME/LevelledMobs.git
```

## Step 3: Push Your Changes

```bash
git push -u origin main
```

If you get authentication errors, you may need to:
- Use a Personal Access Token instead of password
- Or use SSH: `git remote set-url origin git@github.com:YOUR_USERNAME/LevelledMobs.git`

## Step 4: Create Pull Request

1. Go to https://github.com/ArcanePlugins/LevelledMobs
2. Click "New Pull Request"
3. Select your fork and branch
4. Use the description from `PULL_REQUEST_TEMPLATE.md`
5. Mention that you recommend this as a separate plugin option

