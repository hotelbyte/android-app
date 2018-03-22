package org.hotelbyte.wallet.interfaces;

import java.math.BigInteger;


public interface RxSingleCallback {

    void success(BigInteger integer);

    void canceled(String error);
}
