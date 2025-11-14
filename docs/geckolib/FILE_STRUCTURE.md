# File Structure and Organization Guide

## Overview

This document provides the complete file structure for the GeckoLib animation system, including naming conventions, organization patterns, and file templates.

---

## Directory Tree

### Complete Project Structure

```
mc-5e-srd/
├── docs/                                           # Documentation (this directory)
│   ├── GECKOLIB_ARCHITECTURE.md
│   ├── BLOCKBENCH_SPECIFICATIONS.md
│   ├── ANIMATION_RETARGETING.md
│   ├── LAYER_SYSTEM.md
│   ├── IMPLEMENTATION_PLAN.md
│   └── FILE_STRUCTURE.md (this file)
│
├── src/
│   ├── main/
│   │   ├── java/ninja/trek/srd/
│   │   │   │
│   │   │   ├── character/                          # Character system (shared)
│   │   │   │   ├── entity/
│   │   │   │   │   └── CharacterEntity.java        # Main entity class
│   │   │   │   │
│   │   │   │   ├── skeleton/                       # Skeleton & retargeting (shared)
│   │   │   │   │   ├── SkeletonProfile.java        # Skeleton definitions
│   │   │   │   │   ├── HumanoidBones.java          # Bone name constants
│   │   │   │   │   ├── BoneDefinition.java         # Single bone data
│   │   │   │   │   └── BoneRetargeting.java        # Retargeting math
│   │   │   │   │
│   │   │   │   ├── layer/                          # Layer configurations (shared)
│   │   │   │   │   ├── BodyPartConfiguration.java
│   │   │   │   │   ├── EquipmentLayerConfiguration.java
│   │   │   │   │   ├── ClothingConfiguration.java
│   │   │   │   │   ├── LayerConfiguration.java
│   │   │   │   │   ├── EquipmentSlot.java
│   │   │   │   │   └── LayerType.java
│   │   │   │   │
│   │   │   │   ├── animation/                      # Animation data (shared)
│   │   │   │   │   ├── AnimationType.java
│   │   │   │   │   ├── AnimationSpeed.java
│   │   │   │   │   ├── RetargetedAnimation.java
│   │   │   │   │   └── RetargetedKeyframe.java
│   │   │   │   │
│   │   │   │   └── data/                           # Existing character data
│   │   │   │       ├── Race.java
│   │   │   │       ├── CharacterClass.java
│   │   │   │       └── ...
│   │   │   │
│   │   │   └── registry/
│   │   │       ├── ModEntities.java
│   │   │       └── ...
│   │   │
│   │   └── resources/assets/fiveesrd/
│   │       │
│   │       ├── geo/                                # GeckoLib geometry files
│   │       │   └── entity/
│   │       │       ├── character/
│   │       │       │   ├── base/
│   │       │       │   │   ├── humanoid_reference.geo.json
│   │       │       │   │   └── README.md
│   │       │       │   │
│   │       │       │   ├── dwarf/
│   │       │       │   │   ├── dwarf_body.geo.json
│   │       │       │   │   ├── dwarf_heads.geo.json
│   │       │       │   │   └── dwarf_limbs.geo.json
│   │       │       │   │
│   │       │       │   ├── elf/
│   │       │       │   │   ├── elf_body.geo.json
│   │       │       │   │   ├── elf_heads.geo.json
│   │       │       │   │   └── elf_limbs.geo.json
│   │       │       │   │
│   │       │       │   ├── human/
│   │       │       │   │   ├── human_body.geo.json
│   │       │       │   │   ├── human_heads.geo.json
│   │       │       │   │   └── human_limbs.geo.json
│   │       │       │   │
│   │       │       │   └── halfling/
│   │       │       │       ├── halfling_body.geo.json
│   │       │       │       ├── halfling_heads.geo.json
│   │       │       │       └── halfling_limbs.geo.json
│   │       │       │
│   │       │       └── equipment/
│   │       │           ├── armor/
│   │       │           │   ├── leather/
│   │       │           │   │   ├── leather_helmet.geo.json
│   │       │           │   │   ├── leather_chestplate.geo.json
│   │       │           │   │   ├── leather_leggings.geo.json
│   │       │           │   │   └── leather_boots.geo.json
│   │       │           │   │
│   │       │           │   ├── chainmail/
│   │       │           │   │   ├── chainmail_coif.geo.json
│   │       │           │   │   ├── chainmail_hauberk.geo.json
│   │       │           │   │   ├── chainmail_chausses.geo.json
│   │       │           │   │   └── chainmail_boots.geo.json
│   │       │           │   │
│   │       │           │   ├── plate/
│   │       │           │   │   ├── plate_helmet.geo.json
│   │       │           │   │   ├── plate_chestplate.geo.json
│   │       │           │   │   ├── plate_greaves.geo.json
│   │       │           │   │   └── plate_sabatons.geo.json
│   │       │           │   │
│   │       │           │   └── robes/
│   │       │           │       ├── wizard_hat.geo.json
│   │       │           │       ├── wizard_robe.geo.json
│   │       │           │       └── wizard_shoes.geo.json
│   │       │           │
│   │       │           ├── weapons/
│   │       │           │   ├── swords.geo.json
│   │       │           │   ├── axes.geo.json
│   │       │           │   ├── bows.geo.json
│   │       │           │   └── staves.geo.json
│   │       │           │
│   │       │           └── accessories/
│   │       │               ├── capes.geo.json
│   │       │               ├── belts.geo.json
│   │       │               └── backpacks.geo.json
│   │       │
│   │       ├── animations/                         # GeckoLib animation files
│   │       │   └── entity/
│   │       │       └── character/
│   │       │           ├── locomotion.animation.json
│   │       │           ├── combat.animation.json
│   │       │           ├── magic.animation.json
│   │       │           ├── emotes.animation.json
│   │       │           └── race_specific/
│   │       │               ├── dwarf_special.animation.json
│   │       │               └── elf_special.animation.json
│   │       │
│   │       └── textures/                           # Texture files
│   │           └── entity/
│   │               └── character/
│   │                   ├── base/                   # Base skin textures
│   │                   │   ├── dwarf_skin_0.png
│   │                   │   ├── dwarf_skin_1.png
│   │                   │   ├── dwarf_skin_2.png
│   │                   │   ├── elf_skin_0.png
│   │                   │   ├── elf_skin_1.png
│   │                   │   ├── human_skin_0.png
│   │                   │   ├── human_skin_1.png
│   │                   │   ├── halfling_skin_0.png
│   │                   │   └── ...
│   │                   │
│   │                   ├── overlays/               # Hair, tattoos, scars
│   │                   │   ├── hair_overlay_brown.png
│   │                   │   ├── hair_overlay_black.png
│   │                   │   ├── tattoo_tribal_0.png
│   │                   │   ├── scar_face_0.png
│   │                   │   └── ...
│   │                   │
│   │                   ├── clothing/               # Clothing textures
│   │                   │   ├── shirt_tunic_red.png
│   │                   │   ├── shirt_tunic_blue.png
│   │                   │   ├── pants_leather_brown.png
│   │                   │   ├── robe_wizard_purple.png
│   │                   │   └── ...
│   │                   │
│   │                   └── equipment/              # Armor textures
│   │                       ├── armor/
│   │                       │   ├── leather_brown.png
│   │                       │   ├── leather_black.png
│   │                       │   ├── leather_dyed_FF0000.png (red)
│   │                       │   ├── chainmail_steel.png
│   │                       │   ├── plate_iron.png
│   │                       │   ├── plate_gold.png
│   │                       │   └── robes_wizard_blue.png
│   │                       │
│   │                       └── accessories/
│   │                           ├── cape_red.png
│   │                           ├── cape_blue.png
│   │                           └── belt_leather.png
│   │
│   └── client/
│       └── java/ninja/trek/srd/client/
│           │
│           ├── model/                              # Client-only models
│           │   └── geckolib/
│           │       ├── CharacterGeoModel.java
│           │       ├── EquipmentGeoModel.java
│           │       ├── ArmorGeoModel.java
│           │       └── AccessoryGeoModel.java
│           │
│           ├── render/                             # Client-only renderers
│           │   └── geckolib/
│           │       ├── CharacterGeoRenderer.java
│           │       │
│           │       ├── layer/                      # Render layers
│           │       │   ├── BaseBodyLayerRenderer.java
│           │       │   ├── EquipmentLayerRenderer.java
│           │       │   ├── ClothingLayerRenderer.java
│           │       │   ├── HeldItemLayerRenderer.java
│           │       │   ├── EffectsLayerRenderer.java
│           │       │   └── LayerRenderManager.java
│           │       │
│           │       ├── animation/                  # Animation controllers
│           │       │   ├── CharacterAnimationController.java
│           │       │   ├── BoneRetargetingController.java
│           │       │   ├── AnimationStateManager.java
│           │       │   ├── RetargetingStrategy.java
│           │       │   ├── BasicRetargetingStrategy.java
│           │       │   ├── LocomotionRetargetingStrategy.java
│           │       │   └── ChainRetargetingStrategy.java
│           │       │
│           │       └── util/                       # Utilities
│           │           ├── LayerVisibilityManager.java
│           │           ├── DynamicTextureComposer.java
│           │           ├── TextureCache.java
│           │           └── ArmorLayerManager.java
│           │
│           ├── registry/                           # Client registries
│           │   ├── ModGeoModels.java
│           │   └── ModAnimations.java
│           │
│           └── FiveESrdModClient.java              # Client entrypoint
│
├── blockbench/                                     # BlockBench source files
│   ├── characters/
│   │   ├── dwarf/
│   │   │   ├── dwarf_body.bbmodel
│   │   │   ├── dwarf_heads.bbmodel
│   │   │   └── dwarf_limbs.bbmodel
│   │   │
│   │   ├── elf/
│   │   │   ├── elf_body.bbmodel
│   │   │   ├── elf_heads.bbmodel
│   │   │   └── elf_limbs.bbmodel
│   │   │
│   │   ├── human/
│   │   │   ├── human_body.bbmodel
│   │   │   ├── human_heads.bbmodel
│   │   │   └── human_limbs.bbmodel
│   │   │
│   │   └── halfling/
│   │       ├── halfling_body.bbmodel
│   │       ├── halfling_heads.bbmodel
│   │       └── halfling_limbs.bbmodel
│   │
│   ├── equipment/
│   │   ├── armor/
│   │   │   ├── leather_armor.bbmodel
│   │   │   ├── chainmail_armor.bbmodel
│   │   │   ├── plate_armor.bbmodel
│   │   │   └── wizard_robes.bbmodel
│   │   │
│   │   └── accessories/
│   │       ├── capes.bbmodel
│   │       └── backpacks.bbmodel
│   │
│   ├── animations/
│   │   ├── locomotion.bbmodel              # Idle, walk, run animations
│   │   ├── combat.bbmodel                  # Attack, block animations
│   │   ├── magic.bbmodel                   # Casting animations
│   │   └── emotes.bbmodel                  # Wave, point, etc.
│   │
│   └── templates/
│       ├── character_template.bbmodel       # Template with skeleton
│       ├── armor_template.bbmodel           # Template for armor
│       └── README.md                        # Template usage guide
│
└── build.gradle                                    # Project build file
```

