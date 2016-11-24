package com.lan.capstonedesign;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;

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

    Runnable selectNodeRunnable = new Runnable() {
        public void run() {

            while(true){
                nodeInfoArrayList = dbManager.getNodeInfoArrayList();
//                nodeInfoArrayList = dbManager.getLastetNodeInfo();
                Log.d(TAG, "Thread 돌고 있당~!");
                try {
                    Thread.sleep(5000);
                    node_adapter.setNodeArrayList(nodeInfoArrayList);
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            node_adapter.notifyDataSetChanged();
//                            m_ListView.invalidateViews();
//                            m_ListView.refreshDrawableState();

                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }



        }
    };
    Runnable onUiRunnable = new Runnable() {
        @Override
        public void run() {
            node_adapter.notifyDataSetChanged();
            Log.d(TAG, "UI Thread 돌고 있당~!");
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();
        t.interrupt();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        t.interrupt();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.node_listview);

        if(getIntent().getExtras() != null) {
            mt_id = getIntent().getExtras().getInt("MT_ID");
        }
        Log.d(TAG, "Adpater onCreate!");
        dbManager = DynamoDBManager.getInstance(this);


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
}
