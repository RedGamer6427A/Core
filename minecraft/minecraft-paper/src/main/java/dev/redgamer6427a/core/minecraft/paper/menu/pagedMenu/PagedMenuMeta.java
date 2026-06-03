package dev.redgamer6427a.admiral.paper.menu.pagedMenu;

import dev.redgamer6427a.admiral.paper.menu.MenuMeta;
import net.kyori.adventure.text.Component;

import java.util.*;

public class PagedMenuMeta extends MenuMeta {

    private final Map<Integer, Integer> rangesMap = new LinkedHashMap   <>();

    public PagedMenuMeta(int rows, Component title, Integer... mutableBounds) {
        super(rows, title);

        List<Integer> list = mutableBounds == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(mutableBounds));

        if(list.isEmpty()) {
            throw new IllegalArgumentException("mutableBounds may not be empty.");
        }

        if(list.size() % 2 == 1) {
            list.add(rows*9-1);
        }

        if(list.get(0) < 0) {
            throw new IllegalArgumentException("mutableRangeStart may not be smaller than 0, since there are no negative menu slots.");
        }

        if( list.get(list.size()-1) > rows*9-1){
            throw new IllegalArgumentException("mutableRangeStart may not be bigger than the slots present in the inventory, since there are not enough slots.");
        }
        for (int i = 0; i < list.size()/2; i++) {
            rangesMap.put(list.get(i*2), list.get(i*2+1));
        }



    }
    public Map<Integer, Integer> mutableRanges() {
        return Collections.unmodifiableMap(rangesMap);
    }
}
