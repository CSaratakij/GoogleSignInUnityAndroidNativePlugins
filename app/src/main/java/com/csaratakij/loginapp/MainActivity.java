package com.csaratakij.loginapp;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import com.csaratakij.googlesignin.GoogleSignInActivity;
import com.csaratakij.googlesignin.GoogleSignInResult;
import com.csaratakij.googlesignin.OneTapSignInActivity;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnSignIn = findViewById(R.id.btnSignIn);
        Button btnOneTapSignIn = findViewById(R.id.btnOneTapSignIn);
        Button btnSignOut = findViewById(R.id.btnSignOut);

        btnSignIn.setOnClickListener(v -> {
            SignIn(getString(R.string.client_id));
        });

        btnOneTapSignIn.setOnClickListener(v -> {
            OneTapSignIn(getString(R.string.client_id));
        });

        btnSignOut.setOnClickListener(v -> {
            SignOut();
        });
    }

    private void onSignInResult(GoogleSignInResult result)
    {
        Log.w(TAG, result.toString());
    }

    private void SignIn(String clientId)
    {
        if (!GoogleSignInActivity.IsInitialize())
        {
            GoogleSignInActivity.Initialize(this, this::onSignInResult);
        }

        GoogleSignInActivity.SignIn(clientId);
    }

    private void OneTapSignIn(String clientId)
    {
        if (!OneTapSignInActivity.IsInitialize())
        {
            OneTapSignInActivity.Initialize(this, this::onSignInResult);
        }

        OneTapSignInActivity.SignIn(clientId);
    }

    private void SignOut()
    {
        if (GoogleSignInActivity.IsInitialize())
        {
            GoogleSignInActivity.SignOut();
        }

        if (OneTapSignInActivity.IsInitialize())
        {
            OneTapSignInActivity.SignOut();
        }

        Log.w(TAG, "Called sign-out");
    }
}