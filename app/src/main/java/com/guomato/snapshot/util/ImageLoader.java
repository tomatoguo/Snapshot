package com.guomato.snapshot.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.util.LruCache;

import com.guomato.snapshot.constant.HttpConstant;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import libcore.io.DiskLruCache;

/**
 * Created by 永辉 on 2015/10/6.
 */
public class ImageLoader {

    private static final int DISK_CACHE_SIZE = 15 * 1024 * 1024;

    private LruCache<String, Bitmap> mLruCache;

    private DiskLruCache mDiskLruCache;

    public ImageLoader(Context context) {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory());
        int cacheSize = maxMemory / 8;
        mLruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };

        try {
            File cacheDir = getDiskCacheDir(context, "bitmap");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            mDiskLruCache = DiskLruCache.open(cacheDir, getApplicationVersion(context), 1, DISK_CACHE_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Bitmap loadBitmap(String sceneUrl) {
        Bitmap bitmap = mLruCache.get(sceneUrl);

        if (bitmap == null) {
            FileInputStream fis = null;
            try {
                String key = MD5Helper.generateMD5(sceneUrl);
                DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                if (snapshot == null) {
                    DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                    if (editor != null) {
                        OutputStream outputStream = editor.newOutputStream(0);
                        if (downloadBitmap(sceneUrl, outputStream)) {
                            editor.commit();
                        } else {
                            editor.abort();
                        }
                        mDiskLruCache.flush();

                        snapshot = mDiskLruCache.get(key);
                    }
                }

                FileDescriptor fd = null;
                if (snapshot != null) {
                    fis = (FileInputStream) snapshot.getInputStream(0);
                    fd = fis.getFD();
                }
                if (fd != null) {
                    bitmap = BitmapFactory.decodeFileDescriptor(fd);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return bitmap;
    }

    private boolean downloadBitmap(String sceneUrl, OutputStream outputStream) {
        HttpURLConnection conn = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            URL url = new URL(sceneUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(HttpConstant.CONNECT_TIMEOUT);
            conn.setReadTimeout(HttpConstant.READ_TIMEOUT);

            bis = new BufferedInputStream(conn.getInputStream());
            bos = new BufferedOutputStream(outputStream);

            int oneByte;
            while ((oneByte = bis.read()) != -1) {
                bos.write(oneByte);
            }
            bos.flush();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    private File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }

        return new File(cachePath + File.separator + uniqueName);
    }

    private int getApplicationVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return 0;
    }


}
