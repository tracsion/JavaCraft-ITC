package com.uni.javacraft.chat;


// SO listen kids this is the message dfa validator and it works i made it myself with some help from very original sources but since chat gpt IS FUCKING DUMB i had to write this.

public final class MessageValidator {

    private MessageValidator() {}

    public static boolean isValid(String s) {
        if (s == null) return false;
        int i = 0, n = s.length();

        // 1 '@'
        if (n == 0 || s.charAt(i) != '@') return false;
        i++;

        // 2 name: first must be a letter
        if (i >= n || !isLetter(s.charAt(i))) return false;
        i++;
        while (i < n && isNameChar(s.charAt(i))) i++;

        // 3 ':' exactly once
        if (i >= n || s.charAt(i) != ':') return false;
        i++;

        // 4 single space after colon
        if (i >= n || s.charAt(i) != ' ') return false;
        i++;

        // 5 text must be at least one char
        if (i >= n) return false;

        // this ensures that the rest are printable
        for (; i < n; i++) {
            char c = s.charAt(i);
            if (Character.isISOControl(c)) return false;
        }
        return true;
    }

    private static boolean isLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }
    private static boolean isNameChar(char c) {
        return isLetter(c) || (c >= '0' && c <= '9') || c == '_';
    }
}