---

## File Naming Conventions

### Java Classes

```
Pattern: PascalCase
Examples:
- CharacterEntity.java
- SkeletonProfile.java
- BoneRetargetingController.java
- EquipmentLayerRenderer.java
```

### Resource Files

#### Geometry Files (.geo.json)
```
Pattern: {race}_{part}.geo.json or {type}_{item}.geo.json
Examples:
- dwarf_body.geo.json
- elf_heads.geo.json
- leather_helmet.geo.json
- wizard_robes.geo.json
```

#### Animation Files (.animation.json)
```
Pattern: {category}.animation.json or {race}_{special}.animation.json
Examples:
- locomotion.animation.json
- combat.animation.json
- magic.animation.json
- dwarf_special.animation.json
```

#### Texture Files (.png)
```
Pattern: {type}_{variant}_{color}.png or {type}_{variant}.png
Examples:
- dwarf_skin_0.png
- human_skin_1.png
- leather_brown.png
- leather_dyed_FF0000.png (red dyed)
- chainmail_steel.png
- wizard_robe_blue.png
```

#### BlockBench Files (.bbmodel)
```
Pattern: {race}_{part}.bbmodel or {type}_{item}.bbmodel
Examples:
- dwarf_body.bbmodel
- elf_heads.bbmodel
- leather_armor.bbmodel
- wizard_robes.bbmodel
```

