package com.tonsail.visit.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tonsail.visit.R;
import com.tonsail.visit.utils.TimeJudge;
import com.tonsail.visit.utils.Visitor;

import java.util.ArrayList;
import java.util.List;

public class AllDetail extends RecyclerView.Adapter<AllDetail.Holder> {
    private static final String TAG = "zwb_AllDetail";

    public List<Visitor.ContentBean> list = new ArrayList<>();
    public List<Visitor.ContentBean> originalList = new ArrayList<>();
    private String selectName;

    public List<Visitor.ContentBean> getList() {
        return list;
    }

    public AllDetail() {

    }

    public void setData(List<Visitor.ContentBean> list) {
        this.list.clear();
        this.list.addAll(list);
    }

    public void setExplorData(String name) {
        selectName = name;
        originalList.clear();
        if (!TextUtils.isEmpty(name)) {
            for (Visitor.ContentBean contentBean : list) {
                if (contentBean.getName().startsWith(name)) {
                    originalList.add(contentBean);
                }
            }
        }
        notifyDataSetChanged();

    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.detail_recycler_item, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        if (position % 2 == 0) {
            holder.view.setBackgroundResource(R.color.white);
        } else {
            holder.view.setBackgroundResource(R.color.item_bg);
        }
        Visitor.ContentBean contentBean = null;
        if (TextUtils.isEmpty(selectName)) {
            contentBean = list.get(position);
        } else {
            contentBean = originalList.get(position);
        }
        holder.visitorName.setText(contentBean.getName());
        holder.companyName.setText(contentBean.getCompany());
        holder.visitReason.setText(contentBean.getReason());
        holder.others.setText(contentBean.getAccompanyingPerson());
        if (contentBean.getType() == 0) {
            holder.toWays.setText(R.string.initiative_visit);
        } else if (contentBean.getType() == 1) {
            holder.toWays.setText(R.string.invite_visit);
        }
        holder.visitTime.setText(TimeJudge.splitTime(contentBean.getTime()));
        holder.leaveTime.setText(contentBean.getLeaveTime());
        holder.homeName.setText(contentBean.getReceiverName());

    }

    @Override
    public int getItemCount() {

        return TextUtils.isEmpty(selectName) ? list.size() : originalList.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        TextView visitorName;
        TextView companyName;
        TextView visitReason;
        TextView others;
        TextView toWays;
        TextView visitTime;
        TextView leaveTime;
        TextView homeName;
        View view;

        public Holder(@NonNull View itemView) {
            super(itemView);
            view = itemView.findViewById(R.id.root);
            visitorName = itemView.findViewById(R.id.visitor_name);
            companyName = itemView.findViewById(R.id.company_name);
            visitReason = itemView.findViewById(R.id.visit_reason);
            others = itemView.findViewById(R.id.others);
            toWays = itemView.findViewById(R.id.to_ways);
            visitTime = itemView.findViewById(R.id.visit_time);
            leaveTime = itemView.findViewById(R.id.leave_time);
            homeName = itemView.findViewById(R.id.home_name);
        }
    }
}

