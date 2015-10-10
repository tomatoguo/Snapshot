package com.guomato.snapshot.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SceneManager extends HandlerThread implements Handler.Callback {

    private static final int MSG_DOWNLOAD_SCENE_IMAGE = 0;

    private Map<ImageView, String> mRequestMap;

    private Handler mHandler;

    private Handler mResponseHandler;

    private ExecutorService mExecutorService;

    private ImageLoader mSceneDownloader;

    private Callback mCallback;

    public interface Callback {
        void onSceneImageDownload(ImageView imageView, Bitmap bitmap);
    }

    public SceneManager(Context context, Handler responseHandler, Callback callback) {
        super("SceneManager");

        mRequestMap = new HashMap<>();
        mResponseHandler = responseHandler;
        mCallback = callback;
        mExecutorService = Executors.newFixedThreadPool(20);
        mSceneDownloader = new ImageLoader(context);
    }

    @Override
    protected void onLooperPrepared() {
        mHandler = new Handler(this);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_DOWNLOAD_SCENE_IMAGE:
                mExecutorService.submit(new DownloadSceneImageRunnable((ImageView) msg.obj));
                break;

            default:
                break;
        }
        return false;
    }

    /**
     * 请求下载图片
     *
     * @param imageView 请求下载的ImageView
     * @param url       图片的url
     */
    public void queueDownloadSceneRequest(ImageView imageView, String url) {
        if (imageView == null || url == null) {
            return;
        }

        mRequestMap.put(imageView, url);
        mHandler.obtainMessage(MSG_DOWNLOAD_SCENE_IMAGE, imageView).sendToTarget();
    }

    public void clearQueue() {
        mHandler.removeMessages(MSG_DOWNLOAD_SCENE_IMAGE);
    }

    private class DownloadSceneImageRunnable implements Runnable {

        private ImageView mSceneImageView;

        public DownloadSceneImageRunnable(ImageView sceneImageView) {
            mSceneImageView = sceneImageView;
        }

        @Override
        public void run() {
            final String sceneUrl = mRequestMap.get(mSceneImageView);
            if (sceneUrl == null) {
                return;
            }

            final Bitmap bitmap = mSceneDownloader.loadBitmap(sceneUrl);

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!sceneUrl.equals(mRequestMap.get(mSceneImageView)) || bitmap == null) {
                        return;
                    }

                    mRequestMap.remove(mSceneImageView);

                    mCallback.onSceneImageDownload(mSceneImageView, bitmap);
                }
            });
        }
    }
}
