package com.lan.capstonedesign;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Created by kslee7746 on 2016. 11. 9..
 */

public class AdminActivity extends Activity {
    private static final String TAG = "AdminActivity";
    private int mt_id;
    private boolean AdminLoginStatus = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_main);

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
                        sendPushToFCMServer();
                    }
                }).start();
            }
        });


    }

    //thread.start();
    private void sendPushToFCMServer(){
        Socket socket;
        final int SERVERPORT = 8888;
        final String SERVER_IP = "172.20.10.10"; //"192.168.0.96";
        boolean receivedAck = false;


        try {
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
            socket = new Socket(serverAddr, SERVERPORT);
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter output = new PrintWriter(out);
            output.flush();
            output.print("send");

//            while(!receivedAck){
//                String readData = input.readLine();
//                if(readData.equals("Success")){
//                    Toast.makeText(this, readData, Toast.LENGTH_SHORT).show();
//                    break;
//                }
//            }
            output.close();
            socket.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
