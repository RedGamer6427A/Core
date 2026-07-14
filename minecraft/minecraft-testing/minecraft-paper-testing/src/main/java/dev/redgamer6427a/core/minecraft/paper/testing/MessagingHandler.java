package dev.redgamer6427a.core.minecraft.paper.testing;

import dev.redgamer6427a.core.logging.Logger;
import dev.redgamer6427a.core.messagebus.Message;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;

import static dev.redgamer6427a.core.minecraft.common.text.AdventureMM.mm;

public class MessagingHandler {
private static final Logger logger = Logger.create();

    public static void handle(Message message) {

        Map<String, String> data = message.contents();

        if (data.get("eventType").equals("globalchat")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(mm("<dark_gray>[<gray>"+message.sender()+"<dark_gray>] <white>"+data.get("sender")+ "<gray>: <white>").append(Component.text(data.get("content")).style(Style.style(TextColor.color(255,255,255)))));
            }
        }
        if (data.get("eventType").equals("global-op")) {
            OfflinePlayer subject = Bukkit.getOfflinePlayer(data.get("subject"));
            subject.setOp(true);
            if (subject.isOnline()) {
                Player subjectOnline = Bukkit.getPlayer(data.get("subject"));
                subjectOnline.sendMessage("<green>You've been made an operator on the whole network!");
            }
            logger.info(subject.getName() + " has been made an operator on the whole network by "+data.get("sender")+" on "+message.sender()+"!");
        }
        if (data.get("eventType").equals("global-deop")) {
            OfflinePlayer subject = Bukkit.getOfflinePlayer(data.get("subject"));
            subject.setOp(false);
            if (subject.isOnline()) {
                Player subjectOnline = Bukkit.getPlayer(data.get("subject"));
                subjectOnline.sendMessage("<red>You've been unmade an operator on the whole network!");
            }
            logger.info(subject.getName() + " has been unmade an operator on the whole network by "+data.get("sender")+" on "+message.sender()+"!");
        }
        if (data.get("eventType").equals("reload-all")) {
            Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(mm("<blue>Global Reload...")));
            logger.info("Global reload!");
            Bukkit.getServer().reload();
        }
    }


}
