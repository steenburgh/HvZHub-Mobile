package com.hvzhub.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.hvzhub.app.API.API;
import com.hvzhub.app.API.HvZHubClient;
import com.hvzhub.app.API.model.Games.News.GameNewsContainer;
import com.hvzhub.app.API.model.Games.News.GameNewsItem;
import com.hvzhub.app.API.model.Uuid;

import java.util.List;

import retrofit2.Call;

public class GameNewsFragment extends NewsFragment<GameNewsItem, GameNewsContainer> {

    public GameNewsFragment() {
        super(true);
    }

    public static GameNewsFragment newInstance() {
        return new GameNewsFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        getActivity().setTitle(getString(R.string.game_news));
        return v;
    }

    @Override
    protected BaseAdapter createAdapter(List<GameNewsItem> list) {
        return new GameNewsAdapter(getActivity(), list);
    }

    @Override
    protected Call<GameNewsContainer> createLoadNewsCall(
            Uuid uuid,
            int gameId,
            int initialNum,
            boolean fetchEntireList,
            int numItemsToFetch
    ) {
        HvZHubClient client = API.getInstance(getActivity()).getHvZHubClient();
        return client.getNews(
                uuid,
                gameId,
                initialNum,

                // If this is -1, the entire list is fetched, so we don't even have to check fetchEntireList
                numItemsToFetch
        );
    }
}
