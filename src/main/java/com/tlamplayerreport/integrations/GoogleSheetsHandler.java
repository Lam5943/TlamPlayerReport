import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import java.io.IOException;
import java.security.GeneralSecurityException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.util.Collections;

public class GoogleSheetsHandler {

    private Sheets sheetsService;
    private String SPREADSHEET_ID;

    public GoogleSheetsHandler(String spreadsheetId) throws GeneralSecurityException, IOException {
        this.SPREADSHEET_ID = spreadsheetId;
        this.sheetsService = createSheetsService();
    }

    private Sheets createSheetsService() throws GeneralSecurityException, IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("path/to/your/service-account.json"))
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
        return new Sheets.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                .setApplicationName("TlamPlayerReport").build();
    }

    // Add methods to read, write data from sheets etc.
}