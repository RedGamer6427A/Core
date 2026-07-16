package dev.redgamer6427a.core.minecraft.velocity.configuration.values;
import dev.redgamer6427a.core.minecraft.velocity.configuration.AbstractConfigurationSection;
import dev.redgamer6427a.core.minecraft.velocity.configuration.ConfigurationValue;
import dev.redgamer6427a.core.minecraft.common.util.TransformValues;

public class LongCV extends ConfigurationValue<Long> {

    public LongCV(String subPath, Long defaultValue, AbstractConfigurationSection parent) {
        super(subPath, defaultValue, parent);
    }

    @Override
    protected Long makeValue(Object o) {
        return TransformValues.makeLong(o);
    }
}
