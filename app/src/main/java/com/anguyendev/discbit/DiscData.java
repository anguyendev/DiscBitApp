package com.anguyendev.discbit;

import org.json.JSONException;
import org.json.JSONObject;

public class DiscData extends Object{
    private  JSONObject jsonDiscData;
    private double ax, ay, az;
    private double gx, gy, gz;
    private double mx, my, mz;
    private double yaw, pitch, roll;

    public DiscData(JSONObject jsonObject) {
        jsonDiscData = jsonObject;
        try {
            ax = jsonObject.getDouble("ax");
            ay = jsonObject.getDouble("ay");
            az = jsonObject.getDouble("az");
            gx = jsonObject.getDouble("gx");
            gy = jsonObject.getDouble("gy");
            gz = jsonObject.getDouble("gz");
            mx = jsonObject.getDouble("mx");
            my = jsonObject.getDouble("my");
            mz = jsonObject.getDouble("mz");
            yaw = jsonObject.getDouble("yaw");
            pitch = jsonObject.getDouble("pitch");
            roll = jsonObject.getDouble("roll");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return jsonDiscData.toString();
    }

    public double getAx() {
        return ax;
    }

    public double getAy() {
        return ay;
    }

    public double getAz() {
        return az;
    }

    public double getGx() {
        return gx;
    }

    public double getGy() {
        return gy;
    }

    public double getGz() {
        return gz;
    }

    public double getMx() {
        return mx;
    }

    public double getMy() {
        return my;
    }

    public double getMz() {
        return mz;
    }

    public double getYaw() {
        return yaw;
    }

    public double getPitch() {
        return pitch;
    }

    public double getRoll() {
        return roll;
    }
}
