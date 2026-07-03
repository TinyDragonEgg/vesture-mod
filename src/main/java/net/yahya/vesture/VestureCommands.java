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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.yahya.vesture.item.CosmeticSlot;

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

        dispatcher.register(Commands.literal("vesture")
            .then(slotsNode)
            .then(adminNode));
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
}
