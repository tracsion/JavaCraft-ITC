package com.uni.javacraft.chat;

import com.uni.javacraft.io.InputReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.IOException;

public class ChatClient {

    public void start(InputReader in) {
        System.out.println("===== Chat Client =====");
        System.out.print("Enter your username (without @): ");
        String username = in.nextLine("").trim();
        if (username.isEmpty()) username = "player";

        final String key = ChatConfig.KEY;
        System.out.println("Using shared key: " + key);
        System.out.println("\nConnected as @" + username);
        System.out.println("Message format: @" + username + ": your message");
        System.out.println("Type '/quit' to exit.\n");

        try (Socket socket = new Socket(ChatConfig.HOST, ChatConfig.PORT)) {
            socket.setSoTimeout(2500); // avoid blocking forever
            var inputFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            var output = new PrintWriter(socket.getOutputStream(), true);

            // authenticate with server
            output.println(ChatConfig.PASSWORD);

            while (true) {
                // ping to receive updates
                output.println("");

                // read incoming messages
                while (true) {
                    String line;
                    try {
                        line = inputFromServer.readLine();
                    } catch (java.net.SocketTimeoutException timeout) {
                        break; // no more lines right now cuz fuck this
                    }
                    if (line == null) {
                        System.out.println("Server closed connection.");
                        return;
                    }
                    if (line.equals("+")) break;

                    // show server notices
                    if (line.startsWith("* ") || line.endsWith("connected") || line.endsWith("disconnected")) {
                        System.out.println(line);
                        continue;
                    }

                    // extract encrypted message part
                    String encryptedPart = line;
                    int arrowIndex = line.indexOf(">>> ");
                    if (arrowIndex != -1 && arrowIndex + 4 < line.length()) {
                        encryptedPart = line.substring(arrowIndex + 4).trim();
                    }

                    // decrypt only the message
                    String decrypted = BeaufortCipher.decrypt(encryptedPart, key);

                    // show only valid chat messages
                    if (MessageValidator.isValid(decrypted)) {
                        System.out.println(decrypted);
                    }
                }

                // get user input
                System.out.print("> ");
                String msg = in.nextLine("").trim();

                if (msg.equalsIgnoreCase("/quit")) {
                    System.out.println("Exiting chat.");
                    return;
                }

                // validate before sending
                if (!MessageValidator.isValid(msg)) {
                    System.out.println("Invalid format. Use: @" + username + ": your message");
                    continue;
                }

                if (!msg.startsWith("@" + username + ":")) {
                    System.out.println("You must tag yourself as @" + username + ":");
                    continue;
                }

                // encrypt and send
                String enc = BeaufortCipher.encrypt(msg, key);
                output.println(enc);
            }

        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        } catch (Throwable t) {
            System.err.println("Unexpected error: " + t.getMessage());
        }
    }
}
