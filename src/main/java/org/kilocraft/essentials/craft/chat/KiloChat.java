package org.kilocraft.essentials.craft.chat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.client.network.packet.PlaySoundIdS2CPacket;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.packet.ChatMessageC2SPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.craft.KiloEssentials;
import org.kilocraft.essentials.craft.config.KiloConifg;
import org.kilocraft.essentials.craft.config.provided.ConfigValueGetter;
import org.kilocraft.essentials.craft.config.provided.localVariables.PlayerConfigVariables;

public class KiloChat {
    private static ConfigValueGetter config = KiloConifg.getProvider().getMain();

    public static void sendMessageTo(ServerPlayerEntity player, ChatMessage chatMessage) {
        sendMessageTo(player, new LiteralText(chatMessage.getFormattedMessage()));
    }

    public static void sendMessageTo(ServerCommandSource source, ChatMessage chatMessage) throws CommandSyntaxException {
        sendMessageTo(source.getPlayer(), new LiteralText(chatMessage.getFormattedMessage()));
    }

    public static void sendMessageTo(ServerPlayerEntity player, Text text) {
        player.sendChatMessage(text, MessageType.CHAT);
    }

    public static void sendMessageTo(ServerCommandSource source, Text text) {
        source.sendFeedback(text, false);
    }

    public static void broadCast(ChatMessage chatMessage) {
        KiloServer.getServer().getPlayerManager().getPlayerList().forEach((playerEntity) -> {
            playerEntity.sendChatMessage(new LiteralText(chatMessage.getFormattedMessage()), MessageType.CHAT);
        });

        KiloServer.getServer().sendMessage(TextFormat.removeAlternateColorCodes('&', chatMessage.getFormattedMessage()));
    }

    public static void sendChatMessage(ServerPlayerEntity player, ChatMessageC2SPacket packet) {
        ChatMessage message = new ChatMessage(
                packet.getChatMessage(),
                Thimble.hasPermissionOrOp(player.getCommandSource(), KiloEssentials.getPermissionFor("chat.format"), 2)
        );

        /**
         * REMINDER: ADD PERMISSION FOR THIS
         */
        if (config.getValue("chat.ping.enable")) {
            String pingSenderFormat = config.get(false, "chat.ping.format");
            String pingFormat = config.get(false, "chat.ping.pinged");

            for (String playerName : KiloServer.getServer().getPlayerManager().getPlayerNames()) {
                String thisPing = pingSenderFormat.replace("%PLAYER_NAME%", playerName);

                if (packet.getChatMessage().contains(thisPing.replace("%PLAYER_NAME%", playerName))) {
                    message.setMessage(
                            message.getFormattedMessage().replaceAll(
                                    thisPing,
                                    pingFormat.replace("%PLAYER_NAME%", playerName) + "&r")
                    );

                    if (Thimble.hasPermissionOrOp(player.getCommandSource(), KiloEssentials.getPermissionFor("chat.ping.other"), 2))
                        if (config.getValue("chat.ping.sound.enable"))
                            pingPlayer(playerName);
                }

            }


        }

        broadCast(
                new ChatMessage(
                        config.getLocal(
                                true,
                                "chat.messageFormat",
                                new PlayerConfigVariables(player)
                        ).replace("%MESSAGE%", message.getFormattedMessage())
                                .replace("%PLAYER_DISPLAYNAME%", player.getDisplayName().asFormattedString()),
                        true
                )
        );
    }

    public static void pingPlayer(String playerToPing) {
        ServerPlayerEntity target = KiloServer.getServer().getPlayer(playerToPing);
        Vec3d vec3d = target.getCommandSource().getPosition();
        String soundId = "minecraft:" + config.getValue("chat.ping.sound.id");
        float volume = Float.parseFloat(config.getValue("chat.ping.sound.volume"));
        float pitch = Float.parseFloat(config.getValue("chat.ping.sound.pitch"));

        target.networkHandler.sendPacket(new PlaySoundIdS2CPacket(new Identifier(soundId), SoundCategory.MASTER, vec3d, volume, pitch));
    }


}