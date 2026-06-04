package dev.redgamer6427a.core.minecraft.paper.player.modules;

import dev.redgamer6427a.core.minecraft.paper.PaperPlugin;
import dev.redgamer6427a.core.minecraft.paper.player.ExtendedPlayer;
import dev.redgamer6427a.core.minecraft.paper.player.PlayerModule;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public final class PDCModule extends PlayerModule {


    public PDCModule(ExtendedPlayer player) {
        super(player);
    }

    public void setString(String id, String value) {
        player.getPlayer().getPersistentDataContainer().set(new NamespacedKey(PaperPlugin.getInstance(), id), PersistentDataType.STRING, value);
        }

        public void setDouble(String id, double value) {
            player.getPlayer().getPersistentDataContainer().set(new NamespacedKey(PaperPlugin.getInstance(), id), PersistentDataType.DOUBLE, value);
        }

        public void setFloat(String id, float value) {
            player.getPlayer().getPersistentDataContainer().set(new NamespacedKey(PaperPlugin.getInstance(), id), PersistentDataType.FLOAT, value);
        }

        public void setInt(String id, int value) {
            player.getPlayer().getPersistentDataContainer().set(new NamespacedKey(PaperPlugin.getInstance(), id), PersistentDataType.INTEGER, value);
        }

        public void setBoolean(String id, boolean value) {
            player.getPlayer().getPersistentDataContainer().set(new NamespacedKey(PaperPlugin.getInstance(), id), PersistentDataType.BOOLEAN, value);
        }

        public String getString(String id) {
            return player.getPlayer().getPersistentDataContainer().get(
                    new NamespacedKey(PaperPlugin.getInstance(), id),
                    PersistentDataType.STRING
            );
        }

        public Double getDouble(String id) {
            return player.getPlayer().getPersistentDataContainer().get(
                    new NamespacedKey(PaperPlugin.getInstance(), id),
                    PersistentDataType.DOUBLE
            );
        }

        public Float getFloat(String id) {
            return player.getPlayer().getPersistentDataContainer().get(
                    new NamespacedKey(PaperPlugin.getInstance(), id),
                    PersistentDataType.FLOAT
            );
        }

        public Integer getInt(String id) {
            return player.getPlayer().getPersistentDataContainer().get(
                    new NamespacedKey(PaperPlugin.getInstance(), id),
                    PersistentDataType.INTEGER
            );
        }

        public Boolean getBoolean(String id) {
            return player.getPlayer().getPersistentDataContainer().get(
                    new NamespacedKey(PaperPlugin.getInstance(), id),
                    PersistentDataType.BOOLEAN
            );
        }

        public Player player() {
            return player.getPlayer();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (PDCModule) obj;
            return Objects.equals(this.player, that.player);
        }

        @Override
        public int hashCode() {
            return Objects.hash(player);
        }

        @Override
        public String toString() {
            return "PDCModule[" +
                    "player=" + player + ']';
        }

    }
