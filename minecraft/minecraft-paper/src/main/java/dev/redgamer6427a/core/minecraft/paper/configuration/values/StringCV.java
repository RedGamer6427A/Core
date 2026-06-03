package dev.redgamer6427a.admiral.paper.configuration.values;

import dev.redgamer6427a.admiral.paper.configuration.AbstractConfigurationSection;
import dev.redgamer6427a.admiral.paper.configuration.ConfigurationValue;

public class StringCV extends ConfigurationValue<String> {
    public StringCV(String subPath, String defaultValue, AbstractConfigurationSection parent) {
        super(subPath, defaultValue, parent);
    }

    @Override
    protected String makeValue(Object o) {
        return o.toString();
    }
}
