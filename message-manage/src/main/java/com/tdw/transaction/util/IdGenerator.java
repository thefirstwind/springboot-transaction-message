package com.tdw.transaction.util;

import java.util.UUID;


public class IdGenerator {

    public static String uuid36() {
        return UUID.randomUUID().toString().toUpperCase();
    }

    public static String uuid32() {
        return uuid36().replaceAll("-", "");
    }
    
}
