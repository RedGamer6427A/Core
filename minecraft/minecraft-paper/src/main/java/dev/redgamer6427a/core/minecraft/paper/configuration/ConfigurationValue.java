package dev.redgamer6427a.core.minecraft.paper.configuration;

public abstract class ConfigurationValue<T> implements ConfigurationPart {

    public ConfigurationValue(String subPath, T defaultValue, AbstractConfigurationSection parent) {


        this.subPath = parent.subPath +"."+ subPath;
        this.defaultValue = defaultValue;
        this.parent = parent;
        this.parent.addValue(this);

    }

    public final String subPath;

    public final AbstractConfigurationSection parent;

    public final T defaultValue;

    public T value() {
        return value != null ? value : defaultValue;
    }

    public void setValue(T value) {
        this.value = value;
    }

    protected T value;

    public void load() {
        Object o = parent.getRootSection().config.get(subPath);

        if (o == null){
            value = defaultValue;
        } else {
            value = makeValue(o);

            if (value == null) {
                value = defaultValue;
            }
        }


    }

    public void save() {
        parent.getRootSection().config.set(subPath, value);
    }

    protected abstract T makeValue(Object o);

}
