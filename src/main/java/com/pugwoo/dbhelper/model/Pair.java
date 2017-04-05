package com.pugwoo.dbhelper.model;

import java.io.Serializable;

/**
 * Created by pugwoo on 2017/4/5.
 *
 * 用于存放两个对象
 */
public class Pair<T1, T2> implements Serializable {

	private static final long serialVersionUID = 1L;

	private T1 t1;

    private T2 t2;

    public Pair() {
    }

    public Pair(T1 t1, T2 t2) {
        this.t1 = t1;
        this.t2 = t2;
    }

    public T1 getT1() {
        return t1;
    }

    public void setT1(T1 t1) {
        this.t1 = t1;
    }

	public T2 getT2() {
		return t2;
	}

	public void setT2(T2 t2) {
		this.t2 = t2;
	}
    
}
