package dev.redgamer6427a.core.minecraft.velocity.configuration.values;

import dev.redgamer6427a.core.minecraft.velocity.configuration.AbstractConfigurationSection;
import dev.redgamer6427a.core.minecraft.velocity.configuration.ConfigurationValue;
import dev.redgamer6427a.core.minecraft.common.util.TransformValues;

public class IntegerCV extends ConfigurationValue<Integer> {

    public IntegerCV(String subPath, Integer defaultValue, AbstractConfigurationSection parent) {
        super(subPath, defaultValue, parent);
    }

    @Override
    protected Integer makeValue(Object o) {
        return TransformValues.makeInt(o);
    }
}
