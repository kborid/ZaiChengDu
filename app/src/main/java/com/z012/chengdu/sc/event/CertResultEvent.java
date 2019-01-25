package com.z012.chengdu.sc.event;

public class CertResultEvent {
    private String retMsg;

    public CertResultEvent(String retMsg) {
        this.retMsg = retMsg;
    }

    public String getRetMsg() {
        return retMsg;
    }

    public void setRetMsg(String retMsg) {
        this.retMsg = retMsg;
    }
}
