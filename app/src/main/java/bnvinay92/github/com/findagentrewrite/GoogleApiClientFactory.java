package bnvinay92.github.com.findagentrewrite;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;

class GoogleApiClientFactory {

    private final Context context;

    public GoogleApiClientFactory(Context context) {
        this.context = context;
    }

    @SafeVarargs
    final Observable<LocationQuery.PlayServicesConnectionState> createWithApis(Api<? extends Api.ApiOptions.NotRequiredOptions>... apis) {
        return Observable.create(new GoogleApiClientSource(Arrays.asList(apis)));
    }

    private class GoogleApiClientSource implements ObservableOnSubscribe<LocationQuery.PlayServicesConnectionState>, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

        private final List<Api<? extends Api.ApiOptions.NotRequiredOptions>> apis;

        private GoogleApiClient client;
        private ObservableEmitter<LocationQuery.PlayServicesConnectionState> emitter;

        GoogleApiClientSource(List<Api<? extends Api.ApiOptions.NotRequiredOptions>> apis) {
            this.apis = apis;
        }

        @Override
        public void subscribe(@NonNull ObservableEmitter<LocationQuery.PlayServicesConnectionState> e) throws Exception {
            emitter = e;
            client = createApiClient();
            client.connect();
        }

        private GoogleApiClient createApiClient() {
            GoogleApiClient.Builder apiClientBuilder = new GoogleApiClient.Builder(context);
            for (Api<? extends Api.ApiOptions.NotRequiredOptions> api : apis) {
                apiClientBuilder.addApi(api);
            }
            apiClientBuilder.addConnectionCallbacks(this);
            apiClientBuilder.addOnConnectionFailedListener(this);
            return apiClientBuilder.build();
        }

        @Override public void onConnected(@Nullable Bundle bundle) {
            emitter.onNext(LocationQuery.PlayServicesConnectionState.createConnected(client));
        }

        @Override public void onConnectionSuspended(int cause) {
            emitter.onNext(LocationQuery.PlayServicesConnectionState.createSuspended(cause));
        }

        @Override
        public void onConnectionFailed(@android.support.annotation.NonNull ConnectionResult connectionResult) {
            emitter.onNext(LocationQuery.PlayServicesConnectionState.createFailed(connectionResult));
            emitter.onComplete();
        }
    }
}
