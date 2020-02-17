package com.megatech.fms.view;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.megatech.fms.FMSApplication;
import com.megatech.fms.R;
import com.megatech.fms.RefuelItemDetailFragment;
import com.megatech.fms.RefuelListFragment;

public class PageAdapter extends FragmentPagerAdapter {

    public PageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        return  new RefuelListFragment(position == 0);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (position ==0)
            return FMSApplication.getApplication().getTruckNo();
        else
            return  FMSApplication.getApplication().getResources().getString(R.string.others);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

//        RefuelRecyclerView rv = (RefuelRecyclerView)item.getView();
//        rv.getAdapter().notifyDataSetChanged();

    }

    public  void updateLists()
    {
        RefuelListFragment fragment = (RefuelListFragment) this.getItem(0);
        if (fragment!=null)
            fragment.onResume();
    }
}
