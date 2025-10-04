package com.uni.javacraft.chat;

public class BeaufortCipher {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String encrypt(String text, String key) {
        return process(text, key);
    }
    public static String decrypt(String text, String key) {
        return process(text, key);
    }
    private static String process(String text, String key) {
        text = text.toUpperCase();
        key = key.toUpperCase();
        StringBuilder result = new StringBuilder();
        int keyIndex = 0;
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                int p = ALPHABET.indexOf(c);
                int k = ALPHABET.indexOf(key.charAt(keyIndex));
                int cpos = (k - p + 26) % 26;
                result.append(ALPHABET.charAt(cpos));
                keyIndex = (keyIndex + 1) % key.length();
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
