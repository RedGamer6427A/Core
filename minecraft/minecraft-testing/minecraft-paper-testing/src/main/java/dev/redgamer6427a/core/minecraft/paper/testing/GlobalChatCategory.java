package dev.redgamer6427a.core.minecraft.paper.testing;

import dev.redgamer6427a.core.minecraft.paper.configuration.AbstractConfigurationSection;
import dev.redgamer6427a.core.minecraft.paper.configuration.ConfigurationSection;
import dev.redgamer6427a.core.minecraft.paper.configuration.values.StringCV;

public class GlobalChatCategory extends ConfigurationSection {

    public StringCV clientID = new StringCV("clientID", "none", this);

    public GlobalChatCategory(AbstractConfigurationSection parent) {
        super("messaging-bus", parent);
        addValue(clientID);
    }
}

