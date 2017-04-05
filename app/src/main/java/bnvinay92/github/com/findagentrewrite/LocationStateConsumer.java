package bnvinay92.github.com.findagentrewrite;

import android.location.Location;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;

interface LocationStateConsumer {
    void updateLocation(Location location);
    void showPermissionDeniedPermanently();
    void showPermissionDenied();
    void showLocationSettingsResolution(Status status);
    void showRequestToEnableLocationManually();
    void showLocationLoading();
    void showLocationTimedOut();
    void showPlayServicesResolution(ConnectionResult connectionResult);
    /**
     * @param cause Either {@link com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks#CAUSE_NETWORK_LOST}
     *              or {@link com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks#CAUSE_SERVICE_DISCONNECTED}
     */
    void showPlayServicesSuspended(int cause);
    void showLocationError();
}
