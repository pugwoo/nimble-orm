package com.pugwoo.dbhelper.test.utils;

import org.mvel2.MVEL;

public class MVELTest {

    public static void main(String[] args) {
        System.out.println(MVEL.eval("new java.util.Date()"));
    }

}
