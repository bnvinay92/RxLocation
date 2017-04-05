package bnvinay92.github.com.findagentrewrite;

import android.Manifest;
import android.support.annotation.RequiresPermission;

import java.util.List;

interface AgentMapView {
    void showArea(AgentMapArea area);
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    void setUserLocationEnabled();
    void initMap();
    void showAgent(List<Agent> agents);
    float getZoom();
}
