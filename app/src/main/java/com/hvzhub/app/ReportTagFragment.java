package com.hvzhub.app;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

public class ReportTagFragment extends Fragment {
    EditText submitCode;
    ProgressBar progressBar;
    LinearLayout myCodeContainer;
    LinearLayout errorView;

    private OnLogoutListener mListener;

    public ReportTagFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    // TODO: Rename and change types and number of parameters
    public static ReportTagFragment newInstance() {
        ReportTagFragment fragment = new ReportTagFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle(getActivity().getString(R.string.report_tag));
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_report_tag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button tagButton = (Button) view.findViewById(R.id.submit_tag);
        tagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryToTag();
            }
        });
        submitCode = (EditText) view.findViewById(R.id.tag_reported);

        TextView DateInput = (TextView) view.findViewById(R.id.date);
        DateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        TextView TimeInput = (TextView) view.findViewById(R.id.time);
        TimeInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });


        myCodeContainer = (LinearLayout) view.findViewById(R.id.my_code_container);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        errorView = (LinearLayout) view.findViewById(R.id.error_view);
    }

    private void tryToTag() {
        int gameId = getActivity().getSharedPreferences(API.PREFS_API, Context.MODE_PRIVATE).getInt(API.PREFS_GAME_ID, -1);
        HvZHubClient client = API.getInstance(getActivity().getApplicationContext()).getHvZHubClient();
        String uuid = getActivity().getSharedPreferences(API.PREFS_API, Context.MODE_PRIVATE).getString(API.PREFS_SESSION_ID, null);
        TagPlayerRequest tpr = new TagPlayerRequest(
                new Uuid(uuid),
                "ASDFGR",
                new Date()
        );
        Call<APISuccess> call = client.reportTag(gameId, tpr);
        call.enqueue(new Callback<APISuccess>() {
            @Override
            public void onResponse(Call<APISuccess> call, Response<APISuccess> response) {

            }

            @Override
            public void onFailure(Call<APISuccess> call, Throwable t) {

            }
        });

    }

    private void showDatePickerDialog() {
        DatePickerFragment dp = DatePickerFragment.newInstance();
        dp.show(getFragmentManager(), "datePicker");
    }

    private void showTimePickerDialog() {
        TimePickerFragment tp = TimePickerFragment.newInstance();
        tp.show(getFragmentManager(), "datePicker");
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
