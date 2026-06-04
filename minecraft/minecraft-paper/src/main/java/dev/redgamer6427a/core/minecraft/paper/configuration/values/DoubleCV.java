package dev.redgamer6427a.core.minecraft.paper.configuration.values;

import dev.redgamer6427a.core.minecraft.common.util.TransformValues;
import dev.redgamer6427a.core.minecraft.paper.configuration.AbstractConfigurationSection;
import dev.redgamer6427a.core.minecraft.paper.configuration.ConfigurationValue;

public class DoubleCV extends ConfigurationValue<Double> {

    public DoubleCV(String subPath, Double defaultValue, AbstractConfigurationSection parent) {
        super(subPath, defaultValue, parent);
    }

    @Override
    protected Double makeValue(Object o) {
        return TransformValues.makeDouble(o);
    }
}
