package com.se.advancedweb.socket;

import com.alibaba.fastjson.JSONObject;

public class UserData {
    public float x;
    public float y;
    public float z;
    public float heading;
    public float pb;
    public String model;
    public String colour;
    public String action;

    public UserData(float x, float y, float z, float heading, String model, String colour, float pb, String action) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.heading = heading;
        this.model = model;
        this.colour = colour;
        this.pb = pb;
        this.action = action;
    }

    public UserData(float x, float y, float z, float heading) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.heading = heading;
    }

    public static UserData fromJson(JSONObject json) {
        float x = json.getFloatValue("x");
        float y = json.getFloatValue("y");
        float z = json.getFloatValue("z");
        float heading = json.getFloatValue("heading");
        float pb = json.getFloatValue("pb");
        String model = json.getString("model");
        String colour = json.getString("colour");
        String action = json.getString("action");
        return new UserData(x, y, z, heading, model, colour, pb, action);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("x", x);
        json.put("y", y);
        json.put("z", z);
        json.put("heading", heading);
        json.put("model", model);
        json.put("colour", colour);
        json.put("pb", pb);
        json.put("action", action);
        return json;
    }

    @Override
    public String toString() {
        return "UserData{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", heading=" + heading +
                ", pb=" + pb +
                ", model='" + model + '\'' +
                ", colour='" + colour + '\'' +
                ", action='" + action + '\'' +
                '}';
    }
}
