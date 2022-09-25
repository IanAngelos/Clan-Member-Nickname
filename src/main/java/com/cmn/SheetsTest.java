package com.cmn;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.function.UnaryOperator;

public class SheetsTest {

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = SheetsTest.class.getResourceAsStream(Constants.CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + Constants.CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(Constants.JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, Constants.JSON_FACTORY, clientSecrets, Constants.SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(Constants.TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service =
                new Sheets.Builder(HTTP_TRANSPORT, Constants.JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(Constants.APPLICATION_NAME)
                        .build();
        ValueRange response = service.spreadsheets().values()
                .get(Constants.SHEET_ID, Constants.SHEET_RANGE)
                .execute();
        clanNameMapRead(response, service);
    }

    //Read Only Flow:
    //1 Fetch Existing Values from sheet(Can use same Read)
    //2 Display in chat the users nickname
    private static void clanNameMapRead(ValueRange response, Sheets service) throws IOException {
        List<List<Object>> values = response.getValues();
        Map<String, String> clanNameMapRead = new HashMap<>();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            for (List row : values) {
                //Checks for duplicate clan names in the file and stores the pair in a string map.
                clanNameMapRead.putIfAbsent(row.get(0).toString(),row.get(1).toString());
            }
            System.out.printf(clanNameMapRead.toString());
        }

        if (Constants.WRITE) {
            clanNameMapWrite(clanNameMapRead, service, response);
        }
    }

    //Write Flow:
    //1 Fetch Existing Values from sheet(Can use same Read)
    //2 Determine if value exists or not (Update vs. Add)
    //3 Send update/add
    //4 Respond in runelite that this was succesfull?
    private static void clanNameMapWrite(Map<String, String> clanNameMapRead, Sheets service, ValueRange response) throws IOException {
        //  ValueRange updates = new ValueRange();
        //TODO: Update Not Working Currently
        //TODO: Add checks/flags for update vs add
        if (Constants.UPDATE) {
            response.setValues(Collections.singletonList(
                    Arrays.asList(clanNameMapRead)));
            UpdateValuesResponse result = service.spreadsheets().values()
                    .update(Constants.SHEET_ID, Constants.SHEET_RANGE, response)
                    .setValueInputOption("RAW")
                    .execute();
            System.out.println("UPDATE SUCCESS");
        } else {
            ValueRange appendBody = new ValueRange()
                    .setValues(Arrays.asList(
                            //TODO: Replace with variables from runelite
                            Arrays.asList("TESTAPPENDName", "asjdlkasjlkdjalksjhjd")));
            AppendValuesResponse appendResult = service.spreadsheets().values()
                    .append(Constants.SHEET_ID, Constants.SHEET_RANGE, appendBody)
                    .setValueInputOption("RAW")
                    .setInsertDataOption("INSERT_ROWS")
                    .setIncludeValuesInResponse(true)
                    .execute();
            System.out.println("APPEND SUCCESS");
        }
    }
}