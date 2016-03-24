package com.hvzhub.app;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class GameNewsFragment extends Fragment {
    public static final String TITLE = "News";

    public GameNewsFragment() {
        // Required empty public constructor
    }

    public static GameNewsFragment newInstance() {
        return new GameNewsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle(getActivity().getString(R.string.game_news));
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game_news, container, false);
    }

}
