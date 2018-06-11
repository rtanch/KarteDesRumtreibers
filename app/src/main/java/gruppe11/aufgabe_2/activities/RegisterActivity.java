package gruppe11.aufgabe_2.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
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
import gruppe11.aufgabe_2.map_items.LocalizableService;
import gruppe11.aufgabe_2.rest.Event;
import gruppe11.aufgabe_2.rest.RestEvent;
import gruppe11.aufgabe_2.rest.RestService;
import gruppe11.aufgabe_2.utility.Utility;


/**
 * A login screen that offers login via register/password.
 */
public class RegisterActivity extends AppCompatActivity {

    // UI references.
    private static final String DEBUGLOG_TAG = "DEBUGLOG_RA";
    private AutoCompleteTextView mRegisterView;
    private EditText mPasswordView;
    private EditText mPasswordConfirmationView;
    private View mProgressView;
    private View mLoginFormView;
    private View focusView = null;

    private RestService restService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Set up the login form.
        mRegisterView = (AutoCompleteTextView) findViewById(R.id.registration_register);

        mPasswordConfirmationView = (EditText) findViewById(R.id.registration_password_confirmation);

        mPasswordView = (EditText) findViewById(R.id.registration_password);
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

        Button mRegisterSignInButton = (Button) findViewById(R.id.registration_register_sign_in_button);
        mRegisterSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Utility.refreshEventBus(this);

        mLoginFormView = findViewById(R.id.registration_form);
        mProgressView = findViewById(R.id.registration_progress);

        Intent intent = getIntent();
        restService = (RestService) intent.getSerializableExtra("restService");
    }


    /**
     * Event Bus event receiver
     *
     * @param restEvent Change in RetrofitEvent class
     */
    public void onEvent(RestEvent restEvent) {
        Event event = restEvent.getEvent();
        Integer responseCode;

        switch (event) {
            case REGISTER:
                Log.d(DEBUGLOG_TAG, "RegisterActivity RegisterEvent received");
                responseCode = restEvent.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    //MSG: Registration successful
                    restService.loginClient(LocalizableService.getInstance().getClientLocalizable().getUsername(), LocalizableService.getInstance().getClientLocalizable().getPassword());
                    //TODO: add toast
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                } else if (responseCode == HttpURLConnection.HTTP_CONFLICT) {
                    showProgress(false);
                    mRegisterView.setError(getString(R.string.error_username_already_exists));
                    focusView = mRegisterView;
                } else {
                    //MSG: Registration failed
                    //TODO: add toast
                    Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
                    showProgress(false);
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
     * If there are form errors (invalid register, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mRegisterView.setError(null);
        mPasswordView.setError(null);
        mPasswordConfirmationView.setError(null);

        // Store values at the time of the login attempt.
        String username = mRegisterView.getText().toString();
        String password = mPasswordView.getText().toString();
        String passwordConfirmation = mPasswordConfirmationView.getText().toString();

        boolean cancel = false;

        // Validate Password
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password, passwordConfirmation)) {
            cancel = true;
        }

        // Validate Username
        if (TextUtils.isEmpty(username)) {
            mRegisterView.setError(getString(R.string.error_field_required));
            focusView = mRegisterView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mRegisterView.setError(getString(R.string.error_invalid_username));
            focusView = mRegisterView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            Log.d(DEBUGLOG_TAG, "Registration credentials locally validated.");
            //TODO: Replace Some Text with proper description for registration
            showProgress(true);
            if (restService != null) {
                restService.registerClient(username, password, "");
            }
        }
    }

    /**
     * Validates username with rest service
     *
     * @param username chosen username
     * @return true => username is unique
     * false => username already used
     */
    private boolean isUsernameValid(String username) {
        //TODO: local username validation (...)

        return true;
    }

    /**
     * Validates password strength LOCALLY - sets Error messages accordingly
     * Criteria: (1) length >= 8 (2) contains number/s (3) contains symbol/s (4) pws match
     *
     * @param password plain text password
     * @return true: Criteria met
     * false: Criteria not met
     */
    private boolean isPasswordValid(String password, String passwordConfirmation) {

        //length
        if (password.length() >= 8) {
            //numbers
            if (password.matches(".*\\d+.*")) {
                //non-alphanumeric characters
                if (password.matches(".*\\W+.*")) {
                    //match
                    if (password.equals(passwordConfirmation)) {
                        Log.d(DEBUGLOG_TAG, "PASSWORD VALID");
                        return true;
                    } else {
                        Log.d(DEBUGLOG_TAG, "PASSWORDS DIFFER");
                        mPasswordConfirmationView.setError(getString(R.string.error_invalid_password_match));
                        focusView = mPasswordConfirmationView;
                        return false;
                    }
                } else {
                    Log.d(DEBUGLOG_TAG, "PASSWORD DOES NOT CONTAIN SYMBOLS");
                    mPasswordView.setError(getString(R.string.error_invalid_password_symbol));
                    focusView = mPasswordView;
                    return false;
                }
            } else {
                Log.d(DEBUGLOG_TAG, "PASSWORD DOES NOT CONTAIN NUMBERS");
                mPasswordView.setError(getString(R.string.error_invalid_password_number));
                focusView = mPasswordView;
                return false;
            }
        } else {
            Log.d(DEBUGLOG_TAG, "PASSWORD LENGTH TOO SHORT");
            mPasswordView.setError(getString(R.string.error_invalid_password_length));
            focusView = mPasswordView;
            return false;
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