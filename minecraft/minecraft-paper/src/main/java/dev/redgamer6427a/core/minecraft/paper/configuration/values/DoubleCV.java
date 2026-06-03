package dev.redgamer6427a.admiral.paper.configuration.values;

import dev.redgamer6427a.admiral.common.util.TransformValues;
import dev.redgamer6427a.admiral.paper.configuration.AbstractConfigurationSection;
import dev.redgamer6427a.admiral.paper.configuration.ConfigurationValue;

public class DoubleCV extends ConfigurationValue<Double> {

    public DoubleCV(String subPath, Double defaultValue, AbstractConfigurationSection parent) {
        super(subPath, defaultValue, parent);
    }

    @Override
    protected Double makeValue(Object o) {
        return TransformValues.makeDouble(o);
    }
}
