package com.megatech.fms.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.megatech.fms.data.entity.User;

import java.util.List;

@Dao
public interface UserDao {

    @Query("Select * from User")
    List<User> getAll();

    @Query("Select * from User where id= :id")
    User get(Integer id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int update(User user);
}
