package com.hvzhub.app.API.model.Games;

public class Game
    implements Comparable<Game> {
    public int id;
    public String name;

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public int compareTo(Game another) {
        // Games sort in reverse order. A larger id means the game is more recent
        if (this.id == another.id) {
            return 0;
        } else if (this.id < another.id) {
            return 1;
        } else {
            return -1;
        }
    }

}
