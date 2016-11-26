package com.lan.capstonedesign;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kslee7746 on 2016. 11. 4..
 */

public class SelectRegionActivity extends Activity {
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    ArrayList<RegionInfo> regionInfoArrayList = new ArrayList<>();
    static String TAG = "SelectRegionActiviry";
    Thread mythread;
    double latitude = 0, longitude = 0;
    int MT_ID = 0;
    String MT_Name = null;

    DynamoDBManager dbManager;
    Runnable runnable = new Runnable() {
        public void run() {
            try{
                regionInfoArrayList = dbManager.getRegionInfoList();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_region);

        dbManager = DynamoDBManager.getInstance(this);
        mythread = new Thread(runnable);
        mythread.start();
        try {
            mythread.sleep(1200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        prepareListData();
        expListView = (ExpandableListView) findViewById(R.id.lvExp);
        listAdapter = new ExpandableListAdapter(SelectRegionActivity.this, listDataHeader, listDataChild);
        expListView.setAdapter(listAdapter);
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return false;
            }
        });

        // Listview Group expanded listener
        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) { }
        });

        // Listview Group collasped listener
        expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) { }
        });

        // Listview on child click listener
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                String selectedChildName = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);

                for(int i=0; i<regionInfoArrayList.size(); i++) {
                    if (selectedChildName.equals(regionInfoArrayList.get(i).getMountainName())) {
                        MT_ID = regionInfoArrayList.get(i).getMountainID();
                        MT_Name = regionInfoArrayList.get(i).getMountainName();
//                        latitude = regionInfoArrayList.get(i).getLatitude();
//                        longitude = regionInfoArrayList.get(i).getLongitude();
                        Toast.makeText(getApplicationContext(), "MT_ID" + MT_ID, Toast.LENGTH_SHORT).show();
                        break;
                    }
                }

                return false;
            }
        });

        Button settingBtn = (Button) findViewById(R.id.settingButton);
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MT_ID == 0){
                    Toast.makeText(getApplicationContext(), "산사태 확인 할 지역을 선택하세요!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Intent intent = new Intent();
                    //intent.putExtra("latitude", latitude);
                    intent.putExtra("MT_NAME", MT_Name);
                    intent.putExtra("MT_ID", MT_ID);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    public void prepareListData() {
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        String region_name[] = {"서울", "경기도", "강원도"};

        for(int i=0; i<region_name.length; i++){
            List<String> regionArray = new ArrayList<String>();

            listDataHeader.add(region_name[i]);
            for(RegionInfo r : regionInfoArrayList){
                if(region_name[i].equals(r.getRegionName())) {
                    regionArray.add(r.getMountainName());
                }
            }
            listDataChild.put(listDataHeader.get(i), regionArray);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
