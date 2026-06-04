package dev.redgamer6427a.core.minecraft.paper.configuration.values;

import dev.redgamer6427a.core.minecraft.common.util.TransformValues;
import dev.redgamer6427a.core.minecraft.paper.configuration.AbstractConfigurationSection;
import dev.redgamer6427a.core.minecraft.paper.configuration.ConfigurationValue;

public class BooleanCV extends ConfigurationValue<Boolean> {


    public BooleanCV(String subPath, Boolean defaultValue, AbstractConfigurationSection parent) {
        super(subPath, defaultValue, parent);
    }


    @Override
    protected Boolean makeValue(Object o) {
        return TransformValues.makeBoolean(o);
    }
}
