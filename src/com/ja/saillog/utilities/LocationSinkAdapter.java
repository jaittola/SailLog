package com.ja.saillog.utilities;

import com.ja.saillog.quantity.quantity.QuantityFactory;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class LocationSinkAdapter implements LocationListener {

    public LocationSinkAdapter(LocationSink sink) {
        this.sink = sink;
    }

    public LocationSink getSink() {
        return this.sink;
    }

    public void onLocationChanged(Location location) {
        sink.updateLocation(location.getLatitude(),
                            location.getLongitude(),
                            QuantityFactory.metersPerSecond(location.getSpeed()),
                            location.getBearing(),
                            location.getAccuracy(),
                            location.getTime());
    }

    public void onProviderDisabled(String arg0) {
        // TODO Auto-generated method stub
    }

    public void onProviderEnabled(String arg0) {
        // TODO Auto-generated method stub
    }

    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        // TODO Auto-generated method stub
    }

    private LocationSink sink;
}
