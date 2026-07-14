package dev.redgamer6427a.core.minecraft.paper.testing;

import dev.redgamer6427a.core.minecraft.paper.configuration.Configuration;
import lombok.Getter;

public class Config extends Configuration {

    @Getter
    private static Config instance = new Config();

    @Getter
    GlobalChatCategory chatCategory = new GlobalChatCategory(this);

    public Config() {
        super("config");
        addValue(chatCategory);
        this.load();

    }
}
