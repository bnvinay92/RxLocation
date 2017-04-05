package bnvinay92.github.com.findagentrewrite;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;

import io.reactivex.SingleObserver;
import io.reactivex.SingleSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;


public class PendingResultSingle<T extends Result> implements SingleSource<T> {

    private final PendingResult<T> pendingResult;
    private boolean disposed = false;

    public PendingResultSingle(PendingResult<T> pendingResult) {
        this.pendingResult = pendingResult;
    }

    @Override
    public void subscribe(@NonNull final SingleObserver<? super T> subscriber) {
        pendingResult.setResultCallback(result -> {
            subscriber.onSuccess(result);
            disposed = true;
        });

        subscriber.onSubscribe((new Disposable() {
            @Override
            public void dispose() {
                pendingResult.cancel();
                disposed = true;
            }
            @Override public boolean isDisposed() {
                return disposed;
            }
        }));
    }
}
