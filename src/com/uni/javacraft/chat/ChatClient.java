package com.uni.javacraft.chat;

import com.uni.javacraft.io.InputReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {

    public void start(InputReader in) {
        try (Socket socket = new Socket(ChatConfig.HOST, ChatConfig.PORT)) {
            final var inputFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            final var output = new PrintWriter(socket.getOutputStream(), true);

            output.println(ChatConfig.PASSWORD); // initial auth

            while (true) {
                // ping
                output.println("");

                // read until '+'
                while (true) {
                    String line = inputFromServer.readLine();
                    if (line == null) return; // server closed
                    if (line.equals("+")) break;

                    String decrypted = BeaufortCipher.decrypt(line, ChatConfig.KEY);
                    System.out.println("Server: " + decrypted);
                }

                String msg = in.nextLine(""); // prompt-less input
                if ("/quit".equals(msg)) {
                    System.out.println("Quitting chat. Goodbye!");
                    return;
                } else if (msg.isBlank()) {
                    System.out.println("you cannot send an empty message");
                } else if (!msg.contains("@nikos:")) { // keep your tag rule if required
                    System.out.println("Invalid message. You must tag yourself with @nikos");
                } else {
                    String enc = BeaufortCipher.encrypt(msg, ChatConfig.KEY);
                    output.println(enc);
                    System.out.println("Encrypted & sent: " + enc);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
