package com.binance.client.exception;

public class BinanceApiException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 4360108982268949009L;
    public static final String RUNTIME_ERROR = "RuntimeError";
    public static final String INPUT_ERROR = "InputError";
    public static final String KEY_MISSING = "KeyMissing";
    public static final String SYS_ERROR = "SysError";
    public static final String SUBSCRIPTION_ERROR = "SubscriptionError";
    public static final String ENV_ERROR = "EnvironmentError";
    public static final String EXEC_ERROR = "ExecuteError";

    /***Add errCode @2022-06-28-1100***/
    private String errCode;
    private String errType;


    public BinanceApiException(String errType, String errCode,String errMsg) {
        super(errMsg);
        this.errType = errType;
        /***Add errCode @2022-06-28-1100***/
        this.errCode = errCode;

    }

    public BinanceApiException(String errType, String errCode, String errMsg, Throwable e) {
        super(errMsg, e);
        this.errType = errType;
        /***Add errCode @2022-06-28-1100***/
        this.errCode = errCode;
    }

    public String getErrType() {
        return this.errType;
    }
    /***Add errCode @2022-06-28-1100***/
    public String getErrCode() {
        return this.errCode;
    }

}
