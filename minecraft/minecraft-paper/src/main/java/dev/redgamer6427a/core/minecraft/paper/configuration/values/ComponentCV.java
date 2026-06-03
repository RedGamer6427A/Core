package dev.redgamer6427a.admiral.paper.configuration.values;

import dev.redgamer6427a.admiral.common.text.MiniMessageUtils;
import dev.redgamer6427a.admiral.paper.configuration.AbstractConfigurationSection;
import dev.redgamer6427a.admiral.paper.configuration.ConfigurationValue;
import net.kyori.adventure.text.Component;

import static dev.redgamer6427a.admiral.common.text.MiniMessageUtils.mm;


public class ComponentCV extends ConfigurationValue<Component> {

    public ComponentCV(String subPath, Component defaultValue, AbstractConfigurationSection parent) {
        super(subPath, defaultValue, parent);
    }

    @Override
    public void save(){
        parent.getRootSection().config.set(subPath, MiniMessageUtils.serialize(value));
    }

    @Override
    protected Component makeValue(Object o) {
        if(o instanceof Component){
            return (Component) o;
        } else if(o instanceof String s){
            return mm(s);
        } else {
            return null;
        }
    }
}
