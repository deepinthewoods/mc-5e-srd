#!/usr/bin/env python3
"""
Generate a basic 64x64 texture for the human character.
This creates a simple skin texture with different colors for different body parts.
"""

from PIL import Image, ImageDraw

# Create a 64x64 image with transparency
img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

# Define colors (R, G, B, A)
skin_color = (210, 180, 140, 255)  # Tan/skin color
shirt_color = (70, 130, 180, 255)  # Steel blue for shirt
pants_color = (50, 50, 50, 255)    # Dark gray for pants
hair_color = (101, 67, 33, 255)    # Brown hair

# Head (rows 0-15)
# Head front/back/sides
draw.rectangle([0, 0, 31, 15], fill=skin_color)

# Torso (rows 16-27)
# Torso upper (shirt)
draw.rectangle([0, 16, 47, 27], fill=shirt_color)

# Arms (rows 28-47)
# Arms (shirt sleeves)
draw.rectangle([0, 28, 47, 39], fill=shirt_color)
# Hands (skin)
draw.rectangle([32, 28, 47, 35], fill=skin_color)

# Legs (rows 48-63)
# Legs (pants)
draw.rectangle([0, 48, 63, 63], fill=pants_color)

# Add some simple shading for depth
# Darker edges
edge_color = (180, 150, 110, 255)
# Head edges
draw.rectangle([8, 0, 15, 7], fill=edge_color)
# Face area (lighter)
face_color = (230, 200, 160, 255)
draw.rectangle([0, 0, 7, 7], fill=face_color)

# Save the texture
img.save('/home/user/mc-5e-srd/src/main/resources/assets/fiveesrd/textures/entity/character/base/human_default.png')
print("Created human_default.png (64x64)")
