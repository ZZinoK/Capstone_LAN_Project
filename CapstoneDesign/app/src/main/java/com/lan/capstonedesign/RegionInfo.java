package com.lan.capstonedesign;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kslee7746 on 2016. 11. 4..
 */

@DynamoDBTable(tableName = Constants.TABLE_NAME)
public class RegionInfo {
    private String regionName;
    private String mountainName;
    private int mountainID;
    private double latitude;
    private double longitude;
    private String mountainStatus = "safe";

    public RegionInfo(int mt_id, String region_name, String mt_name, double lat, double lon){
        setMountainID(mt_id);
        setRegionName(region_name);
        setMountainName(mt_name);
        setLatitude(lat);
        setLongitude(lon);
    }

    public RegionInfo(){ }

    @DynamoDBHashKey(attributeName = "MT_ID")
    public int getMountainID() {
        return mountainID;
    }
    public void setMountainID(int mountainID) {
        this.mountainID = mountainID;
    }

    @DynamoDBAttribute(attributeName = "Region_Name")
    public String getRegionName() {
        return regionName;
    }
    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    @DynamoDBAttribute(attributeName = "MT_Name")
    public String getMountainName() {
        return mountainName;
    }
    public void setMountainName(String mountainName) {
        this.mountainName = mountainName;
    }

    @DynamoDBAttribute(attributeName = "Latitude")
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    @DynamoDBAttribute(attributeName = "Longitude")
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    @DynamoDBAttribute(attributeName = "Status")
    public String getMountainStatus() { return mountainStatus; }
    public void setMountainStatus(String mountainStatus) { this.mountainStatus = mountainStatus; }
}
