package com.cmn;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.util.Collections;
import java.util.List;

public class Constants {
    public static final String SHEET_ID = "1IZKSdin6ovX1jiknsIpbOkrP5qwUeWKrWMM4sbOGPPE";
    public static final boolean WRITE = true;
    public static final boolean UPDATE = false;
    public static final String SHEET_RANGE = "ClanNicknames!A1:B997";
    public static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    public static final String APPLICATION_NAME = "Clan Member Nickname";
    public static final String TOKENS_DIRECTORY_PATH = "tokens";
    public static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    public static final List<String> SCOPES =
            Collections.singletonList(SheetsScopes.SPREADSHEETS);

}
