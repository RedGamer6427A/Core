package dev.redgamer6427a.core.minecraft.paper.configuration.values;

import dev.redgamer6427a.core.minecraft.common.text.AdventureMM;
import dev.redgamer6427a.core.minecraft.paper.configuration.AbstractConfigurationSection;
import dev.redgamer6427a.core.minecraft.paper.configuration.ConfigurationValue;
import net.kyori.adventure.text.Component;

import static dev.redgamer6427a.core.minecraft.common.text.AdventureMM.cc;


public class ComponentCV extends ConfigurationValue<Component> {

    public ComponentCV(String subPath, Component defaultValue, AbstractConfigurationSection parent) {
        super(subPath, defaultValue, parent);
    }

    @Override
    public void save(){
        parent.getRootSection().config.set(subPath, AdventureMM.serialize(value));
    }

    @Override
    protected Component makeValue(Object o) {
        if(o instanceof Component){
            return (Component) o;
        } else if(o instanceof String s){
            return AdventureMM.cc(s);
        } else {
            return null;
        }
    }
}
