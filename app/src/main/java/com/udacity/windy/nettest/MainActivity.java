package com.udacity.windy.nettest;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnSend;
    private TextView tv;

    public static final int SHOW_RESPONSE = 0;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_RESPONSE:
                    String response = (String) msg.obj;
                    //在这里显示UI，这是在主线程中
                    tv.setText(response);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSend = (Button) findViewById(R.id.btn_send);
        tv = (TextView) findViewById(R.id.tv);

        btnSend.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_send) {

            sendRequestWithHttpURLConnection();
//            sendRequestWithHttpClient();
        }

    }

    /*此方法中开线程，用  HttpURLConnection 访问网络， 用Gson解析*/
    private void sendRequestWithHttpURLConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {

                    //开始Http请求
                    URL url = new URL("http://p.3.cn/prices/mgets?skuIds=J_1378541&type=1");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);

                    //读取输入流
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    //上面读取的结果，存放在message中
                    Message message = new Message();
                    message.what = SHOW_RESPONSE;
                    message.obj = response.toString();
                    handler.sendMessage(message);

                    parseJsonWithGson(response.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }

            }
        }).start();
    }

    /*此方法中开线程，用  HttpClient 访问网络 ，用 JSONObject 解析*/
    private void sendRequestWithHttpClient() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // HttpClient本身是个接口，不能创建对象。
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet("http://p.3.cn/prices/mgets?skuIds=J_1378541&type=1");
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity = httpResponse.getEntity();
                        String response = EntityUtils.toString(entity);

                        //将服务器返回的结果保存在message中
                        Message message = new Message();
                        message.what = SHOW_RESPONSE;
                        message.obj = response;
                        handler.sendMessage(message);

                        parseJsonWithJsonObject(response);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /*此方法解析Json ，用JSONObject */
    private void parseJsonWithJsonObject(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String id = jsonObject.getString("id");
                String p = jsonObject.getString("p");
                String m = jsonObject.getString("m");
                Log.d("id is", id);
                Log.d("p is", p);
                Log.d("m is", m);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*此方法解析Json ，用 Gson */
    private void parseJsonWithGson(String jsonData) {
        Gson gson = new Gson();
        List<JingDongPrice> priceList = gson.fromJson(jsonData, new TypeToken<List<JingDongPrice>>() {}.getType());
        for (JingDongPrice price : priceList) {
            Log.d("Gson", "id is" + price.getId());
            Log.d("Gson", "p is" + price.getP());
            Log.d("Gson", "m is" + price.getM());
        }
    }
}
