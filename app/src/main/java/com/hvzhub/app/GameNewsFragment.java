package com.hvzhub.app;

import android.widget.BaseAdapter;

import com.hvzhub.app.API.API;
import com.hvzhub.app.API.HvZHubClient;
import com.hvzhub.app.API.model.Games.News.GameNewsItem;
import com.hvzhub.app.API.model.Games.News.NewsContainer;
import com.hvzhub.app.API.model.Uuid;

import java.util.List;

import retrofit2.Call;

public class GameNewsFragment extends NewsFragment<GameNewsItem> {

    public GameNewsFragment() {
        super();
        // Required empty public constructor
    }

    public static GameNewsFragment newInstance() {
        return new GameNewsFragment();
    }

    @Override
    protected BaseAdapter createAdapter(List<GameNewsItem> list) {
        return new GameNewsAdapter(getActivity(), list);
    }

    @Override
    protected Call<NewsContainer<GameNewsItem>> createLoadNewsCall(
            Uuid uuid,
            int gameId,
            int initialNum,
            int numItemsToFetch
    ) {
        HvZHubClient client = API.getInstance(getActivity()).getHvZHubClient();
        return client.getNews(
                uuid,
                gameId,
                initialNum,
                numItemsToFetch
        );
    }
}
