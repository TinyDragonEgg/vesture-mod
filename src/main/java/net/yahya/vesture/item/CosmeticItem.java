package net.yahya.vesture.item;

import eu.pb4.trinkets.api.component.TrinketDataComponents;
import eu.pb4.trinkets.api.component.TrinketEquippable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.Equippable;

public class CosmeticItem extends Item {
    private final CosmeticSlot cosmeticSlot;
    private final Identifier equipmentModel;

    public CosmeticItem(Properties properties, CosmeticSlot slot, Identifier equipmentModel) {
        super(applyComponents(properties, slot, equipmentModel));
        this.cosmeticSlot = slot;
        this.equipmentModel = equipmentModel;
    }

    private static Properties applyComponents(Properties props, CosmeticSlot slot, Identifier equipmentModel) {
        props = props.component(
            TrinketDataComponents.EQUIPMENT,
            TrinketEquippable.DEFAULT
                .withSlots(slot.trinketsId())
                .withAssetId(equipmentModel)
        );
        EquipmentSlot vanillaSlot = slot.vanillaSlot();
        if (vanillaSlot != null) {
            props = props.component(DataComponents.EQUIPPABLE, Equippable.builder(vanillaSlot).build());
        }
        return props;
    }

    public CosmeticSlot getCosmeticSlot() { return cosmeticSlot; }
    public Identifier getEquipmentModel() { return equipmentModel; }
}
