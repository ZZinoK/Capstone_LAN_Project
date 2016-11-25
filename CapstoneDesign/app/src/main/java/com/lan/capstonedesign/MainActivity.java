package com.lan.capstonedesign;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import static java.lang.System.in;
import static java.lang.System.out;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    double lat = 0, lon = 0;
    int SECOND_ACTIVITY = 2;
    String status = "safe";
    int mt_id = 0;
    String mt_name = null;
    private static final String adminID = "admin";
    private static final String adminPwd = "1234";
    private String networkState = null;
    private TextView mt_status_view;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SECOND_ACTIVITY) {
            if(resultCode == RESULT_OK){
                mt_id = data.getIntExtra("MT_ID", 0);
                mt_name = data.getStringExtra("MT_NAME");

                SharedPreferences userData = getSharedPreferences("Setting", MODE_PRIVATE);
                SharedPreferences.Editor editor = userData.edit();
                editor.putInt("MT_ID", mt_id);
                editor.putString("MT_NAME", mt_name);
                mt_status_view.setText("설정된 산 이름 : " + mt_name);
                editor.commit();
                //lat = data.getDoubleExtra("latitude", 000.000000);
                //lon = data.getDoubleExtra("longitude", 000.000000);
                Toast.makeText(MainActivity.this, "MT_ID from user : " + mt_id, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseMessaging.getInstance().subscribeToTopic("lan"); //Setting Topic for receive Push
        ConnectivityManager manager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        mt_status_view = (TextView) findViewById(R.id.mt_status);

        // wifi 또는 모바일 네트워크 어느 하나라도 연결이 되어있다면,
        if (!wifi.isConnected()){
            networkState = "WIFI";
            alertCheckNetwork();
        } /*else if(!mobile.isConnected()){
            networkState = "DATA";
            alertCheckNetwork();
        }*/
        // [START handle_data_extras]
        // User Shared Data
        SharedPreferences saveData = getSharedPreferences("Setting", MODE_PRIVATE);
        mt_name = saveData.getString("MT_NAME", "지역을 선택해주세요.");
        mt_id = saveData.getInt("MT_ID", 0);
        // User Shared Data End
        mt_status_view.setText("설정된 산 이름 : " + mt_name);
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);

                if(key.equals("MT_ID")){
                    mt_id = Integer.parseInt(value.toString());
                }
            }
            Intent intent = new Intent(MainActivity.this, DaumMapActivity.class);//푸시알람 클릭시 띄울 다음 지도
            intent.putExtra("MT_ID", mt_id);
            startActivity(intent);

        }
        // [END handle_data_extras]



        Button subscribeButton = (Button) findViewById(R.id.selectRegion);
        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SelectRegionActivity.class);
                startActivityForResult(intent, SECOND_ACTIVITY);
            }
        });

        Button logTokenButton = (Button) findViewById(R.id.stateRegion);
        logTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DaumMapActivity.class);
                intent.putExtra("MT_ID", mt_id);
                startActivity(intent);
            }
        });


        Button awsDBCheckBtn = (Button) findViewById(R.id.dbCheck);
        awsDBCheckBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DynamoDBExecutor.class);
                startActivity(intent);
            }
        });

        Button loginAdminBtn = (Button) findViewById(R.id.loginAdmin);
        loginAdminBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginDialog();
            }
        });

    }
    private void moveConfigWiFi(String networkStatus) {
        Intent intent;
        if(networkStatus.equals("WIFI"))
            intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        else
            intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
        startActivity(intent);
    }
    private void alertCheckNetwork() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("WiFi 또는 데이터 사용 설정")
                .setMessage("무선 네트워크 사용 또는 Wi-Fi 연결이 되야 서비스 이용이 가능합니다.\n네트워크 설정을 하시겠습니까?")
                .setPositiveButton("설정",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                moveConfigWiFi(networkState);
                            }
                        })
                .setNegativeButton("확인",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }
    private void showLoginDialog() {
        //dbManager = DynamoDBManager.getInstance(this);
        LayoutInflater li = LayoutInflater.from(this);
        View prompt = li.inflate(R.layout.login_view, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(prompt);

        final EditText admin_id = (EditText) prompt.findViewById(R.id.login_id);
        final EditText pwd = (EditText) prompt.findViewById(R.id.login_pwd);
        admin_id.setText(adminID);
        pwd.setText(adminPwd);

        alertDialogBuilder.setTitle("관리자 로그인");
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("로그인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String password = pwd.getText().toString();
                        String adminID = admin_id.getText().toString();


                        Toast.makeText(MainActivity.this, "ID : " + adminID + " Pass : " + password, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                        startActivity(intent);
                    }
                });

        alertDialogBuilder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();

            }
        });

        alertDialogBuilder.show();
        if (adminID.length()>1) //if we have the username saved then focus on password field, be user friendly :-)
            pwd.requestFocus();
    }

}
