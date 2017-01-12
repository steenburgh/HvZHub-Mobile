package com.hvzhub.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hvzhub.app.API.API;
import com.hvzhub.app.API.ErrorUtils;
import com.hvzhub.app.API.HvZHubClient;
import com.hvzhub.app.API.NetworkUtils;
import com.hvzhub.app.API.model.APIError;
import com.hvzhub.app.API.model.Chapters.ChapterInfo;
import com.hvzhub.app.API.model.Games.PlayerCount;
import com.hvzhub.app.API.model.Uuid;
import com.hvzhub.app.Prefs.GamePrefs;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private TextView humanCount;
    private TextView zombieCount;
    private TextView gameRules;
    private Snackbar sb;
    private boolean loading;

    protected Call<ChapterInfo> loadChapInfo;
    protected Call<PlayerCount> loadPlayerCount;

    private SwipeRefreshLayout swipeContainer;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle(getActivity().getString(R.string.home));
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        humanCount = (TextView) view.findViewById(R.id.humanNum);
        zombieCount = (TextView) view.findViewById(R.id.zombieNum);
        gameRules = (TextView) view.findViewById(R.id.rules);
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
               swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                       @Override
                        public void onRefresh() {
                                if (!loading) {
                                    refreshHome();
                                }
                            }
                    });

        int gameId = getActivity().getSharedPreferences(GamePrefs.NAME, Context.MODE_PRIVATE).getInt(GamePrefs.PREFS_GAME_ID, -1);

        HvZHubClient client = API.getInstance(getActivity()).getHvZHubClient();
        loadPlayerCount = client.numPlayers(
                SessionManager.getInstance().getSessionUUID(),
                gameId
        );
        loadPlayerCount.enqueue(new Callback<PlayerCount>() {
            @Override
            public void onResponse(Call<PlayerCount> call, Response<PlayerCount> response) {
                if (response.isSuccessful()) {
                    humanCount.setText(Integer.toString(response.body().humans));
                    zombieCount.setText(Integer.toString(response.body().zombies));
                } else {
                    APIError apiError = ErrorUtils.parseError(response);
                    String err;
                    String errorMessage;
                    if (apiError.error == null) {
                        err = "";
                    } else {
                        err = apiError.error.toLowerCase();
                    }
                    if (err.contains("invalid")) {
                        Toast.makeText(getActivity().getApplicationContext(), "Invalid Session ID. Logging Out...", Toast.LENGTH_SHORT);
                        SessionManager.getInstance().logout();
                    } else {
                        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());

                        b.setTitle(R.string.unexpected_response)
                                .setMessage(R.string.unexpected_response_hint)
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
            public void onFailure(Call<PlayerCount> call, Throwable t) {
                AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
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

        String chapterURL = getActivity().getSharedPreferences(GamePrefs.NAME, Context.MODE_PRIVATE).getString(GamePrefs.PREFS_CHAPTER_URL, null);
        loadChapInfo = client.getChapterInfo(
                SessionManager.getInstance().getSessionUUID(),
                chapterURL
        );
        loadChapInfo.enqueue(new Callback<ChapterInfo>() {
            @Override
            public void onResponse(Call<ChapterInfo> call, Response<ChapterInfo> response) {
                if (response.isSuccessful()) {
                    gameRules.setText(response.body().rules);
                }
                else{
                    APIError apiError = ErrorUtils.parseError(response);
                    String err = apiError.error.toLowerCase();
                    String errorMessage;
                    if (err.contains("invalid")) {
                        Toast.makeText(getActivity().getApplicationContext(), "Invalid Session ID. Logging Out...", Toast.LENGTH_SHORT);
                        SessionManager.getInstance().logout();
                    }
                    else {
                        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());

                        b.setTitle(R.string.unexpected_response)
                                .setMessage(R.string.unexpected_response_hint)
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
            public void onFailure(Call<ChapterInfo> call, Throwable t) {
                AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
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
        loading = false;
    }

    private void refreshHome(){
        loading = true;
        if (!NetworkUtils.networkIsAvailable(getActivity())) {
            swipeContainer.setRefreshing(false);

            loading = false;
            AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
            b.setTitle(getString(R.string.network_not_available))
                    .setMessage(getString(R.string.network_not_available_hint))
                    .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            refreshHome();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        } else {
            HvZHubClient client = API.getInstance(getActivity()).getHvZHubClient();
            int gameId = getActivity().getSharedPreferences(GamePrefs.NAME, Context.MODE_PRIVATE).getInt(GamePrefs.PREFS_GAME_ID, -1);
            String chapterURL = getActivity().getSharedPreferences(GamePrefs.NAME, Context.MODE_PRIVATE).getString(GamePrefs.PREFS_CHAPTER_URL, null);

            if (loadPlayerCount != null) {
                // Cancel the last call in case it is still in progress.
                loadPlayerCount.cancel();
            }
            loadPlayerCount = client.numPlayers(
                    SessionManager.getInstance().getSessionUUID(),
                    gameId
            );
            loadPlayerCount.enqueue(new Callback<PlayerCount>() {
                @Override
                public void onResponse(Call<PlayerCount> call, Response<PlayerCount> response) {

                        swipeContainer.setRefreshing(false);

                    if (response.isSuccessful()) {
                        humanCount.setText(Integer.toString(response.body().humans));
                        zombieCount.setText(Integer.toString(response.body().zombies));
                        loading = false;

                    } else {
                        swipeContainer.setRefreshing(false);

                        loading = false;


                        APIError apiError = ErrorUtils.parseError(response);
                        String err = apiError.error.toLowerCase();
                        if (err.contains(getString(R.string.invalid_session_id))) {
                            // Notify the parent activity that the user should be logged out
                            // Don't bother stopping the loading animation
                            SessionManager.getInstance().logout();
                        } else {
                            AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                            b.setTitle(getString(R.string.unexpected_response))
                                    .setMessage(getString(R.string.unexpected_response_hint))
                                    .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            refreshHome();
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
                public void onFailure(Call<PlayerCount> call, Throwable t) {
                    swipeContainer.setRefreshing(false);

                    loading = false;

                    AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                    b.setTitle(getString(R.string.generic_connection_error))
                            .setMessage(getString(R.string.generic_connection_error_hint))
                            .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    refreshHome();
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
}
