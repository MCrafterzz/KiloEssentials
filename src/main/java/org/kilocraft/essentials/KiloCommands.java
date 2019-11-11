package org.kilocraft.essentials;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import io.github.indicode.fabric.permissions.PermChangeBehavior;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.SharedConstants;
import net.minecraft.command.CommandException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.commands.help.UsageCommand;
import org.kilocraft.essentials.commands.inventory.AnvilCommand;
import org.kilocraft.essentials.commands.inventory.EnderchestCommand;
import org.kilocraft.essentials.commands.item.ItemCommand;
import org.kilocraft.essentials.commands.locate.WorldLocateCommand;
import org.kilocraft.essentials.commands.messaging.MessageCommand;
import org.kilocraft.essentials.commands.misc.ColorsCommand;
import org.kilocraft.essentials.commands.misc.DiscordCommand;
import org.kilocraft.essentials.commands.misc.PreviewCommand;
import org.kilocraft.essentials.commands.misc.TeleportCommands;
import org.kilocraft.essentials.commands.play.*;
import org.kilocraft.essentials.commands.server.*;
import org.kilocraft.essentials.commands.teleport.BackCommand;
import org.kilocraft.essentials.commands.teleport.RandomTeleportCommand;
import org.kilocraft.essentials.commands.teleport.TpCommand;
import org.kilocraft.essentials.commands.teleport.TpaCommand;
import org.kilocraft.essentials.commands.world.TimeCommand;
import org.kilocraft.essentials.config.KiloConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.kilocraft.essentials.api.KiloEssentials.getInstance;
import static org.kilocraft.essentials.api.KiloEssentials.getLogger;

