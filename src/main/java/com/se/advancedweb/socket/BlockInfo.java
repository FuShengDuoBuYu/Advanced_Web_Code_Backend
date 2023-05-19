package com.se.advancedweb.socket;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BlockInfo {
    // 一个float三元组
    public float x1;
    public float y1;
    public float z1;
    // 另一个float三元组
    public float x2;
    public float y2;
    public float z2;

    @Override
    public String toString() {
        return "BlockInfo{" +
                "x1=" + x1 +
                ", y1=" + y1 +
                ", z1=" + z1 +
                ", x2=" + x2 +
                ", y2=" + y2 +
                ", z2=" + z2 +
                '}';
    }
}
