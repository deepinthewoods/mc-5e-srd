# GeckoLib Animation System Documentation

## Overview

This is the complete documentation suite for building a GeckoLib-based animation system with bone retargeting and modular character layers.

## What This System Provides

✅ **Bone Retargeting**: Share animations across different races (dwarves, elves, humans, halflings) with different proportions
✅ **Modular Layers**: Mix and match body parts, equipment, and armor at runtime
✅ **Size Scaling**: Support creatures from 0.5x to 3.0x normal size with adaptive animations
✅ **Keyframe Animations**: Professional animation workflow using BlockBench
✅ **Equipment System**: Dynamic armor and clothing that layers over base body
✅ **Texture Compositing**: Runtime texture blending for unlimited customization

## Documentation Files

### 1. [GECKOLIB_ARCHITECTURE.md](GECKOLIB_ARCHITECTURE.md)
**Read this first** - High-level system architecture

**Contents**:
- System overview and design goals
- Component architecture diagrams
- Bone retargeting concepts
- Layer system overview
- File organization structure
- GeckoLib integration points

**Who should read**: Everyone - this is your roadmap

---

### 2. [BLOCKBENCH_SPECIFICATIONS.md](BLOCKBENCH_SPECIFICATIONS.md)
**For artists and modelers** - Detailed BlockBench modeling guide

**Contents**:
- BlockBench project setup
- Standard humanoid skeleton (exact bone names and hierarchy)
- Race-specific proportions (dwarf, elf, human, halfling)
- Geometry organization and variants
- Equipment/armor modeling guidelines
- Animation creation specifications
- Export settings and quality checklist

**Who should read**: Anyone creating models or animations in BlockBench

---

### 3. [ANIMATION_RETARGETING.md](ANIMATION_RETARGETING.md)
**For programmers** - Technical implementation of retargeting

**Contents**:
- Mathematical foundations
- Retargeting algorithms (with code examples)
- Skeleton profile data structures
- Locomotion speed adaptation
- GeckoLib integration code
- Performance optimization strategies
- Testing and validation

**Who should read**: Programmers implementing the retargeting system

---

### 4. [LAYER_SYSTEM.md](LAYER_SYSTEM.md)
**For programmers** - Runtime character assembly

**Contents**:
- Layer type definitions
- Base body rendering
- Equipment layer system
- Armor rendering pipeline
- Held item attachment
- Layer visibility rules
- Dynamic texture compositing
- Network synchronization

**Who should read**: Programmers implementing rendering and layer management

---

### 5. [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md)
**Project management** - Phased implementation roadmap

**Contents**:
- 10 implementation phases (Phase 0 through Phase 9)
- Detailed task breakdowns per phase
- Time estimates (optimistic, realistic, conservative)
- Deliverables and success criteria
- Risk mitigation strategies
- Testing requirements

**Who should read**: Project leads and anyone planning the implementation

**Timeline Estimate**: 10-18 weeks depending on experience level

---

### 6. [FILE_STRUCTURE.md](FILE_STRUCTURE.md)
**Reference guide** - Complete file organization

**Contents**:
- Complete directory tree
- Naming conventions
- Java class templates
- Resource file templates
- Build configuration
- Git configuration (LFS, .gitignore)
- Development workflows
- Troubleshooting guide

**Who should read**: Everyone - keep this open while working

---

## Quick Start Guide

### If you're starting implementation:

1. **Read GECKOLIB_ARCHITECTURE.md** (30 minutes)
   - Understand the overall system
   - Familiarize yourself with concepts

2. **Read IMPLEMENTATION_PLAN.md** (20 minutes)
   - Understand the phases
   - Identify which phase you're working on

3. **Reference FILE_STRUCTURE.md** (as needed)
   - Set up directory structure
   - Use templates for new files

4. **For modeling work**:
   - Read BLOCKBENCH_SPECIFICATIONS.md thoroughly
   - Follow the specs exactly for bone names/hierarchy

5. **For programming work**:
   - Reference ANIMATION_RETARGETING.md for retargeting code
   - Reference LAYER_SYSTEM.md for rendering code

### If you're creating models:

1. **Read BLOCKBENCH_SPECIFICATIONS.md** completely
2. **Reference FILE_STRUCTURE.md** for naming conventions
3. Set up BlockBench templates
4. Create test model for one race
5. Export and test in-game
6. Iterate and create remaining models

### If you're programming:

1. **Read GECKOLIB_ARCHITECTURE.md** for context
2. **Read IMPLEMENTATION_PLAN.md** to find your phase
3. **Reference ANIMATION_RETARGETING.md** and **LAYER_SYSTEM.md** for implementation details
4. **Use FILE_STRUCTURE.md** templates for new classes
5. Implement phase by phase
6. Test thoroughly before moving to next phase

## Implementation Phases Summary

