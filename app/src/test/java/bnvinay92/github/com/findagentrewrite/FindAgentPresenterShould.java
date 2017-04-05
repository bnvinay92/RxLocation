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
import io.reactivex.Single;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FindAgentPresenterShould {

    @Rule public MockitoRule rule = MockitoJUnit.rule();
    @Rule public TestSchedulerRule schedulerRule = new TestSchedulerRule();

    FindAgentPresenter presenter;

    @Mock FindAgentView activity;
    @Mock LocationQuery locationQuery;
    @Mock AgentListQuery agentListQuery;
    @Mock LocationState locationState;
    @Mock LocationRepository locationRepository;

    @Mock Location mockLocation;

    @Before
    public void setUp() {
        presenter = new FindAgentPresenter(locationQuery, agentListQuery, locationRepository);
        presenter.attachView(activity);
    }

    @Test
    public void given_location_when_agent_list_fetched_then_show_api_error() {
        Throwable poop = new Throwable();
        when(locationRepository.locationUpdates()).thenReturn(BehaviorRelay.createDefault(mockLocation));
        when(agentListQuery.execute(mockLocation)).thenReturn(Single.error(poop));

        presenter.fetchAgents();

        verify(activity, times(1)).showAgentsError(poop);
    }

    @Test
    public void given_location_when_agent_list_fetched_then_show_agent_found() {
        List<Agent> agents = Collections.singletonList(new Agent());
        when(locationRepository.locationUpdates()).thenReturn(BehaviorRelay.createDefault(mockLocation));
        when(agentListQuery.execute(mockLocation)).thenReturn(Single.just(agents));

        presenter.fetchAgents();

        verify(activity, times(1)).updateAgentList(agents);
        verify(activity, times(1)).showAgentsFound();
    }

    @Test
    public void given_location_when_empty_agent_list_fetched_then_show_no_agent_found_dialog() {
        when(locationRepository.locationUpdates()).thenReturn(BehaviorRelay.createDefault(mockLocation));
        when(agentListQuery.execute(mockLocation)).thenReturn(Single.just(Collections.emptyList()));

        presenter.fetchAgents();

        verify(activity, times(1)).showNoAgentsFound();
    }

    @Test
    public void given_no_dialog_tip_to_show_and_location_not_fetched_when_view_attached_then_show_location_state() {
        when(activity.tipDialogToShow()).thenReturn(BehaviorRelay.createDefault(FindAgentTip.NONE));
        when(locationQuery.execute()).thenReturn(Observable.just(locationState));

        presenter.fetchLocation();

        verify(locationState, times(1)).apply(activity);
    }

    @Test
    public void given_dialog_tip_to_show_and_location_not_fetched_when_view_attached_then_dont_show_location_state() {
        when(activity.tipDialogToShow()).thenReturn(BehaviorRelay.createDefault(FindAgentTip.CASH_IN));

        presenter.fetchLocation();

        verify(locationState, never()).apply(activity);
    }

    @Test
    public void given_no_dialog_tip_to_show_and_location_fetched_when_view_attached_then_dont_show_location_state() {
        when(activity.tipDialogToShow()).thenReturn(BehaviorRelay.createDefault(FindAgentTip.NONE));

        presenter.fetchLocation();

        verify(locationState, never()).apply(activity);
    }
}