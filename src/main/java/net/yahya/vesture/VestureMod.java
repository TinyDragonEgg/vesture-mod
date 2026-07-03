package net.yahya.vesture;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VestureMod implements ModInitializer {
    public static final String MOD_ID = "vesture";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        VestureConfig.load();
        VestureItems.init();
        VestureCommands.register();

        // Apply slot counts after Trinkets has finished syncing slot data to the player.
        // SYNC_DATA_PACK_CONTENTS fires after the Trinkets attachment is ready.
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            VestureApi.applyPlayerSlots(player);
        });

        // Clean player data cache on disconnect.
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            VesturePlayerData.invalidate(handler.player.getUUID());
        });

        LOGGER.info("Vesture mod initialized");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
