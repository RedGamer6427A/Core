package dev.redgamer6427a.core.minecraft.velocity.configuration;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractConfigurationSection implements ConfigurationPart {

    public String subPath;

    public List<ConfigurationPart> children = new ArrayList<>();

    public final AbstractConfigurationSection parent;

    public AbstractConfigurationSection(String subPath, AbstractConfigurationSection parent) {
        if(this instanceof Configuration && parent != null) {
            throw new UnsupportedOperationException("crossbowConfiguration is always the root. The root cannot have a parent.");
        }
        if(parent != null) {
            parent.addValue(this);
        }
        this.parent = parent;

        if(parent == null){
            this.subPath = "";
        } else {
            this.subPath = this.parent.getRootSection().equals(this.parent) ? subPath : parent.subPath +"."+ subPath;
        }



    }

    @Override
    public String toString() {
        return "AbstractConfigurationSection["+subPath+"]";
    }

    public void load(){
        preLoadHook();
        for (ConfigurationPart configurationPart : children) {
            configurationPart.load();
        }
        postLoadHook();
    }

    public void save(){
        preSaveHook();
        for (ConfigurationPart configurationPart : children) {
            configurationPart.save();

        }
        postSaveHook();
    }

    public void addValue(ConfigurationPart value){
        children.add(value);
    }

    public void preLoadHook(){}

    public void postLoadHook(){}

    /**
     * This Hook will be also called after loading
     */

    public void preSaveHook(){}
    /**
     * This Hook will be also called after loading
     */
    public void postSaveHook(){}

    public Configuration getRootSection(){
        if(parent == null){
            if(this instanceof Configuration){
                return (Configuration) this;
            } else {
                throw new IllegalStateException("The root of this tree is not a crossbowConfiguration.");
            }
        } else {
            if(parent.getRootSection() != null){
                return parent.getRootSection();
            } else {
                throw new IllegalStateException("The root of this tree is not a crossbowConfiguration.");
            }
        }
    }

}
