package plugin.artimc.utils;

import org.bukkit.Material;

public class StringUtil {

    private StringUtil() {

    }

    public static boolean isNullOrEmpoty(String value) {
        return value == null || value.isEmpty();
    }

    /**
     * Trim a string if it is longer than a certain length.
     *
     * @param str the string
     * @param len the length to trim to
     * @return a new string
     */
    public static String trimLength(String str, int len) {
        if (str.length() > len) {
            return str.substring(0, len);
        }

        return str;
    }

    /**
     * Formats a length of seconds as a string
     *
     * @param seconds Seconds
     * @return String
     */
    public static String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = seconds % 3600 / 60;
        seconds = seconds % 60;

        String string = "";
        if (hours > 0) {
            string += hours + ":";
            if (minutes < 10) string += "0";
        }
        string += minutes + ":";
        if (seconds < 10) string += "0";
        string += seconds;
        return string;
    }

    public static String tryGet(int index, String... args) {
        try {
            return args[index];
        } catch (Exception e) {
            return "";
        }
    }

    public static int tryGetInt(int index, String... args) {
        try {
            return Integer.parseInt(args[index]);
        } catch (Exception e) {
            return 0;
        }
    }

    public static double tryGetDouble(int index, String... args) {
        try {
            return Double.parseDouble(args[index]);
        } catch (Exception e) {
            return 0;
        }
    }

    public static Material tryGetMaterial(int index, String... args) {
        try {
            return Material.valueOf(args[index].toUpperCase());
        } catch (Exception e) {
            return Material.AIR;
        }
    }

}
