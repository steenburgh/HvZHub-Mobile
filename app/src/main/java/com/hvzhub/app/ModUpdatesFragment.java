package com.hvzhub.app;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class ModUpdatesFragment extends Fragment {
    public static final String TITLE = "News";

    public ModUpdatesFragment() {
        // Required empty public constructor
    }

    public static ModUpdatesFragment newInstance() {
        return new ModUpdatesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle(getActivity().getString(R.string.mod_updates));
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.news_layout, container, false);
    }

}
