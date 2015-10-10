package com.guomato.snapshot.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpHelper {

    public static String getUrl(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public static byte[] getUrlBytes(String urlSpec) throws IOException {
        HttpURLConnection conn = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;

        try {
            URL url = new URL(urlSpec);
            conn = (HttpURLConnection) url.openConnection();
            is = conn.getInputStream();
            baos = new ByteArrayOutputStream();

            int oneByte;
            while ((oneByte = is.read()) != -1) {
                baos.write(oneByte);
            }
            baos.flush();

            return baos.toByteArray();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            if (is != null) {
                is.close();
            }
            if (baos != null) {
                baos.close();
            }
        }
    }

}
