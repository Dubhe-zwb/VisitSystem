package com.tonsail.visit.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tonsail.visit.R;
import com.tonsail.visit.utils.Visitor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TodayInfo extends RecyclerView.Adapter<TodayInfo.Holder> {
    private static final String TAG = "zwb_TodayInfo";
    public List<Visitor.ContentBean> list = new ArrayList<>();

    /**
     * 去除返回数据的秒
     *
     * @param time
     * @return
     */
    public String splitTime(String time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date parse = simpleDateFormat.parse(time);
            String format = simpleDateFormat.format(parse);
            return format;
        } catch (ParseException e) {
            Log.e(TAG, "splitTime: 解析异常");
            return time;
        }

    }

    public TodayInfo() {

    }

    public void setData(List<Visitor.ContentBean> list) {
        this.list.clear();
        this.list.addAll(list);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.right_recycler_item, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Visitor.ContentBean contentBean = list.get(position);
        if (contentBean.getType() == 0) {
            holder.visitType.setText(R.string.initiative_visit);
            holder.view.setBackgroundResource(R.color.visit_background);
            holder.visitType.setBackgroundResource(R.drawable.shape_visit_bg);
        }
        if (contentBean.getType() == 1) {
            holder.visitType.setText(R.string.invite_visit);
            holder.view.setBackgroundResource(R.color.white);
            holder.visitType.setBackgroundResource(R.drawable.shape_invite_bg);
        }
        holder.visitInfo.setText(contentBean.getName() + "已确认" + splitTime(contentBean.getTime()) + "到访，请做好接待。");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        TextView visitType;
        TextView visitInfo;
        View view;

        public Holder(@NonNull View itemView) {
            super(itemView);
            visitType = itemView.findViewById(R.id.visit_type);
            visitInfo = itemView.findViewById(R.id.visit_info);
            view = itemView.findViewById(R.id.root);
        }
    }
}

