package ninja.trek.srd.client.render.geckolib;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import ninja.trek.srd.FiveESrdMod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Dynamic texture composer for the GeckoLib animation system.
 * Blends skin and clothing textures at runtime with caching for performance.
 *
 * Phase 7.3: Dynamic Texture Compositing
 *
 * Features:
 * - Blends base skin texture with clothing/armor overlays
 * - Caches composed textures to avoid re-compositing identical combinations
 * - Supports alpha blending for semi-transparent layers
 * - Automatic cache cleanup for unused textures
 * - Generates unique Minecraft Identifiers for each composition
 */
public class DynamicTextureComposer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicTextureComposer.class);

    // Singleton instance
    private static DynamicTextureComposer instance;

    // Cache configuration
    private static final int MAX_CACHE_SIZE = 256;
    private static final int CACHE_EXPIRE_MINUTES = 10;

    // Texture cache: Maps TextureCompositionKey to generated Identifier
    private final Cache<TextureCompositionKey, Identifier> textureCache;

    // Counter for generating unique texture IDs
    private int textureIdCounter = 0;

    private DynamicTextureComposer() {
        this.textureCache = CacheBuilder.newBuilder()
            .maximumSize(MAX_CACHE_SIZE)
            .expireAfterAccess(CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES)
            .removalListener(notification -> {
                // Clean up texture when evicted from cache
                if (notification.getValue() instanceof Identifier id) {
                    cleanupTexture(id);
                }
            })
            .build();

        LOGGER.info("DynamicTextureComposer initialized with cache size {} and expiration {} minutes",
            MAX_CACHE_SIZE, CACHE_EXPIRE_MINUTES);
    }

    /**
     * Get the singleton instance.
     */
    public static DynamicTextureComposer getInstance() {
        if (instance == null) {
            instance = new DynamicTextureComposer();
        }
        return instance;
    }

    /**
     * Compose a character texture from base skin and clothing layers.
     * Returns a cached texture if this combination has been composed before.
     *
     * @param baseTexture Base skin texture identifier
     * @param clothingLayers List of clothing/armor layer textures to overlay (in order)
     * @return Identifier for the composed texture
     */
    public Identifier composeTexture(Identifier baseTexture, List<Identifier> clothingLayers) {
        // Create cache key
        TextureCompositionKey key = new TextureCompositionKey(baseTexture, clothingLayers);

        // Check cache
        Identifier cached = textureCache.getIfPresent(key);
        if (cached != null) {
            LOGGER.debug("Using cached texture for composition: {}", cached);
            return cached;
        }

        // Compose new texture
        LOGGER.debug("Composing new texture from base {} with {} layers",
            baseTexture, clothingLayers.size());

        Identifier composed = composeTextureInternal(baseTexture, clothingLayers);

        // Cache the result
        textureCache.put(key, composed);

        return composed;
    }

    /**
     * Internal method to actually compose the texture layers.
     */
    private Identifier composeTextureInternal(Identifier baseTexture, List<Identifier> clothingLayers) {
        MinecraftClient client = MinecraftClient.getInstance();
        ResourceManager resourceManager = client.getResourceManager();
        TextureManager textureManager = client.getTextureManager();

        try {
            // Load base texture
            NativeImage baseImage = loadTextureImage(resourceManager, baseTexture);
            if (baseImage == null) {
                LOGGER.warn("Failed to load base texture: {}", baseTexture);
                return baseTexture; // Fallback to original
            }

            // Create a copy for compositing
            NativeImage compositeImage = new NativeImage(
                baseImage.getWidth(),
                baseImage.getHeight(),
                true
            );
            compositeImage.copyFrom(baseImage);
            baseImage.close();

            // Blend each clothing layer
            for (Identifier layerTexture : clothingLayers) {
                NativeImage layerImage = loadTextureImage(resourceManager, layerTexture);
                if (layerImage != null) {
                    blendLayer(compositeImage, layerImage);
                    layerImage.close();
                } else {
                    LOGGER.warn("Failed to load layer texture: {}", layerTexture);
                }
            }

            // Generate unique identifier for this composition
            Identifier composedId = generateComposedTextureId();

            // Register the composed texture
            NativeImageBackedTexture texture = new NativeImageBackedTexture(compositeImage);
            textureManager.registerTexture(composedId, texture);

            LOGGER.debug("Successfully composed texture: {}", composedId);
            return composedId;

        } catch (Exception e) {
            LOGGER.error("Error composing texture", e);
            return baseTexture; // Fallback to base texture on error
        }
    }

    /**
     * Load a NativeImage from a texture identifier.
     */
    private NativeImage loadTextureImage(ResourceManager resourceManager, Identifier textureId) {
        try {
            // Convert texture identifier to resource path
            Identifier resourcePath = Identifier.of(
                textureId.getNamespace(),
                textureId.getPath()
            );

            InputStream stream = resourceManager.getResource(resourcePath)
                .orElseThrow()
                .getInputStream();

            return NativeImage.read(stream);

        } catch (IOException e) {
            LOGGER.warn("Failed to load texture image: {}", textureId, e);
            return null;
        }
    }

    /**
     * Blend a layer onto the composite image using alpha blending.
     * Supports semi-transparent layers.
     */
    private void blendLayer(NativeImage base, NativeImage layer) {
        int width = Math.min(base.getWidth(), layer.getWidth());
        int height = Math.min(base.getHeight(), layer.getHeight());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int layerColor = layer.getColor(x, y);

                // Extract ABGR components
                int layerAlpha = (layerColor >> 24) & 0xFF;

                // Skip fully transparent pixels
                if (layerAlpha == 0) {
                    continue;
                }

                int baseColor = base.getColor(x, y);

                // Alpha blending
                if (layerAlpha == 255) {
                    // Fully opaque - just copy
                    base.setColor(x, y, layerColor);
                } else {
                    // Semi-transparent - blend
                    int blended = alphaBlend(baseColor, layerColor);
                    base.setColor(x, y, blended);
                }
            }
        }
    }

    /**
     * Perform alpha blending between two ABGR colors.
     *
     * @param bottom Bottom layer color (ABGR)
     * @param top Top layer color (ABGR)
     * @return Blended color (ABGR)
     */
    private int alphaBlend(int bottom, int top) {
        // Extract components (ABGR format)
        int topA = (top >> 24) & 0xFF;
        int topB = (top >> 16) & 0xFF;
        int topG = (top >> 8) & 0xFF;
        int topR = top & 0xFF;

        int bottomA = (bottom >> 24) & 0xFF;
        int bottomB = (bottom >> 16) & 0xFF;
        int bottomG = (bottom >> 8) & 0xFF;
        int bottomR = bottom & 0xFF;

        // Alpha blending formula
        float alpha = topA / 255.0f;
        float invAlpha = 1.0f - alpha;

        int outR = (int)(topR * alpha + bottomR * invAlpha);
        int outG = (int)(topG * alpha + bottomG * invAlpha);
        int outB = (int)(topB * alpha + bottomB * invAlpha);
        int outA = (int)(topA + bottomA * invAlpha);

        // Clamp to valid range
        outR = Math.min(255, Math.max(0, outR));
        outG = Math.min(255, Math.max(0, outG));
        outB = Math.min(255, Math.max(0, outB));
        outA = Math.min(255, Math.max(0, outA));

        // Pack back to ABGR
        return (outA << 24) | (outB << 16) | (outG << 8) | outR;
    }

    /**
     * Generate a unique identifier for a composed texture.
     */
    private Identifier generateComposedTextureId() {
        return Identifier.of(FiveESrdMod.MOD_ID,
            "dynamic/composed_texture_" + (textureIdCounter++));
    }

    /**
     * Clean up a texture when it's no longer needed.
     */
    private void cleanupTexture(Identifier textureId) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) {
                TextureManager textureManager = client.getTextureManager();
                textureManager.destroyTexture(textureId);
                LOGGER.debug("Cleaned up texture: {}", textureId);
            }
        } catch (Exception e) {
            LOGGER.warn("Error cleaning up texture: {}", textureId, e);
        }
    }

    /**
     * Manually invalidate cache entries (useful for resource reloading).
     */
    public void invalidateCache() {
        LOGGER.info("Invalidating texture composition cache");
        textureCache.invalidateAll();
    }

    /**
     * Get cache statistics for debugging.
     */
    public String getCacheStats() {
        return String.format("Texture Cache: size=%d, hitRate=%.2f%%",
            textureCache.size(),
            textureCache.stats().hitRate() * 100.0);
    }

    /**
     * Cache key for texture compositions.
     * Represents a unique combination of base texture and clothing layers.
     */
    private static class TextureCompositionKey {
        private final Identifier baseTexture;
        private final List<Identifier> layers;
        private final int hashCode;

        public TextureCompositionKey(Identifier baseTexture, List<Identifier> layers) {
            this.baseTexture = baseTexture;
            this.layers = new ArrayList<>(layers); // Defensive copy
            this.hashCode = computeHashCode();
        }

        private int computeHashCode() {
            return Objects.hash(baseTexture, layers);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof TextureCompositionKey other)) return false;
            return Objects.equals(baseTexture, other.baseTexture)
                && Objects.equals(layers, other.layers);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public String toString() {
            return String.format("TextureComposition[base=%s, layers=%d]",
                baseTexture, layers.size());
        }
    }
}
