package com.example.wejapaprototype;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity
{
    private EditText emailTextField, passwordTextField;
    private Button loginButton, signUpTextButton, resendEmailButton;
    private final String LOG_TAG = "LoginActivity";

    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailTextField = findViewById(R.id.loginActivity_email_input);
        passwordTextField = findViewById(R.id.loginActivity_password_input);
        loginButton = findViewById(R.id.login_button);
        signUpTextButton = findViewById(R.id.loginActivity_signup_button);
        resendEmailButton = findViewById(R.id.resend_verification_email_button);

        setupFirebaseAuth();

        signUpTextButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                goToSignUpActivity();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                logUserIn();
            }
        });

        resendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showResendEmailDialog();
            }
        });
    }

    private void showResendEmailDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View inflatedView = inflater.inflate(R.layout.resend_verification_email, null);

        final EditText reEnterEmailTextView = inflatedView.findViewById(R.id.dialog_email_input);
        final EditText reEnterPasswordTextView = inflatedView.findViewById(R.id.dialog_password_input);

        builder.setView(inflatedView);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                authenticateAndResendEmail(reEnterEmailTextView.getText().toString(), reEnterPasswordTextView.getText().toString());
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void authenticateAndResendEmail(String email, String password)
    {
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isComplete())
                {
                    log("User " + FirebaseAuth.getInstance().getUid() + " re-authenticated successfully");
                    resendVerificationEmail();
                    FirebaseAuth.getInstance().signOut();
                }
            }
        });
    }

    private void resendVerificationEmail()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    showToast("Sent verification email to user with ID " + FirebaseAuth.getInstance().getUid());
                } else {
                    showToast("Couldn't send verification email");
                }
            }
        });
    }

    private void setupFirebaseAuth()
    {
        authStateListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user!= null)
                {
                    if (user.isEmailVerified())
                    {
                        log("onAuthStateChanged: signed_in: " + user.getUid());
                        showToast("Authenticated " + user.getEmail());
                    } else {
                        log("onAuthStateChanged: signed_out");
                    }
                } else {
                    log("onAuthStateChanged: signed_out");
                }
            }
        };
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (authStateListener != null)
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
    }

    private void logUserIn()
    {
        String email = emailTextField.getText().toString();
        final String password = passwordTextField.getText().toString();

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if (task.isComplete())
                {
                    log("User logged in successfully");
                    String userPassword = password;
                    goToLoggedInActivity();
                }
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                log("Login failed");
            }
        });
    }

    private void goToLoggedInActivity()
    {
        Intent loggedInActivityIntent = new Intent(this, LoggedInActivity.class);
        startActivity(loggedInActivityIntent);
        finish();
    }

    private void goToSignUpActivity()
    {
        Intent signUpActivityIntent = new Intent(this, SignUpActivity.class);
        startActivity(signUpActivityIntent);
    }

    private void log(String logMessage)
    {
        Log.d(LOG_TAG, logMessage);
    }

    private void showToast(String toastMessage)
    {
        Toast.makeText(LoginActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
    }

}