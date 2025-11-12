# Offline Build Setup Guide

This guide explains how to set up the repository for offline builds by vendoring Gradle dependencies.

## Problem

The build requires:
1. Gradle wrapper (already committed)
2. Fabric Loom plugin from maven.fabricmc.net
3. Minecraft and Fabric dependencies
4. Build tools and libraries

## Solution: Vendor the Gradle Cache

### Step 1: On Your Local Machine (with network access)

1. Clone the repository:
   ```bash
   git clone https://github.com/deepinthewoods/mc-5e-srd.git
   cd mc-5e-srd
   git checkout claude/implement-next-task-011CV4ToB114WkwVs3dDs2vF
   ```

2. Run a full build to download all dependencies:
   ```bash
   ./gradlew build --refresh-dependencies
   ```

3. Create a vendored cache directory in the repo:
   ```bash
   mkdir -p gradle-cache
   ```

4. Copy your Gradle cache to the repo:
   ```bash
   # Copy the entire cache (can be large, 500MB-1GB+)
   cp -r ~/.gradle/caches gradle-cache/

   # Also copy the Gradle wrapper distribution
   cp -r ~/.gradle/wrapper gradle-cache/wrapper
   ```

5. Create a gitignore exception for the cache:
   ```bash
   echo '!gradle-cache/' >> .gitignore
   echo '!gradle-cache/**' >> .gitignore
   ```

6. Commit and push:
   ```bash
   git add gradle-cache/
   git add .gitignore
   git commit -m "Add vendored Gradle dependencies for offline builds"
   git push
   ```

### Step 2: Configure Gradle to Use Vendored Cache

On the build machine, you'll need to tell Gradle to use the vendored cache by setting the `GRADLE_USER_HOME` environment variable:

```bash
export GRADLE_USER_HOME=/home/user/mc-5e-srd/gradle-cache
gradle build
```

Or you can copy the vendored cache to the default location:

```bash
cp -r gradle-cache/caches ~/.gradle/
cp -r gradle-cache/wrapper ~/.gradle/
./gradlew build --offline
```

## Alternative: Simpler Approach (Smaller Size)

If the full cache is too large, you can use Gradle's dependency locking:

### On your local machine:

1. Generate dependency lock files:
   ```bash
   ./gradlew dependencies --write-locks
   ```

2. This creates `gradle.lockfile` which you commit to the repo

3. Then copy just the essential dependencies:
   ```bash
   # Copy only module metadata and artifacts
   mkdir -p gradle-cache/caches/modules-2
   cp -r ~/.gradle/caches/modules-2/files-2.1 gradle-cache/caches/modules-2/
   cp -r ~/.gradle/caches/modules-2/metadata-2.* gradle-cache/caches/modules-2/

   # Copy buildscript dependencies (Fabric Loom, etc.)
   mkdir -p gradle-cache/caches/8.14.3
   cp -r ~/.gradle/caches/8.14.3/* gradle-cache/caches/8.14.3/
   ```

## Expected Sizes

- Full cache: 500MB - 1.5GB
- Essential dependencies only: 200-400MB
- Lock files only: <1MB (but still needs network access for downloads)

## Testing Offline Build

After setting up, test the offline build:

```bash
# Set Gradle home to use vendored cache
export GRADLE_USER_HOME=/home/user/mc-5e-srd/gradle-cache

# Try building offline
gradle build --offline
```

## Notes

- The vendored cache should be excluded from normal Git operations on development machines
- This is a workaround for network-restricted build environments
- For production CI/CD, consider using a local Maven mirror or Artifactory
