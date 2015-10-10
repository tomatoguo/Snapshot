package com.guomato.snapshot;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import com.guomato.snapshot.constant.HttpConstant;
import com.guomato.snapshot.util.HttpHelper;
import com.guomato.snapshot.util.LogHelper;
import com.guomato.snapshot.util.MD5Helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class StartUpActivity extends AppCompatActivity {

    private String mLocalAlbumPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);

        String appPath;

        //获取当前应用文件夹
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            appPath = getExternalFilesDir("").getAbsolutePath();
        } else {
            appPath = getFilesDir().getAbsolutePath();
        }

        mLocalAlbumPath = appPath + File.separator + "album.txt";

        new CheckServerAlbumVersionTask().execute();
    }

    /**
     * 服务器端album检查完毕，启动MAinActivity
     *
     * @param albumItems 相册列表
     */
    private void onServerAlbumVersionCheck(ArrayList<String> albumItems) {
        Intent intent = new Intent(StartUpActivity.this, MainActivity.class);
        intent.putStringArrayListExtra(MainActivity.EXTRA_ALBUM_LIST, albumItems);

        startActivity(intent);

        finish();
    }

    /**
     * 启动应用时检查服务器端album是否变更
     */
    private class CheckServerAlbumVersionTask extends AsyncTask<Void, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            ArrayList<String> albumItems = new ArrayList<>();

            try {
                //获取服务器端album文件的MD5
                String serverAlbumMD5 = HttpHelper.getUrl(HttpConstant.SERVER_ENDPOINT + HttpConstant.METHOD_CHECK_SERVER_ALBUM_VERSION);

                //获取本地album文件的MD5
                File file = new File(mLocalAlbumPath);
                if (!file.exists()) {
                    file.createNewFile();
                }
                String clientAlbumMD5 = MD5Helper.generateMD5(file);

                if (!clientAlbumMD5.equals(serverAlbumMD5)) {
                    LogHelper.trace("update album from server.");

                    HttpURLConnection conn = (HttpURLConnection) (new URL(HttpConstant.SERVER_ENDPOINT + HttpConstant.METHOD_FETCH_ALBUM_FILE).openConnection());
                    conn.setConnectTimeout(HttpConstant.CONNECT_TIMEOUT);
                    conn.setReadTimeout(HttpConstant.READ_TIMEOUT);

                    InputStream is = conn.getInputStream();
                    FileOutputStream fos = new FileOutputStream(file);

                    int oneByte;
                    while ((oneByte = is.read()) != -1) {
                        fos.write(oneByte);
                    }
                    fos.flush();

                    is.close();
                    fos.close();
                }

                parseAlbumItems(albumItems);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return albumItems;
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            onServerAlbumVersionCheck(strings);
        }

        /**
         * 从文件中解析相册
         *
         * @param albumItems 相册列表
         */
        private void parseAlbumItems(ArrayList<String> albumItems) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(mLocalAlbumPath)));

            String line;
            while ((line = reader.readLine()) != null) {
                albumItems.add(line);
            }
            reader.close();
        }

    }

}
