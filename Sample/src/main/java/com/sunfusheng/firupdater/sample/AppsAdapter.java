package com.sunfusheng.firupdater.sample;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.sunfusheng.FirUpdaterUtils;
import com.sunfusheng.GroupViewHolder;
import com.sunfusheng.HeaderGroupRecyclerViewAdapter;

import java.util.List;

/**
 * @author sunfusheng on 2018/2/17.
 */
public class AppsAdapter extends HeaderGroupRecyclerViewAdapter<DataSource.AppConfig> {

    public AppsAdapter(Context context) {
        super(context);
    }

    public AppsAdapter(Context context, List<List<DataSource.AppConfig>> items) {
        super(context, items);
    }

    public AppsAdapter(Context context, DataSource.AppConfig[][] items) {
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
    public void onBindHeaderViewHolder(GroupViewHolder holder, DataSource.AppConfig item, int groupPosition) {

    }

    @Override
    public void onBindChildViewHolder(GroupViewHolder holder, DataSource.AppConfig item, int groupPosition, int childPosition) {
        Context context = holder.itemView.getContext();
        Drawable icon = null;

        if (FirUpdaterUtils.isAppInstalled(context, context.getString(item.pkgId))) {
            icon = FirUpdaterUtils.getIcon(context, context.getString(item.pkgId));
        }

        if (icon == null) {
            icon = context.getResources().getDrawable(R.mipmap.ic_launcher);
        }

        holder.setImageDrawable(R.id.iv_logo, icon);
        holder.setText(R.id.tv_title, item.titleId);
        holder.setText(R.id.tv_subtitle, item.subtitleId);

        holder.setVisible(R.id.divider, !isGroupLastItem(groupPosition, childPosition));
    }
}
