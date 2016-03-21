package com.hvzhub.app;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hvzhub.app.API.API;
import com.hvzhub.app.API.ErrorUtils;
import com.hvzhub.app.API.HvZHubClient;
import com.hvzhub.app.API.NetworkUtils;
import com.hvzhub.app.API.model.APIError;
import com.hvzhub.app.API.model.APISuccess;
import com.hvzhub.app.API.model.Code;
import com.hvzhub.app.API.model.TagPlayerRequest;
import com.hvzhub.app.API.model.Uuid;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyCodeFragment extends Fragment {
    TextView myCode;
    ProgressBar progressBar;
    LinearLayout myCodeContainer;
    LinearLayout errorView;

    private OnLogoutListener mListener;

    public MyCodeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    // TODO: Rename and change types and number of parameters
    public static MyCodeFragment newInstance() {
        MyCodeFragment fragment = new MyCodeFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle(getActivity().getString(R.string.my_code));
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_code, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        myCode = (TextView) view.findViewById(R.id.my_code);
        myCodeContainer = (LinearLayout) view.findViewById(R.id.my_code_container);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        errorView = (LinearLayout) view.findViewById(R.id.error_view);
        updateMyCode();
    }

    private void updateMyCode() {
        showProgress(true);
        showErrorView(false);
        showContentView(false);

        if (!NetworkUtils.networkIsAvailable(getActivity().getApplicationContext())) {
            setError(getString(R.string.network_not_available), getString(R.string.network_not_available_hint));
            showProgress(false);
            showErrorView(true);
            showContentView(false);

            AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
            b.setTitle(getString(R.string.network_not_available))
                    .setMessage(getString(R.string.network_not_available_hint))
                    .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            updateMyCode();
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
        int gameId = getActivity().getSharedPreferences(API.PREFS_API, Context.MODE_PRIVATE).getInt(API.PREFS_GAME_ID, -1);
        if (gameId == -1) {
            myCode.setText(R.string.empty_code);
        } else {
            HvZHubClient client = API.getInstance(getActivity().getApplicationContext()).getHvZHubClient();
            String uuid = getActivity().getSharedPreferences(API.PREFS_API, Context.MODE_PRIVATE).getString(API.PREFS_SESSION_ID, null);
            Call<Code> call = client.getMyCode(gameId, new Uuid(uuid));
            call.enqueue(new Callback<Code>() {
                @Override
                public void onResponse(Call<Code> call, Response<Code> response) {
                    if (response.isSuccessful()) {
                        showProgress(false);
                        showErrorView(false);
                        showContentView(true);
                        myCode.setText(response.body().code);

                    } else {
                        APIError apiError = ErrorUtils.parseError(response);
                        String err = apiError.error.toLowerCase();
                        if (err.contains(getString(R.string.invalid_session_id))) {
                            // Notify the parent activity that the user should be logged out
                            // Don't bother stopping the loading animation
                            mListener.onLogout();
                        } else {
                            setError(getString(R.string.unexpected_response), getString(R.string.unexpected_response_hint));
                            showProgress(false);
                            showContentView(false);
                            showErrorView(true);
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
                public void onFailure(Call<Code> call, Throwable t) {
                    setError(getString(R.string.generic_connection_error), getString(R.string.generic_connection_error_hint));
                    showProgress(false);
                    showContentView(false);
                    showErrorView(true);
                }
            });
        }

    }

    private void showContentView(final boolean show) {
        myCodeContainer.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showErrorView(final boolean show) {
        errorView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void setError(String msg, String hint) {
        errorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMyCode();
                // TODO: Give feedback when the user presses the button
            }
        });

        TextView errorMsg = (TextView) errorView.findViewById(R.id.error_msg);
        errorMsg.setText(String.format("%s %s", msg, getString(R.string.tap_to_retry)));
        TextView errorHint = (TextView) errorView.findViewById(R.id.error_hint);
        errorHint.setText(hint);

        // Make the LinearLayout react when clicked
        // Source: http://stackoverflow.com/a/28087443
        TypedValue outValue = new TypedValue();
        getActivity().getApplicationContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        errorView.setBackgroundResource(outValue.resourceId);
    }

    private void showProgress(final boolean showProgress) {
        progressBar.setVisibility(showProgress ? View.VISIBLE : View.GONE);
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
