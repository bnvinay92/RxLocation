package bnvinay92.github.com.findagentrewrite;

import android.location.Location;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.jakewharton.rxrelay2.BehaviorRelay;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

import static org.mockito.Mockito.when;

public class LocationQueryShould {

    @Rule public MockitoRule rule = MockitoJUnit.rule();
    @Rule public TestSchedulerRule schedulerRule = new TestSchedulerRule();

    LocationQuery locationQuery;
    @Mock LocationHelper locationHelper;
    @Mock GoogleApiClientFactory apiClientFactory;

    @Mock GoogleApiClient googleApiClient;
    @Mock Location location;

    @Before
    public void setUp() {
        locationQuery = new LocationQuery(locationHelper, apiClientFactory);
    }

    @Test
    public void return_location_error_when_api_factory_throws_an_error_given_permission_granted() {
        when(locationHelper.getPermissionState()).thenReturn(PermissionState.GRANTED);
        Throwable poop = new Throwable("poop");
        when(apiClientFactory.createWithApis(LocationServices.API)).thenReturn(Observable.error(poop));

        TestObserver<LocationState> testObserver = locationQuery.execute().test();
        testObserver.awaitCount(1);

        testObserver.assertValues(LocationState.Error.create(poop));
    }

    @Test
    public void return_location_error_when_location_settings_throws_an_error_given_permission_granted() {
        Throwable poop = new Throwable("poop");
        when(locationHelper.getPermissionState()).thenReturn(PermissionState.GRANTED);
        when(apiClientFactory.createWithApis(LocationServices.API)).thenReturn(BehaviorRelay.createDefault(LocationQuery.PlayServicesConnectionState.createConnected(googleApiClient)));
        when(locationHelper.getSettingsState(googleApiClient)).thenReturn(Single.error(poop));

        TestObserver<LocationState> testObserver = locationQuery.execute().test();
        testObserver.awaitCount(1);

        testObserver.assertValues(LocationState.Error.create(poop));
    }

    @Test
    public void return_location_update_given_permission_granted_and_location_enabled() {
        when(locationHelper.getPermissionState()).thenReturn(PermissionState.GRANTED);
        when(apiClientFactory.createWithApis(LocationServices.API)).thenReturn(BehaviorRelay.createDefault(LocationQuery.PlayServicesConnectionState.createConnected(googleApiClient)));
        Status settingsStatus = new Status(LocationSettingsStatusCodes.SUCCESS);
        Single<LocationSettingsResult> settingsResult = Single.just(new LocationSettingsResult(settingsStatus));
        when(locationHelper.getSettingsState(googleApiClient)).thenReturn(settingsResult);
        when(locationHelper.fetchLocation(googleApiClient)).thenReturn(Single.just(location));

        TestObserver<LocationState> testObserver = locationQuery.execute().test();
        testObserver.awaitCount(2);

        testObserver.assertValues(new LocationState.Loading(), LocationState.Update.create(location));
    }

    @Test
    public void return_play_services_connection_failed_given_permission_granted() {
        ConnectionResult mockConnectionResult = new ConnectionResult(0);
        when(locationHelper.getPermissionState()).thenReturn(PermissionState.GRANTED);
        when(apiClientFactory.createWithApis(LocationServices.API)).thenReturn(BehaviorRelay.createDefault(LocationQuery.PlayServicesConnectionState.createFailed(mockConnectionResult)));

        TestObserver<LocationState> testObserver = locationQuery.execute().test();
        testObserver.awaitCount(1);

        testObserver.assertValues(LocationState.PlayServicesError.create(mockConnectionResult));
    }

    @Test
    public void return_play_services_connection_suspended_given_permission_granted() {
        when(locationHelper.getPermissionState()).thenReturn(PermissionState.GRANTED);
        when(apiClientFactory.createWithApis(LocationServices.API)).thenReturn(Observable.just(LocationQuery.PlayServicesConnectionState.createSuspended(0)));

        TestObserver<LocationState> testObserver = locationQuery.execute().test();
        testObserver.awaitCount(1);

        testObserver.assertValues(LocationState.PlayServicesSuspended.create(0));
    }

