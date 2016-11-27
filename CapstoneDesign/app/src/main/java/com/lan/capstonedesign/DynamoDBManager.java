/*
 * Copyright 2010-2012 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 * 
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.lan.capstonedesign;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapperConfig;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import java.util.ArrayList;
import java.util.HashMap;

public class DynamoDBManager {

    private static final String TAG = "DynamoDBManager";
    public static AmazonDynamoDBClient ddb = null;
    private static Context context;
    public static ArrayList<RegionInfo> regionInfoArrayList = new ArrayList<>();
    public static ArrayList<NodeInfo> nodeInfoArrayList = new ArrayList<>();

    /*
     * Creates a table with the following attributes: Table name: testTableName
     * Hash key: userNo type N Read Capacity Units: 10 Write Capacity Units: 5
     */

    public DynamoDBManager(Context context) {
        this.context = context;
    }

    public static DynamoDBManager instance;

    public static DynamoDBManager getInstance(Context context){
        if(instance == null){
            instance = new DynamoDBManager(context);
            initClients(context);
        }
        return instance;
    }

    private static void initClients(Context context) {
        if(ddb == null){
            CognitoCachingCredentialsProvider credentials = new CognitoCachingCredentialsProvider(
                    context,
                    Constants.IDENTITY_POOL_ID,
                    Regions.AP_NORTHEAST_2);
            ddb = new AmazonDynamoDBClient(credentials);
            ddb.setRegion(Region.getRegion(Regions.AP_NORTHEAST_2));
        }

    }
    public static ArrayList<NodeInfo> getLastetNodeInfo(){
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);
        HashMap<String, String> expressionAttributeNames = new HashMap<>();
        HashMap<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeNames.put("#nodeid", "Node_ID");
        expressionAttributeNames.put("#time", "Time");
        expressionAttributeValues.put(":hashNumber", new AttributeValue("0"));
        expressionAttributeValues.put(":latestTime", new AttributeValue("2016-11-11 00:30"));

        NodeInfo node = new NodeInfo();
        node.setNode_ID(3);
        DynamoDBQueryExpression<NodeInfo> queryExpression = new DynamoDBQueryExpression<>();

        queryExpression
                .withFilterExpression("#nodeid > :hashNumber")
                .withFilterExpression("#time > :latestTime")
                .withExpressionAttributeNames(expressionAttributeNames)
                .withExpressionAttributeValues(expressionAttributeValues);
        DynamoDBMapperConfig mapperConfig = new DynamoDBMapperConfig(DynamoDBMapperConfig.ConsistentReads.CONSISTENT);
        try {
            PaginatedQueryList<NodeInfo> result = mapper.query(NodeInfo.class, queryExpression, mapperConfig);

            ArrayList<NodeInfo> resultList = new ArrayList<NodeInfo>();
            for (NodeInfo up : result) {
                resultList.add(up);
                Log.d(TAG, "Node_ID : " + up.getNode_ID());
                Log.d(TAG, "Node_X : " + up.getNode_X());
                Log.d(TAG, "Node_Y : " + up.getNode_Y());
                Log.d(TAG, "Node_Z : " + up.getLatitude());
            }

            return resultList;

        } catch (AmazonServiceException ex) {
            wipeCredentialsOnAuthError(ex);
        }
        return null;

    }
    public static ArrayList<NodeInfo> getNodeInfoArrayList() {
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);
        Log.d(TAG, "이거 null? : " + mapper.toString());
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();

        try {
            PaginatedScanList<NodeInfo> result = mapper.scan(NodeInfo.class, scanExpression);

            ArrayList<NodeInfo> resultList = new ArrayList<NodeInfo>();
            for (NodeInfo up : result) {
                resultList.add(up);
            }

            return resultList;

        } catch (AmazonServiceException ex) {
            wipeCredentialsOnAuthError(ex);
        }

        return null;
    }

    public static ArrayList<RegionInfo> getRegionInfoList() {
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);
        Log.d(TAG, "이거 null? : " + mapper.toString());
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        try {
            PaginatedScanList<RegionInfo> result = mapper.scan(
                    RegionInfo.class, scanExpression);

            ArrayList<RegionInfo> resultList = new ArrayList<RegionInfo>();
            for (RegionInfo up : result) {

                resultList.add(up);
                //str += "Author : " + up.getAuthor() + " Title : " + up.getTitle();
                Log.d(TAG, "MT_ID : " + up.getMountainID());
                Log.d(TAG, "Region_NAme : " + up.getRegionName());
                Log.d(TAG, "MT_Name : " + up.getMountainName());
                Log.d(TAG, "Latitude : " + up.getLatitude());
                Log.d(TAG, "Longitude : " + up.getLongitude());
                Log.d(TAG, "Status : " + up.getMountainStatus());
            }

            return resultList;

        } catch (AmazonServiceException ex) {
            wipeCredentialsOnAuthError(ex);
        }

        return null;
    }

    public static RegionInfo userSelectedRegionInfo(int MT_ID) {
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);
        try {
            RegionInfo region = mapper.load(RegionInfo.class, MT_ID);
            if(region != null){
                Log.d(TAG, "MT_ID : " + region.getMountainID());
                Log.d(TAG, "Region_NAme : " + region.getRegionName());
                Log.d(TAG, "MT_Name : " + region.getMountainName());
                Log.d(TAG, "Latitude : " + region.getLatitude());
                Log.d(TAG, "Longitude : " + region.getLongitude());
                Log.d(TAG, "Status : " + region.getMountainStatus());
            } else {
                Log.d(TAG, "Fucking Null");
            }

            return region;

        } catch (AmazonServiceException ex) {
            wipeCredentialsOnAuthError(ex);
        }

        return null;
    }

    public static void createTable() {

        Log.d(TAG, "Create table called");

        //AmazonDynamoDBClient ddb = dbManger.ddb();

        KeySchemaElement kse = new KeySchemaElement().withAttributeName(
                "MT_ID").withKeyType(KeyType.HASH);
        AttributeDefinition ad = new AttributeDefinition()
                .withAttributeName("Region_Name").withAttributeType(ScalarAttributeType.N);
        ProvisionedThroughput pt = new ProvisionedThroughput()
                .withReadCapacityUnits(10l).withWriteCapacityUnits(5l);

        CreateTableRequest request = new CreateTableRequest()
                .withTableName(Constants.TABLE_NAME)
                .withKeySchema(kse).withAttributeDefinitions(ad)
                .withProvisionedThroughput(pt);

        try {
            Log.d(TAG, "Sending Create table request");
            ddb.createTable(request);
            Log.d(TAG, "Create request response successfully recieved");
        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error sending create table request", ex);
            wipeCredentialsOnAuthError(ex);
        }
    }

    public static void createNodeTable() {

        Log.d(TAG, "Create table called");

        //AmazonDynamoDBClient ddb = dbManger.ddb();

        KeySchemaElement kse = new KeySchemaElement().withAttributeName(
                "Node_ID").withKeyType(KeyType.HASH);
        AttributeDefinition ad = new AttributeDefinition()
                .withAttributeName("MT_ID").withAttributeType(ScalarAttributeType.N)
        .withAttributeName("Route").withAttributeType(ScalarAttributeType.N)
                .withAttributeName("Node_X").withAttributeType(ScalarAttributeType.N)
                .withAttributeName("Node_Y").withAttributeType(ScalarAttributeType.N)
                .withAttributeName("Node_Z").withAttributeType(ScalarAttributeType.N)
                .withAttributeName("Variation");
        ProvisionedThroughput pt = new ProvisionedThroughput()
                .withReadCapacityUnits(5l).withWriteCapacityUnits(5l);

        CreateTableRequest request = new CreateTableRequest()
                .withTableName(Constants.NODE_TABLE_NAME)
                .withKeySchema(kse).withAttributeDefinitions(ad)
                .withProvisionedThroughput(pt);

        try {
            Log.d(TAG, "Sending Create table request");
            ddb.createTable(request);
            Log.d(TAG, "Create request response successfully recieved");
        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error sending create table request", ex);
            wipeCredentialsOnAuthError(ex);
        }
    }

    /*
     * Retrieves the table description and returns the table status as a string.
     */
    public static String getTestTableStatus() {

        try {
            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(Constants.NODE_TABLE_NAME);
            DescribeTableResult result = ddb.describeTable(request);

            String status = result.getTable().getTableStatus();
            return status == null ? "" : status;

        } catch (ResourceNotFoundException e) {
        } catch (AmazonServiceException ex) {
            wipeCredentialsOnAuthError(ex);
        }

        return "";
    }

    public static void insertNodeInfo() {
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);
        nodeInfoArrayList.add(new NodeInfo(1, 4, 0, 37.277318, 127.010757, 22, 456, 100, 1));
        nodeInfoArrayList.add(new NodeInfo(2, 4, 12, 37.278018, 127.011422,  444, 456, 100, 2));
        nodeInfoArrayList.add(new NodeInfo(3, 4, 13, 37.277993, 127.009609,  32, 564, 100, 3));
        nodeInfoArrayList.add(new NodeInfo(4, 4, 124, 37.279094, 127.011562, 125, 886, 100, 1));
        nodeInfoArrayList.add(new NodeInfo(5, 4, 125, 37.279103, 127.010113, 351, 234, 100, 1));
        nodeInfoArrayList.add(new NodeInfo(6, 4, 136, 37.279077, 127.009019, 234, 155, 100, 3));

        try {
            for(NodeInfo node : nodeInfoArrayList){
                NodeInfo n = new NodeInfo();
                n.setNode_ID(node.getNode_ID());
                n.setMT_ID(node.getMT_ID());
                n.setRoute(node.getRoute());
                n.setLatitude(node.getLatitude());
                n.setLongitude(node.getLongitude());
                n.setNode_X(node.getNode_X());
                n.setNode_Y(node.getNode_Y());
                n.setNode_Z(node.getNode_Z());
                n.setVariation(node.getVariation());
                mapper.save(n);
            }
        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error inserting users");
            wipeCredentialsOnAuthError(ex);
        }
    }

    /* Inserts ten users with userNo from 1 to 10 and random names.

     */

    public static void insertUsers() {
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);
        regionInfoArrayList.add(new RegionInfo(1, "서울", "관악산", 37.442009, 126.963038));
        regionInfoArrayList.add(new RegionInfo(2, "서울", "북한산", 37.658221, 126.978898));

        regionInfoArrayList.add(new RegionInfo(3, "경기도", "청계산", 37.422564, 127.042691));
        regionInfoArrayList.add(new RegionInfo(4, "경기도", "팔달산", 37.279145, 127.009727));
        regionInfoArrayList.add(new RegionInfo(5, "경기도", "광교산", 37.343499, 127.019157));

        regionInfoArrayList.add(new RegionInfo(6, "강원도", "태백산", 37.098211, 128.922869));

        try {
            for(RegionInfo region : regionInfoArrayList){
                RegionInfo r = new RegionInfo();
                r.setMountainID(region.getMountainID());
                r.setRegionName(region.getRegionName());
                r.setMountainName(region.getMountainName());
                r.setLatitude(region.getLatitude());
                r.setLongitude(region.getLongitude());
                r.setMountainStatus(region.getMountainStatus());

                mapper.save(region);
            }
        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error inserting users");
            wipeCredentialsOnAuthError(ex);
        }
    }

    /*
     * Scans the table and returns the list of users.
     */
    public static boolean wipeCredentialsOnAuthError(AmazonServiceException ex) {
        Log.e(TAG, "Error, wipeCredentialsOnAuthError called" + ex);
        if (
            // STS
            // http://docs.amazonwebservices.com/STS/latest/APIReference/CommonErrors.html
                ex.getErrorCode().equals("IncompleteSignature")
                        || ex.getErrorCode().equals("InternalFailure")
                        || ex.getErrorCode().equals("InvalidClientTokenId")
                        || ex.getErrorCode().equals("OptInRequired")
                        || ex.getErrorCode().equals("RequestExpired")
                        || ex.getErrorCode().equals("ServiceUnavailable")

                        // DynamoDB
                        // http://docs.amazonwebservices.com/amazondynamodb/latest/developerguide/ErrorHandling.html#APIErrorTypes
                        || ex.getErrorCode().equals("AccessDeniedException")
                        || ex.getErrorCode().equals("IncompleteSignatureException")
                        || ex.getErrorCode().equals(
                        "MissingAuthenticationTokenException")
                        || ex.getErrorCode().equals("ValidationException")
                        || ex.getErrorCode().equals("InternalFailure")
                        || ex.getErrorCode().equals("InternalServerError")) {

            return true;
        }

        return false;
    }

}
