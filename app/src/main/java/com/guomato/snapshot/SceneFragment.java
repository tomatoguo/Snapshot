package com.guomato.snapshot;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.guomato.snapshot.constant.HttpConstant;
import com.guomato.snapshot.util.HttpHelper;
import com.guomato.snapshot.util.LogHelper;
import com.guomato.snapshot.util.SceneManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class SceneFragment extends Fragment implements SceneManager.Callback {

    private String mAlbum;

    private ListView mSceneList;

    private SceneManager mSceneManager;

    private ArrayList<String> mUrlItems;

    public static SceneFragment newInstance(String album) {
        Bundle args = new Bundle();
        args.putString(MainActivity.EXTRA_ALBUM, album);

        SceneFragment fragment = new SceneFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mAlbum = getArguments().getString(MainActivity.EXTRA_ALBUM);

        mUrlItems = new ArrayList<>();

        mSceneManager = new SceneManager(getActivity(), new Handler(), this);
        mSceneManager.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scene, container, false);

        mSceneList = (ListView) view.findViewById(R.id.scene_list);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        new FetchSceneUrlTask().execute();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mSceneManager.clearQueue();
    }

    @Override
    public void onSceneImageDownload(final ImageView imageView, Bitmap bitmap) {
        if (imageView.isShown()) {
            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);

            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    imageView.clearAnimation();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            imageView.setImageBitmap(bitmap);
            imageView.startAnimation(animation);
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }

    private void setupAdapter() {
        if (!isVisible()) {
            return;
        }
        if (mSceneList == null || mUrlItems == null || mUrlItems.size() == 0) {
            return;
        }

        mSceneList.setAdapter(new SceneListAdapter(getActivity()));
    }

    private class SceneListAdapter extends ArrayAdapter<String> {

        public SceneListAdapter(Context context) {
            super(context, 0, mUrlItems);
        }

        @Override
        public int getCount() {
            return mUrlItems.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder VH;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.scene_item, parent, false);

                VH = new ViewHolder();
                VH.sceneImageView = (ImageView) convertView.findViewById(R.id.scene);

                convertView.setTag(VH);
            } else {
                VH = (ViewHolder) convertView.getTag();
            }

            String sceneUrl = mUrlItems.get(position);

            VH.sceneImageView.setImageBitmap(null);
            mSceneManager.queueDownloadSceneRequest(VH.sceneImageView, sceneUrl);

            return convertView;
        }

        class ViewHolder {
            ImageView sceneImageView;
        }
    }

    private class FetchSceneUrlTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                LogHelper.trace("fetch scene urls from: " + HttpConstant.SERVER_ENDPOINT + HttpConstant.METHOD_FETCH_URL + "?album=" + URLEncoder.encode(mAlbum, "UTF-8"));

                return new HttpHelper().getUrl(HttpConstant.SERVER_ENDPOINT + HttpConstant.METHOD_FETCH_URL + "?album=" + URLEncoder.encode(mAlbum, "UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();

                return null;
            }
        }

        @Override
        protected void onPostExecute(String urlJsonString) {
            if (urlJsonString == null) {
                return;
            }

            ArrayList<String> urlItems = new ArrayList<>();
            parseUrlItems(urlJsonString, urlItems);

            mUrlItems = urlItems;

            setupAdapter();
        }

        /**
         * 从json中解析url
         *
         * @param jsonString json类型字符串
         * @param urlItems   url列表
         */
        private void parseUrlItems(String jsonString, ArrayList<String> urlItems) {
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray jsonArray = jsonObject.getJSONArray("sceneUrlItems");
                for (int i = 0; i < jsonArray.length(); i++) {
                    urlItems.add(jsonArray.getJSONObject(i).getString("url"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
