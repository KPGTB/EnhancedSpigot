package dev.projectenhanced.enhancedspigot.util.time;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Object that handles time (days, hours, minutes, seconds)
 */
@Getter
public class EnhancedTime {
    private final long millis;
    private final String text;

    /**
     * Get object of Time from millis
     * @param millis Milliseconds
     */
    public EnhancedTime(long millis) {
        this.millis = millis;
        this.text = fromMillis(millis);
    }

    /**
     * Get object of Time from string
     * @param text String in format XdXhXmXs (d=days, h=hours, m=minutes, s=seconds, X=integer)
     */
    public EnhancedTime(String text) {
        this.millis = toMillis(text);
        this.text = fromMillis(this.millis);
    }

    /**
     * Get time in ticks
     * @return ticks
     */
    public long getTicks() {return (millis / 1000L * 20L);}

    /**
     * Get time as text in specified format
     * @param format format of text. Placeholders: <days> <hours> <minutes> <seconds>
     * @param hideZero Hide elements with "0" like 0 seconds
     * @param splitSeq String which is a seq of chars between elements
     * @param replaceSplitSeq string that should be replacement for splitSeq or null when it should be like splitSeq
     * @param emptyReplace string that should be returned if millis is 0
     * @return Text in specified format
     */
    public String format(String format, boolean hideZero, String splitSeq, String replaceSplitSeq, String emptyReplace) {
        return fromMillis(this.millis, format, hideZero, splitSeq, replaceSplitSeq, emptyReplace);
    }

    private long toMillis(String time) {

        String temp = "";

        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;

        for (String s : time.split("")) {
            int num = -1;

            try {
                num = Integer.parseInt(s);
            } catch (Exception e) {}

            if(num >= 0) {
                temp += num;
                continue;
            }

            if(temp.isEmpty()) {
                continue;
            }

            switch (s) {
                case "d":
                    days = Integer.parseInt(temp);
                    break;
                case "h":
                    hours = Integer.parseInt(temp);
                    break;
                case "m":
                    minutes = Integer.parseInt(temp);
                    break;
                case "s":
                    seconds = Integer.parseInt(temp);
                    break;
            }

            temp = "";
        }

        hours += days * 24;
        minutes += hours * 60;
        seconds += minutes * 60;

        return seconds * 1000L;
    }

    private String fromMillis(long millis, String format, boolean hideZero, String splitSeq, String replaceSplitSeq, String emptyReplace) {
        String end = replaceSplitSeq == null ? splitSeq : replaceSplitSeq;

        int seconds = (int) Math.floorDiv(millis,1000L);
        int minutes = Math.floorDiv(seconds, 60);
        seconds -= minutes * 60;
        int hours = Math.floorDiv(minutes, 60);
        minutes -= hours * 60;
        int days = Math.floorDiv(hours, 24);
        hours -= days * 24;

        String[] formatSplit = format.split(splitSeq);
        StringBuilder result = new StringBuilder();

        Map<String, Integer> types = new HashMap<>();
        types.put("<days>", days);
        types.put("<hours>", hours);
        types.put("<minutes>", minutes);
        types.put("<seconds>", seconds);

        for (int i = 0; i < formatSplit.length; i++) {
            String text = formatSplit[i];
            String type = "";
            int number = 0;

            for (String t : types.keySet()) {
                if(text.contains(t)) {
                    type = t;
                    number = types.get(t);
                    break;
                }
            }

            if(type.isEmpty()) {
                result.append(text);
                if((i+1) != formatSplit.length) {
                    result.append(end);
                }
                break;
            }

            if(!hideZero || number != 0) {
                result.append(text.replace(type, String.valueOf(number)));
                if((i+1) != formatSplit.length) {
                    result.append(end);
                }
            }
        }

        String resultStr = result.toString();
        if(resultStr.endsWith(end)) {
            resultStr = resultStr.substring(
                    0,
                    resultStr.length() - end.length()
            );
        }

        return resultStr.isEmpty() ? emptyReplace : resultStr;
    }

    private String fromMillis(long millis) {
        return fromMillis(millis,"<days>d <hours>h <minutes>m <seconds>s", true, " ", null, "now");
    }

    @Override
    public String toString() {
        return this.text.replace(" ", "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnhancedTime time = (EnhancedTime) o;
        return millis == time.millis;
    }

    @Override
    public int hashCode() {
        return Objects.hash(millis);
    }
}
