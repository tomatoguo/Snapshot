package com.guomato.snapshot;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class MenuFragment extends Fragment implements AdapterView.OnItemClickListener {

    /**
     * STATE -  当前被选中列表项位置的状态信息
     */
    private static final String STATE_SELECT_POSITION = "com.guomato.snapshot.STATE_SELECT_POSITION";
    
    private ListView mMenuListView;

    private OnMenuItemClickListener mCallback;

    public interface OnMenuItemClickListener {
        void onItemClick(int position);
    }

    public static MenuFragment newInstance(ArrayList<String> albumItems) {
        Bundle args = new Bundle();
        args.putStringArrayList(MainActivity.EXTRA_ALBUM_LIST, albumItems);

        MenuFragment fragment = new MenuFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        ArrayList<String> albumItems = getArguments().getStringArrayList(MainActivity.EXTRA_ALBUM_LIST);

        mMenuListView = (ListView) view.findViewById(R.id.menu);
        mMenuListView.setAdapter(new MenuAdapter(getActivity(), R.layout.menu_item, albumItems));
        mMenuListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (OnMenuItemClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnMenuItemClickListener.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mCallback = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(STATE_SELECT_POSITION, ((MenuAdapter) mMenuListView.getAdapter()).getSelectPosition());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            ((MenuAdapter) mMenuListView.getAdapter()).setSelectPosition(savedInstanceState.getInt(STATE_SELECT_POSITION, 0));
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == ((MenuAdapter) mMenuListView.getAdapter()).getSelectPosition()) {
            mCallback.onItemClick(-1);
            return;
        }

        ((MenuAdapter) mMenuListView.getAdapter()).setSelectPosition(position);
        mCallback.onItemClick(position);
    }

}
