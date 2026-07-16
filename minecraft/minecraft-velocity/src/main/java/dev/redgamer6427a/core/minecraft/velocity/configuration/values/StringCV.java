package dev.redgamer6427a.core.minecraft.velocity.configuration.values;

import dev.redgamer6427a.core.minecraft.velocity.configuration.AbstractConfigurationSection;
import dev.redgamer6427a.core.minecraft.velocity.configuration.ConfigurationValue;

public class StringCV extends ConfigurationValue<String> {
    public StringCV(String subPath, String defaultValue, AbstractConfigurationSection parent) {
        super(subPath, defaultValue, parent);
    }

    @Override
    protected String makeValue(Object o) {
        return o.toString();
    }
}
