package com.megatech.fms.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.megatech.fms.R;
import com.megatech.fms.databinding.B2502ItemBinding;
import com.megatech.fms.databinding.B2505ItemBinding;
import com.megatech.fms.model.BM2505Model;
import com.megatech.fms.model.TruckFuelModel;

import java.util.ArrayList;
import java.util.List;

public class BM2505ArrayAdapter extends ArrayAdapter<BM2505Model> {
    public BM2505ArrayAdapter(@NonNull Context context, @NonNull List<BM2505Model> objects) {
        super(context, 0,  objects);
        allItems = objects;
        checked = new boolean[objects.size()] ;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.b2505_item, parent, false);
        }
        B2505ItemBinding binding =  DataBindingUtil.bind(convertView);

        BM2505Model  item =getItem(position);
                binding.setMItem(item);

        CheckBox cb = convertView.findViewById(R.id.b2502_item_chk);

        if (cb != null) {
            cb.setChecked(checked[position]);

            cb.setOnClickListener(v -> {
                checked[position] = cb.isChecked();
                if (cb.isChecked())
                    checkedItems.add((Integer)item.getLocalId());
                else
                    checkedItems.remove((Integer)item.getLocalId());
            });
        }
        return  binding.getRoot();
    }

    boolean[] checked;
    List<BM2505Model> allItems;
    List<Integer> checkedItems = new ArrayList<>();

    public List<Integer> getCheckedItems() {
        try {
            checkedItems.clear();
            for (int i = 0; i < allItems.size(); i++)
                if (checked[i])
                    checkedItems.add(allItems.get(i).getLocalId());
        }catch (Exception ex)
        {}
        return  checkedItems;
    }

    public void selectAll() {
        selectAll(true);
        notifyDataSetChanged();
    }

    public void selectNone() {
        selectAll(false);
        notifyDataSetChanged();
    }

    void selectAll(boolean selected) {
        for (int i = 0; i < checked.length; i++) {
            checked[i] = selected;
        }
    }
}