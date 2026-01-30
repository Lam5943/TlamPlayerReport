package com.tlamplayerreport.security;

import com.tlamplayerreport.ReportPlugin;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

public class KeyValidator {
    
    private final ReportPlugin plugin;
    private final Set<String> validKeyHashes;
    private boolean validated = false;
    
    public KeyValidator(ReportPlugin plugin) {
        this.plugin = plugin;
        this.validKeyHashes = new HashSet<>();
        
        validKeyHashes.add("8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92");
        validKeyHashes.add("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8");
        validKeyHashes.add("6b86b273ff34fce19d6b804eff5a3f5747ada4eaa22f1d49c01e52ddb7875b4b");
    }
    
    public boolean validate() {
        String key = plugin.getConfig().getString("license.key", "");
        
        if (key.isEmpty() || key.equals("ENTER-YOUR-LICENSE-KEY-HERE")) {
            plugin.getLogger().severe("========================================");
            plugin.getLogger().severe("NO LICENSE KEY CONFIGURED!");
            plugin.getLogger().severe("Please set your license key in config.yml");
            plugin.getLogger().severe("Contact TranLam (Midnight) for a license key");
            plugin.getLogger().severe("========================================");
            return false;
        }
        
        boolean onlineVerification = plugin.getConfig().getBoolean("license.online-verification", false);
        
        if (onlineVerification) {
            validated = validateOnline(key);
        } else {
            validated = validateOffline(key);
        }
        
        if (validated) {
            plugin.getLogger().info("License key validated successfully!");
        } else {
            plugin.getLogger().severe("========================================");
            plugin.getLogger().severe("INVALID LICENSE KEY!");
            plugin.getLogger().severe("The provided license key is not valid");
            plugin.getLogger().severe("Contact TranLam (Midnight) for support");
            plugin.getLogger().severe("========================================");
        }
        
        return validated;
    }
    
    private boolean validateOffline(String key) {
        try {
            String hash = hashKey(key);
            return validKeyHashes.contains(hash);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to validate license key: " + e.getMessage());
            return false;
        }
    }
    
    private boolean validateOnline(String key) {
        try {
            String verificationUrl = plugin.getConfig().getString("license.verification-url");
            
            if (verificationUrl == null || verificationUrl.isEmpty()) {
                plugin.getLogger().warning("Online verification enabled but no URL configured, falling back to offline");
                return validateOffline(key);
            }
            
            URL url = new URL(verificationUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "TLamPlayerReport/1.0");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            String jsonPayload = "{\"key\":\"" + key + "\",\"plugin\":\"TLamPlayerReport\",\"version\":\"1.0.0\"}";
            
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
                wr.flush();
            }
            
            int responseCode = connection.getResponseCode();
            
            if (responseCode == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    
                    return response.toString().contains("\"valid\":true");
                }
            } else {
                plugin.getLogger().warning("Online verification failed with code: " + responseCode);
                return validateOffline(key);
            }
            
        } catch (Exception e) {
            plugin.getLogger().warning("Online verification failed: " + e.getMessage());
            plugin.getLogger().warning("Falling back to offline validation");
            return validateOffline(key);
        }
    }
    
    private String hashKey(String key) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(key.getBytes(StandardCharsets.UTF_8));
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        
        return hexString.toString();
    }
    
    public boolean isValidated() {
        return validated;
    }
    
    public String generateKey(String seed) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(seed.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash).substring(0, 32);
        } catch (Exception e) {
            return null;
        }
    }
}