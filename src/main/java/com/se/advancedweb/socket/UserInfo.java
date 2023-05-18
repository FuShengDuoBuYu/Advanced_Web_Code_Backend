package com.se.advancedweb.socket;

public class UserInfo {
    public String id;
    public String rolename;
    public String username;
    public float x;
    public float y;
    public float z;
    // 旋转角度
    public float r;

    public UserInfo(String id, String rolename, String username, float x, float y, float z, float r) {
        this.id = id;
        this.rolename = rolename;
        this.username = username;
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "username=" + username +
                ", rolename=" + rolename +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", r=" + r +
                '}';
    }
}
