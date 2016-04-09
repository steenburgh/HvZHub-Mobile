package com.hvzhub.app.API.model.Games.News;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ModUpdatesContainer implements NewsContainer<ModUpdateShort> {
    public List<ModUpdateShort> updates;

    @Override
    public List<ModUpdateShort> getNews() {
        Collections.sort(updates, new Comparator<ModUpdateShort>() {
            @Override
            public int compare(ModUpdateShort lhs, ModUpdateShort rhs) {
                Integer right = rhs.id;
                return right.compareTo(lhs.id);
            }
        });
        return updates;
    }
}
