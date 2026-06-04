package dev.redgamer6427a.core.processing;

import java.util.Arrays;
import java.util.List;

import static dev.redgamer6427a.core.console.output.ConsoleMiniMessage.mm;

public class Parameterize {

    public static String parameterize(String main, boolean mmMain, boolean mmParams, List<Object> oList) {

        String out = main;
        if (mmMain) out = mm(out);
        for (Object o : oList) {
            String oS;
            if (o instanceof String) {
                oS = (String) o;
            } else {
                oS = o.toString();
            }
            if (mmParams) oS = mm(oS, true);
            out = out.replaceFirst("\\{}", oS);

        }

        return out;
    }

    public static String parameterize(String main, boolean mmMain, boolean mmParams, Object... oList) {

        return parameterize(main, mmMain, mmParams, Arrays.asList(oList));


    }


}
