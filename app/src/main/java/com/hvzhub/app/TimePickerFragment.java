package com.hvzhub.app;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TimePicker;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private static final String ARGS_HOUR = "hour";
    private static final String ARGS_MINUTE = "minute";

    private TimePickerFragment.OnTimeSetListener mListener;

    public static TimePickerFragment newInstance(int hour, int minute) {
        TimePickerFragment tf = new TimePickerFragment();

        Bundle args = new Bundle();
        args.putInt(ARGS_HOUR, hour);
        args.putInt(ARGS_MINUTE, minute);
        tf.setArguments(args);
        return tf;
    }

    public static TimePickerFragment newInstance() {
        TimePickerFragment tf = new TimePickerFragment();

        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        Bundle args = new Bundle();
        args.putInt(ARGS_HOUR, hour);
        args.putInt(ARGS_MINUTE, minute);
        tf.setArguments(args);
        return tf;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int hour, minute;
        if (getArguments() != null) {
            hour = getArguments().getInt(ARGS_HOUR);
            minute = getArguments().getInt(ARGS_MINUTE);
            return new TimePickerDialog(getActivity(), this, hour, minute, false);
        } else {
            throw new IllegalArgumentException("savedInstanceState must contain arguments. Make sure " +
                    "the TimePickerFragment is being created with TimePickerFragment.newInstance()");
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TimePickerFragment.OnTimeSetListener) {
            mListener = (TimePickerFragment.OnTimeSetListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement TimePickerFragment.OnTimeSetListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mListener.onTimeSet(view, hourOfDay, minute);
    }

    interface OnTimeSetListener {
        void onTimeSet(TimePicker view, int hourOfDay, int minute);
    }
}