| Phase | Name | Estimated Time | Key Deliverables |
|-------|------|----------------|------------------|
| **0** | Setup & Foundation | 1 day | GeckoLib added, structure created |
| **1** | Core Skeleton & Data | 2-3 days | Skeleton profiles, data structures |
| **2** | Basic Model & Rendering | 3-5 days | One race rendering with GeckoLib |
| **3** | Animation System | 4-6 days | Animations playing in-game |
| **4** | Bone Retargeting | 5-7 days | All races share animations |
| **5** | Body Part Variants | 3-4 days | Multiple variants per race |
| **6** | Equipment Layer System | 5-7 days | Armor rendering over body |
| **7** | Advanced Features | 4-6 days | Held items, visibility rules |
| **8** | Additional Animations | 3-5 days | Combat, magic, emotes |
| **9** | Optimization & Polish | 3-4 days | Performance tuning |
| **10** | Testing & Documentation | 2-3 days | Final QA and docs |

**Total**: 10-18 weeks (50-90 days)

## Key Concepts

### Bone Retargeting
Animations created for one skeleton (e.g., human) automatically adapt to other skeletons (dwarf, elf) with different bone lengths. Rotations are preserved, translations are scaled proportionally.

**Example**: A human walk animation with 0.8m stride automatically becomes a 0.58m stride for a dwarf.

### Modular Layers
Characters are assembled from multiple independent layers:
- Base body parts (can mix different heads, torsos, limbs)
- Equipment layers (armor, clothing)
- Held items (weapons, shields)
- Effects (particles, glows)

**Example**: Dwarf body + elf head + plate armor + fire sword = unique character

### Size Scaling
Global scaling (0.5x to 3.0x) with adaptive walk speed:
- Tiny creatures walk slower (tiny steps)
- Giant creatures walk faster (huge strides)
- Combat animations play at normal speed (balance)

**Example**: 3.0x giant walks 3x faster but attacks at normal speed

## Technical Requirements

### Dependencies
- **Minecraft**: 1.21+
- **Fabric API**: Latest for 1.21
- **GeckoLib**: 4.4.7+ (Fabric version)

### Development Tools
- **BlockBench**: Latest version with GeckoLib plugin
- **Java IDE**: IntelliJ IDEA or Eclipse
- **Texture Editor**: GIMP, Photoshop, or Aseprite
- **Git** (optional): For version control
- **Git LFS** (optional): For large model/texture files

## File Size Estimates

### Per Race
- BlockBench models: ~500 KB - 2 MB each
- Exported geometry: ~200 KB - 1 MB each
- Textures: ~20 KB - 100 KB each (64x64 to 128x128 PNG)

### Total Project (4 races, full equipment)
- Models: ~10-20 MB
- Textures: ~5-10 MB
- Animations: ~1-2 MB
- **Total**: ~20-30 MB

## Performance Targets

### Target Metrics
- **60 FPS** with 50+ animated characters on screen
- **< 100ms** animation retargeting per character (with caching)
- **< 10MB** RAM per character (including all layers)

### Optimization Strategies (Phase 9)
- Pre-bake retargeted animations at startup
- Cache composed textures
- Cull hidden layers (don't render shirt under plate armor)
- LOD system for distant entities (optional)

## Common Pitfalls to Avoid

❌ **Don't**: Create separate animations for each race
✅ **Do**: Create one animation set, use retargeting

❌ **Don't**: Use different bone names for different races
✅ **Do**: Use exact same skeleton structure and bone names

❌ **Don't**: Animate positions heavily (except for locomotion)
✅ **Do**: Animate rotations, let retargeting handle positions

❌ **Don't**: Make armor geometry same size as body
✅ **Do**: Inflate armor by 0.5-1.0 units to prevent Z-fighting

❌ **Don't**: Render all layers always
✅ **Do**: Hide layers that are completely covered (optimization)

## Support and Resources

### GeckoLib Resources
- **Documentation**: https://docs.geckolib.com/
- **Discord**: https://discord.gg/MNQcKxB
- **Examples**: https://github.com/bernie-g/geckolib-examples

### BlockBench Resources
- **Website**: https://www.blockbench.net/
- **Discord**: https://discord.gg/fZQbxbg
- **Tutorials**: https://www.blockbench.net/wiki/

### Minecraft Modding
- **Fabric Wiki**: https://fabricmc.net/wiki/
- **Fabric Discord**: https://discord.gg/v6v4pMv

## FAQ

### Q: Can we add more races later?
**A**: Yes! Just create new skeleton profile and models. All existing animations will work via retargeting.

### Q: Can players customize characters?
**A**: Yes, by selecting different body part variants and equipment. You can create a GUI for this.

### Q: What if animations look wrong on a specific race?
**A**: You can create race-specific override animations (see `race_specific/` in file structure).

### Q: Can we use different skeleton structures (quadrupeds, etc.)?
**A**: Yes, but you'd need separate animation sets and retargeting profiles for each skeleton type.

### Q: How hard is it to add new animations?
**A**: Easy! Create in BlockBench, export, add to animation controller. All races automatically support it.

### Q: Can we change models with resource packs?
**A**: Yes! Resource packs can override `.geo.json`, `.animation.json`, and textures.

## Changelog

### Version 1.0 (2025-11-13)
- Initial documentation suite created
- All 6 core documents completed
- Ready for implementation to begin

## Contributors

This documentation was created to support building a modern animation system for the 5e SRD mod.

## License

This documentation is provided as part of the 5e SRD mod project. See main project LICENSE for details.

---

**Ready to start?** Begin with [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) Phase 0!
