package com.csaratakij.googlesignin;

import androidx.annotation.NonNull;

public class GoogleSignInResult
{
    public static final GoogleSignInResult FailResult = new GoogleSignInResult()
    {{
        isSuccess = false;
        failStatusCode = -1;
        idToken = "";
        displayName = "";
        photoUrl = "";
    }};

    public boolean isSuccess;
    public int failStatusCode;
    public String idToken;
    public String displayName;
    public String photoUrl;

    @NonNull
    @Override
    public String toString()
    {
        return String.format("{ isSuccess: %b, failStatusCode: %d, idToken: %s, displayName: %s, photoUrl: %s }", isSuccess, failStatusCode, idToken, displayName, photoUrl);
    }
}
