package com.csaratakij.googlesignin;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class GoogleSignInActivity extends AppCompatActivity
{
    private static final int REQ_RESULT = 2;
    private static boolean isInitialize = false;
    private static Activity parentActivity;
    private static GoogleSignInCallback signInCallback;
    private static GoogleSignInClient client;

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
        Intent intent = new Intent(parentActivity.getApplicationContext(), GoogleSignInActivity.class);

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
            GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(clientId)
                    .requestProfile()
                    .build();

            client = GoogleSignIn.getClient(this, options);
            Intent signInIntent = client.getSignInIntent();

            startActivityForResult(signInIntent, REQ_RESULT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != REQ_RESULT) {
            return;
        }

        switch (resultCode)
        {
            case RESULT_OK:
            {
                try {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    Log.d(TAG, "Legacy signin success");
                    signInCallback.onSignInResult(new GoogleSignInResult()
                    {{
                        isSuccess = true;
                        failStatusCode = 0;
                        idToken = account.getIdToken();
                        displayName = account.getDisplayName();
                        photoUrl = account.getPhotoUrl().toString();
                    }});
                    finish();
                }
                catch (ApiException e) {
                    Log.e(TAG, "Legacy signin failed : " + e.getMessage());
                    signInCallback.onSignInResult(GoogleSignInResult.FailResult);
                    finish();
                }
            }
            break;

            default:
            {
                Log.e(TAG, "Legacy signin failed : Unknown");
                signInCallback.onSignInResult(GoogleSignInResult.FailResult);
                finish();
            }
        }
    }
}
