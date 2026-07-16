package dev.redgamer6427a.core.minecraft.velocity.configuration;

import lombok.Setter;
import org.intellij.lang.annotations.Subst;

public abstract class ConfigurationValue<T> implements ConfigurationPart {

    public ConfigurationValue(String subPath, T defaultValue, AbstractConfigurationSection parent) {
        this.defaultValue = defaultValue;
        this.parent = parent;
        this.parent.addValue(this);
        this.subPath = this.parent.getRootSection().equals(this.parent) ? subPath : parent.subPath +"."+ subPath;


    }

    public final String subPath;

    public final AbstractConfigurationSection parent;

    public final T defaultValue;

    @Subst("")
    public T value() {
        return value != null ? value : defaultValue;
    }

    @Setter
    protected T value;

    public void load() {
        Object o = parent.getRootSection().getConfig().get(subPath);

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
        parent.getRootSection().getConfig().set(subPath, value);
    }

    protected abstract T makeValue(Object o);

}
