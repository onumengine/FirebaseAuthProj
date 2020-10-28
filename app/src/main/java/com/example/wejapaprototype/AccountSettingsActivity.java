package com.example.wejapaprototype;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.text.TextUtils.isEmpty;

public class AccountSettingsActivity extends AppCompatActivity
{
    private final String LOG_TAG = "AccountSettingsActivity";
    private final String VALID_DOMAIN = "gmail.com";

    private EditText newUsernameTextInput, newEmailTextInput, newPhoneNumberTextInput, passwordTextInput;
    private Button confirmButton;

    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        newUsernameTextInput = findViewById(R.id.new_username_textView);
        newEmailTextInput = findViewById(R.id.new_email_textView);
        newPhoneNumberTextInput = findViewById(R.id.new_phone_number_textView);
        passwordTextInput = findViewById(R.id.password_textView);
        confirmButton = findViewById(R.id.apply_changes_button);

        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                applyChanges();
            }
        });
    }

    private void applyChanges()
    {
        if (!isEmpty(newUsernameTextInput.getText().toString()))
            setNewUsername();
        if (!isEmpty(newEmailTextInput.getText().toString()))
            verifyAndUpdateEmail();
        if (!isEmpty(newPhoneNumberTextInput.getText().toString()))
            setNewPhoneNumber();
    }

    private void setNewUsername()
    {
        DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference();
        dbReference.child(String.valueOf(R.string.dbnode_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(String.valueOf(R.string.dbfield_name)).setValue(newUsernameTextInput.getText().toString());
    }

    private void verifyAndUpdateEmail()
    {
        if (!(newEmailTextInput.getText().toString()).equals(FirebaseAuth.getInstance().getCurrentUser().getEmail()))
        {
            if (hasValidDomain(newEmailTextInput.getText().toString()))
            {
                changeEmail(newEmailTextInput.getText().toString());
            } else {
                showToast("You must enter a gmail address");
            }
        } else {
            showToast("You have to enter a new email");
        }

    }

    private boolean hasValidDomain(String email)
    {
        String extractedDomain = email.substring(email.indexOf("@")+1);
        return extractedDomain.equals(VALID_DOMAIN);
    }

    private void changeEmail(final String email)
    {
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>()
        {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task)
            {
                if (task.isSuccessful())
                {
                    if (task.getResult().getSignInMethods().size() == 1) {
                        showToast("This email is already in use");
                    } else {
                        FirebaseAuth.getInstance().getCurrentUser().updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if (task.isSuccessful()) {
                                    sendVerificationEmail();
                                    FirebaseAuth.getInstance().signOut();
                                    goToLoggendInActivity();
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void goToLoginActivity()
    {
        Intent loginActivityIntent = new Intent(this, LoginActivity.class);
        startActivity(loginActivityIntent);
    }

    private void goToLoggendInActivity()
    {
        Intent loggedInActivityIntent = new Intent(this, LoggedInActivity.class);
        startActivity(loggedInActivityIntent);
    }

    private void sendVerificationEmail()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.sendEmailVerification();
    }

    private void setNewPhoneNumber()
    {
        DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference();
        dbReference.child(String.valueOf(R.string.dbnode_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(String.valueOf(R.string.dbfield_phone))
                .setValue(newPhoneNumberTextInput.getText().toString());
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        checkIfCurrentUserIsAuthenticated();
    }

    private void checkIfCurrentUserIsAuthenticated()
    {
        if (currentUser == null)
        {
            log("Current user is not authenticated. Signing out now");
            FirebaseAuth.getInstance().signOut();
            kickUserToLoginActivity();
        }
        else
        {
            log("User with user ID: " + currentUser.getUid() + " is signed in");
        }
    }

    private void kickUserToLoginActivity()
    {
        Intent loginActivityIntent = new Intent(this, LoginActivity.class);
        loginActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginActivityIntent);
    }

    private void log(String logMessage)
    {
        Log.d(LOG_TAG, logMessage);
    }

    private void showToast(String toastMessage)
    {
        Toast.makeText(AccountSettingsActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
    }
}