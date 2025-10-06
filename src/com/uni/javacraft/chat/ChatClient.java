package com.uni.javacraft.chat;

import com.uni.javacraft.io.InputReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.IOException;

// had some problems with the chat box because people were sending empty messages so i added a timeout ye thankgod cuz shit wasnt working properly
// a
public class ChatClient {

    public void start(InputReader in) {
        System.out.println("===== Chat Client =====");
        System.out.print("Enter your username (without @): ");
        String username = in.nextLine("").trim();
        if (username.isEmpty()) username = "player";

        // Always use default key
        final String key = ChatConfig.KEY;
        System.out.println("Using default encryption key.");

        System.out.println("\nConnected as @" + username);
        System.out.println("Message format: @" + username + ": your message");
        System.out.println("Type '/quit' to exit.\n");

        try (Socket socket = new Socket(ChatConfig.HOST, ChatConfig.PORT)) {
            // If the server doesnt send '+' we break prompt
            socket.setSoTimeout(2500); // milsecs

            var inputFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            var output = new PrintWriter(socket.getOutputStream(), true);

            // Sends server password
            output.println(ChatConfig.PASSWORD);

            int validIn = 0, validOut = 0, invalidIn = 0, invalidOut = 0;

            while (true) {
                // 1 Ping server
                output.println("");

                // 2 Read all server responses until '+' OR timeout
                while (true) {
                    String line;
                    try {
                        line = inputFromServer.readLine();
                    } catch (java.net.SocketTimeoutException timeout) {
                        break;
                    }

                    if (line == null) {
                        System.out.println("Server closed the connection.");
                        printSummary(validIn, validOut, invalidIn, invalidOut);
                        return;
                    }
                    if (line.equals("+")) break;

                    // Skip server notices cooking
                    if (!line.isEmpty() && (line.charAt(0) == '*' || line.charAt(0) == '-' || line.endsWith(">>> "))) {
                        System.out.println(line);
                        continue;
                    }

                    // Decrypt and validate chat payloads
                    String decrypted = BeaufortCipher.decrypt(line, key);
                    System.out.println("Encrypted: " + line);
                    System.out.println("Decrypted: " + decrypted);

                    if (MessageValidator.isValid(decrypted)) {
                        System.out.println("Valid incoming: " + decrypted);
                        validIn++;
                    } else {
                        System.out.println("Invalid incoming format!");
                        invalidIn++;
                    }
                }

                // 3 Get user message
                System.out.print("\nEnter message (@" + username + ": text or /quit): ");
                String msg = in.nextLine("");

                if ("/quit".equalsIgnoreCase(msg.trim())) {
                    System.out.println("Quitting chat. Cya mate!");
                    printSummary(validIn, validOut, invalidIn, invalidOut);
                    return;
                }

                // 4 Validate format and selftsh
                if (!MessageValidator.isValid(msg)) {
                    System.out.println("Invalid message format. Use: @" + username + ": your text");
                    invalidOut++;
                    continue;
                }
                if (!msg.startsWith("@" + username + ":")) {
                    System.out.println("You must tag yourself: @" + username + ": message");
                    invalidOut++;
                    continue;
                }

                // 5 Encrypt and send pro
                String enc = BeaufortCipher.encrypt(msg, key);
                output.println(enc);
                System.out.println("Encrypted & sent: " + enc);
                validOut++;
            }

        } catch (IOException e) {
            System.err.println("Could not connect to server: " + e.getMessage());
            System.err.println("Check your ChatConfig host/port and network.");
        } catch (Throwable t) {
            System.err.println("Unexpected error: " + t.getMessage());
        }
    }

    private void printSummary(int validIn, int validOut, int invalidIn, int invalidOut) {
        System.out.println("\n===== Session Summary =====");
        System.out.println("Valid incoming: " + validIn);
        System.out.println("Valid outgoing: " + validOut);
        System.out.println("Invalid incoming: " + invalidIn);
        System.out.println("Invalid outgoing: " + invalidOut);
        System.out.println("============================");
    }
}
