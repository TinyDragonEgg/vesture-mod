package net.yahya.vesture;

import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketInventory;
import eu.pb4.trinkets.api.TrinketsApi;
import eu.pb4.trinkets.impl.TrinketInventoryImpl;
import net.minecraft.server.level.ServerPlayer;
import net.yahya.vesture.item.CosmeticSlot;

import java.util.UUID;

/**
 * Public API for controlling per-player Vesture cosmetic slot counts.
 * Other mods can call these methods to integrate with Vesture.
 */
public final class VestureApi {

    private VestureApi() {}

    /**
     * Sets the number of cosmetic slots a player has for the given slot type.
     * Persists across sessions. Pass -1 to revert to the server default.
     */
    public static void setPlayerSlotCount(ServerPlayer player, CosmeticSlot slot, int count) {
        UUID uuid = player.getUUID();
        VesturePlayerData data = VesturePlayerData.get(uuid);
        data.setPersonal(slot, count);
        data.save(uuid);
        applySingleSlot(player, slot, data.getEffective(slot));
    }

    /**
     * Resets one cosmetic slot back to the server-wide global default.
     */
    public static void resetPlayerSlot(ServerPlayer player, CosmeticSlot slot) {
        setPlayerSlotCount(player, slot, -1);
    }

    /**
     * Resets all cosmetic slots for this player back to server-wide defaults.
     * Removes their personal config file entirely.
     */
    public static void resetAllPlayerSlots(ServerPlayer player) {
        UUID uuid = player.getUUID();
        VesturePlayerData data = VesturePlayerData.get(uuid);
        data.delete(uuid);
        applyPlayerSlots(player);
    }

    /**
     * Returns the effective slot count for a player + slot combination,
     * accounting for both personal overrides and the global config.
     */
    public static int getEffectiveSlotCount(ServerPlayer player, CosmeticSlot slot) {
        return VesturePlayerData.get(player.getUUID()).getEffective(slot);
    }

    /**
     * Reads this player's saved preferences and applies them to their live
     * Trinkets inventories. Call on login to restore persisted counts.
     */
    public static void applyPlayerSlots(ServerPlayer player) {
        VesturePlayerData data = VesturePlayerData.get(player.getUUID());
        TrinketAttachment attachment = TrinketsApi.getAttachment(player);
        if (attachment == null) return;

        for (CosmeticSlot slot : CosmeticSlot.values()) {
            int count = data.getEffective(slot);
            setInventorySlotCount(attachment, slot.trinketsId(), count);
        }
    }

    private static void applySingleSlot(ServerPlayer player, CosmeticSlot slot, int count) {
        TrinketAttachment attachment = TrinketsApi.getAttachment(player);
        if (attachment == null) return;
        setInventorySlotCount(attachment, slot.trinketsId(), count);
    }

    private static void setInventorySlotCount(TrinketAttachment attachment, String slotId, int count) {
        TrinketInventory inv = attachment.getInventory(slotId);
        if (inv instanceof TrinketInventoryImpl impl) {
            impl.setSlotCount(count);
        }
    }
}
