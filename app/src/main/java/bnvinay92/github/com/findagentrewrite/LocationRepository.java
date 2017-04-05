package bnvinay92.github.com.findagentrewrite;

import android.location.Location;

import io.reactivex.Observable;

public interface LocationRepository {
    Observable<Location> locationUpdates();
    Observable<PermissionState> permissionStates();
}
