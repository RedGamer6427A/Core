package dev.redgamer6427a.core.minecraft.velocity.configuration.values;

import dev.redgamer6427a.core.minecraft.velocity.configuration.AbstractConfigurationSection;
import dev.redgamer6427a.core.minecraft.velocity.configuration.ConfigurationValue;

public class EnumCV<T extends Enum<T>> extends ConfigurationValue<T> {

    private final Class<T> type;

    public EnumCV(String subPath, T defaultValue, AbstractConfigurationSection parent) {
        super(subPath, defaultValue, parent);
        this.type = (Class<T>) defaultValue.getClass(); // safe cast for enums
    }

    @Override
    protected T makeValue(Object o) {
        if(o instanceof String) {

            try {
                return Enum.valueOf(type, ((String) o).toUpperCase());
            } catch (IllegalArgumentException ignored) {}

        }
        return null;
    }

    @Override
    public void save() {
        parent.getRootSection().getConfig().set(subPath, value.name().toUpperCase());
    }

    public T[] getAllConstants() {
        return type.getEnumConstants();
    }
}

