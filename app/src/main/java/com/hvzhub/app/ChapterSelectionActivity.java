package com.hvzhub.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.hvzhub.app.API.API;
import com.hvzhub.app.API.ErrorUtils;
import com.hvzhub.app.API.HvZHubClient;
import com.hvzhub.app.API.NetworkUtils;
import com.hvzhub.app.API.model.APIError;
import com.hvzhub.app.API.model.Chapters.Chapter;
import com.hvzhub.app.API.model.Chapters.ChapterListContainer;
import com.hvzhub.app.API.model.Status;
import com.hvzhub.app.API.model.Uuid;
import com.hvzhub.app.Prefs.GamePrefs;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChapterSelectionActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<Chapter> adapter;
    List<Chapter> chapterList;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_selection);

        listView = (ListView) findViewById(R.id.list_view);
        chapterList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, chapterList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                joinChapter(chapterList.get(position));
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progress);

        // Show the close button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white);
        }

        getChapterList();
    }

    private void getChapterList() {
        if (!NetworkUtils.networkIsAvailable(this)) {
            AlertDialog.Builder b = new AlertDialog.Builder(ChapterSelectionActivity.this);
            b.setTitle(getString(R.string.network_not_available))
                    .setMessage(getString(R.string.network_not_available_hint))
                    .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                             getChapterList();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        } else {
            showProgress(true);
            HvZHubClient client = API.getInstance(getApplicationContext()).getHvZHubClient();

            String uuid = getSharedPreferences(GamePrefs.NAME, MODE_PRIVATE).getString(GamePrefs.PREFS_SESSION_ID, null);
            Call<ChapterListContainer> call = client.getChapters(new Uuid(uuid));
            call.enqueue(new Callback<ChapterListContainer>() {
                @Override
                public void onResponse(Call<ChapterListContainer> call, Response<ChapterListContainer> response) {
                    showProgress(false);
                    if (response.isSuccessful()) {
                        chapterList.clear();
                        chapterList.addAll(response.body().chapters);
                        adapter.notifyDataSetChanged();
                    } else {
                        APIError apiError = ErrorUtils.parseError(response);
                        String err = apiError.error.toLowerCase();
                        if (err.contains(getString(R.string.invalid_session_id))) {
                            // Notify the parent activity that the user should be logged out
                            // Don't bother stopping the loading animation
                            logout();
                        } else {
                            AlertDialog.Builder b = new AlertDialog.Builder(ChapterSelectionActivity.this);
                            b.setTitle(getString(R.string.unexpected_response))
                                    .setMessage(getString(R.string.unexpected_response_hint))
                                    .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            getChapterList();
                                        }
                                    })
                                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ChapterListContainer> call, Throwable t) {
                    showProgress(false);

                    AlertDialog.Builder b = new AlertDialog.Builder(ChapterSelectionActivity.this);
                    b.setTitle(getString(R.string.generic_connection_error))
                            .setMessage(getString(R.string.generic_connection_error_hint))
                            .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getChapterList();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
                }
            });

        }
    }

    private void joinChapter(final Chapter chapter) {
        showProgress(true);
        HvZHubClient client = API.getInstance(getApplicationContext()).getHvZHubClient();
        String uuid = getSharedPreferences(GamePrefs.NAME, MODE_PRIVATE).getString(GamePrefs.PREFS_SESSION_ID, null);
        Call<Status> call = client.joinChapter(chapter.getUrl(), new Uuid(uuid));
        call.enqueue(new Callback<Status>() {
            @Override
            public void onResponse(Call<Status> call, Response<Status> response) {
                showProgress(false);
                if (response.isSuccessful()) {
                    SharedPreferences.Editor prefs = getSharedPreferences(GamePrefs.NAME, Context.MODE_PRIVATE).edit();
                    prefs.putString(GamePrefs.PREFS_CHAPTER_URL, chapter.getUrl());
                    prefs.apply();

                    Intent i = new Intent(ChapterSelectionActivity.this, GameSelectionActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    APIError apiError = ErrorUtils.parseError(response);
                    String err;
                    if (apiError.error == null) {
                        err = "";
                    } else {
                        err = apiError.error.toLowerCase();
                    }
                    if (err.equals(getString(R.string.invalid_session_id))) {
                        // This should never happen, but if it does, log the user out so they can obtain a new sessionID
                        Toast t = Toast.makeText(ChapterSelectionActivity.this, R.string.unexpected_response, Toast.LENGTH_LONG);
                        t.show();
                        logout();
                    } else {
                        AlertDialog.Builder b = new AlertDialog.Builder(ChapterSelectionActivity.this);
                        b.setTitle(getString(R.string.unexpected_response))
                                .setMessage(getString(R.string.unexpected_response_hint))
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Do nothing
                                    }
                                })
                                .show();
                        Log.i("Join Chapter Error", err);
                    }

                }
            }

            @Override
            public void onFailure(Call<Status> call, Throwable t) {
                showProgress(false);
                AlertDialog.Builder b = new AlertDialog.Builder(ChapterSelectionActivity.this);
                b.setTitle(getString(R.string.generic_connection_error))
                        .setMessage(getString(R.string.generic_connection_error_hint))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        })
                        .show();
                Log.d("Error", t.getMessage());
            }
        });
    }

    /**
     * Shows the progress UI and hides the selection form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            listView.setVisibility(show ? View.GONE : View.VISIBLE);
            listView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    listView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            progressBar.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            listView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        logout();
    }

    private void logout() {
        // Clear *all* GamePrefs
        SharedPreferences.Editor editor = getSharedPreferences(GamePrefs.NAME, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        // Show the login screen again
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }
}
