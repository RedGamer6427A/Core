package dev.redgamer6427a.core.minecraft.paper.configuration.values;

import dev.redgamer6427a.core.minecraft.paper.configuration.AbstractConfigurationSection;
import dev.redgamer6427a.core.minecraft.paper.configuration.ConfigurationValue;
import dev.redgamer6427a.core.minecraft.paper.util.TransformValues;
import org.bukkit.util.Vector;

public class VectorCV extends ConfigurationValue<Vector> {

    public VectorCV(String subPath, Vector defaultValue, AbstractConfigurationSection parent) {
        super(subPath, defaultValue, parent);
    }

    @Override
    protected Vector makeValue(Object o) {
        return TransformValues.makeVector(o);

    }
}
