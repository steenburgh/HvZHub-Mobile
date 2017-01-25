package com.hvzhub.app.API.model.Games.News;

import java.util.List;

public class GameNewsContainer implements NewsContainer<GameNewsItem> {
    public List<GameNewsItem> news;

    @Override
    public List<GameNewsItem> getNews() {
        return news;
    }
}
