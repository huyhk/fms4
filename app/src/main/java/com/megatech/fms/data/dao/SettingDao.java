package com.megatech.fms.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.megatech.fms.data.entity.Setting;

import java.util.List;
import java.util.Set;

@Dao
public interface SettingDao {

    @Query("Select * from Setting")
    List<Setting> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Setting setting);
}
