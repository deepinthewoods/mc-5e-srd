# GeckoLib Animation System Implementation Plan

## Overview

This document provides a phased implementation plan for migrating to the GeckoLib-based animation and bone retargeting system with modular layers.

## Prerequisites

### Dependencies to Add

Update `build.gradle`:

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

    // Optional: BlockBench asset validator (development only)
    // modImplementation "com.github.wyn-price:blockbench-validator:1.0.0"
}
```

### Development Tools

1. **BlockBench**: Download from https://www.blockbench.net/
   - Install GeckoLib plugin
   - Install model export templates

2. **Texture Editor**: GIMP, Photoshop, or Aseprite for texture creation

3. **Git LFS** (optional): For storing large model/texture files
   ```bash
   git lfs install
   git lfs track "*.geo.json"
   git lfs track "*.animation.json"
   git lfs track "*.png"
   ```

---

## Implementation Phases

### Phase 0: Preparation & Cleanup (1-2 days)

**Goal**: Remove old system and set up GeckoLib foundation

#### Tasks:

1. **Backup Current System**
   ```bash
   git checkout -b backup/old-animation-system
   git push origin backup/old-animation-system
   ```

2. **Remove Old Animation Code**
   - Delete `/src/client/java/ninja/trek/srd/client/model/CharacterModelLoader.java`
   - Delete `/src/client/java/ninja/trek/srd/client/render/CharacterEntityRenderer.java`
   - Delete `/src/client/java/ninja/trek/srd/client/render/CharacterRenderState.java`
   - Keep entity code intact (CharacterEntity.java)

3. **Add GeckoLib Dependency**
   - Update `build.gradle` as shown above
   - Run `./gradlew build` to verify dependency resolution

4. **Create Package Structure**
   ```
   src/main/java/ninja/trek/srd/
   ├── client/
   │   ├── model/
   │   │   └── geckolib/
   │   │       ├── CharacterGeoModel.java
   │   │       ├── EquipmentGeoModel.java
   │   │       └── (more models...)
   │   ├── render/
   │   │   └── geckolib/
   │   │       ├── CharacterGeoRenderer.java
   │   │       ├── layer/
   │   │       │   ├── BaseBodyLayerRenderer.java
   │   │       │   ├── EquipmentLayerRenderer.java
   │   │       │   └── HeldItemLayerRenderer.java
   │   │       └── animation/
   │   │           ├── CharacterAnimationController.java
   │   │           └── BoneRetargetingController.java
   │   └── registry/
   │       ├── ModGeoModels.java
   │       └── ModAnimations.java
   └── character/
       ├── skeleton/
       │   ├── SkeletonProfile.java
       │   ├── HumanoidBones.java
       │   └── BoneRetargeting.java
       └── layer/
           ├── LayerConfiguration.java
           ├── BodyPartConfiguration.java
           ├── EquipmentLayerConfiguration.java
           └── ClothingConfiguration.java
   ```

5. **Create Resource Structure**
   ```
   src/main/resources/assets/fiveesrd/
   ├── geo/
   │   └── entity/
   │       └── character/
   │           ├── base/
   │           ├── dwarf/
   │           ├── elf/
   │           ├── human/
   │           └── halfling/
   ├── animations/
   │   └── entity/
   │       └── character/
   └── textures/
       └── entity/
           └── character/
               ├── base/
               ├── clothing/
               └── armor/
   ```

**Deliverables**:
- ✅ Old system removed
- ✅ GeckoLib dependency added and working
- ✅ Directory structure created
- ✅ Clean build with no errors

---

### Phase 1: Core Skeleton & Data Structures (2-3 days)

**Goal**: Implement skeleton profile system and basic data structures

#### Task 1.1: Skeleton Profile System

Create `SkeletonProfile.java`:
```java
public class SkeletonProfile {
    public static final SkeletonProfile HUMAN = /* ... */;
    public static final SkeletonProfile DWARF = /* ... */;
    public static final SkeletonProfile ELF = /* ... */;
    public static final SkeletonProfile HALFLING = /* ... */;

    // Implementation from ANIMATION_RETARGETING.md
}
```

#### Task 1.2: Bone Definitions

Create `HumanoidBones.java`:
```java
public class HumanoidBones {
    public static final String ROOT = "root";
    public static final String BODY = "body";
    public static final String HEAD = "head";
    // ... all bone name constants
}
```

#### Task 1.3: Layer Configuration Classes

Create data structures:
- `BodyPartConfiguration.java`
- `EquipmentLayerConfiguration.java`
- `ClothingConfiguration.java`

#### Task 1.4: Update CharacterEntity

Add fields to `CharacterEntity.java`:
```java
public class CharacterEntity extends PathAwareEntity implements GeoEntity {

