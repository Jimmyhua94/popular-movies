package me.jimmyhuang.popularmovies.model;

public class Trailer {
    private String key;
    private String name;


    public Trailer(String key, String name, String site) {
        this.key = key;
        this.name = name;
    }

    public void setKey(String key) { this.key = key; }
    public String getKey() { return key; }

    public void setName(String name) { this.name = name; }
    public String getName() { return name; }
}
