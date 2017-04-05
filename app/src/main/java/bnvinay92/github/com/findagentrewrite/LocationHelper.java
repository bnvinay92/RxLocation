package bnvinay92.github.com.findagentrewrite;

import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;

import io.reactivex.Single;

class LocationHelper {

    private final LocationRequest locationRequest;
    private final LocationSettingsRequest locationSettingsRequest;

    LocationHelper(LocationRequest locationRequest) {
        this.locationRequest = locationRequest;
        this.locationSettingsRequest = new LocationSettingsRequest.Builder()
                .setAlwaysShow(true)
                .build();
    }

    public PermissionState getPermissionState() {
        return PermissionState.DENIED;
    }

    public Single<LocationSettingsResult> getSettingsState(GoogleApiClient googleApiClient) {
//        return LocationServices.SettingsApi.checkLocationSettings(googleApiClient, locationSettingsRequest);
        return null;
    }
    public Single<Location> fetchLocation(GoogleApiClient googleApiClient) {
        return Single.never();
    }
}
