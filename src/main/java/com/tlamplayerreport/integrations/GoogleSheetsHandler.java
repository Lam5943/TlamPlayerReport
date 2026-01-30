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