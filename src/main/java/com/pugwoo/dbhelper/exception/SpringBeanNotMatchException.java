package com.pugwoo.dbhelper.exception;

/**
 * 当从Spring中获取的bean的类型不匹配时抛出
 */
public class SpringBeanNotMatchException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SpringBeanNotMatchException() {
    }

    public SpringBeanNotMatchException(String errMsg) {
        super(errMsg);
    }

    public SpringBeanNotMatchException(Throwable e) {
        super(e);
    }

}
