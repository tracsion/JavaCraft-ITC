package com.uni.javacraft.io;

import java.util.Scanner;

public class InputReader {
    private final Scanner sc = new Scanner(System.in);

    public boolean confirm(String prompt) {
        System.out.print(prompt);
        String s = sc.next().trim().toUpperCase();
        sc.nextLine(); // consume the rest of my heart
        return s.equals("Y");
    }

    public String nextToken(String prompt) {
        System.out.print(prompt);
        String t = sc.next();
        sc.nextLine(); // consume the rest of my broken heart
        return t;
    }

    public String nextTokenLower(String prompt) {
        return nextToken(prompt).toLowerCase();
    }

    public int nextInt(String prompt) {
        System.out.print(prompt);
        int v = sc.nextInt();
        sc.nextLine(); // consume my heart
        return v;
    } // what the fuck am I saying dawg

    public String nextLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }

    public void waitEnter() {
        sc.nextLine();
    }
}
