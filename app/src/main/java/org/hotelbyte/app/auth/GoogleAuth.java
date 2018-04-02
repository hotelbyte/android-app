package org.hotelbyte.app.auth;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

/**
 * Created by lexfaraday
 */
public class GoogleAuth {

    public static GoogleSignInOptions buildSignInOptions(String requestToken) {
        // Configure Google Sign In
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(requestToken)
                .requestEmail()
                .build();
    }
}
