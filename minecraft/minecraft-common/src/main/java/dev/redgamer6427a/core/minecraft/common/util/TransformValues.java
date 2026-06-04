package dev.redgamer6427a.core.minecraft.common.util;

import org.jetbrains.annotations.Nullable;

public class TransformValues {

    public static @Nullable Integer makeInt(Object o){
        if(o instanceof Integer){
            return (Integer) o;
        } else if(o instanceof String){
            try {
                return Integer.parseInt((String) o);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if(o instanceof Boolean){
            return (Boolean) o ? 1 : 0;
        } else if(o instanceof Double d){
            return (int) Math.round(d);
        } else if(o instanceof Float f){
            return Math.round(f);
        }

        return null;

    }

    public static @Nullable Double makeDouble(Object o){
        if(o instanceof Double){
            return (Double) o;
        } else if(o instanceof String){
            try {
                return Double.parseDouble((String) o);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if(o instanceof Boolean){
            return (Boolean) o ? 1.0 : 0.0;
        } else if(o instanceof Integer){
            return ((Integer) o).doubleValue();
        } else if(o instanceof Long){
            return ((Long) o).doubleValue();
        } else if(o instanceof Float){
            return ((Float) o).doubleValue();
        }
        return null;
    }

    public static @Nullable Long makeLong(Object o){
        if(o instanceof Long){
            return (Long) o;
        } else if(o instanceof String){
            try {
                return Long.parseLong((String) o);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if(o instanceof Boolean b){
            return b ? 1L : 0L;
        } else if(o instanceof Double d){
            return Math.round(d);
        } else if(o instanceof Float f){
            return (long) Math.round(f);
        } else if(o instanceof Integer i){
            return (long) i;
        }
        return null;

    }

    public static @Nullable Boolean makeBoolean(Object o){
        if (o instanceof Boolean) {
            return (Boolean) o;
        } else if (o instanceof Integer i) {
            return i != 0;
        } else if (o instanceof Double d) {
            return d != 0;
        } else if (o instanceof Float f) {
            return f != 0;
        } else if (o instanceof Long l) {
            return l != 0;
        } else if (o instanceof String s) {
            return Boolean.parseBoolean(s);
        }
        return null;
    }



}
