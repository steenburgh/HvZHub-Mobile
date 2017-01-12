package com.hvzhub.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hvzhub.app.API.API;
import com.hvzhub.app.API.ErrorUtils;
import com.hvzhub.app.API.HvZHubClient;
import com.hvzhub.app.API.NetworkUtils;
import com.hvzhub.app.API.model.APIError;
import com.hvzhub.app.API.model.CurrentUser;
import com.hvzhub.app.API.model.Login.LoginRequest;
import com.hvzhub.app.API.model.Login.Session;
import com.hvzhub.app.API.model.Uuid;
import com.hvzhub.app.Prefs.GamePrefs;
import com.hvzhub.app.Prefs.LoginPrefs;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    public static final String TAG = "LoginActivity";
    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Setup the default user settings if they haven't already been set
        PreferenceManager.setDefaultValues(this, R.xml.pref_notification, false);

        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        // Pushing enter in the password box will attempt a login
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

        String lastEmail = getSharedPreferences(LoginPrefs.NAME, MODE_PRIVATE).getString(LoginPrefs.SAVED_EMAIL, null);
        if (lastEmail != null) {
            mEmailView.setText(lastEmail);
            mPasswordView.requestFocus();
        }

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.progress);
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

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            if (mEmailView.getText().toString().toLowerCase().equals(getString(R.string.ayy))) {
                mEmailView.setError(getString(R.string.lmao));
            } else {
                mEmailView.setError(getString(R.string.error_invalid_email));
            }
            focusView = mEmailView;
            cancel = true;
        }
        if (mPasswordView.getText().toString().toLowerCase().equals("god")) {
            Snackbar sb = Snackbar.make(mPasswordView, R.string.hack_the_planet, Snackbar.LENGTH_SHORT);
            View view = sb.getView();
            TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
            tv.setTextColor(Color.WHITE);
            sb.show();
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else if (!NetworkUtils.networkIsAvailable(this)) {
            AlertDialog.Builder b = new AlertDialog.Builder(LoginActivity.this);
            b.setTitle(getString(R.string.network_not_available))
                    .setMessage(getString(R.string.network_not_available_hint))
                    .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            attemptLogin();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing
                        }
                    })
                    .show();
        } else {
            showProgress(true);

            // Save the user's email for the next time they get logged out
            SharedPreferences.Editor editor = getSharedPreferences(LoginPrefs.NAME, MODE_PRIVATE).edit();
            editor.putString(LoginPrefs.SAVED_EMAIL, email);
            editor.apply();

            HvZHubClient client = API.getInstance(getApplicationContext()).getHvZHubClient();
            LoginRequest lr = new LoginRequest(email, password, true);
            Call<Session> call = client.login(lr);
            call.enqueue(new Callback<Session>() {
                @Override
                public void onResponse(Call<Session> call, Response<Session> response) {

                    if (response.isSuccessful()) {
                        Session s = response.body();
                        SessionManager.getInstance().createSession(s);

                        getUserId();
                    } else {
                        showProgress(false);
                        APIError apiError = ErrorUtils.parseError(response);
                        String err = apiError.error.toLowerCase();
                        if (err.contains("email")) {
                            mEmailView.setError(getString(R.string.error_invalid_email));
                            mEmailView.requestFocus();
                        } else if (err.contains("password")) {
                            mPasswordView.setError(getString(R.string.error_incorrect_password));
                            mPasswordView.requestFocus();
                        } else {
                            AlertDialog.Builder b = new AlertDialog.Builder(LoginActivity.this);
                            b.setTitle(getString(R.string.unexpected_response))
                                    .setMessage(getString(R.string.unexpected_response_hint))
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Do nothing
                                        }
                                    })
                                    .show();
                        }

                    }
                }

                @Override
                public void onFailure(Call<Session> call, Throwable t) {
                    showProgress(false);
                    AlertDialog.Builder b = new AlertDialog.Builder(LoginActivity.this);
                    b.setTitle(getString(R.string.generic_connection_error))
                            .setMessage(getString(R.string.generic_connection_error_hint))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Do nothing
                                }
                            })
                            .show();
                }
            });
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private void getUserId() {
        HvZHubClient client = API.getInstance(getApplicationContext()).getHvZHubClient();
        Call<CurrentUser> call = client.getCurrentUser(
                SessionManager.getInstance().getSessionUUID()
        );
        call.enqueue(new Callback<CurrentUser>() {
            @Override
            public void onResponse(Call<CurrentUser> call, Response<CurrentUser> response) {
                if (response.isSuccessful()) {
                    finish();

                    int userId = response.body().id;
                    SharedPreferences.Editor prefs = getSharedPreferences(GamePrefs.NAME, Context.MODE_PRIVATE).edit();
                    prefs.putInt(GamePrefs.PREFS_USER_ID, userId);
                    prefs.apply();

                    String chapterUrl = getSharedPreferences(GamePrefs.NAME, MODE_PRIVATE).getString(GamePrefs.PREFS_CHAPTER_URL, null);
                    if (chapterUrl == null) {
                        Intent intent = new Intent(LoginActivity.this, ChapterSelectionActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(LoginActivity.this, GameActivity.class);
                        startActivity(intent);
                    }
                } else {
                    AlertDialog.Builder b = new AlertDialog.Builder(LoginActivity.this);
                    b.setTitle(getString(R.string.unexpected_response))
                            .setMessage(getString(R.string.unexpected_response_hint))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Do nothing
                                }
                            })
                            .show();
                }
            }

            @Override
            public void onFailure(Call<CurrentUser> call, Throwable t) {
                showProgress(false);
                AlertDialog.Builder b = new AlertDialog.Builder(LoginActivity.this);
                b.setTitle(getString(R.string.generic_connection_error))
                        .setMessage(getString(R.string.generic_connection_error_hint))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        })
                        .show();
            }
        });
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

