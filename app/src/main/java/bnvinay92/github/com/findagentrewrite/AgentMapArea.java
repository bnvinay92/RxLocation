package bnvinay92.github.com.findagentrewrite;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.auto.value.AutoValue;

@AutoValue
abstract class AgentMapArea {

    public static final float DEFAULT_ZOOM = 4;
    public static final float STREET_ZOOM = 13;
    public static final AgentMapArea INDIA = new AutoValue_AgentMapArea(new LatLng(1, 1), DEFAULT_ZOOM);

    abstract LatLng latLng();
    abstract float zoom();

    static AgentMapArea create(Location location, float zoom) {
        return new AutoValue_AgentMapArea(new LatLng(location.getLatitude(), location.getLongitude()), zoom);
    }
}
