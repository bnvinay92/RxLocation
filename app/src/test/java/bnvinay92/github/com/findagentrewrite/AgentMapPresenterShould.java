package bnvinay92.github.com.findagentrewrite;

import android.location.Location;

import com.jakewharton.rxrelay2.BehaviorRelay;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AgentMapPresenterShould {

    @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

    AgentMapPresenter presenter;

    @Mock AgentMapView fragment;
    @Mock LocationHelper locationHelper;
    @Mock LocationRepository locationRepository;
    @Mock AgentListRepository agentListRepository;

    @Mock Location mockLocation;
    @Mock AgentMapArea india;

    @Before
    public void setup() {
        presenter = new AgentMapPresenter(locationHelper, locationRepository, agentListRepository);
        when(locationRepository.locationUpdates()).thenReturn(Observable.never());
        when(agentListRepository.agentUpdates()).thenReturn(Observable.never());
    }

    @Test
    public void given_location_fetched_when_view_attached_then_show_area_and_agents() {
        BehaviorRelay<PermissionState> relay = BehaviorRelay.create();
        List<Agent> agents = Collections.singletonList(new Agent());
        when(locationHelper.getPermissionState()).thenReturn(PermissionState.DENIED);
        when(locationRepository.permissionStates()).thenReturn(relay);
        when(locationRepository.locationUpdates()).thenReturn(BehaviorRelay.createDefault(mockLocation));
        when(agentListRepository.agentUpdates()).thenReturn(BehaviorRelay.createDefault(agents));
        when(fragment.getZoom()).thenReturn(AgentMapArea.DEFAULT_ZOOM);

        presenter.attachView(fragment);
        relay.accept(PermissionState.GRANTED);

        verify(fragment, times(1)).initMap();
        verify(fragment, times(1)).setUserLocationEnabled();
        verify(fragment, times(1)).showArea(AgentMapArea.INDIA);
        verify(fragment, times(1)).showArea(AgentMapArea.create(mockLocation, AgentMapArea.STREET_ZOOM));
        verify(fragment, times(1)).showAgent(agents);
    }

    @Test
    public void given_location_permission_granted_when_view_attached_then_show_last_known_location_as_initial_area() {
        when(locationHelper.getPermissionState()).thenReturn(PermissionState.GRANTED);
        when(locationHelper.getLastKnownLocation()).thenReturn(mockLocation);

        presenter.attachView(fragment);

        verify(fragment, times(1)).initMap();
        verify(fragment, times(1)).setUserLocationEnabled();
        verify(fragment, times(1)).showArea(AgentMapArea.create(mockLocation, AgentMapArea.DEFAULT_ZOOM));
    }

    @Test
    public void given_location_permission_denied_when_view_attached_then_show_india_as_initial_area() {
        BehaviorRelay<PermissionState> relay = BehaviorRelay.create();
        when(locationHelper.getPermissionState()).thenReturn(PermissionState.NEVER_ASK_AGAIN);
        when(locationRepository.permissionStates()).thenReturn(relay);

        presenter.attachView(fragment);

        verify(fragment, times(1)).initMap();
        verify(fragment, never()).setUserLocationEnabled();
        verify(fragment, times(1)).showArea(AgentMapArea.INDIA);

        relay.accept(PermissionState.GRANTED);
        verify(fragment, times(1)).setUserLocationEnabled();
    }
}