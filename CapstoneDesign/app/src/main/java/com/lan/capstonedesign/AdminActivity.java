package com.lan.capstonedesign;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by kslee7746 on 2016. 11. 9..
 */

public class AdminActivity extends Activity {
    private static final String TAG = "AdminActivity";
    private int mt_id;
    private boolean AdminLoginStatus = true;
    private String SERVER_IP = "52.78.233.109"; //aws ec2 instance ip
    private DynamoDBManager dbManager = null;
    private ArrayList<NodeInfo> nodeInfoArrayList;
    private int dangerCount = 0;
    private boolean checkThreadStatus = false;
    private boolean alarmBtnStatus = false;
    private Button alarmMode;
    Thread checkDangerThread;
    Runnable checkDangerRegion = new Runnable() {
        public void run() {
            while(true){
                if(checkThreadStatus){
                    try {
                        nodeInfoArrayList = dbManager.getNodeInfoArrayList();
                        if(nodeInfoArrayList.equals(null)){
                            nodeInfoArrayList = dbManager.getNodeInfoArrayList();
                            Thread.sleep(1500);
                        }
                        for(NodeInfo node : nodeInfoArrayList){
                            if(node.getVariation() == Constants.DANGER){
                                dangerCount++;
                                showToast("Danger Count : " + dangerCount);
                            }
                        }
                        if(dangerCount > 1){
                            sendPushToFCMServer("alert");
                            Thread.sleep(8000);
                            Log.d(TAG, "관리자 알람 보냄");
                            dangerCount = 0;
                        } else {
                            dangerCount = 0;
                            Log.d(TAG, "Check Thread 돌고 있당~!");
                            Log.d(TAG, "Danger Count : " + dangerCount);
                            Thread.sleep(3000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        checkDangerThread.interrupt();
        checkThreadStatus = false;
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_main);
        FirebaseMessaging.getInstance().subscribeToTopic("admin"); //Setting Topic for receive Push
        FirebaseMessaging.getInstance().unsubscribeFromTopic("lan");
        dbManager = DynamoDBManager.getInstance(this);
        checkDangerThread = new Thread(checkDangerRegion);
        checkDangerThread.start();
        SharedPreferences saveData = getSharedPreferences("Setting", MODE_PRIVATE);
        mt_id = saveData.getInt("MT_ID", 0);

        Button monitoringBtn = (Button) findViewById(R.id.monitoringNodeBtn);

        monitoringBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, NodeMonitoringActivity.class);
                intent.putExtra("MT_ID", mt_id);
                startActivity(intent);
            }
        });

        Button showNodeMap = (Button) findViewById(R.id.showNodeMap);
        showNodeMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, DaumMapActivity.class);
                intent.putExtra("Admin", AdminLoginStatus);
                intent.putExtra("MT_ID", 4);
                startActivity(intent);

            }
        });

        Button pushAlertBtn = (Button) findViewById(R.id.sendPush);
        pushAlertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sendPushToFCMServer("send");
                    }
                }).start();
            }
        });
        Button ipSettingBtn = (Button) findViewById(R.id.settingIP);
        ipSettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showIpConfigDialog();
            }
        });

        alarmMode = (Button) findViewById(R.id.alarmMode);
        alarmMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(alarmBtnStatus){
                            alarmMode.setText("모니터링 OFF");
                            checkThreadStatus = false;
                            alarmBtnStatus = false;
                        } else {
                            alarmMode.setText("모니터링 ON");
                            checkThreadStatus = true;
                            alarmBtnStatus = true;

                        }
                    }
                });
            }
        });
    }

    //thread.start();
    private void sendPushToFCMServer(String msgType){
        Socket socket;
        final int SERVERPORT = 13588;

        try {
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
            socket = new Socket(serverAddr, SERVERPORT);
            OutputStream out = socket.getOutputStream();
            PrintWriter output = new PrintWriter(out);
            output.flush();
            output.print(msgType);

            showToast("Push 메시지 전송 완료");
            output.close();
            socket.close();
        } catch(Exception e) {
            showToast("FCM Server가 닫혀있습니다.\nIP를 확인하세요.");
            e.printStackTrace();
        }
    }
    private void showIpConfigDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        View prompt = li.inflate(R.layout.ip_config_view, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(prompt);

        final EditText ipText = (EditText) prompt.findViewById(R.id.serverIpTxt);
        ipText.setText(SERVER_IP);

        alertDialogBuilder.setTitle("FCM Server IP 설정");
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("설정", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String ip = ipText.getText().toString();
                        SERVER_IP = ip;
                        Toast.makeText(AdminActivity.this, "IP 설정 완료\n" + SERVER_IP , Toast.LENGTH_SHORT).show();

                    }
                });

        alertDialogBuilder.setNegativeButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();

            }
        });

        alertDialogBuilder.show();
    }
    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AdminActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
