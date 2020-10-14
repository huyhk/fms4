package com.megatech.fms.view;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.megatech.fms.R;
import com.megatech.fms.databinding.RefuelPreviewItemBinding;
import com.megatech.fms.model.RefuelItemData;

import java.util.ArrayList;

public class TruckArrayAdapter extends ArrayAdapter<RefuelItemData> {
    public TruckArrayAdapter(Context context, ArrayList<RefuelItemData> items)
    {
        super(context,0, items );
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        RefuelItemData itemData = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.refuel_preview_item, parent, false);
        }


        // Perform the binding

        RefuelPreviewItemBinding binding = DataBindingUtil.bind(convertView);

        binding.setTruckItem(itemData);
        binding.executePendingBindings();
        return binding.getRoot();
    }

}
