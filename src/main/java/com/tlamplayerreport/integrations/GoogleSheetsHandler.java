package com.tlamplayerreport.integrations;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.tlamplayerreport.ReportPlugin;
import com.tlamplayerreport.data.Report;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class GoogleSheetsHandler {
    
    private final ReportPlugin plugin;
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private Sheets sheetsService;
    private boolean initialized = false;
    
    public GoogleSheetsHandler(ReportPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void initialize() {
        try {
            File credentialsFile = new File(plugin.getDataFolder(), plugin.getConfig().getString("google-sheets.credentials-file"));
            
            if (!credentialsFile.exists()) {
                plugin.getLogger().warning("Google Sheets credentials file not found. Google Sheets integration disabled.");
                plugin.getLogger().warning("Please place credentials.json in the plugin folder and restart.");
                return;
            }
            
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                    JSON_FACTORY, 
                    new InputStreamReader(new FileInputStream(credentialsFile))
            );
            
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new File(plugin.getDataFolder(), "tokens")))
                    .setAccessType("offline")
                    .build();
            
            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
            Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
            
            sheetsService = new Sheets.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName("TLamPlayerReport")
                    .build();
            
            initialized = true;
            plugin.getLogger().info("Google Sheets integration initialized successfully!");
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize Google Sheets: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void logReport(Report report) {
        if (!initialized) {
            return;
        }
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String spreadsheetId = plugin.getConfig().getString("google-sheets.spreadsheet-id");
                String range = "Reports!A:L";
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timestamp = dateFormat.format(new Date(report.getTimestamp()));
                
                List<Object> values = Arrays.asList(
                        report.getId(),
                        timestamp,
                        report.getType().toString(),
                        report.getReporterName(),
                        report.getReporterUuid().toString(),
                        report.getCategory(),
                        report.getTargetName() != null ? report.getTargetName() : "N/A",
                        report.getTargetUuid() != null ? report.getTargetUuid().toString() : "N/A",
                        report.getDescription() != null ? report.getDescription() : "",
                        report.getStatus().toString(),
                        report.getReviewerName() != null ? report.getReviewerName() : "",
                        report.getNotes() != null ? report.getNotes() : ""
                );
                
                ValueRange body = new ValueRange().setValues(Collections.singletonList(values));
                
                AppendValuesResponse result = sheetsService.spreadsheets().values()
                        .append(spreadsheetId, range, body)
                        .setValueInputOption("RAW")
                        .execute();
                
                plugin.getLogger().info("Report #" + report.getId() + " logged to Google Sheets successfully!");
                
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to log report to Google Sheets: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    public boolean isInitialized() {
        return initialized;
    }
}