package com.example.radek.mapsproject;

public class Route {

    static final String TABLE_NAME = "routes";
    static final String COLUMN_ID = "id";
    static final String COLUMN_ROUTE = "route";
    static final String COLUMN_JSON = "json";
    static final String COLUMN_TIME = "time";
    static final String COLUMN_COORDINATES = "coordinates";



    private int id;
    private String route;
    private String json;
    private String time;
    private String coordinates;

    static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_ROUTE + " TEXT,"
                    + COLUMN_JSON + " TEXT,"
                    + COLUMN_TIME + " INTEGER,"
                    + COLUMN_COORDINATES + " TEXT"
                    + ")";

    Route() {

    }

    Route(int id, String route, String json, String time, String coordinates) {
        this.id = id;
        this.route = route;
        this.json = json;
        this.time = time;
        this.coordinates = coordinates;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    String getJson() { return json;}

    void setJson(String json) { this.json = json;}

    public String getTime() { return time;}

    public void setTime(String time) {
        this.time = time;
    }

    public String getCoordinates() { return coordinates;}

    public void setCoordinates(String coordinates) { this.coordinates = coordinates;}

}
