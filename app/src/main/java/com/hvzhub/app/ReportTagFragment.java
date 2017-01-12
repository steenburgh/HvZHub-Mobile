package com.hvzhub.app;

import android.app.AlertDialog;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.hvzhub.app.API.API;
import com.hvzhub.app.API.ErrorUtils;
import com.hvzhub.app.API.HvZHubClient;
import com.hvzhub.app.API.model.APIError;
import com.hvzhub.app.API.model.APISuccess;
import com.hvzhub.app.API.model.TagPlayerRequest;
import com.hvzhub.app.Prefs.GamePrefs;
import com.hvzhub.app.Prefs.TagLocationPref;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportTagFragment extends Fragment implements DatePickerFragment.OnDateSetListener, TimePickerFragment.OnTimeSetListener {
    EditText submitCode;
    ProgressBar progressBar;
    LinearLayout myCodeContainer;
    LinearLayout errorView;
    TextView TimeInput;
    TextView DateInput;
    TextView LocationInput;
    Calendar tagTime;
    LatLng tagLocation;
    FragmentManager mapManager;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    public ReportTagFragment() {
        tagTime = Calendar.getInstance();
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

        DateInput = (TextView) view.findViewById(R.id.date);
        DateInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        TimeInput = (TextView) view.findViewById(R.id.time);
        TimeInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

        LocationInput = (TextView) view.findViewById(R.id.location);
        LocationInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start TagMap
                Intent mapstart = new Intent(getActivity(), TagMapActivity.class);
                startActivity(mapstart);
            }
        });

        myCodeContainer = (LinearLayout) view.findViewById(R.id.my_code_container);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        errorView = (LinearLayout) view.findViewById(R.id.error_view);

        setLatLng();

        SharedPreferences prefs = getActivity().getSharedPreferences(TagLocationPref.NAME, 0);
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {

            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (key.equals(TagLocationPref.Latitude)){
                    setLatLng();
                }
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    private void tryToTag() {
        int gameId = getActivity().getSharedPreferences(GamePrefs.NAME, Context.MODE_PRIVATE).getInt(GamePrefs.PREFS_GAME_ID, -1);
        HvZHubClient client = API.getInstance(getActivity().getApplicationContext()).getHvZHubClient();
        String tagCode = submitCode.getText().toString();
        Date tagDate = tagTime.getTime();

        final SharedPreferences prefs = getActivity().getSharedPreferences(TagLocationPref.NAME, Context.MODE_PRIVATE);
        String latStr = prefs.getString(TagLocationPref.Latitude, null);
        String longStr = prefs.getString(TagLocationPref.Longitude, null);

        TagPlayerRequest tpr;
        if (latStr == null || longStr == null) {
            tpr = new TagPlayerRequest(
                    SessionManager.getInstance().getSessionUUID(),
                    tagCode,
                    tagDate
            );
        } else {
            double Lat = Double.parseDouble(latStr);
            double Long = Double.parseDouble(longStr);
            tpr = new TagPlayerRequest(
                    SessionManager.getInstance().getSessionUUID(),
                    tagCode,
                    tagDate,
                    Lat,
                    Long
            );
        }

        Call<APISuccess> call = client.reportTag(gameId, tpr);
        call.enqueue(new Callback<APISuccess>() {
            @Override
            public void onResponse(Call<APISuccess> call, Response<APISuccess> response) {
                if (response.isSuccessful()) {
                    //make the response string

                    if (response.body().success != null) {
                        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                        b.setTitle("Tag Successful")
                                .setMessage(response.body().success.toString())
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Do nothing
                                    }
                                })
                                .show();
                        prefs.edit().clear().apply();
                        setLatLng();
                    } else {
                        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
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

                } else {
                    APIError apiError = ErrorUtils.parseError(response);
                    String errorMessage;
                    if (apiError.error == null){
                        errorMessage = getString(R.string.unexpected_response);
                    }
                    else {
                        String err = apiError.error.toLowerCase();

                        if (err.contains("join")) {
                            errorMessage = "You must join this game to be able to tag a player";
                        } else if (err.contains("submit")) {
                            errorMessage = getString(R.string.you_must_submit_a_tag_code);
                        } else if (err.contains("player")) {
                            errorMessage = getString(R.string.that_code_doesnt_belong_to_a_player);
                        } else if (err.contains("timestamp")) {
                            errorMessage = getString(R.string.you_must_enter_a_timestamp);
                        } else if (err.contains("fuck")) {
                            errorMessage = getString(R.string.you_must_be_a_zombie_to_make_a_tag);
                        } else {
                            errorMessage = getString(R.string.unexpected_response);
                        }
                    }
                    AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                    b.setTitle("Tag Failed")
                            .setMessage(errorMessage)
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
            public void onFailure(Call<APISuccess> call, Throwable t) {
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
    }

    private void showDatePickerDialog() {
        DatePickerFragment dp = DatePickerFragment.newInstance();
        dp.setTargetFragment(this, 0);
        dp.show(getActivity().getSupportFragmentManager(), "datePicker");
    }

    private void showTimePickerDialog() {
        TimePickerFragment tp = TimePickerFragment.newInstance();
        tp.setTargetFragment(this, 0);
        tp.show(getActivity().getSupportFragmentManager(), "timePicker");
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
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

        // Update the associated calendar
        tagTime.set(year, monthOfYear, dayOfMonth);
        // Update the associated textview
        DateFormat dateFormat = SimpleDateFormat.getDateInstance();
        String strDate = dateFormat.format(tagTime.getTime());
        DateInput.setText(strDate);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        // Update the associated calendar
        tagTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        tagTime.set(Calendar.MINUTE, minute);
        if (hourOfDay == 4 && minute == 20 || hourOfDay == 16 && minute == 20){
            Toast MLG = Toast.makeText(getActivity(), "420 BLAZIT 360 NOSCOPE", Toast.LENGTH_LONG);
            MLG.show();
        }
        if (hourOfDay == 9 && minute == 11 || hourOfDay == 21 && minute == 11){
            Toast Bush = Toast.makeText(getActivity(), "JetFuel can't melt Dank Memes", Toast.LENGTH_LONG);
            Bush.show();
        }
        // Update the associated textview
        DateFormat timeFormat = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
        String strTime = timeFormat.format(tagTime.getTime());
        TimeInput.setText(strTime);
    }

    public void setLatLng(){
        SharedPreferences prefs = getActivity().getSharedPreferences(TagLocationPref.NAME, 0);
        String latVal = prefs.getString(TagLocationPref.Latitude, null); // If Latitude isn't set, return "0"
        String longVal = prefs.getString(TagLocationPref.Longitude, null); // If Latitude isn't set, return "0"
        if (latVal == null || longVal == null) {
            LocationInput.setText(R.string.tap_to_enter_location);
            return;
        }

        if(latVal.length() >= 11){
            latVal = latVal.substring(0, 10);
        }
        if(longVal.length() >= 11) {
            longVal = longVal.substring(0, 10);
        }
        LocationInput.setText("(" + latVal + "," + longVal + ")");
    }
}
