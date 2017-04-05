package bnvinay92.github.com.findagentrewrite;

import android.util.Log;

import io.reactivex.disposables.CompositeDisposable;

public class AgentMapPresenter {

    private final LocationHelper locationHelper;
    private final LocationRepository locationRepository;
    private final AgentListRepository agentListRepository;

    private AgentMapView fragment;
    private CompositeDisposable disposables = new CompositeDisposable();

    public AgentMapPresenter(LocationHelper locationHelper, LocationRepository locationRepository, AgentListRepository agentListRepository) {
        this.locationHelper = locationHelper;
        this.locationRepository = locationRepository;
        this.agentListRepository = agentListRepository;
    }

    public void attachView(AgentMapView view) {
        fragment = view;
        fragment.initMap();
        if (locationHelper.getPermissionState() == PermissionState.GRANTED) {
            fragment.setUserLocationEnabled();
            fragment.showArea(AgentMapArea.create(locationHelper.getLastKnownLocation(), AgentMapArea.DEFAULT_ZOOM));
        } else {
            fragment.showArea(AgentMapArea.INDIA);
            disposables.add(locationRepository.permissionStates()
                    .filter(permissionState -> permissionState == PermissionState.GRANTED)
                    .subscribe(
                            __ -> fragment.setUserLocationEnabled(),
                            throwable -> Log.w(throwable.getMessage(), throwable)
                    ));
        }
        disposables.add(locationRepository.locationUpdates()
                .subscribe(
                        location -> {
                            float zoom = Math.max(AgentMapArea.STREET_ZOOM, fragment.getZoom());
                            fragment.showArea(AgentMapArea.create(location, zoom));
                        },
                        throwable -> Log.w(throwable.getMessage(), throwable)
                ));
        disposables.add(agentListRepository.agentUpdates()
                .filter(agents -> !agents.isEmpty())
                .subscribe(
                        agents -> fragment.showAgent(agents),
                        throwable -> Log.w(throwable.getMessage(), throwable)
                ));
    }

    public void detachView() {
        disposables.dispose();
        fragment = null;
    }
}
