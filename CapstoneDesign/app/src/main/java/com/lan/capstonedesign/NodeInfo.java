package com.lan.capstonedesign;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

/**
 * Created by kslee7746 on 2016. 11. 9..
 */
@DynamoDBTable(tableName = Constants.NODE_TABLE_NAME)
public class NodeInfo {
    private int Node_ID;
    private int MT_ID;
    private int Route;
    private double latitude;
    private double longitude;
    private int Node_X;
    private int Node_Y;
    private int Node_Z;
    private int Variation;

    public NodeInfo(int node_id, int mt_id, int route, double latitude, double longitude, int node_X, int node_Y, int node_Z, int variation){
        setNode_ID(node_id);
        setMT_ID(mt_id);
        setRoute(route);
        setLatitude(latitude);
        setLongitude(longitude);
        setNode_X(node_X);
        setNode_Y(node_Y);
        setNode_Z(node_Z);
        setVariation(variation);
    }
    public NodeInfo() { }

    @DynamoDBHashKey(attributeName = "Node_ID")
    public int getNode_ID() {
        return Node_ID;
    }

    public void setNode_ID(int node_ID) {
        Node_ID = node_ID;
    }

    @DynamoDBAttribute(attributeName = "MT_ID")
    public int getMT_ID() {
        return MT_ID;
    }
    public void setMT_ID(int MT_ID) {
        this.MT_ID = MT_ID;
    }
    @DynamoDBAttribute(attributeName = "Route")
    public int getRoute() {
        return Route;
    }
    public void setRoute(int route) {
        Route = route;
    }
    @DynamoDBAttribute(attributeName = "Latitude")
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @DynamoDBAttribute(attributeName = "Longitude")
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    @DynamoDBAttribute(attributeName = "Node_X")
    public int getNode_X() {
        return Node_X;
    }
    public void setNode_X(int node_X) {
        Node_X = node_X;
    }
    @DynamoDBAttribute(attributeName = "Node_Y")
    public int getNode_Y() {
        return Node_Y;
    }
    public void setNode_Y(int node_Y) {
        Node_Y = node_Y;
    }

    @DynamoDBAttribute(attributeName = "Node_Z")
    public int getNode_Z() {
        return Node_Z;
    }
    public void setNode_Z(int node_Z) {
        Node_Z = node_Z;
    }
    @DynamoDBAttribute(attributeName = "Variation")
    public int getVariation() {
        return Variation;
    }
    public void setVariation(int variation) {
        Variation = variation;
    }

}

