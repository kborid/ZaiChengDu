package com.z012.chengdu.sc.net.response;


import com.z012.chengdu.sc.net.ApiErrorDef;
import com.z012.chengdu.sc.net.exception.ServerException;

import io.reactivex.functions.Function;

public class ResponseFunc<T> implements Function<ResponseComm<T>, T> {
    @Override
    public T apply(ResponseComm<T> tResponseComm) throws Exception {
        if (null != tResponseComm.getHead()) {
            if (!ApiErrorDef.SUCCESS.equals(tResponseComm.getHead().getRtnCode())) {
                throw new ServerException(tResponseComm.getHead().getRtnCode(), tResponseComm.getHead().getRtnMsg());
            }
        }

        if (null == tResponseComm.getBody()) {
            return (T) ("{}");
        }
        return tResponseComm.getBody();
    }
}
