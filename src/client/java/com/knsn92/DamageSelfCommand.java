package com.knsn92;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class DamageSelfCommand implements ClientCommandRegistrationCallback {

    private final static SimpleCommandExceptionType INVULNERABLE = new SimpleCommandExceptionType(Text.literal("You are invulnerable."));

    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register(
            literal("damageself")
            .then(argument("damage", IntegerArgumentType.integer(1, 7))
            .executes(ctx -> {
                int amount = IntegerArgumentType.getInteger(ctx, "damage");
                MinecraftClient mc = ctx.getSource().getClient();
                if (mc.player.getAbilities().invulnerable) {
                    throw INVULNERABLE.create();
                }
                damagePlayer(mc, amount);
                return SINGLE_SUCCESS;
            })
        ));
    }

    private void damagePlayer(MinecraftClient mc, int amount) {
        Vec3d pos = mc.player.getPos();

        for(int i = 0; i < 80; i++) {
            sendPositionPacket(mc, pos.x, pos.y + amount + 2.1, pos.z, false);
            sendPositionPacket(mc, pos.x, pos.y + 0.05, pos.z, false);
        }

        sendPositionPacket(mc, pos.x, pos.y, pos.z, true);
    }

    private void sendPositionPacket(MinecraftClient mc, double x, double y, double z, boolean onGround) {
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, onGround, mc.player.horizontalCollision));
    }
}