---

## File Templates

### Java Class Templates

#### Skeleton Profile Template

```java
package ninja.trek.srd.character.skeleton;

import org.joml.Vector3f;
import java.util.HashMap;
import java.util.Map;

/**
 * Skeleton profile for [RACE_NAME]
 * Total height: [HEIGHT]m
 * Proportions: [DESCRIPTION]
 */
public class SkeletonProfile {

    public final String raceName;
    public final float totalHeight;
    public final Map<String, BoneDefinition> bones;

    private SkeletonProfile(String raceName, float totalHeight, Map<String, BoneDefinition> bones) {
        this.raceName = raceName;
        this.totalHeight = totalHeight;
        this.bones = bones;
    }

    public BoneDefinition getBone(String name) {
        return bones.get(name);
    }

    public float getBoneLength(String name) {
        BoneDefinition bone = bones.get(name);
        return bone != null ? bone.length : 0.0f;
    }

    // Pre-defined profiles
    public static final SkeletonProfile HUMAN = new Builder()
        .raceName("human")
        .totalHeight(1.8f)
        .addBone("body", 0.6f, new Vector3f(0, 12, 0), null)
        .addBone("head", 0.3f, new Vector3f(0, 24, 0), "body")
        // ... more bones
        .build();

    public static class Builder {
        private String raceName;
        private float totalHeight;
        private Map<String, BoneDefinition> bones = new HashMap<>();

        public Builder raceName(String name) {
            this.raceName = name;
            return this;
        }

        public Builder totalHeight(float height) {
            this.totalHeight = height;
            return this;
        }

        public Builder addBone(String name, float length, Vector3f pivot, String parent) {
            bones.put(name, new BoneDefinition(name, length, pivot, parent));
            return this;
        }

        public SkeletonProfile build() {
            return new SkeletonProfile(raceName, totalHeight, bones);
        }
    }

    public static class BoneDefinition {
        public final String name;
        public final float length;
        public final Vector3f pivot;
        public final String parent;

        public BoneDefinition(String name, float length, Vector3f pivot, String parent) {
            this.name = name;
            this.length = length;
            this.pivot = pivot;
            this.parent = parent;
        }
    }
}
```

