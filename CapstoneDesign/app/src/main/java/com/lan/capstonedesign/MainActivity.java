package com.lan.capstonedesign;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    double lat = 0, lon = 0;
    int SECOND_ACTIVITY = 2;
    String status = "safe";
    int mt_id = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SECOND_ACTIVITY) {
            if(resultCode == RESULT_OK){
                mt_id = data.getIntExtra("MT_ID", 0);
                //lat = data.getDoubleExtra("latitude", 000.000000);
                //lon = data.getDoubleExtra("longitude", 000.000000);
                Toast.makeText(MainActivity.this, "MT_ID from user : " + mt_id, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseMessaging.getInstance().subscribeToTopic("lan"); //Setting Topic for receive Push

        // [START handle_data_extras]
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
                if(key.equals("latitude")){
                    lat = Double.parseDouble(value.toString());
                } else if(key.equals("longitude")){
                    lon = Double.parseDouble(value.toString());
                } else if(key.equals("state")){
                    status = value.toString();
                }
            }
            //Toast.makeText(MainActivity.this, "위도 : " + lat + " 경도 : " + lon + " 상태 : " + status, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(MainActivity.this, DaumMapActivity.class);//푸시알람 클릭시 띄울 다음 지도
            intent.putExtra("lat", lat); // 인텐트에 데이터 위도 담아주기
            intent.putExtra("lon", lon); // 인텐트에 데이터 경도 담아주기
            intent.putExtra("status", status); // 인텐트에 산 상태 담아주기

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

    }
}
