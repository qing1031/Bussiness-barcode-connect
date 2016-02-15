package com.foodlogiq.distributormobile.miscellaneousHelpers;

/**
 * Utility functions to modify strings.
 */
public class StringUtils {
    /**
     * Returns the input string with first charact after every delimiter capitalized.
     *
     * @param input String to be "titleized"
     * @return "Titleized" representation of input string.
     */
    public static String toTitleCase(String input) {
        StringBuilder titleCase = new StringBuilder();
        boolean nextTitleCase = true;

        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }

            titleCase.append(c);
        }

        return titleCase.toString();
    }
}
