package net.yahya.vesture.item;

import eu.pb4.trinkets.api.component.TrinketDataComponents;
import eu.pb4.trinkets.api.component.TrinketEquippable;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public class CosmeticItem extends Item {
    private final CosmeticSlot cosmeticSlot;
    private final Identifier equipmentModel;

    public CosmeticItem(Properties properties, CosmeticSlot slot, Identifier equipmentModel) {
        super(properties.component(
            TrinketDataComponents.EQUIPMENT,
            TrinketEquippable.DEFAULT
                .withSlots(slot.trinketsId())
                .withAssetId(equipmentModel)
        ));
        this.cosmeticSlot = slot;
        this.equipmentModel = equipmentModel;
    }

    public CosmeticSlot getCosmeticSlot() { return cosmeticSlot; }
    public Identifier getEquipmentModel() { return equipmentModel; }
}
