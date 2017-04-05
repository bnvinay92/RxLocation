package bnvinay92.github.com.findagentrewrite;


import java.util.List;

import io.reactivex.Observable;

interface FindAgentView extends LocationStateConsumer {
    Observable<FindAgentTip> tipDialogToShow();
    void showNoAgentsFound();
    void updateAgentList(List<Agent> agents);
    void showAgentsFound();
    void showAgentsError(Throwable throwable);
}
