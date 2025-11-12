package com.example.db;

import io.github.cdimascio.dotenv.Dotenv;

public class TestEnv {
    public static void main(String[] args) {  // ✅ This is required
        Dotenv dotenv = Dotenv.load();
        String uri = dotenv.get("MONGO_URI");

        if (uri != null && !uri.isEmpty()) {
            System.out.println("Loaded URI: ✅ Found");
        } else {
            System.out.println("❌ MONGO_URI not found in .env file");
        }
    }
}
