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

import android.util.Log;
import android.widget.Toast;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;

import java.util.ArrayList;

public class DynamoDBManager {

    private static final String TAG = "DynamoDBManager";

    /*
     * Creates a table with the following attributes: Table name: testTableName
     * Hash key: userNo type N Read Capacity Units: 10 Write Capacity Units: 5
     */
    public static ArrayList<RegionInfo> regionInfoArrayList = new ArrayList<>();

    public static void createTable() {

        Log.d(TAG, "Create table called");

        AmazonDynamoDBClient ddb = DynamoDBExecutor.clientManager
                .ddb();

        KeySchemaElement kse = new KeySchemaElement().withAttributeName(
                "MT_ID").withKeyType(KeyType.HASH);
        AttributeDefinition ad = new AttributeDefinition()
                .withAttributeName("Region_Name").withAttributeType(ScalarAttributeType.N);
        /*.withAttributeName("MT_Name").withAttributeType(ScalarAttributeType.N)
                .withAttributeName("Latitude").withAttributeType(ScalarAttributeType.N)
                .withAttributeName("Longitude").withAttributeType(ScalarAttributeType.N)
                .withAttributeName("Status").withAttributeType(ScalarAttributeType.N)*/
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
            DynamoDBExecutor.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }

    /*
     * Retrieves the table description and returns the table status as a string.
     */
    public static String getTestTableStatus() {

        try {
            AmazonDynamoDBClient ddb = DynamoDBExecutor.clientManager
                    .ddb();

            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(Constants.TABLE_NAME);
            DescribeTableResult result = ddb.describeTable(request);

            String status = result.getTable().getTableStatus();
            return status == null ? "" : status;

        } catch (ResourceNotFoundException e) {
        } catch (AmazonServiceException ex) {
            DynamoDBExecutor.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return "";
    }

    /*
     * Inserts ten users with userNo from 1 to 10 and random names.
     */
    public static void insertUsers() {
        AmazonDynamoDBClient ddb = DynamoDBExecutor.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);
        regionInfoArrayList.add(new RegionInfo("관악산", 1, 37.442009, 126.963038));
        regionInfoArrayList.add(new RegionInfo("북한산", 2, 37.658221, 126.978898));

        regionInfoArrayList.add(new RegionInfo("청계산", 3, 37.422564, 127.042691));
        regionInfoArrayList.add(new RegionInfo("팔달산", 4, 37.279145, 127.009727));
        regionInfoArrayList.add(new RegionInfo("광교산", 5, 37.343499, 127.019157));

        regionInfoArrayList.add(new RegionInfo("태백산", 6, 37.098211, 128.922869));
        /*try {
            for (int i = 1; i <= 10; i++) {
                Books book = new Books();
                book.setTitle("Algorithm");
                book.setIsbn(Constants.getRandomName());
                book.setAuthor(Constants.getRandomName());

                Log.d(TAG, "Inserting users");
                mapper.save(book);
                Log.d(TAG, "Users inserted");
            }
        } catch (AmazonServiceException ex) {
            Log.e(TAG, "Error inserting users");
            DynamoDBExecutor.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }*/
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
            DynamoDBExecutor.clientManager.wipeCredentialsOnAuthError(ex);
        }
    }

    /*
     * Scans the table and returns the list of users.
     */
    public static ArrayList<RegionInfo> getRegionInfoList() {

        AmazonDynamoDBClient ddb = DynamoDBExecutor.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

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
            DynamoDBExecutor.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return null;
    }

    /*
     * Retrieves all of the attribute/value pairs for the specified user.
     */
    /*public static UserPreference getUserPreference(int userNo) {

        AmazonDynamoDBClient ddb = MainActivity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            UserPreference userPreference = mapper.load(UserPreference.class,
                    userNo);

            return userPreference;

        } catch (AmazonServiceException ex) {
            MainActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }

        return null;
    }*/

    /*
     * Updates one attribute/value pair for the specified user.
     */
    /*public static void updateUserPreference(UserPreference updateUserPreference) {

        AmazonDynamoDBClient ddb = MainActivity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            mapper.save(updateUserPreference);

        } catch (AmazonServiceException ex) {
            MainActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }*/

    /*
     * Deletes the specified user and all of its attribute/value pairs.
     */
    /*public static void deleteUser(UserPreference deleteUserPreference) {

        AmazonDynamoDBClient ddb = MainActivity.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);

        try {
            mapper.delete(deleteUserPreference);

        } catch (AmazonServiceException ex) {
            MainActivity.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }*/

    /*
     * Deletes the test table and all of its users and their attribute/value
     * pairs.
     */
    /*public static void cleanUp() {

        AmazonDynamoDBClient ddb = DynamoDBExecutor.clientManager
                .ddb();

        DeleteTableRequest request = new DeleteTableRequest()
                .withTableName(Constants.TEST_TABLE_NAME);
        try {
            ddb.deleteTable(request);

        } catch (AmazonServiceException ex) {
            DynamoDBExecutor.clientManager
                    .wipeCredentialsOnAuthError(ex);
        }
    }*/


}
