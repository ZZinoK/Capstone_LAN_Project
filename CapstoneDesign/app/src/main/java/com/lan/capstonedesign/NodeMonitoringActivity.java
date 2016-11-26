package com.lan.capstonedesign;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by kslee7746 on 2016. 11. 10..
 */

public class NodeMonitoringActivity extends Activity {

    private ListView m_ListView;
    private NodeCustomAdapter node_adapter;
    DynamoDBManager dbManager;
    private ArrayList<NodeInfo> nodeInfoArrayList;
    private Thread t;
    private static final String TAG = "MonitoringAdapter";
    private int mt_id = 0;
    private String mt_name = null;
    private Boolean threadState = true;

    Runnable selectNodeRunnable = new Runnable() {
        public void run() {

            while(threadState){
                try {
                    nodeInfoArrayList = dbManager.getNodeInfoArrayList();
                    if(nodeInfoArrayList.equals(null)){
                        nodeInfoArrayList = dbManager.getNodeInfoArrayList();
                        Thread.sleep(1500);
                    }
                    Collections.sort(nodeInfoArrayList, new NoAscCompare());
                    Log.d(TAG, "Thread 돌고 있당~!");
                    Thread.sleep(5000);
                    node_adapter.setNodeArrayList(nodeInfoArrayList);
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            node_adapter.notifyDataSetChanged();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    static class NoAscCompare implements Comparator<NodeInfo> {
        @Override
        public int compare(NodeInfo arg0, NodeInfo arg1) {
            return arg0.getNode_ID() < arg1.getNode_ID() ? -1 : arg0.getNode_ID() > arg1.getNode_ID() ? 1:0;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.node_listview);
        SharedPreferences saveData = getSharedPreferences("Setting", MODE_PRIVATE);
        mt_name = saveData.getString("MT_NAME", "서비스 준비 중인 지역입니다.");

        if(getIntent().getExtras() != null) {
            mt_id = getIntent().getExtras().getInt("MT_ID");
        }
        Log.d(TAG, "Adpater onCreate!");
        dbManager = DynamoDBManager.getInstance(this);
        TextView mtTxtView = (TextView) findViewById(R.id.mt_name_txtView);
        mtTxtView.setText(mt_name + "의 현재 노드 상태");

        t = new Thread(selectNodeRunnable);
        t.start();
        try {
            t.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        node_adapter = new NodeCustomAdapter(getLayoutInflater());
        node_adapter.setNodeArrayList(nodeInfoArrayList);
        m_ListView = (ListView) findViewById(R.id.node_listview);
        m_ListView.setAdapter(node_adapter);
    }
    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();
        threadState = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        threadState = false;
    }
}
