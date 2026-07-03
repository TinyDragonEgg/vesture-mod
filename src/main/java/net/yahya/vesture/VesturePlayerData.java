package net.yahya.vesture;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.yahya.vesture.item.CosmeticSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VesturePlayerData {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PLAYERS_DIR = FabricLoader.getInstance()
            .getConfigDir().resolve("vesture").resolve("players");

    // -1 means "use global config default"
    public int headSlots = -1;
    public int chestSlots = -1;
    public int legsSlots = -1;
    public int feetSlots = -1;
    public int handSlots = -1;
    public int backSlots = -1;

    private static final Map<UUID, VesturePlayerData> CACHE = new HashMap<>();

    public static VesturePlayerData get(UUID uuid) {
        return CACHE.computeIfAbsent(uuid, VesturePlayerData::load);
    }

    public static void invalidate(UUID uuid) {
        CACHE.remove(uuid);
    }

    private static VesturePlayerData load(UUID uuid) {
        Path path = playerPath(uuid);
        if (Files.exists(path)) {
            try (Reader r = Files.newBufferedReader(path)) {
                VesturePlayerData data = GSON.fromJson(r, VesturePlayerData.class);
                return data != null ? data : new VesturePlayerData();
            } catch (IOException e) {
                VestureMod.LOGGER.error("Failed to read player data for {}", uuid, e);
            }
        }
        return new VesturePlayerData();
    }

    public void save(UUID uuid) {
        try {
            Files.createDirectories(PLAYERS_DIR);
            try (Writer w = Files.newBufferedWriter(playerPath(uuid))) {
                GSON.toJson(this, w);
            }
        } catch (IOException e) {
            VestureMod.LOGGER.error("Failed to save player data for {}", uuid, e);
        }
        CACHE.put(uuid, this);
    }

    public void delete(UUID uuid) {
        try {
            Files.deleteIfExists(playerPath(uuid));
        } catch (IOException e) {
            VestureMod.LOGGER.error("Failed to delete player data for {}", uuid, e);
        }
        CACHE.remove(uuid);
    }

    private static Path playerPath(UUID uuid) {
        return PLAYERS_DIR.resolve(uuid + ".json");
    }

    /** Returns the effective slot count — player override if set, otherwise global config. */
    public int getEffective(CosmeticSlot slot) {
        int personal = getPersonal(slot);
        if (personal >= 0) return personal;
        VestureConfig cfg = VestureConfig.getInstance();
        return cfg.getAmount(slot.group, slot.name);
    }

    /** Returns -1 if this player has no personal override for the given slot. */
    public int getPersonal(CosmeticSlot slot) {
        return switch (slot) {
            case HEAD  -> headSlots;
            case CHEST -> chestSlots;
            case LEGS  -> legsSlots;
            case FEET  -> feetSlots;
            case HAND  -> handSlots;
            case BACK  -> backSlots;
        };
    }

    public void setPersonal(CosmeticSlot slot, int count) {
        switch (slot) {
            case HEAD  -> headSlots  = count;
            case CHEST -> chestSlots = count;
            case LEGS  -> legsSlots  = count;
            case FEET  -> feetSlots  = count;
            case HAND  -> handSlots  = count;
            case BACK  -> backSlots  = count;
        }
    }

    public void resetPersonal(CosmeticSlot slot) {
        setPersonal(slot, -1);
    }

    public boolean hasAnyOverride() {
        return headSlots >= 0 || chestSlots >= 0 || legsSlots >= 0
            || feetSlots >= 0 || handSlots >= 0 || backSlots >= 0;
    }
}
