package bnvinay92.github.com.findagentrewrite;

import java.util.List;

import io.reactivex.Observable;

interface AgentListRepository {
    Observable<List<Agent>> agentUpdates();
}
