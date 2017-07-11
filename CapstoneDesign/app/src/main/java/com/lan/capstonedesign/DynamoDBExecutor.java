package com.lan.capstonedesign;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by kslee7746 on 2016. 11. 5..
 */

public class DynamoDBExecutor extends Activity {
    private static final String TAG = "DynamoDBExecutor";
    public static AmazonClientManager clientManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check_db);

        clientManager = new AmazonClientManager(this);
        final Button createTableBttn = (Button) findViewById(R.id.create_table_bttn);

        createTableBttn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Log.i(TAG, "createTableBtn clicked.");

                new DynamoDBManagerTask()
                        .execute(DynamoDBManagerType.CREATE_TABLE);
            }
        });

        final Button insertUsersBttn = (Button) findViewById(R.id.insert_users_bttn);
        insertUsersBttn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Log.i(TAG, "insertUsersBtn clicked.");

                new DynamoDBManagerTask()
                        .execute(DynamoDBManagerType.INSERT_USER);
            }
        });
        final Button listUsersBttn = (Button) findViewById(R.id.list_users_bttn);
        listUsersBttn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Log.i(TAG, "listUsersBtn clicked.");

                new DynamoDBManagerTask()
                        .execute(DynamoDBManagerType.LIST_USERS);
            }
        });

        /*final Button deleteTableBttn = (Button) findViewById(R.id.delete_table_bttn);
        deleteTableBttn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Log.i(TAG, "deleteTableBttn clicked.");

                new DynamoDBManagerTask().execute(DynamoDBManagerType.CLEAN_UP);
            }
        });*/
    }


    private class DynamoDBManagerTask extends
            AsyncTask<DynamoDBManagerType, Void, DynamoDBManagerTaskResult> {

        protected DynamoDBManagerTaskResult doInBackground(
                DynamoDBManagerType... types) {

            DynamoDBManager dbManager = DynamoDBManager.getInstance(DynamoDBExecutor.this);
            String tableStatus = dbManager.getTestTableStatus();
            DynamoDBManagerTaskResult result = new DynamoDBManagerTaskResult();

            Log.d(TAG, "table status : " + result.toString());
            result.setTableStatus(tableStatus);
            result.setTaskType(types[0]);

            if (types[0] == DynamoDBManagerType.CREATE_TABLE) {
                if (tableStatus.length() == 0) {
                    dbManager.createNodeTable();
                }
            } else if (types[0] == DynamoDBManagerType.INSERT_USER) {
                if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                    dbManager.insertNodeInfo();
                }
            } else if (types[0] == DynamoDBManagerType.LIST_USERS) {
                if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                    dbManager.getRegionInfoList();
                }
            } /*else if (types[0] == DynamoDBManagerType.CLEAN_UP) {
                if (tableStatus.equalsIgnoreCase("ACTIVE")) {
                    DynamoDBManager.cleanUp();
                }
            }*/

            return result;
        }

        protected void onPostExecute(DynamoDBManagerTaskResult result) {

            if (result.getTaskType() == DynamoDBManagerType.CREATE_TABLE) {

                if (result.getTableStatus().length() != 0) {
                    Toast.makeText(
                            DynamoDBExecutor.this,
                            "The test table already exists.\nTable Status: "
                                    + result.getTableStatus(),
                            Toast.LENGTH_LONG).show();
                }

            } else if (result.getTaskType() == DynamoDBManagerType.LIST_USERS
                    && result.getTableStatus().equalsIgnoreCase("ACTIVE")) {

                startActivity(new Intent(DynamoDBExecutor.this,
                        MainActivity.class));

            } else if (!result.getTableStatus().equalsIgnoreCase("ACTIVE")) {

                Toast.makeText(
                        DynamoDBExecutor.this,
                        "The test table is not ready yet.\nTable Status: "
                                + result.getTableStatus(), Toast.LENGTH_LONG)
                        .show();
            } else if (result.getTableStatus().equalsIgnoreCase("ACTIVE")
                    && result.getTaskType() == DynamoDBManagerType.INSERT_USER) {
                Toast.makeText(DynamoDBExecutor.this,
                        "Users inserted successfully!", Toast.LENGTH_SHORT).show();
            }
        }
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
}
