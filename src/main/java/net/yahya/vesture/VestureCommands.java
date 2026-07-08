package net.yahya.vesture;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionSet;
import eu.pb4.trinkets.api.TrinketsApi;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.yahya.vesture.item.CosmeticItem;
import net.yahya.vesture.item.CosmeticSlot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class VestureCommands {

    private VestureCommands() {}

    private static final Predicate<CommandSourceStack> IS_OP = src -> {
        PermissionSet perms = src.permissions();
        if (perms instanceof LevelBasedPermissionSet lbps) {
            return lbps.level().isEqualOrHigherThan(PermissionLevel.GAMEMASTERS);
        }
        return false;
    };

    public static void register() {
        CommandRegistrationCallback.EVENT.register(VestureCommands::build);
    }

    private static void build(CommandDispatcher<CommandSourceStack> dispatcher,
                              CommandBuildContext ctx,
                              Commands.CommandSelection selection) {

        // /vesture slots  → show all counts
        // /vesture slots get [<slot>]
        // /vesture slots set <slot> <count>
        // /vesture slots reset [<slot>]
        var slotsNode = Commands.literal("slots")
            .executes(VestureCommands::cmdGetAll);

        // get subcommand
        var getNode = Commands.literal("get").executes(VestureCommands::cmdGetAll);
        for (CosmeticSlot slot : CosmeticSlot.values()) {
            final CosmeticSlot s = slot;
            getNode = getNode.then(Commands.literal(s.name().toLowerCase())
                .executes(ctx2 -> cmdGetOne(ctx2, s)));
        }
        slotsNode = slotsNode.then(getNode);

        // set subcommand
        var setNode = Commands.literal("set");
        for (CosmeticSlot slot : CosmeticSlot.values()) {
            final CosmeticSlot s = slot;
            setNode = setNode.then(Commands.literal(s.name().toLowerCase())
                .then(Commands.argument("count", IntegerArgumentType.integer(0, 9))
                    .executes(ctx2 -> cmdSet(ctx2, s))));
        }
        slotsNode = slotsNode.then(setNode);

        // reset subcommand
        var resetNode = Commands.literal("reset").executes(VestureCommands::cmdResetAll);
        for (CosmeticSlot slot : CosmeticSlot.values()) {
            final CosmeticSlot s = slot;
            resetNode = resetNode.then(Commands.literal(s.name().toLowerCase())
                .executes(ctx2 -> cmdReset(ctx2, s)));
        }
        slotsNode = slotsNode.then(resetNode);

        // /vesture admin slots <player> set <slot> <count>
        var adminSetNode = Commands.literal("set");
        for (CosmeticSlot slot : CosmeticSlot.values()) {
            final CosmeticSlot s = slot;
            adminSetNode = adminSetNode.then(Commands.literal(s.name().toLowerCase())
                .then(Commands.argument("count", IntegerArgumentType.integer(0, 9))
                    .executes(ctx2 -> cmdAdminSet(ctx2, s))));
        }

        var adminNode = Commands.literal("admin").requires(IS_OP)
            .then(Commands.literal("slots")
                .then(Commands.argument("target", EntityArgument.player())
                    .then(adminSetNode)));

        // /vesture test  → spawn chest(s) + armor stands for all equipment sets
        var testNode = Commands.literal("test")
            .requires(IS_OP)
            .executes(VestureCommands::cmdTest);

        dispatcher.register(Commands.literal("vesture")
            .then(slotsNode)
            .then(adminNode)
            .then(testNode));
    }

    // /vesture slots  or  /vesture slots get
    private static int cmdGetAll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        VesturePlayerData data = VesturePlayerData.get(player.getUUID());
        StringBuilder sb = new StringBuilder("Vesture cosmetic slot counts:\n");
        for (CosmeticSlot slot : CosmeticSlot.values()) {
            int personal = data.getPersonal(slot);
            int effective = data.getEffective(slot);
            String label = personal >= 0 ? effective + " (personal)" : effective + " (server default)";
            sb.append("  ").append(slot.name().toLowerCase()).append(": ").append(label).append("\n");
        }
        ctx.getSource().sendSuccess(() -> Component.literal(sb.toString().trim()), false);
        return 1;
    }

    // /vesture slots get <slot>
    private static int cmdGetOne(CommandContext<CommandSourceStack> ctx, CosmeticSlot slot) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        int count = VestureApi.getEffectiveSlotCount(player, slot);
        ctx.getSource().sendSuccess(() -> Component.literal(
                slot.name().toLowerCase() + " slots: " + count), false);
        return count;
    }

    // /vesture slots set <slot> <count>
    private static int cmdSet(CommandContext<CommandSourceStack> ctx, CosmeticSlot slot) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        int count = IntegerArgumentType.getInteger(ctx, "count");
        VestureApi.setPlayerSlotCount(player, slot, count);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Set your " + slot.name().toLowerCase() + " cosmetic slots to " + count), false);
        return count;
    }

    // /vesture slots reset
    private static int cmdResetAll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        VestureApi.resetAllPlayerSlots(player);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Reset all cosmetic slots to server defaults"), false);
        return 1;
    }

    // /vesture slots reset <slot>
    private static int cmdReset(CommandContext<CommandSourceStack> ctx, CosmeticSlot slot) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        VestureApi.resetPlayerSlot(player, slot);
        int def = VestureApi.getEffectiveSlotCount(player, slot);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Reset " + slot.name().toLowerCase() + " to server default (" + def + ")"), false);
        return def;
    }

    // /vesture admin slots <player> set <slot> <count>
    private static int cmdAdminSet(CommandContext<CommandSourceStack> ctx, CosmeticSlot slot) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        int count = IntegerArgumentType.getInteger(ctx, "count");
        VestureApi.setPlayerSlotCount(target, slot, count);
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Set " + target.getName().getString() + "'s " + slot.name().toLowerCase() + " slots to " + count), true);
        return count;
    }

    // /vesture test
    private static int cmdTest(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel level = (ServerLevel) player.level();
        BlockPos playerPos = player.blockPosition();

        // Group CosmeticItems by equipment model ID — same model = same set
        Map<Identifier, List<CosmeticItem>> sets = new LinkedHashMap<>();
        List<CosmeticItem> standalone = new ArrayList<>();

        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof CosmeticItem ci) {
                Identifier model = ci.getEquipmentModel();
                if (model != null) {
                    sets.computeIfAbsent(model, k -> new ArrayList<>()).add(ci);
                } else {
                    standalone.add(ci);
                }
            }
        }

        // Full item list: sets first (so chest is organized by set), then accessories
        List<CosmeticItem> allItems = new ArrayList<>();
        sets.values().forEach(allItems::addAll);
        allItems.addAll(standalone);

        // Place chests in a row going north (z-1, z-2, ...), 27 items each
        int chestsNeeded = (allItems.size() + 26) / 27;
        for (int c = 0; c < chestsNeeded; c++) {
            BlockPos chestPos = playerPos.offset(c, 0, -1);
            level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
            if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
                int start = c * 27;
                int end = Math.min(start + 27, allItems.size());
                for (int i = start; i < end; i++) {
                    chest.setItem(i - start, new ItemStack(allItems.get(i)));
                }
            }
        }

        // Spawn one labeled armor stand per set, in a 10-wide grid going east then south
        int standIdx = 0;
        for (Map.Entry<Identifier, List<CosmeticItem>> entry : sets.entrySet()) {
            double x = playerPos.getX() + 2 + (standIdx % 10) * 2.0;
            double y = playerPos.getY();
            double z = playerPos.getZ() + (standIdx / 10) * 2.0;

            ArmorStand stand = new ArmorStand(level, x, y, z);
            stand.setCustomName(Component.literal(entry.getKey().getPath()));
            stand.setCustomNameVisible(true);
            stand.setNoGravity(true);

            var trinkets = TrinketsApi.getAttachment(stand);
            for (CosmeticItem ci : entry.getValue()) {
                var inventory = trinkets.getInventory(ci.getCosmeticSlot().trinketsId());
                if (inventory != null) {
                    inventory.setItem(0, new ItemStack(ci));
                }
            }

            level.addFreshEntity(stand);
            standIdx++;
        }

        final int totalStands = standIdx;
        final int totalItems = allItems.size();
        final int totalChests = chestsNeeded;
        ctx.getSource().sendSuccess(() -> Component.literal(
            "Spawned " + totalStands + " armor stands (" + sets.size() + " sets) and " +
            totalChests + " chest(s) with all " + totalItems + " vesture items."), false);
        return 1;
    }
}
