package net.yahya.vesture.item;

import eu.pb4.trinkets.api.TrinketDropRule;
import eu.pb4.trinkets.api.component.TrinketDataComponents;
import eu.pb4.trinkets.api.component.TrinketEquippable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.EquipmentAssets;

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
                .withDropRule(TrinketDropRule.DROP)
        );
        EquipmentSlot vanillaSlot = slot.vanillaSlot();
        if (vanillaSlot != null) {
            Equippable.Builder builder = Equippable.builder(vanillaSlot);
            if (equipmentModel != null) {
                builder.setAsset(ResourceKey.create(EquipmentAssets.ROOT_ID, equipmentModel));
            }
            props = props.component(DataComponents.EQUIPPABLE, builder.build());
        }
        return props;
    }

    public CosmeticSlot getCosmeticSlot() { return cosmeticSlot; }
    public Identifier getEquipmentModel() { return equipmentModel; }
}
