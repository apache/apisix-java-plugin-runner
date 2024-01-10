package org.apache.apisix.plugin.runner.exception;

public class ApisixException extends RuntimeException{

    private int code;

    public ApisixException(int code, String msg) {
        super(msg);
        this.code = code;

    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
