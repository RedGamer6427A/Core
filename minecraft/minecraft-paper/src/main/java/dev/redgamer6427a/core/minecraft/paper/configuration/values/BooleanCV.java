package dev.redgamer6427a.admiral.paper.configuration.values;

import dev.redgamer6427a.admiral.common.util.TransformValues;
import dev.redgamer6427a.admiral.paper.configuration.AbstractConfigurationSection;
import dev.redgamer6427a.admiral.paper.configuration.ConfigurationValue;

public class BooleanCV extends ConfigurationValue<Boolean> {


    public BooleanCV(String subPath, Boolean defaultValue, AbstractConfigurationSection parent) {
        super(subPath, defaultValue, parent);
    }


    @Override
    protected Boolean makeValue(Object o) {
        return TransformValues.makeBoolean(o);
    }
}