    // Layer configurations
    private BodyPartConfiguration bodyConfig = new BodyPartConfiguration();
    private EquipmentLayerConfiguration equipmentConfig = new EquipmentLayerConfiguration();
    private ClothingConfiguration clothingConfig = new ClothingConfiguration();

    // Size scale (0.5 - 3.0)
    private float sizeScale = 1.0f;

    // GeckoLib animation cache
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // To be implemented in Phase 3
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // Getters/setters for configurations
}
```

**Deliverables**:
- ✅ Skeleton profiles for all 4 races
- ✅ Bone name constants defined
- ✅ Layer configuration classes
- ✅ CharacterEntity implements GeoEntity
- ✅ Unit tests for skeleton profiles

---

### Phase 2: Basic Model & Rendering (3-5 days)

**Goal**: Get a single race rendering with GeckoLib (no animations yet)

#### Task 2.1: Create Test Model in BlockBench

1. Create `human_body.bbmodel`:
   - Follow specifications from `BLOCKBENCH_SPECIFICATIONS.md`
   - Create skeleton with all required bones
   - Model simple body variant (basic cube shapes)
   - Export to `.geo.json`

2. Create simple texture:
   - 64x64 PNG with basic colors
   - Save to `assets/fiveesrd/textures/entity/character/base/human_skin_default.png`

#### Task 2.2: Implement GeoModel

Create `CharacterGeoModel.java`:
```java
public class CharacterGeoModel extends GeoModel<CharacterEntity> {

    @Override
    public Identifier getModelResource(CharacterEntity entity) {
        String race = entity.getRace().getName().toLowerCase();
        return new Identifier("fiveesrd", "geo/entity/character/" + race + "/" + race + "_body.geo.json");
    }

    @Override
    public Identifier getTextureResource(CharacterEntity entity) {
        String skinTexture = entity.getBodyConfiguration().skinTexture;
        return new Identifier("fiveesrd", "textures/entity/character/base/" + skinTexture + ".png");
    }

    @Override
    public Identifier getAnimationResource(CharacterEntity entity) {
        // Return null for now (Phase 3)
        return null;
    }
}
```

#### Task 2.3: Implement GeoRenderer

Create `CharacterGeoRenderer.java`:
```java
public class CharacterGeoRenderer extends GeoEntityRenderer<CharacterEntity> {