#### GeoModel Template

```java
package ninja.trek.srd.client.model.geckolib;

import net.minecraft.util.Identifier;
import ninja.trek.srd.character.entity.CharacterEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

/**
 * GeckoLib model for [DESCRIPTION]
 */
public class [NAME]GeoModel extends GeoModel<CharacterEntity> {

    @Override
    public Identifier getModelResource(CharacterEntity entity) {
        // Dynamic model selection based on entity data
        String race = entity.getRace().getName().toLowerCase();
        return new Identifier("fiveesrd", "geo/entity/character/" + race + "/" + race + "_body.geo.json");
    }

    @Override
    public Identifier getTextureResource(CharacterEntity entity) {
        // Dynamic texture selection
        String texture = entity.getBodyConfiguration().skinTexture;
        return new Identifier("fiveesrd", "textures/entity/character/base/" + texture + ".png");
    }

    @Override
    public Identifier getAnimationResource(CharacterEntity entity) {
        return new Identifier("fiveesrd", "animations/entity/character/locomotion.animation.json");
    }
}
```

#### Renderer Template

```java
package ninja.trek.srd.client.render.geckolib;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.client.model.geckolib.[NAME]GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Renderer for [DESCRIPTION]
 */
public class [NAME]GeoRenderer extends GeoEntityRenderer<CharacterEntity> {

    public [NAME]GeoRenderer(EntityRendererFactory.Context context) {
        super(context, new [NAME]GeoModel());

        this.shadowRadius = 0.5f;

        // Add render layers
        // addRenderLayer(new EquipmentLayerRenderer(this));
    }

    @Override
    public void render(CharacterEntity entity, float entityYaw, float partialTick,
                       MatrixStack poseStack, VertexConsumerProvider bufferSource,
                       int packedLight) {

        // Apply size scaling
        float scale = entity.getSizeScale();
        poseStack.scale(scale, scale, scale);

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
```

---

### Resource File Templates

#### Animation JSON Template

```json
{
  "format_version": "1.8.0",
  "animations": {
    "animation.character.idle": {
      "loop": true,
      "animation_length": 2.0,
      "bones": {
        "body": {
          "rotation": {
            "0.0": [0, 0, 0],
            "1.0": [2, 0, 0],
            "2.0": [0, 0, 0]
          }
        },
        "head": {
          "rotation": {
            "0.0": [0, 0, 0],
            "1.5": [-2, 0, 0],
            "2.0": [0, 0, 0]
          }
        }
      }
    },
    "animation.character.walk": {
      "loop": true,
      "animation_length": 1.0,
      "bones": {
        "leg_right": {
          "rotation": {
            "0.0": [0, 0, 0],
            "0.25": [45, 0, 0],
            "0.5": [0, 0, 0],
            "0.75": [-45, 0, 0],
            "1.0": [0, 0, 0]
          }
        },
        "leg_left": {
          "rotation": {
            "0.0": [0, 0, 0],
            "0.25": [-45, 0, 0],
            "0.5": [0, 0, 0],
            "0.75": [45, 0, 0],
            "1.0": [0, 0, 0]
          }
        }
      }
    }
  }
}
```

