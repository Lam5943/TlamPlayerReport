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
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
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

    /**
     * Initialize Sheets client.
     * Configuration options (plugin config):
     * - google-sheets.service-account-file  (preferred; path relative to plugin data folder)
     * - google-sheets.credentials-file      (fallback; installed-app flow — requires interactive auth)
     */
    public void initialize() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists() && !dataFolder.mkdirs()) {
                plugin.getLogger().warning("Could not create plugin data folder: " + dataFolder.getAbsolutePath());
            }

            String spreadsheetId = plugin.getConfig().getString("google-sheets.spreadsheet-id", "");
            if (spreadsheetId == null || spreadsheetId.isEmpty()) {
                plugin.getLogger().warning("No google-sheets.spreadsheet-id configured. Google Sheets integration disabled.");
                return;
            }

            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            // Prefer service account (non-interactive, recommended for servers)
            String serviceAccountPath = plugin.getConfig().getString("google-sheets.service-account-file", "");
            if (serviceAccountPath != null && !serviceAccountPath.isEmpty()) {
                File saFile = new File(dataFolder, serviceAccountPath);
                if (!saFile.exists()) {
                    plugin.getLogger().severe("Configured service account file not found: " + saFile.getAbsolutePath());
                    return;
                }

                GoogleCredentials credentials = ServiceAccountCredentials.fromStream(new FileInputStream(saFile))
                        .createScoped(SCOPES);

                sheetsService = new Sheets.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                        .setApplicationName("TLamPlayerReport")
                        .build();

                initialized = true;
                plugin.getLogger().info("Google Sheets integration initialized using service account.");

                return;
            }

            // Fallback to installed-app OAuth flow (requires user interaction)
            String credentialsPath = plugin.getConfig().getString("google-sheets.credentials-file", "credentials.json");
            File credentialsFile = new File(dataFolder, credentialsPath);

            if (!credentialsFile.exists()) {
                plugin.getLogger().warning("Google Sheets credentials file not found (" + credentialsFile.getAbsolutePath() + "). Google Sheets integration disabled.");
                plugin.getLogger().warning("Either place credentials.json in the plugin folder or configure google-sheets.service-account-file in the config.");
                return;
            }

            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                    JSON_FACTORY,
                    new InputStreamReader(new FileInputStream(credentialsFile))
            );

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new File(dataFolder, "tokens")))
                    .setAccessType("offline")
                    .build();

            LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
            Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

            sheetsService = new Sheets.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName("TLamPlayerReport")
                    .build();

            initialized = true;
            plugin.getLogger().info("Google Sheets integration initialized successfully (installed-app flow).");

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize Google Sheets: " + e.getMessage());
            e.printStackTrace();
            initialized = false;
        }
    }

    public void logReport(Report report) {
        if (!initialized || sheetsService == null) {
            return;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String spreadsheetId = plugin.getConfig().getString("google-sheets.spreadsheet-id", "");
                if (spreadsheetId == null || spreadsheetId.isEmpty()) {
                    plugin.getLogger().warning("No spreadsheet ID configured; skipping Sheets log.");
                    return;
                }

                String range = "Reports!A:L";

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timestamp = dateFormat.format(new Date(report.getTimestamp()));

                // Safely convert potentially-null fields
                Object reporterUuid = report.getReporterUuid() != null ? report.getReporterUuid().toString() : "N/A";
                Object targetUuid = report.getTargetUuid() != null ? report.getTargetUuid().toString() : "N/A";

                List<Object> values = Arrays.asList(
                        report.getId(),
                        timestamp,
                        report.getType() != null ? report.getType().toString() : "UNKNOWN",
                        report.getReporterName() != null ? report.getReporterName() : "N/A",
                        reporterUuid,
                        report.getCategory() != null ? report.getCategory() : "",
                        report.getTargetName() != null ? report.getTargetName() : "N/A",
                        targetUuid,
                        report.getDescription() != null ? report.getDescription() : "",
                        report.getStatus() != null ? report.getStatus().toString() : "UNKNOWN",
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