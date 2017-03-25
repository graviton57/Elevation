package com.havrylyuk.elevation.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.havrylyuk.elevation.BuildConfig;
import com.havrylyuk.elevation.R;
import com.havrylyuk.elevation.events.ElevationEvent;
import com.havrylyuk.elevation.util.SourceType;
import com.havrylyuk.elevation.util.PreferencesHelper;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;

/**
 *
 * Created by Igor Havrylyuk on 25.03.2017.
 */
public class ElevationService extends IntentService {

    public static final String LATLNG_DATA_EXTRA = "com.havrylyuk.elevation.LATLNG_DATA_EXTRA";

    private static final int NO_DATA_SRTM = -32768; // SRTM1,SRTM3: no data - sea, ocean, etc
    private static final int NO_DATA_GTOPO30 = -9999; // GTOPO30,ASTERGDEM: no data - sea, ocean, etc
    private static final String LOG_TAG = ElevationService.class.getSimpleName();

    private PreferencesHelper prefHelper;

    public ElevationService() {
        super("ElevationService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        prefHelper = PreferencesHelper.getInstance();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            ApiService service = ApiClient.getClient().create(ApiService.class);
            LatLng latLng = intent.getParcelableExtra(LATLNG_DATA_EXTRA);
            String elevation =  getElevation(service, latLng);
            String location = parseLocation(latLng);
            EventBus.getDefault().postSticky(new ElevationEvent(latLng, elevation, location));
        }
    }

    private String getElevation(ApiService service, LatLng latLng ){
        String elevation = getString(R.string.no_elevation_data);
        if (latLng == null) {
            return elevation;
        }
        Call<Integer> responseCall;
        SourceType selectedSource = SourceType.values()[prefHelper.getApiType(this)];
        switch (selectedSource){
            case SRTM3:responseCall = service.getSrtm3(latLng.latitude,
                        latLng.longitude, BuildConfig.GEONAME_API_KEY);
                break;
            case ASTERGDEM:responseCall = service.getAstergdem(latLng.latitude,
                        latLng.longitude, BuildConfig.GEONAME_API_KEY);
                break;
            case GTOPO30:responseCall = service.getGtopo30(latLng.latitude,
                        latLng.longitude, BuildConfig.GEONAME_API_KEY);
                break;
            default:responseCall = service.getSrtm1(latLng.latitude,
                    latLng.longitude, BuildConfig.GEONAME_API_KEY);
                break;
        }
        try {
            int result = responseCall.execute().body();
            if (result != NO_DATA_SRTM && result != NO_DATA_GTOPO30) {
                elevation = getString(R.string.format_elevation, result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return elevation;
    }

    public String parseLocation(LatLng latLng) {
        if (latLng == null) {
            return getString(R.string.error_unknown_location);
        }
        StringBuilder result = new StringBuilder("");
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses  = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                if (addresses.get(0).getCountryName() != null) {
                    result.append(addresses.get(0).getCountryName());
                }
                if (addresses.get(0).getAdminArea() != null) {
                    result.append(",").append(addresses.get(0).getAdminArea());
                }
                if (addresses.get(0).getLocality() != null) {
                    result.append(",").append(addresses.get(0).getLocality());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            result.append(getString(R.string.error_parse_location));
        }
        if (TextUtils.isEmpty(result.toString())) {
            result.append(getString(R.string.error_unknown_location));
        }
        Log.d(LOG_TAG, "parseLocation: " + result.toString());
        return result.toString();
    }
}
