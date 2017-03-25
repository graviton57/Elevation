package com.havrylyuk.elevation.service;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 *
 * Created by Igor Havrylyuk on 25.03.2017.
 */
public interface ApiService {


    @GET("srtm1")
    Call<Integer> getSrtm1(
            @Query("lat")        double lat,
            @Query("lng")        double lng,
            @Query("username")   String userName);

    @GET("srtm3")
    Call<Integer> getSrtm3(
            @Query("lat")        double lat,
            @Query("lng")        double lng,
            @Query("username")   String userName);

    @GET("astergdem")
    Call<Integer> getAstergdem(
            @Query("lat")        double lat,
            @Query("lng")        double lng,
            @Query("username")   String userName);

    @GET("gtopo30")
    Call<Integer> getGtopo30(
            @Query("lat")        double lat,
            @Query("lng")        double lng,
            @Query("username")   String userName);
}
