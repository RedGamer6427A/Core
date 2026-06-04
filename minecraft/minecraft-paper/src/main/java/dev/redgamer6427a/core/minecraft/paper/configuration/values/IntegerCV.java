package dev.redgamer6427a.core.minecraft.paper.configuration.values;

import dev.redgamer6427a.core.minecraft.common.util.TransformValues;
import dev.redgamer6427a.core.minecraft.paper.configuration.AbstractConfigurationSection;
import dev.redgamer6427a.core.minecraft.paper.configuration.ConfigurationValue;

public class IntegerCV extends ConfigurationValue<Integer> {

    public IntegerCV(String subPath, Integer defaultValue, AbstractConfigurationSection parent) {
        super(subPath, defaultValue, parent);
    }

    @Override
    protected Integer makeValue(Object o) {
        return TransformValues.makeInt(o);
    }
}
