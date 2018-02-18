package com.sunfusheng.firupdater.sample;

import android.content.Context;

import com.sunfusheng.GroupViewHolder;
import com.sunfusheng.HeaderGroupRecyclerViewAdapter;

import java.util.List;

/**
 * @author sunfusheng on 2018/2/17.
 */
public class AppAdapter extends HeaderGroupRecyclerViewAdapter<DataUtil.AppConfig> {

    public AppAdapter(Context context) {
        super(context);
    }

    public AppAdapter(Context context, List<List<DataUtil.AppConfig>> items) {
        super(context, items);
    }

    public AppAdapter(Context context, DataUtil.AppConfig[][] items) {
        super(context, items);
    }

    @Override
    public int getHeaderLayoutId(int viewType) {
        return R.layout.divider_20dp;
    }

    @Override
    public int getChildLayoutId(int viewType) {
        return R.layout.item_list_layout;
    }

    @Override
    public void onBindHeaderViewHolder(GroupViewHolder holder, DataUtil.AppConfig item, int groupPosition) {

    }

    @Override
    public void onBindChildViewHolder(GroupViewHolder holder, DataUtil.AppConfig item, int groupPosition, int childPosition) {
        holder.setText(R.id.tv_title, item.titleId);
        holder.setText(R.id.tv_subtitle, item.subtitleId);

        holder.setVisible(R.id.divider, !isGroupLastItem(groupPosition, childPosition));
    }
}
