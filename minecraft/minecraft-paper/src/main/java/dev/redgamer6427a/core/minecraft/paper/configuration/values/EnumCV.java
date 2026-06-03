package dev.redgamer6427a.admiral.paper.configuration.values;

import dev.redgamer6427a.admiral.paper.configuration.AbstractConfigurationSection;
import dev.redgamer6427a.admiral.paper.configuration.ConfigurationValue;
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
        parent.getRootSection().config.set(subPath, value.name().toUpperCase());
    }

    public T[] getAllConstants() {
        return type.getEnumConstants();
    }
}

