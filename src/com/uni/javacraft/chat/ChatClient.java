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

        if (username.isEmpty()) {
            username = "player";
        }

        System.out.print("Enter shared key (default = SECRETKEY): ");
        String keyInput = in.nextLine("").trim();
        String key = keyInput.isEmpty() ? ChatConfig.KEY : keyInput;

        System.out.println("\n✅ Connected as @" + username);
        System.out.println("📝 Message format: @" + username + ": your message");
        System.out.println("Type '/quit' to exit.\n");

        try (Socket socket = new Socket(ChatConfig.HOST, ChatConfig.PORT);
             var inputFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             var output = new PrintWriter(socket.getOutputStream(), true)) {

            // Sends server password
            output.println(ChatConfig.PASSWORD);

            int validIn = 0, validOut = 0, invalidIn = 0, invalidOut = 0;

            while (true) {
                // 1 Pings the fucking server
                output.println("");

                // 2) Read all server responses until '+'
                while (true) {
                    String line = inputFromServer.readLine();
                    if (line == null) {
                        System.out.println("❌ Server closed the connection.");
                        printSummary(validIn, validOut, invalidIn, invalidOut);
                        return;
                    }
                    if (line.equals("+")) break;

                    // decrypt
                    String decrypted = BeaufortCipher.decrypt(line, key);

                    // show both ciphertext and plaintext cuz hell yeah thats cool
                    System.out.println("📩 Encrypted: " + line);
                    System.out.println("📤 Decrypted: " + decrypted);

                    if (MessageValidator.isValid(decrypted)) {
                        System.out.println("✅ Valid incoming: " + decrypted);
                        validIn++;
                    } else {
                        System.out.println("❌ Invalid incoming format!");
                        invalidIn++;
                    }
                }

                // 3 Get user message with prompt meow
                System.out.print("\nEnter message (@" + username + ": text or /quit): ");
                String msg = in.nextLine("");

                if ("/quit".equalsIgnoreCase(msg.trim())) {
                    System.out.println("👋 Quitting chat. Goodbye!");
                    printSummary(validIn, validOut, invalidIn, invalidOut);
                    return;
                }

                // 4 Check format and username
                if (!MessageValidator.isValid(msg)) {
                    System.out.println("❌ Invalid message format. Use: @" + username + ": your text");
                    invalidOut++;
                    continue;
                }

                if (!msg.startsWith("@" + username + ":")) {
                    System.out.println("⚠️ You must tag yourself: @" + username + ": message");
                    invalidOut++;
                    continue;
                }

                // Encrypt and send
                String enc = BeaufortCipher.encrypt(msg, key);
                output.println(enc);
                System.out.println("✅ Encrypted & sent: " + enc);
                validOut++;
            }

        } catch (IOException e) {
            System.out.println("❌ Could not connect to server: " + e.getMessage());
            System.out.println("Check your internet or server details in ChatConfig.");
        } catch (Throwable t) {
            System.out.println("⚠️ Unexpected error: " + t.getMessage());
            t.printStackTrace();
        }
    }

    private void printSummary(int validIn, int validOut, int invalidIn, int invalidOut) {
        System.out.println("\n===== Session Summary =====");
        System.out.println("✅ Valid incoming: " + validIn);
        System.out.println("✅ Valid outgoing: " + validOut);
        System.out.println("❌ Invalid incoming: " + invalidIn);
        System.out.println("❌ Invalid outgoing: " + invalidOut);
        System.out.println("============================");
    }
}
