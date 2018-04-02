package org.hotelbyte.app.util.rx;

import org.hotelbyte.app.interfaces.RxSingleCallback;

import java.math.BigInteger;

import rx.Single;
import rx.SingleSubscriber;
import rx.schedulers.Schedulers;


public class RxUtil {

    /**
     * <p>SignleAsync</p>
     *
     * @param number
     * @param callback
     */
    public static void SignleAsync(BigInteger number, RxSingleCallback callback) {
        Single.fromCallable(() -> number)
                .subscribeOn(Schedulers.io())
                .subscribe(new SingleSubscriber<BigInteger>() {
                    @Override
                    public void onSuccess(BigInteger value) {
                        callback.success(value);
                    }

                    @Override
                    public void onError(Throwable error) {
                        callback.canceled(error.getMessage());
                    }
                });
    }
}
