package dev.redgamer6427a.admiral.paper.configuration.values;

import dev.redgamer6427a.admiral.common.util.TransformValues;
import dev.redgamer6427a.admiral.paper.configuration.AbstractConfigurationSection;
import dev.redgamer6427a.admiral.paper.configuration.ConfigurationValue;

public class IntegerCV extends ConfigurationValue<Integer> {

    public IntegerCV(String subPath, Integer defaultValue, AbstractConfigurationSection parent) {
        super(subPath, defaultValue, parent);
    }

    @Override
    protected Integer makeValue(Object o) {
        return TransformValues.makeInt(o);
    }
}
