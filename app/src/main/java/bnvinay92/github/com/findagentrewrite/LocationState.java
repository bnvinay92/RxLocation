package bnvinay92.github.com.findagentrewrite;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;
import com.google.auto.value.AutoValue;

abstract class LocationState {

    public abstract void apply(LocationStateConsumer consumer);

    static class DeniedPermanently extends LocationState {
        @Override
        public void apply(LocationStateConsumer consumer) {
            consumer.showPermissionDeniedPermanently();
        }
    }

    static class Denied extends LocationState {
        @Override
        public void apply(LocationStateConsumer consumer) {
            consumer.showPermissionDenied();
        }
    }

    @AutoValue
    abstract static class RequiresSettingsResolution extends LocationState {

        @Override
        public void apply(LocationStateConsumer consumer) {
            consumer.showLocationSettingsResolution(status());
        }

        public abstract Status status();

        static RequiresSettingsResolution create(Status status) {
            return new AutoValue_LocationState_RequiresSettingsResolution(status);
        }
    }

    static class RequiresManualSettingsResolution extends LocationState {
        @Override
        public void apply(LocationStateConsumer consumer) {
            consumer.showRequestToEnableLocationManually();
        }
    }

    static class Loading extends LocationState {
        @Override
        public void apply(LocationStateConsumer consumer) {
            consumer.showLocationLoading();
        }
    }

    @AutoValue
    abstract static class Update extends LocationState {

        @Override
        public void apply(LocationStateConsumer consumer) {
            consumer.updateLocation(location());
        }

        public abstract Location location();

        static Update create(Location location) {
            return new AutoValue_LocationState_Update(location);
        }
    }

    static class TimedOut extends LocationState {
        @Override
        public void apply(LocationStateConsumer consumer) {
            consumer.showLocationTimedOut();
        }
    }

    @AutoValue
    abstract static class PlayServicesError extends LocationState {

        @Override
        public void apply(LocationStateConsumer consumer) {
            consumer.showPlayServicesResolution(connectionResult());
        }

        public abstract ConnectionResult connectionResult();

        static PlayServicesError create(ConnectionResult connectionResult) {
            return new AutoValue_LocationState_PlayServicesError(connectionResult);
        }
    }

    @AutoValue
    abstract static class PlayServicesSuspended extends LocationState {

        @Override
        public void apply(LocationStateConsumer consumer) {
            consumer.showPlayServicesSuspended(cause());
        }

        public abstract int cause();

        static PlayServicesSuspended create(int cause) {
            return new AutoValue_LocationState_PlayServicesSuspended(cause);
        }
    }

    @AutoValue
    abstract static class Error extends LocationState {

        @Override
        public void apply(LocationStateConsumer consumer) {
            // TODO: Use timber instead.
            Log.e(throwable().getMessage(), throwable().getMessage(), throwable());
            consumer.showLocationError();
        }

        abstract Throwable throwable();

        public static Error create(Throwable throwable) {
            return new AutoValue_LocationState_Error(throwable);
        }
    }

    @Override public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return this.getClass().equals(obj.getClass());
    }
}
