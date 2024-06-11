package gg.generations.rarecandy.tools.gui;

import java.util.Comparator;

class AnimationComparator implements Comparator<String> {
    @Override
    public int compare(String s1, String s2) {
        String substring1 = extractSortingSubstring(s1);
        String substring2 = extractSortingSubstring(s2);

        int result = substring1.compareTo(substring2);

        if (result == 0) {
            String pmSubstring1 = s1.substring(0, 18);
            String pmSubstring2 = s2.substring(0, 18);
            result = pmSubstring1.compareTo(pmSubstring2);
        }

        return result;
    }

    private String extractSortingSubstring(String s) {
        int startIndex = s.indexOf("pm") + 18;
        return s.substring(startIndex);
    }
}