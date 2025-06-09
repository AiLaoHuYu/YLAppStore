package com.yl.ylappstore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yl.basemvp.AppInfo;
import com.yl.ylappstore.adapter.AppAdapter;

import java.util.ArrayList;
import java.util.List;

public class AppListFragment extends Fragment {

    private static final String ARG_CATEGORY = "category";
    private AppAdapter adapter;
    private String currentCategory;

    public static AppListFragment newInstance(String category) {
        AppListFragment fragment = new AppListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentCategory = getArguments().getString(ARG_CATEGORY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);

        // 筛选当前分类的应用
        List<AppInfo> filteredList = filterAppsByCategory(currentCategory);

        // 初始化适配器
        adapter = new AppAdapter(filteredList, requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        return view;
    }

    private List<AppInfo> filterAppsByCategory(String category) {
        List<AppInfo> result = new ArrayList<>();
        for (AppInfo app : ((MainActivity)requireActivity()).systemApps) {
            if (app.getCategory().equals(category)) {
                result.add(app);
            }
        }
        return result;
    }

    public void refreshAppList() {
        if (adapter != null) {
            List<AppInfo> newList = filterAppsByCategory(currentCategory);
            adapter.updateList(newList);
        }
    }

    // 处理车机硬件选择事件
    public void handleHardwareSelect() {
        if (adapter != null) {
            RecyclerView recyclerView = getView().findViewById(R.id.recycler_view);
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            int position = layoutManager.findFirstVisibleItemPosition();
            if (position != RecyclerView.NO_POSITION) {
                adapter.performItemClick(position, (AppAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(position));
            }
        }
    }

}