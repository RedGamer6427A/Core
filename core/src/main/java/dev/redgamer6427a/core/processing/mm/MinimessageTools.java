package dev.redgamer6427a.core.processing.mm;

import java.util.List;

public class MinimessageTools {

    public static String createListItem(String name, String hover, String click, List<ItemPart> itemParts) {

        StringBuilder sb = new StringBuilder("<gray> ");
        if (click != null) sb.append("<click:"+click+">");
        if (hover != null) sb.append("<hover:show_text:'"+hover+"'>");
        sb.append("> ");
        if (hover != null) sb.append("</hover>");
        for (ItemPart itemPart : itemParts) {

            sb.append(itemPart.render()).append(" ");

        }
        sb.append("<gray>").append(name);

        return sb.toString();
    }


    public interface ItemPart {


        String render();

    }

    public record SimplePart(String text, String hover) implements ItemPart {

        @Override
        public String render() {
            StringBuilder sb = new StringBuilder("<dark_gray>");
            if (hover != null) sb.append("<hover:show_text:'"+hover+"'>");
            sb.append("["+ text +"<dark_gray>]");
            if (hover != null) sb.append("</hover>");
            return sb.toString();
        }
    }

    public record DoubleListPart(String color, List<Double> doubles, String hoverColor, List<Double> hoverDoubles, int maxDigitsBehindFloatingPoint) implements ItemPart {

        @Override
        public String render() {

            StringBuilder stringBuilder = new StringBuilder("<dark_gray><hover:show_text:'");

            int i = 0;

            for (Double f : hoverDoubles) {
                i++;

                stringBuilder.append(hoverColor);
                stringBuilder.append(String.format("%."+ maxDigitsBehindFloatingPoint +"f", f));
                if (i != hoverDoubles.size()) {
                    stringBuilder.append("<dark_gray>, ");
                }
            }
            stringBuilder.append("'><dark_gray>[");
            i = 0;

            for (Double f : doubles) {
                i++;
                stringBuilder.append(color);
                stringBuilder.append(String.format("%."+ maxDigitsBehindFloatingPoint +"f", f));
                if (i != doubles.size()) {
                    stringBuilder.append("<dark_gray>, ");
                }
            }
            stringBuilder.append("<dark_gray>]</hover>");
            return stringBuilder.toString();
        }
    }

    public record ListPart(String color, List<String> list, String hoverColor, List<String> hoverList) implements ItemPart {

        @Override
        public String render() {
            StringBuilder stringBuilder = new StringBuilder("<dark_gray><hover:show_text:'");

            int i = 0;

            for (String s : hoverList) {
                i++;

                stringBuilder.append(hoverColor);
                stringBuilder.append(s);
                if (i != hoverList.size()) {
                    stringBuilder.append("<dark_gray>, ");
                }
            }
            stringBuilder.append("'><dark_gray>[");
            i = 0;

            for (String s : list) {
                i++;
                stringBuilder.append(color);
                stringBuilder.append(s);
                if (i != list.size()) {
                    stringBuilder.append("<dark_gray>, ");
                }
            }
            stringBuilder.append("<dark_gray>]</hover>");
            return stringBuilder.toString();
        }
    }


}
