package org.hotelbyte.app.interfaces;

import java.math.BigInteger;


public interface RxSingleCallback {

    void success(BigInteger integer);

    void canceled(String error);
}
