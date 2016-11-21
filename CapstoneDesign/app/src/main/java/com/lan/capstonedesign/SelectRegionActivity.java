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

    double latitude = 0, longitude = 0;
    int MT_ID = 0;

    DynamoDBManager dbManager;
    Runnable runnable = new Runnable() {
        public void run() {
            dbManager.dynamoDBSelect();
            regionInfoArrayList = dbManager.getRegionInfoList();

            for (RegionInfo up : regionInfoArrayList) {
                //resultList.add(up);
                //str += "Author : " + up.getAuthor() + " Title : " + up.getTitle();
                Log.d(TAG, "MT_ID : " + up.getMountainID());
                Log.d(TAG, "Region_NAme : " + up.getRegionName());
                Log.d(TAG, "MT_Name : " + up.getMountainName());
                Log.d(TAG, "Latitude : " + up.getLatitude());
                Log.d(TAG, "Longitude : " + up.getLongitude());
                Log.d(TAG, "Status : " + up.getMountainStatus());
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_region);

        dbManager = DynamoDBManager.getInstance(this);
        Thread mythread = new Thread(runnable);
        mythread.start();
        try {
            mythread.sleep(1000);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Button settingBtn = (Button) findViewById(R.id.settingButton);
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MT_ID == 0){
                    Toast.makeText(getApplicationContext(),
                        "산사태 확인 할 지역을 선택하세요!",
                        Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Intent intent = new Intent();
                    //intent.putExtra("latitude", latitude);
                    intent.putExtra("MT_ID", longitude);
                    Toast.makeText(getApplicationContext(), " MT_ID : " + MT_ID, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        expListView = (ExpandableListView) findViewById(R.id.lvExp);



        prepareListData();

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
                        latitude = regionInfoArrayList.get(i).getLatitude();
                        longitude = regionInfoArrayList.get(i).getLongitude();
                        Toast.makeText(getApplicationContext(), selectedChildName + " Latitude : " + regionInfoArrayList.get(i).getLatitude() + " Longitude : " +
                                regionInfoArrayList.get(i).getLongitude(), Toast.LENGTH_SHORT).show();
                        break;
                    }
                }

                return false;
            }
        });
    }
    public void settingRegionData(){
        regionInfoArrayList.add(new RegionInfo("관악산", 2, 37.442009, 126.963038));
        regionInfoArrayList.add(new RegionInfo("북한산", 3, 37.658221, 126.978898));

        regionInfoArrayList.add(new RegionInfo("청계산", 1, 37.422564, 127.042691));
        regionInfoArrayList.add(new RegionInfo("팔달산", 4, 37.279145, 127.009727));
        regionInfoArrayList.add(new RegionInfo("광교산", 5, 37.343499, 127.019157));

        regionInfoArrayList.add(new RegionInfo("태백산", 6, 37.098211, 128.922869));


    }
    public void prepareListData() {
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add("서울");
        listDataHeader.add("경기도");
        listDataHeader.add("강원도");

        // Adding child data
        List<String> seoul = new ArrayList<String>();

        seoul.add(regionInfoArrayList.get(0).getMountainName());
        seoul.add(regionInfoArrayList.get(1).getMountainName());

//        seoul.add("청계산");
//        seoul.add("남한산성");

        List<String> gyunki = new ArrayList<String>();
        gyunki.add(regionInfoArrayList.get(2).getMountainName());
        gyunki.add(regionInfoArrayList.get(3).getMountainName());
        gyunki.add(regionInfoArrayList.get(4).getMountainName());
//
//        gyunki.add("팔달산");
//        gyunki.add("광교산");

        List<String> gangone = new ArrayList<String>();
        gangone.add(regionInfoArrayList.get(5).getMountainName());



//        gangone.add("태백산");


        listDataChild.put(listDataHeader.get(0), seoul); // Header, Child data
        listDataChild.put(listDataHeader.get(1), gyunki);
        listDataChild.put(listDataHeader.get(2), gangone);
    }
}
