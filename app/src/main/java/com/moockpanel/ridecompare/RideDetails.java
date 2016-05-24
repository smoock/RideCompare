package com.moockpanel.ridecompare;

/**
 * Created by stephenmoock on 5/23/16.
 */
public class RideDetails {
    private String mRideType;
    private String mCurrency;
    private int mEstMinCost;
    private int mEstMaxCost;
    private double mPrimetimePercentage;
    private int mAvgCost;


    public double getAvgCost() {
        return mAvgCost;
    }

    public void calcAvgCost(int minCost, int maxCost) {
        mAvgCost = (minCost + maxCost)/2;
    }

    public String getRideType() {
        return mRideType;
    }

    public void setRideType(String rideType) {
        mRideType = rideType;
    }

    public String getCurrency() {
        return mCurrency;
    }

    public void setCurrency(String currency) {
        mCurrency = currency;
    }

    public int getEstMinCost() {
        return mEstMinCost;
    }

    public void setEstMinCost(int estMinCost) {
        mEstMinCost = estMinCost;
    }

    public int getEstMaxCost() {
        return mEstMaxCost;
    }

    public void setEstMaxCost(int estMaxCost) {
        mEstMaxCost = estMaxCost;
    }

    public double getPrimetimePercentage() {
        return mPrimetimePercentage;
    }

    public void setPrimetimePercentage(String primetimePercentage) {
        if (primetimePercentage.contains("%")) {
            mPrimetimePercentage = Integer.parseInt(primetimePercentage.substring(0,primetimePercentage.length()-1));
        }
        else {
            mPrimetimePercentage = Double.parseDouble(primetimePercentage);
        }
    }
}