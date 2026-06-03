package dev.redgamer6427a.admiral.paper.util;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class MusicUtil {

    Map<Player, Integer> musicMap = new HashMap<>();

    final String id;

    final int length;

    public MusicUtil(String id, int length) {
        this.id = id;
        this.length = length;
    }

    public void start(Player p){
        musicMap.put(p, length);
        p.playSound(p.getLocation(), id, 0.1f, 1);


    }

    public void stop(Player p){
        p.stopSound(id);
        musicMap.remove(p);
    }

    private void tick(){

        for (Map.Entry<Player, Integer> entry : musicMap.entrySet()) {
            musicMap.put(entry.getKey(), entry.getValue() - 1);
            if(entry.getValue() == 0){
                start(entry.getKey());
            }

        }

    }

    public void startLoop(){
        Procrastinator.repeat(1, this::tick);

    }




}
