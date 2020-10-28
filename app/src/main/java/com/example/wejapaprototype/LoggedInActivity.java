package com.example.wejapaprototype;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.wejapaprototype.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoggedInActivity extends AppCompatActivity
{
    private final String LOG_TAG = "LoggedInActivity";
    private TextView usernameTextView, emailTextView, photoUrlTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        usernameTextView = findViewById(R.id.profile_username_textView);
        emailTextView = findViewById(R.id.profile_email_textView);
        photoUrlTextView = findViewById(R.id.profile_url_textView);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        emailTextView.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        checkIfCurrentUserHasAnAccount();
        displayUserInfo();
    }

    private void displayUserInfo()
    {
        DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference();

        Query query = dbReference.child("users").orderByKey().equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for (DataSnapshot singleSnapshot : snapshot.getChildren())
                {
                    User user = singleSnapshot.getValue(User.class);

                    usernameTextView.setText(user.getName());
                    photoUrlTextView.setText(user.getProfile_image());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.logged_in_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout_action)
        {
            log("You clicked the logout action");
            logUserOut();
            gotoLoginActivity();
            return true;
        } else if (item.getItemId() == R.id.account_settings_action)
        {
            log("Moving to AccountSettingsActivity");
            goToAccountSettingsActivity();
            return true;
        } else {
            return false;
        }
    }

    private void checkIfCurrentUserHasAnAccount()
    {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null)
        {
            log("the current user isn't authenticated. Signing out now..");
            forceUnauthorizedUserOut();
        } else {
            log("Current user is authenticated. User ID: " + FirebaseAuth.getInstance().getCurrentUser().getUid());
        }
    }

    private void forceUnauthorizedUserOut()
    {
        FirebaseAuth.getInstance().signOut();
        Intent forcedLoginActivityIntent = new Intent(this, LoginActivity.class);
        forcedLoginActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(forcedLoginActivityIntent);
    }

    private void logUserOut()
    {
        FirebaseAuth.getInstance().signOut();
    }

    private void gotoLoginActivity()
    {
        Intent loginActivityIntent = new Intent(this, LoginActivity.class);
        startActivity(loginActivityIntent);
    }

    private void goToAccountSettingsActivity()
    {
        Intent accountSettingsActivityIntent = new Intent(this, AccountSettingsActivity.class);
        startActivity(accountSettingsActivityIntent);
    }

    private void log(String logMessage)
    {
        Log.d(LOG_TAG, logMessage);
    }
}