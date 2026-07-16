package dev.redgamer6427a.core.minecraft.velocity.configuration.values;

import dev.redgamer6427a.core.minecraft.velocity.configuration.AbstractConfigurationSection;
import dev.redgamer6427a.core.minecraft.velocity.configuration.ConfigurationValue;
import dev.redgamer6427a.core.minecraft.common.util.TransformValues;

public class BooleanCV extends ConfigurationValue<Boolean> {


    public BooleanCV(String subPath, Boolean defaultValue, AbstractConfigurationSection parent) {
        super(subPath, defaultValue, parent);
    }


    @Override
    protected Boolean makeValue(Object o) {
        return TransformValues.makeBoolean(o);
    }
}
