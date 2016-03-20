package com.hvzhub.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ListView;

import com.hvzhub.app.DB.DB;
import com.hvzhub.app.DB.Message;
import com.hvzhub.app.Prefs.ChatPrefs;

import java.util.List;


public class ChatFragment extends Fragment {
    private final String TAG = "ChatFragment";

    private BroadcastReceiver msgBroadcastReceiver;
    private boolean msgReceiverIsRegistered;
    private boolean chatNeedsToBeRefreshed = true;

    List<Message> messages;
    ChatAdapter adapter;

    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
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

        ListView lv = (ListView) view.findViewById(R.id.list_view);
        messages = DB.getInstance(getActivity().getApplicationContext()).getMessages(DB.HUMAN_CHAT);
        adapter = new ChatAdapter(getActivity().getApplicationContext(), messages);
        lv.setAdapter(adapter);

        msgBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                List<Message> msgsFromDB = DB.getInstance(getActivity().getApplicationContext()).getMessages(DB.HUMAN_CHAT);
                Message messageObj = msgsFromDB.get(msgsFromDB.size() - 1);
                messages.add(messageObj);
                adapter.notifyDataSetChanged();
                Log.i(TAG, "Received new chat message:");
                Log.i(TAG, messageObj.getMessage());
            }
        };
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
}
