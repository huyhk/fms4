package com.megatech.fms.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.megatech.fms.R;
import com.megatech.fms.RefuelDetailActivity;
import com.megatech.fms.RefuelPreviewActivity;
import com.megatech.fms.UserBaseActivity;
import com.megatech.fms.databinding.CardviewRefuelItemBinding;
import com.megatech.fms.model.REFUEL_ITEM_STATUS;
import com.megatech.fms.model.RefuelItemData;

import java.util.List;

public class RefuelRecyclerViewAdapter extends RecyclerView.Adapter<RefuelRecyclerViewAdapter.MyViewHolder> {

    private UserBaseActivity mContext;

    private List<RefuelItemData> mData;

    private UserBaseActivity mParentActivity;

    public RefuelRecyclerViewAdapter(UserBaseActivity mContext, List<RefuelItemData> mData) {
        this.mContext = mContext;
        this.mData = mData;

    }



    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = inflater.inflate(R.layout.cardview_refuel_item,parent,false);
        //return new MyViewHolder(view);
        CardviewRefuelItemBinding itemBinding = CardviewRefuelItemBinding.inflate(inflater,parent,false);
        return new MyViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        RefuelItemData model = mData.get(position);

        holder.itemView.setOnClickListener(subClick);

        holder.bind(model);

//        holder.mParkingLot.setText(mData.get(position).getParkingLot());
//        holder.mAircraftCode.setText(mData.get(position).getAircraftCode());
//        holder.mFlightCode.setText(mData.get(position).getFlightCode());
//
//
//        if (model.getStatus() == REFUEL_ITEM_STATUS.DONE) {
//            if (model.getPostStatus() == RefuelItemData.ITEM_POST_STATUS.ERROR)
//                holder.mCheck.setCheckMarkDrawable(R.drawable.ic_error);
//            else
//                holder.mCheck.setCheckMarkDrawable(R.drawable.ic_check);
//            holder.mCheck.setChecked(true);
//            holder.itemView.setBackgroundColor(Color.LTGRAY);
//        }
//        else
//        {
//            holder.mCheck.setCheckMarkDrawable(null);
//            holder.mCheck.setChecked(false);
//            holder.itemView.setBackgroundColor(Color.WHITE);
//        }
//        holder.itemView.setTag(mData.get(position));
//
//        holder.itemView.setOnClickListener(mOnClickListener);
//        holder.itemView.setTag(mData.get(position));
        holder.itemView.setOnClickListener(mOnClickListener);
        holder.itemView.setTag(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Bundle arguments = new Bundle();
            RefuelItemData item = (RefuelItemData)v.getTag();
            Context context = v.getContext();
            Intent intent;
            if (item.getStatus() == REFUEL_ITEM_STATUS.DONE)
                intent = new Intent(mContext, RefuelPreviewActivity.class);
            else
                intent = new Intent(mContext, RefuelDetailActivity.class);
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

            intent.putExtra("REFUEL", gson.toJson(item));

            mContext.startActivityForResult(intent, item.getId());
        }
    };

    private final View.OnClickListener subClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView mFlightCode;
        TextView mAircraftCode;
        TextView mParkingLot;
        CheckedTextView mCheck;
        private   CardviewRefuelItemBinding binding ;
        LinearLayout expandArea;

        public MyViewHolder(CardviewRefuelItemBinding binding, @NonNull View itemView)
        {
            super(itemView);
            this.binding = binding;
        }

        public MyViewHolder(CardviewRefuelItemBinding binding)
        {
            super(binding.getRoot());
            this.binding = binding;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            mFlightCode = itemView.findViewById(R.id.refuel_item_flightCode);
            mAircraftCode   = itemView.findViewById(R.id.refuel_item_aircraftCode);
            mParkingLot   = itemView.findViewById(R.id.refuel_item_parlingLot);
            mCheck   = itemView.findViewById(R.id.refuel_item_chk);
        }

        public void bind(RefuelItemData itemData)
        {
            binding.setMItem(itemData);
            binding.executePendingBindings();

        }
    }
}
