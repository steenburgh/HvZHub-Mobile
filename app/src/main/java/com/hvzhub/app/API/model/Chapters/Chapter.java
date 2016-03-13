package com.hvzhub.app.API.model.Chapters;

public class Chapter {
    public int id;
    public String name;
    public LinkContainer _links;

    public class LinkContainer {
        public String self;
        public String collection;
    }

    public String getUrl() {
        return this._links.self.replace(this._links.collection, "");
    }

    @Override
    public String toString() {
        return this.name;
    }
}
