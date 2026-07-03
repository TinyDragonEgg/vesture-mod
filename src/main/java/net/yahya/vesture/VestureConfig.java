package net.yahya.vesture;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class VestureConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("vesture.json");

    private static VestureConfig INSTANCE;

    // How many slots of each cosmetic type each player gets.
    // Change and reload the data pack (e.g. /reload) for new values to take effect.
    public int headSlots = 1;
    public int chestSlots = 1;
    public int legsSlots = 1;
    public int feetSlots = 1;
    public int handSlots = 1;
    public int backSlots = 1;

    public static VestureConfig getInstance() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
                INSTANCE = GSON.fromJson(r, VestureConfig.class);
            } catch (IOException e) {
                VestureMod.LOGGER.error("Failed to read vesture config, using defaults", e);
                INSTANCE = new VestureConfig();
            }
        } else {
            INSTANCE = new VestureConfig();
        }
        INSTANCE.save();
    }

    private void save() {
        try (Writer w = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(this, w);
        } catch (IOException e) {
            VestureMod.LOGGER.error("Failed to write vesture config", e);
        }
    }

    /** Returns the configured amount for the given trinket slot, or -1 if not a vesture slot. */
    public int getAmount(String group, String name) {
        return switch (group + "/" + name) {
            case "head/cosmetic" -> headSlots;
            case "chest/cosmetic" -> chestSlots;
            case "legs/cosmetic" -> legsSlots;
            case "feet/cosmetic" -> feetSlots;
            case "hand/cosmetic" -> handSlots;
            case "back/wings" -> backSlots;
            default -> -1;
        };
    }
}
