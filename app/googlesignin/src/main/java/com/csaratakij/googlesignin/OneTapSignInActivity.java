package com.csaratakij.googlesignin;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;

public class OneTapSignInActivity extends AppCompatActivity
{
    private static final int REQ_RESULT = 3;
    private static boolean isInitialize = false;
    private static Activity parentActivity;
    private static GoogleSignInCallback signInCallback;
    private static SignInClient client;

    public static boolean IsInitialize()
    {
        return isInitialize;
    }

    public static void Initialize(Activity activity, GoogleSignInCallback callback)
    {
        signInCallback = callback;
        parentActivity = activity;
        isInitialize = true;
    }

    public static void SignIn(String clientId)
    {
        Intent intent = new Intent(parentActivity.getApplicationContext(), OneTapSignInActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("clientId", clientId);

        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        parentActivity.startActivity(intent);
    }

    public static void SignOut()
    {
        if (client != null)
        {
            client.signOut();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        String clientId = "";
        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            clientId = bundle.getString("clientId");
            //Log.w(TAG, "Got client id : " + clientId);
        }

        if (clientId.isEmpty()) {
            signInCallback.onSignInResult(GoogleSignInResult.FailResult);
            finish();
        }
        else {
            BeginSignInRequest request = BeginSignInRequest.builder()
                    .setGoogleIdTokenRequestOptions(
                            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                    .setSupported(true)
                                    .setServerClientId(clientId)
                                    .setFilterByAuthorizedAccounts(true)
                                    .build()
                    )
                    .setAutoSelectEnabled(false)
                    .build();

            client = Identity.getSignInClient(this);
            client.beginSignIn(request)
                    .addOnSuccessListener((result) -> {
                        try {
                            Log.d(TAG, "One Tap UI Success");
                            IntentSender intentSender = result.getPendingIntent().getIntentSender();
                            startIntentSenderForResult(intentSender, REQ_RESULT, null, 0, 0, 0, null);
                        } catch (IntentSender.SendIntentException e) {
                            Log.e(TAG, e.getLocalizedMessage());
                            signInCallback.onSignInResult(GoogleSignInResult.FailResult);
                            finish();
                        }
                    })
                    .addOnFailureListener((e) ->
                    {
                        Log.e(TAG, e.getLocalizedMessage());
                        signInCallback.onSignInResult(GoogleSignInResult.FailResult);
                        finish();
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != REQ_RESULT) {
            return;
        }

        if (data == null) {
            Log.e(TAG, "Failed to get result from intent");
            signInCallback.onSignInResult(GoogleSignInResult.FailResult);
            finish();
        }
        else {
            try {
                SignInCredential credential = client.getSignInCredentialFromIntent(data);
                signInCallback.onSignInResult(new GoogleSignInResult()
                {{
                        isSuccess = true;
                        failStatusCode = 0;
                        idToken = credential.getGoogleIdToken();
                        displayName = credential.getDisplayName();
                        photoUrl = credential.getProfilePictureUri().toString();
                }});
                finish();
            }
            catch (ApiException e) {
                switch (e.getStatusCode()) {
                    case CommonStatusCodes.CANCELED:
                    {
                        Log.e(TAG, "OneTap UI cancled");
                        // Don't re-prompt the user
                        signInCallback.onSignInResult(new GoogleSignInResult()
                        {{
                            isSuccess = false;
                            failStatusCode = CommonStatusCodes.CANCELED;
                        }});
                    }
                    break;

                    case CommonStatusCodes.NETWORK_ERROR:
                    {
                        Log.e(TAG, "OneTap UI encountered a network error");
                        // Try again or just ignore
                        signInCallback.onSignInResult(new GoogleSignInResult()
                        {{
                            isSuccess = false;
                            failStatusCode = CommonStatusCodes.NETWORK_ERROR;
                        }});
                    }
                    break;

                    default: {
                        Log.e(TAG, "OneTap UI Couldn't get credential from result : " + e.getLocalizedMessage());
                        signInCallback.onSignInResult(GoogleSignInResult.FailResult);
                    }
                    break;
                }

                finish();
            }
        }
    }
}
