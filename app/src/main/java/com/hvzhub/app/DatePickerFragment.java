package com.hvzhub.app;

        import android.app.DatePickerDialog;
        import android.app.Dialog;
        import android.app.DialogFragment;
        import android.content.Context;
        import android.os.Bundle;
        import android.widget.DatePicker;

        import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private static final String ARGS_YEAR = "year";
    private static final String ARGS_MONTH = "month";
    private static final String ARGS_DAY = "day";

    private DatePickerFragment.OnDateSetListener mListener;

    /**
     * Creates a new DatePickerFragment.
     *
     * @param year Initial year
     * @param month Initial month
     * @param day Initial day
     * @return a new DatePickerFragment
     */
    public static DatePickerFragment newInstance(int year, int month, int day) {
        DatePickerFragment df = new DatePickerFragment();

        Bundle args = new Bundle();
        args.putInt(ARGS_YEAR, year);
        args.putInt(ARGS_MONTH, month);
        args.putInt(ARGS_DAY, day);
        df.setArguments(args);
        return df;
    }

    /**
     * Creates a new DatePickerFragment with today as the initial date.
     */
    public static DatePickerFragment newInstance() {
        DatePickerFragment df = new DatePickerFragment();

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        Bundle args = new Bundle();
        args.putInt(ARGS_YEAR, year);
        args.putInt(ARGS_MONTH, month);
        args.putInt(ARGS_DAY, day);
        df.setArguments(args);
        return df;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int year, month, day;
        if (getArguments() != null) {
            year = getArguments().getInt(ARGS_YEAR);
            month = getArguments().getInt(ARGS_MONTH);
            day = getArguments().getInt(ARGS_DAY);
            return new DatePickerDialog(getActivity(), this, year, month, day);
        } else {
            throw new IllegalArgumentException("savedInstanceState must contain arguments. Make sure " +
                    "the DatePickerFragment is being created with DatePickerFragment.newInstance()");
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DatePickerFragment.OnDateSetListener) {
            mListener = (DatePickerFragment.OnDateSetListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement DatePickerFragment.OnDateSetListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mListener.onDateSet(view, year, monthOfYear, dayOfMonth);
    }

    interface OnDateSetListener {
        void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth);
    }
}