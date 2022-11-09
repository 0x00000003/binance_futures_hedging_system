package com.binance.client.impl;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.binance.client.exception.BinanceApiException;
import com.binance.client.impl.utils.JsonWrapper;

abstract class RestApiInvoker {

    private static final Logger log = LoggerFactory.getLogger(RestApiInvoker.class);
    private static final OkHttpClient client = new OkHttpClient();
    /**TODO：更新并发访问数量，以在一个进程中同时监听所有币种
     * 单个连接最多可以订阅 200 个Streams
     * https://binance-docs.github.io/apidocs/futures/cn/#websocket
     * */
    static{
        client.dispatcher().setMaxRequests(200);
    }

    static void checkResponse(JsonWrapper json) {
        try {
            if (json.containKey("success")) {
                boolean success = json.getBoolean("success");
                if (!success) {
                    String err_code = json.getStringOrDefault("code", "");
                    String err_msg = json.getStringOrDefault("msg", "");
                    if ("".equals(err_code)) {
                        throw new BinanceApiException(BinanceApiException.EXEC_ERROR, err_code, "[Executing] " + err_msg);
                    } else {
                        /***Add errCode @2022-06-28-1100***/
                        throw new BinanceApiException(BinanceApiException.EXEC_ERROR, err_code,
                                "[Executing] " + err_code + ": " + err_msg);
                    }
                }
            } else if (json.containKey("code")) {

                int code = json.getInteger("code");
                if (code != 200) {
                    /***Add errCode @2022-06-28-1100***/
                    String message = json.getStringOrDefault("msg", "");
                    throw new BinanceApiException(BinanceApiException.EXEC_ERROR, code + "",
                            "[Executing] " + code + ": " + message);

                }
            }
        } catch (BinanceApiException e) {
            throw e;
        } catch (Exception e) {
            /***Add errCode @2022-06-28-1100***/
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR, BinanceApiException.RUNTIME_ERROR,
                    "[Invoking] Unexpected error: " + e.getMessage());
        }
    }

    static <T> T callSync(RestApiRequest<T> request) {
        try {
            String str;
            log.debug("Request URL " + request.request.url());
            Response response = client.newCall(request.request).execute();
            // System.out.println(response.body().string());
            if (response != null && response.body() != null) {
                str = response.body().string();
                response.close();
            } else {
                /***Add errCode @2022-06-28-1100***/
                throw new BinanceApiException(BinanceApiException.ENV_ERROR,BinanceApiException.ENV_ERROR,
                        "[Invoking] Cannot get the response from server");
            }
            log.debug("Response =====> " + str);
            JsonWrapper jsonWrapper = JsonWrapper.parseFromString(str);
            checkResponse(jsonWrapper);
            return request.jsonParser.parseJson(jsonWrapper);
        } catch (BinanceApiException e) {
            throw e;
        } catch (Exception e) {
            /***Add errCode @2022-06-28-1100***/
            throw new BinanceApiException(BinanceApiException.ENV_ERROR,BinanceApiException.ENV_ERROR,
                    "[Invoking] Unexpected error: " + e.getMessage());
        }
    }

    static WebSocket createWebSocket(Request request, WebSocketListener listener) {
        return client.newWebSocket(request, listener);
    }

}
