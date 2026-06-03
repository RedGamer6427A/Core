package dev.redgamer6427a.admiral.paper.configuration.values;

import dev.redgamer6427a.admiral.common.util.TransformValues;
import dev.redgamer6427a.admiral.paper.configuration.AbstractConfigurationSection;
import dev.redgamer6427a.admiral.paper.configuration.ConfigurationValue;

public class LongCV extends ConfigurationValue<Long> {

    public LongCV(String subPath, Long defaultValue, AbstractConfigurationSection parent) {
        super(subPath, defaultValue, parent);
    }

    @Override
    protected Long makeValue(Object o) {
        return TransformValues.makeLong(o);
    }
}
