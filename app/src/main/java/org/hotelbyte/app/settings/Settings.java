package org.hotelbyte.app.settings;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Settings {

    public static boolean walletBeingGenerated = false;
    public static boolean walletBeingDeleted = false;
    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd. MMMM yyyy, HH:mm", Locale.getDefault());
}