public class KiloCommands {
    private static final SimpleCommandExceptionType SMART_USAGE_FAILED_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("Unknown command or insufficient permissions"));
    private static List<String> initializedPerms = new ArrayList<>();
    private CommandDispatcher<ServerCommandSource> dispatcher;

    public KiloCommands() {
        this.dispatcher = KiloEssentialsImpl.commandDispatcher;
        register(true);
    }

    public static String getCommandPermission(String command) {
        if (!initializedPerms.contains(command)) {
            initializedPerms.add(command);
        }
        return "kiloessentials.command." + command;
    }

    private void register(boolean devEnv) {
        if (devEnv) {
            KiloEssentialsImpl.getLogger().info("Alert [!]: Server is running in debug mode!");
            SharedConstants.isDevelopment = devEnv;
            //TestCommand.register(this.dispatcher);
        }


        VersionCommand.register(this.dispatcher);
        ReloadCommand.register(this.dispatcher);
        ColorsCommand.register(this.dispatcher);
        GamemodeCommand.register(this.dispatcher);
        EnderchestCommand.register(this.dispatcher);
        TpaCommand.register(this.dispatcher);
        KillCommand.register(this.dispatcher);
        RealNameCommand.register(this.dispatcher);
        RandomTeleportCommand.register(this.dispatcher);
        RealNameCommand.register(this.dispatcher);
        MessageCommand.register(this.dispatcher);
        SudoCommand.register(this.dispatcher);
        BroadcastCommand.register(this.dispatcher);
        DiscordCommand.register(this.dispatcher);
        UsageCommand.register(this.dispatcher);
        PlayerParticlesCommand.register(this.dispatcher);
        AnvilCommand.register(this.dispatcher);
        ItemCommand.register(this.dispatcher);
        ColorsCommand.register(this.dispatcher);
        EnderchestCommand.register(this.dispatcher);
        WorldLocateCommand.register(this.dispatcher);
        UserBanCommand.register(this.dispatcher);
        BackCommand.register(this.dispatcher);
        HealCommand.register(this.dispatcher);
        FeedCommand.register(this.dispatcher);
        TimeCommand.register(this.dispatcher);
        FlyCommand.register(this.dispatcher);
        SpeedCommand.register(this.dispatcher);
        InfoCommand.register(this.dispatcher);
        StopCommand.register(this.dispatcher);
        OperatorCommand.register(this.dispatcher);
        InvulnerablemodeCommand.register(this.dispatcher);
        PreviewCommand.register(this.dispatcher);
        TeleportCommands.register(this.dispatcher);
        NicknameCommand.register(this.dispatcher);
        TpCommand.register(this.dispatcher);

        Thimble.permissionWriters.add((map, server) -> {
            initializedPerms.forEach(perm -> map.registerPermission("kiloessentials.command." + perm, PermChangeBehavior.UPDATE_COMMAND_TREE));
        });
    }

    public static int executeUsageFor(String langKey, ServerCommandSource source) {
        String fromLang = ModConstants.getLang().getProperty(langKey);
        if (fromLang != null)
            KiloChat.sendMessageToSource(source, new ChatMessage("&6Command usage:\n" + fromLang, true));
        else
            KiloChat.sendLangMessageTo(source, "general.usage.help");
        return 1;
    }

    public static int executeSmartUsage(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String command = ctx.getInput().replace("/", "");
        ParseResults<ServerCommandSource> parseResults = getDispatcher().parse(command, ctx.getSource());
        if (parseResults.getContext().getNodes().isEmpty()) {
            throw SMART_USAGE_FAILED_EXCEPTION.create();
        } else {
            Map<CommandNode<ServerCommandSource>, String> commandNodeStringMap = getDispatcher().getSmartUsage(((ParsedCommandNode)Iterables.getLast(parseResults.getContext().getNodes())).getNode(), ctx.getSource());
            Iterator<String> iterator = commandNodeStringMap.values().iterator();

            KiloChat.sendLangMessageTo(ctx.getSource(), "command.usage.firstRow", command);
            KiloChat.sendLangMessageTo(ctx.getSource(), "command.usage.commandRow", command, "");

            while (iterator.hasNext()) {
                if (iterator.next().equals("/" + command)) continue;
                KiloChat.sendLangMessageTo(ctx.getSource(), "command.usage.commandRow", command, iterator.next());
            }

            return 1;
        }
    }

    public static int executeSmartUsageFor(String command, ServerCommandSource source) throws CommandSyntaxException {
        ParseResults<ServerCommandSource> parseResults = getDispatcher().parse(command, source);
        if (parseResults.getContext().getNodes().isEmpty()) {
            throw SMART_USAGE_FAILED_EXCEPTION.create();
        } else {
            Map<CommandNode<ServerCommandSource>, String> commandNodeStringMap = getDispatcher().getSmartUsage(((ParsedCommandNode)Iterables.getLast(parseResults.getContext().getNodes())).getNode(), source);
            Iterator<String> iterator = commandNodeStringMap.values().iterator();

            KiloChat.sendLangMessageTo(source, "command.usage.firstRow", command);
            if (parseResults.getContext().getNodes().get(0).getNode().getCommand() != null)
                KiloChat.sendLangMessageTo(source, "command.usage.commandRow", parseResults.getReader().getString(), "");

            int usages = 0;
            while (iterator.hasNext()) {
                usages++;
                String usage = iterator.next();
                KiloChat.sendLangMessageTo(source, "command.usage.commandRow", parseResults.getReader().getString(), usage);
            }

            if (usages == 0) KiloChat.sendLangMessageTo(source, "command.usage.commandRow", command, "");

            return commandNodeStringMap.size();
        }
    }

    public static LiteralText getPermissionError(String hoverText) {
        LiteralText literalText = LangText.get(true, "command.exception.permission");
        literalText.styled((style) -> {
           style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(hoverText).formatted(Formatting.YELLOW)));
        });
        return literalText;
    }

    public static CommandDispatcher<ServerCommandSource> getDispatcher() {
        return KiloEssentialsImpl.commandDispatcher;
    }

    public int execute(ServerCommandSource executor, String commandToExecute) {
        ChatMessage cmdExMessage = new ChatMessage(
                KiloConfig.getProvider().getMessages().get(true, "commands.context.execution_exception"), true);

        StringReader stringReader = new StringReader(commandToExecute);
        if (stringReader.canRead() && stringReader.peek() == '/') {
            stringReader.skip();
        }

        getInstance().getServer().getVanillaServer().getProfiler().push(commandToExecute);

        byte var = 0;
        try {
            try {
                return this.dispatcher.execute(stringReader, executor);
            } catch (CommandException e) {
                executor.sendError(e.getTextMessage());
                var = 0;
                return var;
            } catch (CommandSyntaxException e) {
                if (e.getRawMessage().getString().equals("Unknown command"))
                    KiloChat.sendMessageToSource(executor, cmdExMessage);
                else {
                    executor.sendError(Texts.toText(e.getRawMessage()));
                    if (e.getInput() != null && e.getCursor() >= 0) {
                        int cursor = Math.min(e.getInput().length(), e.getCursor());
                        Text text = (new LiteralText("")).formatted(Formatting.GRAY).styled((style) -> {
                            style.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, commandToExecute));
                            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(commandToExecute).formatted(Formatting.YELLOW)));
                        });

                        if (cursor > 10) text.append("...");

                        text.append(e.getInput().substring(Math.max(0, cursor - 10), cursor));
                        if (cursor < e.getInput().length()) {
                            Text text_2 = (new LiteralText(e.getInput().substring(cursor))).formatted(Formatting.RED, Formatting.UNDERLINE);
                            text.append(text_2);
                        }

                        text.append(new LiteralText("<--[HERE]").formatted(Formatting.RED, Formatting.ITALIC));
                        executor.sendError(text);
                    }
                }

            }
        } catch (Exception e) {
            Text text = new LiteralText(e.getMessage() == null ? e.getClass().getName() : e.getMessage());
            if (getLogger().isDebugEnabled()) {
                getLogger().error("Command exception: {}", commandToExecute, e);
                StackTraceElement[] stackTraceElements = e.getStackTrace();

                for(int i = 0; i < Math.min(stackTraceElements.length, 3); ++i) {
                    text.append("\n\n").append(stackTraceElements[i].getMethodName()).append("\n ").append(stackTraceElements[i].getFileName()).append(":").append(String.valueOf(stackTraceElements[i].getLineNumber()));
                }
            }

            executor.sendError((new TranslatableText("command.failed")).styled((style) -> {
                style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, text));
            }));

            if (SharedConstants.isDevelopment) {
                executor.sendError(new LiteralText(Util.getInnermostMessage(e)));
                getLogger().error("'" + commandToExecute + "' threw an exception", e);
            }

            return (byte) 0;

        } finally {
            getInstance().getServer().getVanillaServer().getProfiler().pop();
        }

        return var;
    }


    public static int SUCCESS() {
        return 1;
    }

    public static int FAILED() {
        return -1;
    }

}