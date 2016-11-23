package com.lan.capstonedesign;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import java.io.BufferedWriter;
import java.io.IOException;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_main);

        Button monitoringBtn = (Button) findViewById(R.id.networkCheck);
        monitoringBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, DaumMapActivity.class);
                intent.putExtra("Admin", true);
                intent.putExtra("MT_ID", "3");
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
    HttpURLConnection conn = null;

    public void method() {
        try {
        URL url = new URL("https://fcm.googleapis.com/fcm/send"); //요청 URL을 입력
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST"); //요청 방식을 설정 (default : GET)
        conn.setDoInput(true); //input을 사용하도록 설정 (default : true)
        conn.setDoOutput(true); //output을 사용하도록 설정 (default : false)

        conn.setConnectTimeout(60); //타임아웃 시간 설정 (default : 무한대기)

        OutputStream out = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8")); //캐릭터셋 설정

        writer.write(
                "=value1" +"&key2=value2"+"&key3=value3"
        ); //요청 파라미터를 입력
        writer.flush();
        writer.close();
        out.close();
            conn.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*Thread thread = new Thread() {
        @Override
        public void run() {
            HttpClient httpClient = new H();

            String urlString = "http://192.168.1.101/login";
            try {
                URI url = new URI(urlString);

                HttpPost httpPost = new HttpPost();
                httpPost.setURI(url);

                List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("userId", "saltfactory"));
                nameValuePairs.add(new BasicNameValuePair("password", "password"));

                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));


                HttpResponse response = httpClient.execute(httpPost);
                String responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);

                Log.d(TAG, responseString);

            } catch (URISyntaxException e) {
                Log.e(TAG, e.getLocalizedMessage());
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                Log.e(TAG, e.getLocalizedMessage());
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, e.getLocalizedMessage());
                e.printStackTrace();
            }

        }
    };*/

    //thread.start();
    private void sendPushToFCMServer(){
        Socket socket;
        final int SERVERPORT = 8888;
        final String SERVER_IP = "192.168.0.96";

        try {
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
            socket = new Socket(serverAddr, SERVERPORT);
            OutputStream out = socket.getOutputStream();
            PrintWriter output = new PrintWriter(out);
            output.flush();
            output.print("send");
            output.close();
            socket.close();
        } catch(UnknownHostException e1) {
            e1.printStackTrace();
        } catch(IOException e1) {
            e1.printStackTrace();
        }
    }
}
