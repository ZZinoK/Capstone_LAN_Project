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
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
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
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import java.util.ArrayList;

public class DynamoDBManager {

    private static final String TAG = "DynamoDBManager";
    public static AmazonDynamoDBClient ddb = null;
    private static Context context;
    public static ArrayList<RegionInfo> regionInfoArrayList = new ArrayList<>();

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
    public void dynamoDBSelect(){
        new DynamoDBManagerTask().execute(DynamoDBManagerType.LIST_USERS);
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

    public static void createTable() {

        Log.d(TAG, "Create table called");

        //AmazonDynamoDBClient ddb = dbManger.ddb();

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
            wipeCredentialsOnAuthError(ex);
        }
    }

    /*
     * Retrieves the table description and returns the table status as a string.
     */
    public static String getTestTableStatus() {

        try {
            /*AmazonDynamoDBClient ddb = ddb.;*/

            DescribeTableRequest request = new DescribeTableRequest()
                    .withTableName(Constants.TABLE_NAME);
            DescribeTableResult result = ddb.describeTable(request);

            String status = result.getTable().getTableStatus();
            return status == null ? "" : status;

        } catch (ResourceNotFoundException e) {
        } catch (AmazonServiceException ex) {
            wipeCredentialsOnAuthError(ex);
        }

        return "";
    }

    /*
     * Inserts ten users with userNo from 1 to 10 and random names.
     */
    /*public static void insertUsers() {
        AmazonDynamoDBClient ddb = DynamoDBExecutor.clientManager
                .ddb();
        DynamoDBMapper mapper = new DynamoDBMapper(ddb);
        regionInfoArrayList.add(new RegionInfo("관악산", 1, 37.442009, 126.963038));
        regionInfoArrayList.add(new RegionInfo("북한산", 2, 37.658221, 126.978898));

        regionInfoArrayList.add(new RegionInfo("청계산", 3, 37.422564, 127.042691));
        regionInfoArrayList.add(new RegionInfo("팔달산", 4, 37.279145, 127.009727));
        regionInfoArrayList.add(new RegionInfo("광교산", 5, 37.343499, 127.019157));

        regionInfoArrayList.add(new RegionInfo("태백산", 6, 37.098211, 128.922869));
        *//*try {
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
        }*//*
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
    }*/

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
    private class DynamoDBManagerTask extends AsyncTask<DynamoDBManagerType, Void, DynamoDBManagerTaskResult> {

        protected DynamoDBManagerTaskResult doInBackground(
                DynamoDBManagerType... types) {

            String tableStatus = getTestTableStatus();

            DynamoDBManagerTaskResult result = new DynamoDBManagerTaskResult();
            Log.d(TAG, "table status : " + result.toString());
            result.setTableStatus(tableStatus);
            result.setTaskType(types[0]);

            if (types[0] == DynamoDBManagerType.CREATE_TABLE) {
                if (tableStatus.length() == 0) {
                    createTable();
                }
            } /*else if (types[0] == DynamoDBManagerType.INSERT_USER) {
                if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                    insertUsers();
                }
            } */else if (types[0] == DynamoDBManagerType.LIST_USERS) {
                if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                    regionInfoArrayList = getRegionInfoList();
                }
            } /*else if (types[0] == DynamoDBManagerType.CLEAN_UP) {
                if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                    DynamoDBManager.cleanUp();
                }
            }*/

            return result;
        }

        /*protected void onPostExecute(DynamoDBManagerTaskResult result) {

            if (result.getTaskType() == DynamoDBManagerType.CREATE_TABLE) {

                if (result.getTableStatus().length() != 0) {
                    Toast.makeText(context,
                            "The test table already exists.\nTable Status: "
                                    + result.getTableStatus(),
                            Toast.LENGTH_LONG).show();
                }
            } else if (result.getTaskType() == DynamoDBManagerType.LIST_USERS
                    && result.getTableStatus().equalsIgnoreCase("ACTIVE")) {


            } else if (!result.getTableStatus().equalsIgnoreCase("ACTIVE")) {

                Toast.makeText(context, "The test table is not ready yet.\nTable Status: "
                                + result.getTableStatus(), Toast.LENGTH_LONG)
                        .show();
            } else if (result.getTableStatus().equalsIgnoreCase("ACTIVE")
                    && result.getTaskType() == DynamoDBManagerType.INSERT_USER) {
                Toast.makeText(context, "Users inserted successfully!", Toast.LENGTH_SHORT).show();
            }
        }*/
    }

    private enum DynamoDBManagerType {
        GET_TABLE_STATUS, CREATE_TABLE, INSERT_USER, LIST_USERS, CLEAN_UP
    }

    private class DynamoDBManagerTaskResult {
        private DynamoDBManagerType taskType;
        private String tableStatus;

        public DynamoDBManagerType getTaskType() {
            return taskType;
        }

        public void setTaskType(DynamoDBManagerType taskType) {
            this.taskType = taskType;
        }

        public String getTableStatus() {
            return tableStatus;
        }

        public void setTableStatus(String tableStatus) {
            this.tableStatus = tableStatus;
        }
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
