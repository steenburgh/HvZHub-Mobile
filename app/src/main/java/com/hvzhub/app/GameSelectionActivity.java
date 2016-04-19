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
import android.support.design.widget.Snackbar;
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
import com.hvzhub.app.API.model.Chapters.ChapterInfo;
import com.hvzhub.app.API.model.Games.Game;
import com.hvzhub.app.API.model.Games.Record;
import com.hvzhub.app.API.model.Games.RecordContainer;
import com.hvzhub.app.API.model.Status;
import com.hvzhub.app.API.model.Uuid;
import com.hvzhub.app.Prefs.GamePrefs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameSelectionActivity extends AppCompatActivity {
    public static final String TAG = "GameSelectionActivity";

    ListView listView;
    ArrayAdapter<Game> adapter;
    List<Game> gameList;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_selection);

        listView = (ListView) findViewById(R.id.list_view);
        gameList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, gameList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                joinGame(gameList.get(position));
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progress);

        // Show the close button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getGameList();
    }

    private void getGameList() {
        if (!NetworkUtils.networkIsAvailable(this)) {
            AlertDialog.Builder b = new AlertDialog.Builder(GameSelectionActivity.this);
            b.setTitle(getString(R.string.network_not_available))
                    .setMessage(getString(R.string.network_not_available_hint))
                    .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getGameList();
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
            String chapterUrl = getSharedPreferences(GamePrefs.NAME, MODE_PRIVATE).getString(GamePrefs.PREFS_CHAPTER_URL, null);
            Call<ChapterInfo> call = client.getChapterInfo(
                    new Uuid(uuid),
                    chapterUrl
            );
            call.enqueue(new Callback<ChapterInfo>() {
                @Override
                public void onResponse(Call<ChapterInfo> call, Response<ChapterInfo> response) {
                    showProgress(false);
                    if (response.isSuccessful()) {
                        if (response.body().games == null || response.body().games.isEmpty()) {
                            // No games found for the chapter
                            Toast.makeText(GameSelectionActivity.this, R.string.no_games_found, Snackbar.LENGTH_SHORT).show();
                            onBackPressed();
                            finish();
                            return;
                        }
                        gameList.clear();
                        gameList.addAll(response.body().games);
                        Collections.sort(gameList); // Ensure the list is sorted. See Game.compareTo() for more info
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
                            Toast t = Toast.makeText(GameSelectionActivity.this, R.string.unexpected_response, Toast.LENGTH_LONG);
                            t.show();
                            goBackToChapterSelection();
                        } else {
                            AlertDialog.Builder b = new AlertDialog.Builder(GameSelectionActivity.this);
                            b.setTitle(getString(R.string.unexpected_response))
                                    .setMessage(getString(R.string.unexpected_response_hint))
                                    .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            getGameList();
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
                public void onFailure(Call<ChapterInfo> call, Throwable t) {
                    showProgress(false);

                    AlertDialog.Builder b = new AlertDialog.Builder(GameSelectionActivity.this);
                    b.setTitle(getString(R.string.generic_connection_error))
                            .setMessage(getString(R.string.generic_connection_error_hint))
                            .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getGameList();
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

    private void joinGame(final Game game) {
        showProgress(true);
        HvZHubClient client = API.getInstance(getApplicationContext()).getHvZHubClient();
        String uuid = getSharedPreferences(GamePrefs.NAME, MODE_PRIVATE).getString(GamePrefs.PREFS_SESSION_ID, null);
        Call<Status> call = client.joinGame(game.id, new Uuid(uuid));
        call.enqueue(new Callback<Status>() {
            @Override
            public void onResponse(Call<Status> call, Response<Status> response) {
                if (response.isSuccessful()) {
                    SharedPreferences.Editor prefs = getSharedPreferences(GamePrefs.NAME, Context.MODE_PRIVATE).edit();
                    prefs.putInt(GamePrefs.PREFS_GAME_ID, game.id);
                    prefs.apply();
                    updateIsHuman();
                } else {
                    showProgress(false);
                    APIError apiError = ErrorUtils.parseError(response);
                    String err = apiError.error.toLowerCase();
                    if (err.equals(getString(R.string.invalid_session_id))) {
                        // This should never happen, but if it does, log the user out so they can obtain a new sessionID
                        Toast t = Toast.makeText(GameSelectionActivity.this, R.string.unexpected_response, Toast.LENGTH_LONG);
                        t.show();
                        goBackToChapterSelection();
                    } else {
                        AlertDialog.Builder b = new AlertDialog.Builder(GameSelectionActivity.this);
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
            public void onFailure(Call<Status> call, Throwable t) {
                showProgress(false);
                AlertDialog.Builder b = new AlertDialog.Builder(GameSelectionActivity.this);
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

    public void updateIsHuman() {
        HvZHubClient client = API.getInstance(getApplicationContext()).getHvZHubClient();
        SharedPreferences prefs = getSharedPreferences(GamePrefs.NAME, MODE_PRIVATE);
        String uuid = prefs.getString(GamePrefs.PREFS_SESSION_ID, null);
        int gameId = prefs.getInt(GamePrefs.PREFS_GAME_ID, -1);
        Call<RecordContainer> call = client.getMyRecord(new Uuid(uuid), gameId);
        call.enqueue(new Callback<RecordContainer>() {
            @Override
            public void onResponse(Call<RecordContainer> call, Response<RecordContainer> response) {
                showProgress(false);
                if (response.isSuccessful()) {
                    Record r = response.body().record;
                    SharedPreferences.Editor editor = getSharedPreferences(GamePrefs.NAME, MODE_PRIVATE).edit();
                    editor.putBoolean(GamePrefs.PREFS_IS_HUMAN, r.status == Record.HUMAN);
                    editor.apply();

                    Intent i = new Intent(GameSelectionActivity.this, GameActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                } else {
                    AlertDialog.Builder b = new AlertDialog.Builder(GameSelectionActivity.this);
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
            public void onFailure(Call<RecordContainer> call, Throwable t) {
                showProgress(false);
                AlertDialog.Builder b = new AlertDialog.Builder(GameSelectionActivity.this);
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
        goBackToChapterSelection();
    }

    private void goBackToChapterSelection() {
        // Clear *all* GamePrefs
        SharedPreferences.Editor editor = getSharedPreferences(GamePrefs.NAME, Context.MODE_PRIVATE).edit();
        editor.putString(GamePrefs.PREFS_CHAPTER_URL, null);
        editor.apply();

        // Show the login screen again
        Intent i = new Intent(this, ChapterSelectionActivity.class);
        startActivity(i);
        finish();
    }
}
