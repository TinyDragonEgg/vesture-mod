package net.yahya.vesture.item;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public class CosmeticItem extends Item {
    private final CosmeticSlot cosmeticSlot;
    private final Identifier equipmentModel;

    public CosmeticItem(Properties properties, CosmeticSlot slot, Identifier equipmentModel) {
        super(properties);
        this.cosmeticSlot = slot;
        this.equipmentModel = equipmentModel;
    }

    public CosmeticSlot getCosmeticSlot() {
        return cosmeticSlot;
    }

    public Identifier getEquipmentModel() {
        return equipmentModel;
    }
}
