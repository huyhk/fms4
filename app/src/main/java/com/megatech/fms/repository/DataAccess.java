package com.megatech.fms.repository;

import com.megatech.fms.data.AppDatabase;
import com.megatech.fms.data.entity.ParkingLot;

import java.util.List;

import retrofit2.Retrofit;

import static com.megatech.fms.BuildConfig.API_BASE_URL;

public class DataAccess {
    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .build();


}
