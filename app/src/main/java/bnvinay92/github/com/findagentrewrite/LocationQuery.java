package bnvinay92.github.com.findagentrewrite;

import android.support.annotation.VisibleForTesting;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.auto.value.AutoValue;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;

import static bnvinay92.github.com.findagentrewrite.LocationState.Denied;
import static bnvinay92.github.com.findagentrewrite.LocationState.DeniedPermanently;
import static bnvinay92.github.com.findagentrewrite.LocationState.Loading;
import static bnvinay92.github.com.findagentrewrite.LocationState.PlayServicesError;
import static bnvinay92.github.com.findagentrewrite.LocationState.PlayServicesSuspended;
import static bnvinay92.github.com.findagentrewrite.LocationState.RequiresManualSettingsResolution;
import static bnvinay92.github.com.findagentrewrite.LocationState.RequiresSettingsResolution;
import static bnvinay92.github.com.findagentrewrite.LocationState.TimedOut;
import static bnvinay92.github.com.findagentrewrite.LocationState.Update;

public class LocationQuery {

    @VisibleForTesting static final long TIMEOUT_SECONDS = 15L;

    private final LocationHelper locationHelper;
    private final GoogleApiClientFactory apiClientFactory;

    public LocationQuery(LocationHelper locationHelper, GoogleApiClientFactory apiClientFactory) {
        this.locationHelper = locationHelper;
        this.apiClientFactory = apiClientFactory;
    }

    Observable<LocationState> execute() {
        PermissionState locationPermissionState = locationHelper.getPermissionState();
        switch (locationPermissionState) {
            case DENIED:
                return Observable.just(new Denied());
            case NEVER_ASK_AGAIN:
                return Observable.just(new DeniedPermanently());
            case GRANTED:
                return apiClientFactory.createWithApis(LocationServices.API)
                        .switchMap(playServicesConnectionState -> playServicesConnectionState.locationState(this))
                        .onErrorReturn(LocationState.Error::create);
            default:
                throw new InvalidEnumArgumentException(locationPermissionState);
        }
    }

    private Observable<LocationState> queryLocation(GoogleApiClient googleApiClient) {
        return locationHelper.getSettingsState(googleApiClient)
                .map(LocationSettingsResult::getStatus)
                .flatMapObservable(status -> {
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            return Observable.just(RequiresSettingsResolution.create(status));
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            return Observable.just(new RequiresManualSettingsResolution());
                        case LocationSettingsStatusCodes.SUCCESS:
                            return locationHelper.fetchLocation(googleApiClient)
                                    .<LocationState>map(Update::create)
                                    .timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS, Single.just(new TimedOut()))
                                    .toObservable()
                                    .startWith(new Loading());
                        default:
                            throw new AssertionError("Unhandled status code: " + status.getStatusCode());
                    }
                });
    }

    @AutoValue
    abstract static class Connected extends PlayServicesConnectionState {

        public abstract GoogleApiClient client();

        @Override public Observable<LocationState> locationState(LocationQuery locationQuery) {
            return locationQuery.queryLocation(client());
        }
    }

    @AutoValue
    abstract static class Suspended extends PlayServicesConnectionState {

        public abstract int cause();

        @Override public Observable<LocationState> locationState(LocationQuery locationQuery) {
            return Observable.just(PlayServicesSuspended.create(cause()));
        }
    }

    @AutoValue
    abstract static class Failed extends PlayServicesConnectionState {

        public abstract ConnectionResult connectionResult();

        @Override public Observable<LocationState> locationState(LocationQuery locationQuery) {
            return Observable.just(PlayServicesError.create(connectionResult()));
        }
    }

    abstract static class PlayServicesConnectionState {

        public abstract Observable<LocationState> locationState(LocationQuery locationQuery);

        static Connected createConnected(GoogleApiClient client) {
            return new AutoValue_LocationQuery_Connected(client);
        }

        static Suspended createSuspended(int errorCause) {
            return new AutoValue_LocationQuery_Suspended(errorCause);
        }

        static Failed createFailed(ConnectionResult connectionResult) {
            return new AutoValue_LocationQuery_Failed(connectionResult);
        }
    }
}