---

## Configuration Files

### README for BlockBench Templates

Create `blockbench/templates/README.md`:

```markdown
# BlockBench Templates

## Character Template (`character_template.bbmodel`)

Use this template when creating new character race models.

### Pre-configured:
- Standard humanoid skeleton
- All bone names matching `HumanoidBones.java`
- Correct pivot points
- 64x64 texture setup

### Usage:
1. Open `character_template.bbmodel`
2. Save As: `{race}_{part}.bbmodel`
3. Adjust bone lengths for race proportions
4. Create geometry variants
5. Export to `.geo.json`

## Armor Template (`armor_template.bbmodel`)

Use this template when creating armor pieces.

### Pre-configured:
- Same skeleton as character template
- Inflation offset guides
- Multi-layer setup

### Usage:
1. Open `armor_template.bbmodel`
2. Save As: `{armor_type}_{piece}.bbmodel`
3. Add armor geometry to appropriate bones
4. Leave non-armored bones empty
5. Export to `.geo.json`
```

---

## Build Configuration

### Update `build.gradle`

Add to dependencies section:

```gradle
repositories {
    maven {
        name = "GeckoLib"
        url = "https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/"
    }
}

dependencies {
    // Existing dependencies...

    // GeckoLib
    modImplementation "software.bernie.geckolib:geckolib-fabric-1.21:4.4.7"
    include "software.bernie.geckolib:geckolib-fabric-1.21:4.4.7"
}
```

---

## Git Configuration

### `.gitignore` Updates

Add to `.gitignore`:

```
# BlockBench autosaves
blockbench/**/*.bbmodel.backup
blockbench/**/*.bbmodel.bak
blockbench/**/*_backup_*.bbmodel

# Texture working files
*.psd
*.xcf
*.kra

# Generated textures (optional, if dynamically generated)
src/main/resources/assets/fiveesrd/textures/entity/character/generated/

# Cached animation data (optional, if baking animations)
src/main/resources/assets/fiveesrd/animations/cache/
```

### Git LFS (Optional, for large files)

Create `.gitattributes`:

```
# BlockBench models
*.bbmodel filter=lfs diff=lfs merge=lfs -text

# GeckoLib exports
*.geo.json filter=lfs diff=lfs merge=lfs -text
*.animation.json filter=lfs diff=lfs merge=lfs -text

# Textures
*.png filter=lfs diff=lfs merge=lfs -text
```

---

## Documentation Structure

### Per-Directory README Files

#### `blockbench/characters/README.md`

```markdown
# Character Models

This directory contains BlockBench source files for character models.

## Organization

Each race has its own subdirectory:
- `dwarf/` - Dwarf character models
- `elf/` - Elf character models
- `human/` - Human character models
- `halfling/` - Halfling character models

## File Structure

Each race should have:
- `{race}_body.bbmodel` - Torso variants
- `{race}_heads.bbmodel` - Head variants
- `{race}_limbs.bbmodel` - Arm/leg variants

## Workflow

1. Open template from `blockbench/templates/`
2. Adjust proportions for race
3. Create variants (different physiques, faces, etc.)
4. Export to `src/main/resources/assets/fiveesrd/geo/entity/character/{race}/`
5. Commit both `.bbmodel` source and `.geo.json` export
```

#### `blockbench/equipment/README.md`

```markdown
# Equipment Models

This directory contains BlockBench source files for equipment/armor.

## Organization

- `armor/` - Armor pieces (helmets, chestplates, etc.)
- `accessories/` - Capes, belts, backpacks

## Important

All equipment models MUST use the same skeleton as character models.
Use the template from `blockbench/templates/armor_template.bbmodel`.

## Workflow

1. Open `armor_template.bbmodel`
2. Add geometry ONLY to bones that need armor
3. Leave other bones empty
4. Apply inflation (0.5-1.0 units larger than body)
5. Export to `src/main/resources/assets/fiveesrd/geo/entity/equipment/`
```

