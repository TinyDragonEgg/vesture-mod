package net.yahya.vesture.item;

import net.minecraft.world.entity.EquipmentSlot;

public enum CosmeticSlot {
    HEAD("head", "cosmetic"),
    CHEST("chest", "cosmetic"),
    LEGS("legs", "cosmetic"),
    FEET("feet", "cosmetic"),
    HAND("hand", "cosmetic"),
    BACK("back", "wings");

    public final String group;
    public final String name;

    CosmeticSlot(String group, String name) {
        this.group = group;
        this.name = name;
    }

    public String trinketsId() {
        return group + "/" + name;
    }

    public EquipmentSlot vanillaSlot() {
        return switch (this) {
            case HEAD  -> EquipmentSlot.HEAD;
            case CHEST -> EquipmentSlot.CHEST;
            case LEGS  -> EquipmentSlot.LEGS;
            case FEET  -> EquipmentSlot.FEET;
            default    -> null;
        };
    }
}
