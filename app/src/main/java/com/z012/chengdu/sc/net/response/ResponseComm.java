package com.z012.chengdu.sc.net.response;

import com.alibaba.fastjson.JSON;

public class ResponseComm<T> {
    private Header head;
    private T body;

    public Header getHead() {
        return head;
    }

    public void setHead(Header head) {
        this.head = head;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public T getBody() {
        return body;
    }

    public String bodyToString() {
        return JSON.toJSONString(body);
    }

    public static class Header {
        private String rtnMsg;
        private String rtnCode;

        public String getRtnMsg() {
            return rtnMsg;
        }

        public void setRtnMsg(String rtnMsg) {
            this.rtnMsg = rtnMsg;
        }

        public String getRtnCode() {
            return rtnCode;
        }

        public void setRtnCode(String rtnCode) {
            this.rtnCode = rtnCode;
        }

        @Override
        public String toString() {
            return "[Header:" + "rtnCode = " + rtnCode + ", rtnMsg = " + rtnMsg + "]";
        }
    }

    @Override
    public String toString() {
        return head.toString() + ", [Body:" + bodyToString() + "]";
    }
}