---

## Resource Pack Structure (Optional)

For users who want to customize models/textures:

```
resourcepacks/
└── custom_characters/
    ├── pack.mcmeta
    ├── pack.png
    └── assets/
        └── fiveesrd/
            ├── geo/
            │   └── entity/
            │       └── character/
            │           └── [override .geo.json files]
            ├── animations/
            │   └── entity/
            │       └── character/
            │           └── [override .animation.json files]
            └── textures/
                └── entity/
                    └── character/
                        └── [override .png files]
```

`pack.mcmeta`:
```json
{
  "pack": {
    "pack_format": 15,
    "description": "Custom character models and textures for 5e SRD"
  }
}
```

---

## Development Workflow

### Adding a New Race

1. **Create skeleton profile**:
   - Edit `SkeletonProfile.java`
   - Add new profile constant
   - Define bone lengths and pivots

2. **Create BlockBench models**:
   - Create directory: `blockbench/characters/{race}/`
   - Create `{race}_body.bbmodel`
   - Create `{race}_heads.bbmodel`
   - Create `{race}_limbs.bbmodel`

3. **Export geometry**:
   - Export to `assets/fiveesrd/geo/entity/character/{race}/`

4. **Create textures**:
   - Create at least 1-3 skin textures
   - Save to `assets/fiveesrd/textures/entity/character/base/`

5. **Update Race enum**:
   - Add new race to `Race.java`
   - Link to skeleton profile

6. **Test**:
   - Spawn entity with new race
   - Verify model loads
   - Test all animations
   - Verify retargeting works

### Adding a New Animation

1. **Create in BlockBench**:
   - Open existing animation project or create new
   - Add animation to appropriate category (locomotion, combat, etc.)
   - Follow naming convention: `animation.character.{name}`

2. **Export**:
   - Export to `assets/fiveesrd/animations/entity/character/`
   - Update existing `.animation.json` or create new category file

3. **Update AnimationType**:
   - Add new enum value to `AnimationType.java`

4. **Update controller**:
   - Add condition to `CharacterAnimationController.java`
   - Define when animation should play

5. **Test**:
   - Trigger animation condition in-game
   - Verify smooth playback
   - Test on all races (retargeting)

### Adding New Equipment

1. **Create BlockBench model**:
   - Use armor template
   - Add geometry for armor piece
   - Apply appropriate inflation

2. **Export**:
   - Export to `assets/fiveesrd/geo/entity/equipment/armor/`

3. **Create textures**:
   - Create texture variants
   - Save to `assets/fiveesrd/textures/entity/equipment/armor/`

4. **Register in code**:
   - Update `ArmorLayerManager.java` if needed
   - Add to equipment registry

5. **Test**:
   - Equip armor on character
   - Verify layering (no clipping)
   - Test on all races
   - Test visibility rules

---

## Maintenance

### File Cleanup

Periodically check for:
- Unused `.geo.json` files
- Orphaned textures (no model uses them)
- Old backup files from BlockBench
- Duplicate animation definitions

### Version Control Best Practices

1. **Commit both source and exports**:
   - Always commit `.bbmodel` AND `.geo.json` together
   - Link in commit message

2. **Meaningful commit messages**:
   ```
   feat(models): Add elf head variants 4-6
   - Created 3 new elf head geometries
   - Exported to elf_heads.geo.json
   - Added corresponding skin textures
   ```

3. **Branch naming**:
   - `feature/add-{race}-models`
   - `feature/add-{animation}-animation`
   - `fix/retargeting-{race}-arms`

---

## Troubleshooting

### Common File Issues

**Issue**: Model doesn't load in-game

**Check**:
1. File exists at expected path
2. File name matches GeoModel resource path
3. JSON is valid (use validator)
4. Model identifier matches (`geometry.character.{race}`)

---

**Issue**: Texture not displaying

**Check**:
1. Texture file exists
2. Texture path in GeoModel is correct
3. UV coordinates in model are correct
4. Texture size matches model expectations (64x64 or 128x128)

---

**Issue**: Animation not playing

**Check**:
1. Animation file exists
2. Animation name matches controller reference
3. Bone names in animation match model
4. Animation length > 0
5. Controller is registered in entity

---

**Document Version**: 1.0
**Last Updated**: 2025-11-13
**Status**: Complete Reference
