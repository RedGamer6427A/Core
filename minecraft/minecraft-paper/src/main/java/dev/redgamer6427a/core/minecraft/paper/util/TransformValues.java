package dev.redgamer6427a.admiral.paper.util;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TransformValues {
    public static @Nullable Vector makeVector(Object o){
        if(o instanceof Vector v){
            return v;
        } else if (o instanceof String s) {
            s = s.trim();
            if (s.startsWith("[") && s.endsWith("]")) {
                s = s.substring(1, s.length() - 1).trim();
            }

            String[] parts = s.split("[,\\s]+");
            if (parts.length != 3) return null;

            try {
                double x = Double.parseDouble(parts[0]);
                double y = Double.parseDouble(parts[1]);
                double z = Double.parseDouble(parts[2]);
                return new Vector(x, y, z);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (o instanceof List<?> rawList) {
            boolean allNumbers = true;
            for (Object item : rawList) {
                if (!(item instanceof Number)) {
                    allNumbers = false;
                    break;
                }
            }

            if (allNumbers) {
                List<Number> numbers = new ArrayList<>();
                for (Object item : rawList) {
                    numbers.add((Number) item);
                }
                if(numbers.size() != 3) return null;
                return new Vector(numbers.get(0).doubleValue(), numbers.get(1).doubleValue(), numbers.get(2).doubleValue());

                // now 'numbers' is a List<Number>
            }



        }
        return null;

    }


}
