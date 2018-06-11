package gruppe11.aufgabe_2.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpURLConnection;

import gruppe11.aufgabe_2.R;
import gruppe11.aufgabe_2.rest.Event;
import gruppe11.aufgabe_2.rest.RestEvent;
import gruppe11.aufgabe_2.rest.RestService;
import gruppe11.aufgabe_2.utility.Utility;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    // UI references.
    private static final String DEBUGLOG_TAG = "DEBUGLOG-LA";
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private RestService restService = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.login_username);

        mPasswordView = (EditText) findViewById(R.id.login_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.login_email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Utility.refreshEventBus(this);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        Intent intent = getIntent();
        restService = (RestService) intent.getSerializableExtra("restService");
    }


    /**
     * Event Bus event receiver for Login Event
     *
     * @param restEvent Change in LoginEvent class
     */
    public void onEvent(RestEvent restEvent) {

        Event event = restEvent.getEvent();
        Integer responseCode;

        switch (event) {
            case LOGIN:
                responseCode = restEvent.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {//200
                    //do nothing here - handled by receiver in MapsActivity
                } else if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {//403
                    Log.d(DEBUGLOG_TAG, "Authentication with Rest Service failed.");
                    showProgress(false);
                    Toast.makeText(this, "Authentication with Rest Service failed.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(DEBUGLOG_TAG, "Authentication with Rest Service failed");
                    showProgress(false);
                    Toast.makeText(this, "Authentication with Rest Service failed.", Toast.LENGTH_SHORT).show();
                }
                break;

            case COMMUNITY:
                responseCode = restEvent.getResponseCode();
                Log.d(DEBUGLOG_TAG, "LoginActivity COMMUNITYGPSDATA Event Received");
                if (responseCode == HttpURLConnection.HTTP_OK) {//200
                    showProgress(false);
                    finish();
                } else if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {//403
                    showProgress(false);
                } else {
                    showProgress(false);
                }
                break;

            case FAILURE:
                showProgress(false);
                Toast.makeText(this, "Connection failure", Toast.LENGTH_LONG).show();
                break;
        }

        Utility.refreshEventBus(this);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        showProgress(true);

        if (restService != null) {
            restService.loginClient(email, password);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}