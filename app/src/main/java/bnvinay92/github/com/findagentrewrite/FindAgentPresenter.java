package bnvinay92.github.com.findagentrewrite;

import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.schedulers.Schedulers;

class FindAgentPresenter {

    private final LocationQuery locationQuery;
    private final AgentListQuery agentListQuery;
    private final LocationRepository locationRepository;

    private Disposable locationDisposable = Disposables.disposed();
    private Disposable agentDisposable = Disposables.disposed();
    private FindAgentView activity;

    public FindAgentPresenter(LocationQuery locationQuery, AgentListQuery agentListQuery, LocationRepository locationRepository) {
        this.locationQuery = locationQuery;
        this.agentListQuery = agentListQuery;
        this.locationRepository = locationRepository;
    }

    public void attachView(FindAgentView view) {
        this.activity = view;
    }

    void fetchLocation() {
        assert locationDisposable.isDisposed();
        locationDisposable = activity.tipDialogToShow()
                .flatMap(findAgentTip -> findAgentTip == FindAgentTip.NONE
                        ? locationQuery.execute()
                        : Observable.empty())
                .subscribe(
                        state -> state.apply(activity),
                        throwable -> Log.w(throwable.getMessage(), throwable)
                );
    }

    void fetchAgents() {
        assert agentDisposable.isDisposed();
        agentDisposable = locationRepository.locationUpdates()
                .switchMapSingle(location -> agentListQuery.execute(location)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()))
                .subscribe(
                        agents -> {
                            if (agents.isEmpty()) {
                                activity.showNoAgentsFound();
                            } else {
                                activity.updateAgentList(agents);
                                activity.showAgentsFound();
                            }
                        },
                        throwable -> activity.showAgentsError(throwable)
                );
    }

    void detachView() {
        locationDisposable.dispose();
        agentDisposable.dispose();
        activity = null;
    }
}
