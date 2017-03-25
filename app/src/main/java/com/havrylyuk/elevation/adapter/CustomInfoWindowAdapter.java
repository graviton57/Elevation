package com.havrylyuk.elevation.adapter;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.havrylyuk.elevation.R;

/**
 * Created by Igor Havrylyuk on 25.03.2017.
 */

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Activity context;

    public CustomInfoWindowAdapter(Activity context) {
        this.context = context;
    }

    @Override
    // Return null here, so that getInfoContents() is called next.
    public View getInfoWindow(Marker arg0) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        // Inflate the layouts for the info window, title and snippet.
        View infoWindow = context.getLayoutInflater().inflate(R.layout.custom_info_contents, null);

        TextView title = ((TextView) infoWindow.findViewById(R.id.title));
        title.setText(marker.getTitle());

        TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
        snippet.setText(marker.getSnippet());

        return infoWindow;
    }
}
