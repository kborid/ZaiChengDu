package com.z012.chengdu.sc.net.bean;

import java.io.Serializable;

public class CertUserAuth implements Serializable {

    public boolean isAuth;
    public UserAuth userAuth;

    public static class UserAuth {
        public String uid;
        public int times;
        public String idType;
        public String bankCardNo;
        public String name;
        public String authResult;
        public String mobileNo;
        public String updatetime;
        public String idNo;
    }
}
