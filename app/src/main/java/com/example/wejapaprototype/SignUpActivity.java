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

import com.example.wejapaprototype.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import static android.text.TextUtils.indexOf;
import static android.text.TextUtils.isEmpty;

public class SignUpActivity extends AppCompatActivity
{
    private EditText emailTextView, passwordTextView, confirmPasswordTextView;
    private Button signUpButton;
    private final String LOG_TAG = "SignUpActivity";
    private final String VALID_DOMAIN = "gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        emailTextView = findViewById(R.id.signupActivity_email_input);
        passwordTextView = findViewById(R.id.signupActivity_password_input);
        confirmPasswordTextView = findViewById(R.id.confirm_password_input);
        signUpButton = findViewById(R.id.signupActivity_signup_button);

        signUpButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                registerNewUser();
            }
        });
    }

    private void sendVerificationEmail()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.sendEmailVerification().addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            showToast("Sent verification email to user with ID " + FirebaseAuth.getInstance().getUid());
                        } else {
                            showToast("Couldn't send verification email");
                        }
                    }
                }
        );
    }

    private void registerNewUser()
    {
        String extractedEmail = emailTextView.getText().toString();
        String extractedPassword = passwordTextView.getText().toString();
        String secondExtractedPassword = confirmPasswordTextView.getText().toString();

        if (!isEmpty(extractedEmail) && !isEmpty(extractedPassword) && !isEmpty(secondExtractedPassword))
        {
            if (hasTheRightDomain(extractedEmail))
            {
                if(stringsAreTheSame(extractedPassword, secondExtractedPassword))
                {
                    createNewUser(extractedEmail, extractedPassword);
                } else {
                    showToast("Passwords do not match");
                }
            } else {
                showToast("Enter a valid email");
            }
        } else {
            showToast("You have to enter all fields");
        }
    }

    private boolean hasTheRightDomain(String email)
    {
        String domainExtractedFromEmail = email.substring(email.indexOf("@")+1).toLowerCase();
        return domainExtractedFromEmail.equals(VALID_DOMAIN);
    }

    private void createNewUser(final String email, String password)
    {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(
            new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if(task.isComplete())
                    {
                        Log.d(LOG_TAG, "User with user ID " + FirebaseAuth.getInstance().getUid() + " has been created");
                        sendVerificationEmail();

                        User user = new User();
                        user.setName(email.substring(0, email.indexOf("@")));
                        user.setPhone("1");
                        user.setProfile_image("");
                        user.setSecurity_level("1");
                        user.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

                        FirebaseDatabase.getInstance().getReference()
                                .child(String.valueOf(R.string.dbnode_users))
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                FirebaseAuth.getInstance().signOut();
                                goToLoginActivity();
                                showToast("Check your inbox for a verification email");
                            }
                        }).addOnFailureListener(new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                FirebaseAuth.getInstance().signOut();
                                goToLoginActivity();
                                showToast("Something went wrong");
                            }
                        });
                    }
                }
            }
        );
    }

    private void goToLoginActivity()
    {
        Intent loginActivityIntent = new Intent(this, LoginActivity.class);
        startActivity(loginActivityIntent);
    }

    private void showToast(String toastMessage)
    {
        Toast.makeText(SignUpActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
    }

    private boolean stringsAreTheSame(String firstString, String secondString)
    {
        return firstString.equals(secondString);
    }
}