    public CharacterGeoRenderer(EntityRendererFactory.Context context) {
        super(context, new CharacterGeoModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public void render(CharacterEntity entity, float entityYaw, float partialTick,
                       MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {

        // Apply size scaling
        float scale = entity.getSizeScale();
        poseStack.scale(scale, scale, scale);

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
```

#### Task 2.4: Register Renderer

In `FiveESrdModClient.java`:
```java
@Override
public void onInitializeClient() {
    EntityRendererRegistry.register(
        ModEntities.CHARACTER_ENTITY,
        CharacterGeoRenderer::new
    );
}
```

#### Task 2.5: Test In-Game

1. Build mod: `./gradlew build`
2. Run client: `./gradlew runClient`
3. Spawn character entity: `/summon fiveesrd:character`
4. Verify:
   - ✅ Model loads without errors
   - ✅ Texture displays correctly
   - ✅ Entity renders at correct size
   - ✅ No console errors

**Deliverables**:
- ✅ Human race model rendering
- ✅ Basic texture applied
- ✅ Renderer registered and working
- ✅ In-game test successful

---

### Phase 3: Animation System (4-6 days)

**Goal**: Implement animations and basic retargeting

#### Task 3.1: Create Animations in BlockBench

1. Create `locomotion.animation.json`:
   - Idle (2s loop)
   - Walk (1s loop)
   - Run (0.6s loop)

2. Follow keyframe guidelines from `BLOCKBENCH_SPECIFICATIONS.md`
3. Export animations

#### Task 3.2: Implement Animation Controller

Create `CharacterAnimationController.java`:
```java
public class CharacterAnimationController extends AnimationController<CharacterEntity> {

    public CharacterAnimationController(CharacterEntity entity) {
        super(entity, "movement", 0, this::predicate);
    }

    private PlayState predicate(AnimationState<CharacterEntity> state) {
        CharacterEntity entity = state.getAnimatable();

        if (state.isMoving()) {
            if (entity.isSprinting()) {
                state.getController().setAnimation(RawAnimation.begin().thenLoop("run"));
            } else {
                state.getController().setAnimation(RawAnimation.begin().thenLoop("walk"));
            }
        } else {
            state.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
        }

        return PlayState.CONTINUE;
    }
}
```

#### Task 3.3: Register Controller in Entity

In `CharacterEntity.java`:
```java
@Override
public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(new CharacterAnimationController(this));
}
```

#### Task 3.4: Update GeoModel for Animations

In `CharacterGeoModel.java`:
```java
@Override
public Identifier getAnimationResource(CharacterEntity entity) {
    return new Identifier("fiveesrd", "animations/entity/character/locomotion.animation.json");
}
```

#### Task 3.5: Test Animations

1. Spawn character
2. Verify idle animation plays
3. Push character (should trigger walk)
4. Verify smooth transitions

**Deliverables**:
- ✅ Idle, walk, run animations created
- ✅ Animation controller working
- ✅ Smooth animation transitions
- ✅ Animations loop correctly

---

### Phase 4: Bone Retargeting (5-7 days)

**Goal**: Implement retargeting so all races can use same animations

#### Task 4.1: Implement Basic Retargeting

Create `BoneRetargetingController.java`:
- Implement algorithms from `ANIMATION_RETARGETING.md`
- Start with basic position retargeting
- Keep rotations unchanged

#### Task 4.2: Create Remaining Race Models

1. Create `dwarf_body.bbmodel` with dwarf proportions
2. Create `elf_body.bbmodel` with elf proportions
3. Create `halfling_body.bbmodel` with halfling proportions
4. Use SAME bone names and hierarchy
5. Export all to `.geo.json`

#### Task 4.3: Integrate Retargeting with Animation Controller

Modify `CharacterAnimationController.java`:
```java
private final BoneRetargetingController retargeting;

public CharacterAnimationController(CharacterEntity entity) {
    super(entity, "movement", 0, this::predicate);
    this.retargeting = new BoneRetargetingController(SkeletonProfile.HUMAN);
}

private PlayState predicate(AnimationState<CharacterEntity> state) {
    // Load base animation
    Animation baseAnim = selectAnimation(state);

    // Retarget to entity's race
    RetargetedAnimation retargeted = retargeting.retargetAnimation(
        baseAnim,
        entity.getRace().getName(),
        entity.getSizeScale()
    );

    // Play retargeted animation
    state.getController().setAnimation(retargeted);

    return PlayState.CONTINUE;
}
```

#### Task 4.4: Test Retargeting

1. Spawn all 4 races side-by-side
2. Make them all walk
3. Verify:
   - ✅ Each race has proportional stride
   - ✅ No limb disconnection
   - ✅ Natural motion for each
   - ✅ Animations stay synchronized

#### Task 4.5: Implement Locomotion Speed Scaling

Add walk speed adjustment based on leg length:
```java
public float getLocomotionSpeed(SkeletonProfile profile, float sizeScale) {
    float legRatio = getLegLengthRatio(profile);
    return legRatio * sizeScale;
}
```

**Deliverables**:
- ✅ All 4 race models created
- ✅ Retargeting system working
- ✅ Animations adapt to each race
- ✅ Walk speed scales correctly
- ✅ Visual tests pass

---

### Phase 5: Body Part Variants (3-4 days)

**Goal**: Support multiple variants per body part

#### Task 5.1: Create Variant Models

For each race, create variants:
- 3-4 body variants
- 3-6 head variants
- 3-4 limb variants

In BlockBench:
- Use variant groups (folders)
- Export all variants in single file

#### Task 5.2: Implement Variant Selection

Update `CharacterGeoModel.java`:
```java
@Override
public void setCustomAnimations(CharacterEntity entity, long instanceId, AnimationState<?> state) {
    super.setCustomAnimations(entity, instanceId, state);

    GeoModel model = getGeoModel();
    BodyPartConfiguration config = entity.getBodyConfiguration();

    // Show/hide variant groups
    showVariantGroup(model, "head", config.headVariant);
    showVariantGroup(model, "body", config.bodyVariant);
    showVariantGroup(model, "arms", config.armsVariant);
    showVariantGroup(model, "legs", config.legsVariant);
}

private void showVariantGroup(GeoModel model, String part, int variant) {
    // Hide all variants for this part
    for (int i = 0; i < 10; i++) {
        String groupName = part + "_variant_" + i;
        GeoBone bone = model.getBone(groupName).orElse(null);
        if (bone != null) {
            bone.setHidden(i != variant);
        }
    }
}
```

#### Task 5.3: Add Variant Selection GUI (Optional)

Create character customization screen for testing:
- Sliders for each body part
- Live preview
- Save configuration to entity

**Deliverables**:
- ✅ Multiple variants per race
- ✅ Runtime variant switching
- ✅ Variants render correctly
- ✅ Configuration persists

---

### Phase 6: Equipment Layer System (5-7 days)

**Goal**: Implement armor and equipment as separate layers

#### Task 6.1: Create Armor Models

In BlockBench, create armor pieces:
1. `armor_leather.bbmodel` - light armor set
2. `armor_plate.bbmodel` - heavy armor set

Use SAME skeleton, add geometry only where needed.

#### Task 6.2: Implement Equipment Layer Renderer

Create `EquipmentLayerRenderer.java`:
- Extends GeoLayerRenderer
- Renders equipment over base body
- Applies inflation offset

```java
public class EquipmentLayerRenderer extends GeoLayerRenderer<CharacterEntity> {

    @Override
    public void render(MatrixStack poseStack, VertexConsumerProvider bufferSource,
                       int packedLight, CharacterEntity entity, float limbSwing,
                       float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {

        EquipmentLayerConfiguration config = entity.getEquipmentConfiguration();

        // Render each equipped piece
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (config.hasEquipment(slot)) {
                renderEquipmentPiece(slot, config.getLayer(slot), poseStack, bufferSource, packedLight);
            }
        }
    }
}
```

#### Task 6.3: Add Equipment Layer to Renderer

In `CharacterGeoRenderer.java`:
```java
public CharacterGeoRenderer(EntityRendererFactory.Context context) {
    super(context, new CharacterGeoModel());

    // Add equipment layer
    addRenderLayer(new EquipmentLayerRenderer(this));

    // Add held item layer (Phase 7)
}
```

#### Task 6.4: Implement Armor Manager

Create `ArmorLayerManager.java`:
- Reads ItemStack armor from entity
- Updates EquipmentLayerConfiguration
- Syncs to clients

#### Task 6.5: Test Equipment

1. Give character leather helmet
2. Verify helmet renders over head
3. Equip full plate armor
4. Verify all pieces render correctly
5. Test layer inflation (no clipping)

**Deliverables**:
- ✅ Armor models created
- ✅ Equipment layer renderer working
- ✅ Armor displays over body
- ✅ No Z-fighting or clipping
- ✅ Layer system functional

---

### Phase 7: Advanced Features (4-6 days)

**Goal**: Add held items, visibility rules, and polish

#### Task 7.1: Held Item Rendering

Create `HeldItemLayerRenderer.java`:
- Attaches items to hand bones
- Uses Minecraft's ItemRenderer
- Applies item-specific transforms

#### Task 7.2: Layer Visibility System

Implement `LayerVisibilityManager.java`:
- Helmet hides hair
- Robe hides clothing
- Heavy armor hides base layers

#### Task 7.3: Dynamic Texture Compositing

Implement `DynamicTextureComposer.java`:
- Blend skin + clothing textures
- Cache composed textures
- Support texture overlays

#### Task 7.4: Network Synchronization

Create packet handlers:
- Sync layer configurations to clients
- Update when equipment changes
- Efficient incremental updates

**Deliverables**:
- ✅ Held items render correctly
- ✅ Visibility rules working
- ✅ Dynamic textures functional
- ✅ Network sync operational

---

### Phase 8: Additional Animations (3-5 days)

**Goal**: Add combat, magic, and emote animations

#### Task 8.1: Create Combat Animations

In BlockBench:
- Attack animations (1h, 2h, unarmed)
- Bow animations (draw, hold, release)
- Block/defend animations

#### Task 8.2: Create Magic Animations

- Casting animations (instant, channeling, ground target)
- Spell effects (hand gestures)

#### Task 8.3: Add Animation State Machine

Expand `CharacterAnimationController`:
- Handle animation priorities
- Smooth transitions
- Interrupt conditions

```java
private PlayState predicate(AnimationState<CharacterEntity> state) {
    CharacterEntity entity = state.getAnimatable();

    // Priority 1: Action animations (attacks, casts)
    if (entity.isAttacking()) {
        return handleAttackAnimation(state);
    }

    if (entity.isCasting()) {
        return handleCastAnimation(state);
    }

    // Priority 2: Locomotion
    if (state.isMoving()) {
        return handleLocomotionAnimation(state);
    }

    // Priority 3: Idle
    return handleIdleAnimation(state);
}
```

**Deliverables**:
- ✅ Combat animations created
- ✅ Magic animations created
- ✅ Animation state machine working
- ✅ Smooth transitions
- ✅ Proper animation priorities

---

### Phase 9: Optimization & Polish (3-4 days)

**Goal**: Improve performance and add final touches

#### Task 9.1: Animation Baking

Pre-compute retargeted animations:
- Bake at startup for common size scales
- Cache in memory
- Reduce runtime computation

#### Task 9.2: Layer Culling

Skip rendering hidden layers:
- Don't render shirt under heavy armor
- Don't render hair under helmet

#### Task 9.3: LOD System (Optional)

Implement level-of-detail:
- Simpler models for distant entities
- Reduced animation quality at distance
- Disable minor layers (accessories)

#### Task 9.4: Texture Atlas Optimization

Combine similar textures:
- Create texture atlases
- Reduce texture switches
- Improve batch rendering

**Deliverables**:
- ✅ Performance optimizations applied
- ✅ Smooth 60 FPS with 50+ entities
- ✅ Reduced memory usage
- ✅ Final polish complete

---

### Phase 10: Testing & Documentation (2-3 days)

**Goal**: Thorough testing and documentation

#### Task 10.1: Comprehensive Testing

1. **Unit Tests**:
   - Skeleton profile calculations
   - Retargeting algorithms
   - Layer configuration

2. **Integration Tests**:
   - All races render correctly
   - Animations retarget properly
   - Equipment layers work

3. **Visual Tests**:
   - Screenshot all race/variant combinations
   - Test all animations
   - Verify armor sets

#### Task 10.2: In-Game Testing

- Spawn large groups of entities
- Test in combat scenarios
- Verify multiplayer sync
- Test edge cases (tiny/huge sizes)

#### Task 10.3: Update Documentation

- Document any deviations from design
- Create user guide for adding new races
- Create guide for adding new animations
- Document known issues/limitations

**Deliverables**:
- ✅ All tests passing
- ✅ No critical bugs
- ✅ Documentation complete
- ✅ Ready for production use

---

## Timeline Estimate

### Optimistic (Developer with GeckoLib experience)
- **Total**: 30-40 days (6-8 weeks)

### Realistic (First time with GeckoLib)
- **Total**: 50-70 days (10-14 weeks)

### Conservative (Including iterations and polish)
- **Total**: 70-90 days (14-18 weeks)

## Parallel Work Opportunities

Some phases can be parallelized if multiple developers:

**Developer 1** (Programmer):
- Phases 1, 3, 4, 6, 7, 9

**Developer 2** (Artist/Modeler):
- Phases 2, 5, 8 (creating models and animations in BlockBench)

This can reduce total time by 30-40%.

---

## Risk Mitigation

### Risk 1: GeckoLib Compatibility Issues

**Mitigation**:
- Test GeckoLib with minimal example first
- Check version compatibility with Fabric 1.21
- Have fallback plan to use older GeckoLib version

### Risk 2: Performance Problems

**Mitigation**:
- Profile early and often
- Set performance targets (60 FPS with 50 entities)
- Implement optimizations incrementally

### Risk 3: Animation Quality Issues

**Mitigation**:
- Create test animations early
- Get feedback from artists/community
- Iterate on animations before creating all variants

### Risk 4: Scope Creep

**Mitigation**:
- Stick to phase plan
- Mark features as "post-MVP" if not essential
- Re-evaluate scope at end of each phase

---

## Success Criteria

### Minimum Viable Product (MVP)

- ✅ All 4 races render with unique proportions
- ✅ Basic animations (idle, walk, run) work for all races
- ✅ Bone retargeting functional
- ✅ At least 1 armor set renders correctly
- ✅ Held weapons display properly
- ✅ No critical bugs or crashes

### Full Feature Set

- ✅ All planned animations implemented
- ✅ Multiple variants per body part
- ✅ Complete equipment layer system
- ✅ Dynamic texture compositing
- ✅ Layer visibility rules
- ✅ Performance optimizations
- ✅ Comprehensive documentation

---

## Post-Implementation

### Future Enhancements

1. **Additional Races**:
   - Tiefling, Dragonborn, Gnome, Half-Orc
   - Follow same skeleton structure
   - Reuse all animations via retargeting

2. **IK (Inverse Kinematics)**:
   - Foot placement on slopes
   - Hand placement on objects
   - Look-at targets

3. **Facial Animations**:
   - Blinking
   - Talking (lip sync)
   - Expressions (happy, angry, surprised)

4. **Advanced Effects**:
   - Cloth physics (capes, robes)
   - Hair physics
   - Particle attachments

5. **Animation Blending**:
   - Blend between walk and run based on speed
   - Upper/lower body split (walk while attacking)
   - Additive animations (injuries, fatigue)

---

**Document Version**: 1.0
**Last Updated**: 2025-11-13
**Status**: Ready to Begin Implementation
