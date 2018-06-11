package gruppe11.aufgabe_2.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import gruppe11.aufgabe_2.R;
import gruppe11.aufgabe_2.map_items.LocalizableService;
import gruppe11.aufgabe_2.rest.RestService;
import gruppe11.aufgabe_2.utility.Utility;

/**
 * Created by gruppe11 on 10/25/17.
 * <p>
 * class holds the function to configurate the name, password and description
 * of the user, uses a button to save the changes
 */
public class ConfigureAccountActivity extends AppCompatActivity {

    private static final String DEBUGLOG_TAG = "DEBUGLOG-CAA";
    private TextView configName;
    private TextView configPassword;
    private TextView configDescription;
    private static String PASSWORD_HASH = null;
    private static int MAX_PASSWORD_LENGTH = 32;

    private EditText mPasswordView;
    private EditText mPasswordConfirmationView;
    private View focusView;

    private AlertDialog alert2;

    private RestService restService = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_account);

        Intent intent = getIntent();
        restService = (RestService) intent.getSerializableExtra("restService");

        configName();
        configPassword();
        configDescription();
        saveChanges();
    }

    /**
     * methode allows to configurate your user-name in the Configure Account settings
     * click on the name, dialog opens to entry your new name
     */
    private void configName() {
        configName = (TextView) findViewById(R.id.textView_config_name);
        configName.setText(LocalizableService.getInstance().getClientLocalizable().getUsername());
        configName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder mBuilder = new AlertDialog.Builder(ConfigureAccountActivity.this);
                mBuilder.setTitle(R.string.dialog_choose_name);

                final EditText input = new EditText(ConfigureAccountActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});

                mBuilder.setView(input);

                mBuilder.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (input.getText().toString().isEmpty()) {
                            configName.setText("");

                        } else {
                            configName.setText(input.getText().toString());
                        }
                    }
                });

                mBuilder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                mBuilder.show();


            }
        });


    }

    /**
     * Methode to change your password in Configure Account settings
     * on click on the textview dialog opens, enter current password,
     * than type in new password and confirm, passwords are masked, default password set
     */

    private void configPassword() {
        configPassword = (TextView) findViewById(R.id.textView_config_password);

        configPassword.setText(R.string.mask_password);
        configPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(ConfigureAccountActivity.this);
                mBuilder.setTitle(R.string.current_password);


                final EditText passwordConfirmationInput = new EditText(ConfigureAccountActivity.this);
                passwordConfirmationInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordConfirmationInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_PASSWORD_LENGTH)});


                mBuilder.setView(passwordConfirmationInput);
                mBuilder.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (!Utility.hash(passwordConfirmationInput.getText().toString()).equals(LocalizableService.getInstance().getClientLocalizable().getPassword())) {
                            Toast.makeText(ConfigureAccountActivity.this, R.string.false_password, Toast.LENGTH_LONG).show();
                            Log.d(DEBUGLOG_TAG, "password hash false");


                        } else {
                            Toast.makeText(ConfigureAccountActivity.this, R.string.right_password, Toast.LENGTH_LONG).show();
                            Log.d(DEBUGLOG_TAG, "password hash true");

                            TextInputLayout.LayoutParams inputParams = new TextInputLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


                            TextInputLayout layoutTeIn1 = new TextInputLayout(ConfigureAccountActivity.this);
                            mPasswordView = new EditText(ConfigureAccountActivity.this);
                            mPasswordView.setHint(R.string.new_password);
                            mPasswordView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            layoutTeIn1.addView(mPasswordView, inputParams);

                            TextInputLayout layoutTeIn2 = new TextInputLayout(ConfigureAccountActivity.this);
                            mPasswordConfirmationView = new EditText(ConfigureAccountActivity.this);
                            mPasswordConfirmationView.setHint(R.string.new_password_confirm);
                            mPasswordConfirmationView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            layoutTeIn2.addView(mPasswordConfirmationView, inputParams);


                            LinearLayout linearLayout = new LinearLayout(ConfigureAccountActivity.this);
                            linearLayout.setOrientation(LinearLayout.VERTICAL);
                            linearLayout.setPadding(0, 100, 0, 0);
                            linearLayout.addView(layoutTeIn1);
                            linearLayout.addView(layoutTeIn2);

                            AlertDialog.Builder mBuilder2 = new AlertDialog.Builder(ConfigureAccountActivity.this);
                            mBuilder2.setTitle(R.string.new_password);
                            mBuilder2.setView(linearLayout);


                            mBuilder2.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });

                            mBuilder2.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            alert2 = mBuilder2.create();

                            alert2.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    Button button = alert2.getButton(DialogInterface.BUTTON_POSITIVE);
                                    button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (isPasswordValid(mPasswordView.getText().toString(), mPasswordConfirmationView.getText().toString())) {
                                                PASSWORD_HASH = Utility.hash(mPasswordView.getText().toString());
                                                alert2.cancel();
                                            } else {
                                                focusView.requestFocus();
                                            }
                                        }
                                    });
                                }
                            });

                            alert2.show();
                        }
                    }
                });

                mBuilder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                mBuilder.show();
            }
        });

    }

    /**
     * Methode to configurate the describtion of the user in Configure Account setting
     * on click Listener opens dialog to enter it
     */

    private void configDescription() {
        configDescription = (TextView) findViewById(R.id.textView_config_Description);
        configDescription.setText(LocalizableService.getInstance().getClientLocalizable().getDescription());
        configDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                AlertDialog.Builder mBuilder = new AlertDialog.Builder(ConfigureAccountActivity.this);
                mBuilder.setTitle(R.string.change_description);

                final EditText input = new EditText(ConfigureAccountActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});
                mBuilder.setView(input);

                mBuilder.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (input.getText().toString().isEmpty()) {
                            configDescription.setText("");

                        } else {
                            configDescription.setText(input.getText().toString());
                        }
                    }
                });

                mBuilder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                mBuilder.show();


            }
        });

    }

    /**
     * Methode with Button in Configure Account settings, to save the changes of name, password and
     * description
     */
    private void saveChanges() {

        FloatingActionButton saveConfig = (FloatingActionButton) findViewById(R.id.fab_saveConfig);
        saveConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (LocalizableService.getInstance().getClientLocalizable() != null) {
                    if (!configName.getText().toString().isEmpty()) {
                        LocalizableService.getInstance().getClientLocalizable().setUsername(configName.getText().toString());
                    }
                    if (PASSWORD_HASH != null) {
                        LocalizableService.getInstance().getClientLocalizable().setPassword(PASSWORD_HASH);
                    }

                    LocalizableService.getInstance().getClientLocalizable().setDescription(configDescription.getText().toString());


                } else {

                    Log.d(DEBUGLOG_TAG, "LocalizableService.getClientLocalizable() == null");
                }
                if (restService != null) {
                    restService.updateClientData();
                }
                finish();
            }
        });
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
                        Log.d("DEBUGLOG-RA:", "PASSWORD VALID");
                        return true;
                    } else {
                        Log.d("DEBUGLOG-RA:", "PASSWORDS DIFFER");
                        mPasswordConfirmationView.setError(getString(R.string.error_invalid_password_match));
                        focusView = mPasswordConfirmationView;
                        return false;
                    }
                } else {
                    Log.d("DEBUGLOG-RA:", "PASSWORD DOES NOT CONTAIN SYMBOLS");
                    mPasswordView.setError(getString(R.string.error_invalid_password_symbol));
                    focusView = mPasswordView;
                    return false;
                }
            } else {
                Log.d("DEBUGLOG-RA:", "PASSWORD DOES NOT CONTAIN NUMBERS");
                mPasswordView.setError(getString(R.string.error_invalid_password_number));
                focusView = mPasswordView;
                return false;
            }
        } else {
            Log.d("DEBUGLOG-RS:", "PASSWORD LENGTH TOO SHORT");
            mPasswordView.setError(getString(R.string.error_invalid_password_length));
            focusView = mPasswordView;
            return false;
        }
    }
}