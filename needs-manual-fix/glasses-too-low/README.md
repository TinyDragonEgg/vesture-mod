# Glasses - Too Low on Face

These items render slightly too low when equipped in the head slot.
To fix: open the _on_head model in Blockbench and shift all elements UP by ~2-3 pixels (increase Y values).

Items to fix:
- circle_framed_glasses -> models/item/circle_framed_glasses_on_head.json (or _default_on_head)
- circle_framed_sunglasses -> models/item/circle_framed_sunglasses_on_head.json (or _default_on_head)
- framed_glasses -> models/item/framed_glasses_on_head.json (or _default_on_head)
- framed_sunglasses -> models/item/framed_sunglasses_on_head.json (or _default_on_head)
- glasses -> models/item/glasses_on_head.json (or _default_on_head)
- goggles -> models/item/goggles_on_head.json (or _default_on_head)
- sunglasses -> models/item/sunglasses_on_head.json (or _default_on_head)
- thick_framed_glasses -> models/item/thick_framed_glasses_on_head.json (or _default_on_head)
- thick_framed_sunglasses -> models/item/thick_framed_sunglasses_on_head.json (or _default_on_head)
- thin_framed_glasses -> models/item/thin_framed_glasses_on_head.json (or _default_on_head)
- thin_framed_sunglasses -> models/item/thin_framed_sunglasses_on_head.json (or _default_on_head)
- cartographers_monocle -> models/item/cartographers_monocle_on_head.json (or _default_on_head)
- weaponsmiths_eye_patch -> models/item/weaponsmiths_eye_patch_on_head.json (or _default_on_head)


Steps in Blockbench:
1. Open src/main/resources/assets/vesture/models/item/{name}_on_head.json
2. Select all elements (Ctrl+A)
3. Move up by 2-3 pixels on Y axis
4. Save