    @Test
    public void return_timedout_when_query_takes_long_given_permission_granted_and_location_enabled() {
        when(locationHelper.getPermissionState()).thenReturn(PermissionState.GRANTED);
        when(apiClientFactory.createWithApis(LocationServices.API)).thenReturn(BehaviorRelay.createDefault(LocationQuery.PlayServicesConnectionState.createConnected(googleApiClient)));
        Status settingsStatus = new Status(LocationSettingsStatusCodes.SUCCESS);
        Single<LocationSettingsResult> settingsResult = Single.just(new LocationSettingsResult(settingsStatus));
        when(locationHelper.getSettingsState(googleApiClient)).thenReturn(settingsResult);
        when(locationHelper.fetchLocation(googleApiClient)).thenReturn(Single.never());

        TestObserver<LocationState> testObserver = locationQuery.execute().test();
        schedulerRule.getTestScheduler().advanceTimeBy(LocationQuery.TIMEOUT_SECONDS, TimeUnit.SECONDS);
        testObserver.awaitCount(2);

        testObserver.assertValues(new LocationState.Loading(), new LocationState.TimedOut());
    }

    @Test
    public void return_denied_given_permission_denied() {
        when(locationHelper.getPermissionState()).thenReturn(PermissionState.DENIED);

        TestObserver<LocationState> testObserver = locationQuery.execute().test();
        testObserver.awaitTerminalEvent();

        testObserver.assertValue(new LocationState.Denied());
    }

    @Test
    public void return_denied_permanently_given_permission_denied_permanently() {
        when(locationHelper.getPermissionState()).thenReturn(PermissionState.NEVER_ASK_AGAIN);

        TestObserver<LocationState> testObserver = locationQuery.execute().test();
        testObserver.awaitTerminalEvent();

        testObserver.assertValue(new LocationState.DeniedPermanently());
    }

    @Test
    public void return_settings_resolution_given_permission_granted_but_location_disabled() {
        when(locationHelper.getPermissionState()).thenReturn(PermissionState.GRANTED);
        when(apiClientFactory.createWithApis(LocationServices.API)).thenReturn(BehaviorRelay.createDefault(LocationQuery.PlayServicesConnectionState.createConnected(googleApiClient)));
        Status settingsStatus = new Status(LocationSettingsStatusCodes.RESOLUTION_REQUIRED);
        Single<LocationSettingsResult> settingsResult = Single.just(new LocationSettingsResult(settingsStatus));
        when(locationHelper.getSettingsState(googleApiClient)).thenReturn(settingsResult);

        TestObserver<LocationState> testObserver = locationQuery.execute().test();
        testObserver.awaitCount(1);

        testObserver.assertValues(LocationState.RequiresSettingsResolution.create(settingsStatus));
    }

    @Test
    public void return_request_to_manually_enable_location_given_ppermissiong_granted_but_settings_unresolvable() {
        when(locationHelper.getPermissionState()).thenReturn(PermissionState.GRANTED);
        when(apiClientFactory.createWithApis(LocationServices.API)).thenReturn(BehaviorRelay.createDefault(LocationQuery.PlayServicesConnectionState.createConnected(googleApiClient)));
        Status settingsStatus = new Status(LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE);
        Single<LocationSettingsResult> settingsResult = Single.just(new LocationSettingsResult(settingsStatus));
        when(locationHelper.getSettingsState(googleApiClient)).thenReturn(settingsResult);

        TestObserver<LocationState> testObserver = locationQuery.execute().test();
        testObserver.awaitCount(1);

        testObserver.assertValues(new LocationState.RequiresManualSettingsResolution());
    }
}