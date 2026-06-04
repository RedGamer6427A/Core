package dev.redgamer6427a.core.minecraft.paper.testing;

import dev.redgamer6427a.core.minecraft.paper.PaperPlugin;
import dev.redgamer6427a.core.minecraft.paper.testing.command.TestCommand;
import dev.redgamer6427a.core.minecraft.paper.util.PaperParameters;
import org.bukkit.permissions.Permission;
import org.jspecify.annotations.NonNull;

public class PaperTestPlugin extends PaperPlugin {
    @Override
    public @NonNull PaperParameters getParameters() {
        return new PaperParameters(new Permission("papertest.verbose"), null, false, false);
    }

    @Override
    public void defineCommands() {
        new TestCommand().register();
    }
}
