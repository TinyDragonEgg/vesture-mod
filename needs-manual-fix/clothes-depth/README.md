# Clothes - Render at Same Depth as Armor

Clothes currently render at the same distance from the body as armor (vanilla thickness).
This is a limitation of the vanilla equipment rendering system used by equipment_replace.

To get clothes closer to the body requires either:
- Custom rendering code (a new render type in Java)
- Or modifying the equipment layer geometry (not possible via JSON alone)

This is a known limitation for now.