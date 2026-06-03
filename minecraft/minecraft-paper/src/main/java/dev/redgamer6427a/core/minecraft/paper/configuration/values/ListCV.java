package dev.redgamer6427a.admiral.paper.configuration.values;

import dev.redgamer6427a.admiral.paper.configuration.AbstractConfigurationSection;
import dev.redgamer6427a.admiral.paper.configuration.ConfigurationValue;

import java.util.ArrayList;
import java.util.List;

public class ListCV extends ConfigurationValue<List<Object>> {

    public ListCV(String subPath, List<Object> defaultValue, AbstractConfigurationSection parent) {
        super(subPath, defaultValue, parent);
    }

    public void addValue(Object value1) {
        value.add(value1);

    }

    public void removeValue(Object value1) {
        value.remove(value1);

    }

    public void clearValues(){
        value = new ArrayList<>();

    }

    public Object getValue(int index){
        return value.get(index);
    }



    @Override
    protected List<Object> makeValue(Object o) {
        if(o instanceof List l){



            return l;
        }
        return null;
    }
}
