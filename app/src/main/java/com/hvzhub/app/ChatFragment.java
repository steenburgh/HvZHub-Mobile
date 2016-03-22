package com.hvzhub.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hvzhub.app.API.API;
import com.hvzhub.app.API.ErrorUtils;
import com.hvzhub.app.API.HvZHubClient;
import com.hvzhub.app.API.NetworkUtils;
import com.hvzhub.app.API.model.APIError;
import com.hvzhub.app.API.model.Chat.Message;
import com.hvzhub.app.API.model.Chat.MessageListContainer;
import com.hvzhub.app.API.model.Chat.PostChatRequest;
import com.hvzhub.app.API.model.Chat.PostChatResponse;
import com.hvzhub.app.API.model.Uuid;
import com.hvzhub.app.DB.DB;
import com.hvzhub.app.Prefs.ChatPrefs;
import com.hvzhub.app.Prefs.GamePrefs;

import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ChatFragment extends Fragment {
    private final String TAG = "ChatFragment";

    private BroadcastReceiver msgBroadcastReceiver;
    private boolean msgReceiverIsRegistered;
    private boolean chatNeedsToBeRefreshed = true;
    private OnLogoutListener mListener;

    List<Message> messages;
    ChatAdapter adapter;
    ListView listView;
    EditText message;
    ImageButton send;
    ProgressBar progressBar;

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance() {
        return new ChatFragment();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatNeedsToBeRefreshed = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle(getActivity().getString(R.string.chat));
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);

        // TODO: Programmatically update text hint based on which side you're chatting with
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = (ListView) view.findViewById(R.id.list_view);
        boolean isHuman = getActivity().getSharedPreferences(GamePrefs.PREFS_GAME, Context.MODE_PRIVATE).getBoolean(GamePrefs.PREFS_IS_HUMAN, false);
        messages = DB.getInstance(getActivity().getApplicationContext()).getMessages(isHuman ? DB.HUMAN_CHAT : DB.ZOMBIE_CHAT);
        adapter = new ChatAdapter(getActivity().getApplicationContext(), messages);
        listView.setAdapter(adapter);

        msgBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                boolean isHuman = getActivity().getSharedPreferences(GamePrefs.PREFS_GAME, Context.MODE_PRIVATE).getBoolean(GamePrefs.PREFS_IS_HUMAN, false);
                boolean justTurned = getActivity().getSharedPreferences(GamePrefs.PREFS_GAME, Context.MODE_PRIVATE).getBoolean(GamePrefs.PREFS_JUST_TURNED, false);
                List<Message> msgsFromDB = DB.getInstance(getActivity().getApplicationContext()).getMessages(isHuman ? DB.HUMAN_CHAT : DB.ZOMBIE_CHAT);
                if (justTurned) {
                    messages.clear();
                    messages.addAll(msgsFromDB);
                    getActivity().getSharedPreferences(GamePrefs.PREFS_GAME, Context.MODE_PRIVATE).edit()
                            .putBoolean(GamePrefs.PREFS_JUST_TURNED, false)
                            .apply();
                    Log.d(TAG, "Player was just turned. Reloading chat list");
                } else {
                    Message messageObj = msgsFromDB.get(msgsFromDB.size() - 1);
                    messages.add(messageObj);
                    Log.i(TAG, "Received new chat message:");
                    Log.i(TAG, messageObj.getMessage());
                }
                adapter.notifyDataSetChanged();

            }
        };

        message = (EditText) view.findViewById(R.id.compose_msg);
        send = (ImageButton) view.findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMsg();
            }
        });

        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
    }

    private void sendMsg() {
        message.setError(null);
        if (TextUtils.isEmpty(message.getText())) {
            message.setError(getString(R.string.enter_a_message));
            message.requestFocus();
            return;
        }
        if (!NetworkUtils.networkIsAvailable(getActivity().getApplicationContext())) {
            AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
            b.setTitle(getString(R.string.network_not_available))
                    .setMessage(getString(R.string.network_not_available_hint))
                    .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendMsg();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing
                        }
                    })
                    .show();
            return;
        }

        showProgress(true);
        String uuid = getActivity().getSharedPreferences(GamePrefs.PREFS_GAME, Context.MODE_PRIVATE).getString(GamePrefs.PREFS_SESSION_ID, null);
        int gameId = getActivity().getSharedPreferences(GamePrefs.PREFS_GAME, Context.MODE_PRIVATE).getInt(GamePrefs.PREFS_GAME_ID, -1);
        int userId = 1;
        boolean isHuman = getActivity().getSharedPreferences(GamePrefs.PREFS_GAME, Context.MODE_PRIVATE).getBoolean(GamePrefs.PREFS_IS_HUMAN, false);

        HvZHubClient client = API.getInstance(getActivity().getApplicationContext()).getHvZHubClient();
        Call<PostChatResponse> call = client.postChat(
                gameId,
                new PostChatRequest(
                        uuid,
                        userId,
                        message.getText().toString(),
                        isHuman
                )
        );
        call.enqueue(new Callback<PostChatResponse>() {
            @Override
            public void onResponse(Call<PostChatResponse> call, Response<PostChatResponse> response) {
                showProgress(false);
                if (response.isSuccessful()) {
                    message.setText("");
                } else {
                    APIError apiError = ErrorUtils.parseError(response);
                    String err = apiError.error.toLowerCase();
                    if (err.contains(getString(R.string.invalid_session_id))) {
                        // Notify the parent activity that the user should be logged out
                        // Don't bother stopping the loading animation
                        mListener.onLogout();
                    } else {
                        Snackbar snackbar = Snackbar.make(listView, R.string.unexpected_response, Snackbar.LENGTH_LONG);
                        View view = snackbar.getView();
                        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                        tv.setTextColor(Color.WHITE);
                        snackbar.show();
                        Log.i("API Error", "Error connecting to HvZHub.com");

                        if (err.equals("")) {
                            Log.i("API Error", String.format("Error was: %s", err));
                        } else {
                            Log.i("API Error", response.errorBody().toString());
                        }

                    }
                }
            }

            @Override
            public void onFailure(Call<PostChatResponse> call, Throwable t) {
                showProgress(false);
                // TODO: Make this an alert box
                Snackbar snackbar = Snackbar.make(listView, R.string.generic_connection_error, Snackbar.LENGTH_LONG);
                View view = snackbar.getView();
                TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                tv.setTextColor(Color.WHITE);
                snackbar.show();
            }
        });
    }

    /**
     * Shows the loader and hides disables the send UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);


            send.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
            send.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    send.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
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
            send.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerMsgReceiver();

        // Chat is now open
        SharedPreferences.Editor prefs = getActivity().getSharedPreferences(ChatPrefs.NAME, Context.MODE_PRIVATE).edit();
        prefs.putBoolean(ChatPrefs.IS_OPEN, true);
        prefs.apply();
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(msgBroadcastReceiver);
        msgReceiverIsRegistered = false;

        // Chat is now closed
        SharedPreferences.Editor prefs = getActivity().getSharedPreferences(ChatPrefs.NAME, Context.MODE_PRIVATE).edit();
        prefs.putBoolean(ChatPrefs.IS_OPEN, false);
        prefs.apply();
    }

    private void registerMsgReceiver() {
        if (!msgReceiverIsRegistered) {
            LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
                    .registerReceiver(
                            msgBroadcastReceiver,
                            new IntentFilter(ChatPrefs.MESSAGE_RECEIVED_BROADCAST)
                    );
            msgReceiverIsRegistered = true;
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // Hide keyboard
        if (getView() != null) {
            final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLogoutListener) {
            mListener = (OnLogoutListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must be an instance of OnLogoutListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
