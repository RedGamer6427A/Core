package dev.redgamer6427a.core.minecraft.velocity.command.argument;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.redgamer6427a.core.minecraft.velocity.VelocityPlugin;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class RegisteredServerArgumentType implements SmartArgumentType<String, RegisteredServer> {

    private RegisteredServerArgumentType() {
    }

    public static RegisteredServerArgumentType server() {
        return new RegisteredServerArgumentType();
    }

    public String parse(StringReader reader) throws CommandSyntaxException {
        String input = reader.readUnquotedString();
        boolean exists = VelocityPlugin.getInstance().getProxyServer().getAllServers().stream()
                .anyMatch(server -> server.getServerInfo().getName().equalsIgnoreCase(input));

        if (!exists) {
            throw new CommandSyntaxException(new SimpleCommandExceptionType(() -> "Unknown Server."), new Message() {
                @Override
                public String getString() {
                    return "Unknown Server.";
                }
            });

        }

        return input;
    }


    @Override
    public RegisteredServer resolve(CommandContext<CommandSource> context, String s, Argument argument) throws CommandSyntaxException {
        RegisteredServer server = VelocityPlugin.getInstance().getProxyServer().getServer(s).orElse(null);

        if (server == null) {
            throw new CommandSyntaxException(new SimpleCommandExceptionType(() -> "Unknown Server."), new Message() {
                @Override
                public String getString() {
                    return "Unknown Server.";
                }
            });
        }
        return server;


    }

    @Override
    public CompletableFuture<Suggestions> suggest(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {

        AtomicBoolean matching = new AtomicBoolean(false);

        VelocityPlugin.getInstance().getProxyServer().getAllServers().forEach(server -> {
            String name = server.getServerInfo().getName();
            if (name.toLowerCase().startsWith(builder.getRemaining().toLowerCase())) {
                builder.suggest(name);
            }

            if (builder.getRemaining().toLowerCase().equals(name.toLowerCase())) {
                matching.set(true);
            }

        });
//
//        if (!matching.get()) {
//            throw new CommandSyntaxException(new SimpleCommandExceptionType(() -> "Unknown Server."), new Message() {
//                @Override
//                public String getString() {
//                    return "Unknown Server.";
//                }
//            });
//        }

        return builder.buildFuture();

    }

    @Override
    public ArgumentType<String> getSimple() {
        return StringArgumentType.word();
    }
}
