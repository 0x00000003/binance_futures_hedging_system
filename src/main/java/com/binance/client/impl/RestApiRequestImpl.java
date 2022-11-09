package com.binance.client.impl;

import com.alibaba.fastjson.JSONObject;
import com.binance.client.RequestOptions;
import com.binance.client.exception.BinanceApiException;
import com.binance.client.impl.utils.Handler;
import com.binance.client.impl.utils.JsonWrapper;
import com.binance.client.impl.utils.JsonWrapperArray;
import com.binance.client.impl.utils.UrlParamsBuilder;
import com.binance.client.model.enums.*;
import com.binance.client.model.market.ExchangeFilter;
import com.binance.client.model.market.ExchangeInfoEntry;
import com.binance.client.model.market.ExchangeInformation;
import com.binance.client.model.market.RateLimit;
import com.binance.client.model.trade.AccountBalance;
import com.binance.client.model.trade.Order;
import okhttp3.Request;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class RestApiRequestImpl {

    private String apiKey;
    private String secretKey;
    private String serverUrl;

    RestApiRequestImpl(String apiKey, String secretKey, RequestOptions options) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.serverUrl = options.getUrl();
    }

    /**Remove print @ 2022-06-16-1324***/
    private Request createRequestByGet(String address, UrlParamsBuilder builder) {
        //System.out.println(serverUrl);
        return createRequestByGet(serverUrl, address, builder);
    }

    private Request createRequestByGet(String url, String address, UrlParamsBuilder builder) {
        return createRequest(url, address, builder);
    }

    private Request createRequest(String url, String address, UrlParamsBuilder builder) {
        String requestUrl = url + address;
        System.out.print(requestUrl);
        if (builder != null) {
            if (builder.hasPostParam()) {
                return new Request.Builder().url(requestUrl).post(builder.buildPostBody())
                        .addHeader("Content-Type", "application/json")
                        .addHeader("client_SDK_Version", "binance_futures-1.0.1-java").build();
            } else {
                return new Request.Builder().url(requestUrl + builder.buildUrl())
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .addHeader("client_SDK_Version", "binance_futures-1.0.1-java").build();
            }
        } else {
            return new Request.Builder().url(requestUrl).addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("client_SDK_Version", "binance_futures-1.0.1-java")
                    .build();
        }
    }

    private Request createRequestWithSignature(String url, String address, UrlParamsBuilder builder) {
        if (builder == null) {
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,BinanceApiException.RUNTIME_ERROR,
                    "[Invoking] Builder is null when create request with Signature");
        }
        String requestUrl = url + address;
        new ApiSignature().createSignature(apiKey, secretKey, builder);
        if (builder.hasPostParam()) {
            requestUrl += builder.buildUrl();
            return new Request.Builder().url(requestUrl).post(builder.buildPostBody())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-MBX-APIKEY", apiKey)
                    .addHeader("client_SDK_Version", "binance_futures-1.0.1-java")
                    .build();
        } else if (builder.checkMethod("PUT")) {
            requestUrl += builder.buildUrl();
            return new Request.Builder().url(requestUrl)
                    .put(builder.buildPostBody())
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("X-MBX-APIKEY", apiKey)
                    .addHeader("client_SDK_Version", "binance_futures-1.0.1-java")
                    .build();
        } else if (builder.checkMethod("DELETE")) {
            requestUrl += builder.buildUrl();
            return new Request.Builder().url(requestUrl)
                    .delete()
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("client_SDK_Version", "binance_futures-1.0.1-java")
                    .addHeader("X-MBX-APIKEY", apiKey)
                    .build();
        } else {
            requestUrl += builder.buildUrl();
            return new Request.Builder().url(requestUrl)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("client_SDK_Version", "binance_futures-1.0.1-java")
                    .addHeader("X-MBX-APIKEY", apiKey)
                    .build();
        }
    }

    private Request createRequestByPostWithSignature(String address, UrlParamsBuilder builder) {
        return createRequestWithSignature(serverUrl, address, builder.setMethod("POST"));
    }

    private Request createRequestByGetWithSignature(String address, UrlParamsBuilder builder) {
        return createRequestWithSignature(serverUrl, address, builder);
    }

    private Request createRequestWithApikey(String url, String address, UrlParamsBuilder builder) {
        if (builder == null) {
            throw new BinanceApiException(BinanceApiException.RUNTIME_ERROR,BinanceApiException.RUNTIME_ERROR,
                    "[Invoking] Builder is null when create request with Signature");
        }
        String requestUrl = url + address;
        requestUrl += builder.buildUrl();
        if (builder.hasPostParam()) {
            return new Request.Builder().url(requestUrl)
                    .post(builder.buildPostBody())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-MBX-APIKEY", apiKey)
                    .addHeader("client_SDK_Version", "binance_futures-1.0.1-java")
                    .build();
        } else if (builder.checkMethod("DELETE")) {
            return new Request.Builder().url(requestUrl)
                    .delete()
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("X-MBX-APIKEY", apiKey)
                    .addHeader("client_SDK_Version", "binance_futures-1.0.1-java")
                    .build();
        } else if (builder.checkMethod("PUT")) {
            return new Request.Builder().url(requestUrl)
                    .put(builder.buildPostBody())
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("X-MBX-APIKEY", apiKey)
                    .addHeader("client_SDK_Version", "binance_futures-1.0.1-java")
                    .build();
        } else {
            return new Request.Builder().url(requestUrl)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("X-MBX-APIKEY", apiKey)
                    .addHeader("client_SDK_Version", "binance_futures-1.0.1-java")
                    .build();
        }
    }

    private Request createRequestByGetWithApikey(String address, UrlParamsBuilder builder) {
        return createRequestWithApikey(serverUrl, address, builder);
    }

    RestApiRequest<ExchangeInformation> getExchangeInformation() {
        RestApiRequest<ExchangeInformation> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build();
        request.request = createRequestByGet("/fapi/v1/exchangeInfo", builder);

        /**remove Lambda @ 2022-07-02*/
        request.jsonParser = new RestApiJsonParser<ExchangeInformation>() {
            @Override
            public ExchangeInformation parseJson(JsonWrapper jsonWrapper) {

                ExchangeInformation result = new ExchangeInformation();
                result.setTimezone(jsonWrapper.getString("timezone"));
                result.setServerTime(jsonWrapper.getLong("serverTime"));

                List<RateLimit> elementList = new LinkedList<>();
                JsonWrapperArray dataArray = jsonWrapper.getJsonArray("rateLimits");

                /**remove Lambda @ 2022-07-02*/
                dataArray.forEach(new Handler<JsonWrapper>() {
                    @Override
                    public void handle(JsonWrapper item) {
                        RateLimit element = new RateLimit();
                        element.setRateLimitType(item.getString("rateLimitType"));
                        element.setInterval(item.getString("interval"));
                        element.setIntervalNum(item.getLong("intervalNum"));
                        element.setLimit(item.getLong("limit"));
                        elementList.add(element);
                    }
                });

                result.setRateLimits(elementList);

                List<ExchangeFilter> filterList = new LinkedList<>();
                JsonWrapperArray filterArray = jsonWrapper.getJsonArray("exchangeFilters");

                /**remove Lambda @ 2022-07-02*/
                filterArray.forEach(new Handler<JsonWrapper>() {
                    @Override
                    public void handle(JsonWrapper item) {
                        ExchangeFilter filter = new ExchangeFilter();
                        filter.setFilterType(item.getString("filterType"));
                        filter.setMaxNumOrders(item.getLong("maxNumOrders"));
                        filter.setMaxNumAlgoOrders(item.getLong("maxNumAlgoOrders"));
                        filterList.add(filter);
                    }
                });
                result.setExchangeFilters(filterList);

                List<ExchangeInfoEntry> symbolList = new LinkedList<>();
                JsonWrapperArray symbolArray = jsonWrapper.getJsonArray("symbols");

                /**remove Lambda @ 2022-07-02*/
                symbolArray.forEach(new Handler<JsonWrapper>() {
                    @Override
                    public void handle(JsonWrapper item) {
                        ExchangeInfoEntry symbol = new ExchangeInfoEntry();
                        symbol.setSymbol(item.getString("symbol"));
                        symbol.setStatus(item.getString("status"));
                        symbol.setMaintMarginPercent(item.getBigDecimal("maintMarginPercent"));
                        symbol.setRequiredMarginPercent(item.getBigDecimal("requiredMarginPercent"));
                        symbol.setBaseAsset(item.getString("baseAsset"));
                        symbol.setQuoteAsset(item.getString("quoteAsset"));
                        symbol.setPricePrecision(item.getLong("pricePrecision"));
                        symbol.setQuantityPrecision(item.getLong("quantityPrecision"));
                        symbol.setBaseAssetPrecision(item.getLong("baseAssetPrecision"));
                        symbol.setQuotePrecision(item.getLong("quotePrecision"));
                        symbol.setOrderTypes(item.getJsonArray("orderTypes").convert2StringList());
                        symbol.setTimeInForce(item.getJsonArray("orderTypes").convert2StringList());
                        List<List<Map<String, String>>> valList = new LinkedList<>();
                        JsonWrapperArray valArray = item.getJsonArray("filters");

                        /**remove Lambda @ 2022-07-02*/
                        valArray.forEach(new Handler<JsonWrapper>() {
                            @Override
                            public void handle(JsonWrapper val) {
                                valList.add(val.convert2DictList());
                            }
                        });
                        symbol.setFilters(valList);
                        symbolList.add(symbol);
                    }
                });
                result.setSymbols(symbolList);

                return result;
            }
        };

        return request;
    }

    RestApiRequest<Order> postOrder(String symbol, OrderSide side, PositionSide positionSide, OrderType orderType,
            TimeInForce timeInForce, String quantity, String price, String reduceOnly,
            String newClientOrderId, String stopPrice, WorkingType workingType, NewOrderRespType newOrderRespType) {
        RestApiRequest<Order> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build()
                .putToUrl("symbol", symbol)
                .putToUrl("side", side)
                .putToUrl("positionSide", positionSide)
                .putToUrl("type", orderType)
                .putToUrl("timeInForce", timeInForce)
                .putToUrl("quantity", quantity)
                .putToUrl("price", price)
                .putToUrl("reduceOnly", reduceOnly)
                .putToUrl("newClientOrderId", newClientOrderId)
                .putToUrl("stopPrice", stopPrice)
                .putToUrl("workingType", workingType)
                .putToUrl("newOrderRespType", newOrderRespType);

        request.request = createRequestByPostWithSignature("/fapi/v1/order", builder);

        /**remove Lambda @ 2022-07-02*/
        request.jsonParser = new RestApiJsonParser<Order>() {
            @Override
            public Order parseJson(JsonWrapper jsonWrapper) {
                Order result = new Order();
                result.setClientOrderId(jsonWrapper.getString("clientOrderId"));
                result.setCumQuote(jsonWrapper.getBigDecimal("cumQuote"));
                result.setExecutedQty(jsonWrapper.getBigDecimal("executedQty"));
                result.setOrderId(jsonWrapper.getLong("orderId"));
                result.setOrigQty(jsonWrapper.getBigDecimal("origQty"));
                result.setPrice(jsonWrapper.getBigDecimal("price"));
                result.setReduceOnly(jsonWrapper.getBoolean("reduceOnly"));
                result.setSide(jsonWrapper.getString("side"));
                result.setPositionSide(jsonWrapper.getString("positionSide"));
                result.setStatus(jsonWrapper.getString("status"));
                result.setStopPrice(jsonWrapper.getBigDecimal("stopPrice"));
                result.setSymbol(jsonWrapper.getString("symbol"));
                result.setTimeInForce(jsonWrapper.getString("timeInForce"));
                result.setType(jsonWrapper.getString("type"));
                result.setUpdateTime(jsonWrapper.getLong("updateTime"));
                result.setWorkingType(jsonWrapper.getString("workingType"));
                return result;
            }
        };

        return request;
    }

    RestApiRequest<JSONObject> getPositionSide() {
        RestApiRequest<JSONObject> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build();
        request.request = createRequestByGetWithSignature("/fapi/v1/positionSide/dual", builder);

        /**remove Lambda @ 2022-07-02*/
        request.jsonParser = new RestApiJsonParser<JSONObject>() {
            @Override
            public JSONObject parseJson(JsonWrapper jsonWrapper) {
                JSONObject result = new JSONObject();
                result.put("dualSidePosition", jsonWrapper.getBoolean("dualSidePosition"));
                return result;
            }
        };
        return request;
    }

    RestApiRequest<List<AccountBalance>> getBalance() {
        RestApiRequest<List<AccountBalance>> request = new RestApiRequest<>();
        UrlParamsBuilder builder = UrlParamsBuilder.build();
        request.request = createRequestByGetWithSignature("/fapi/v1/balance", builder);

        /**remove Lambda @ 2022-07-02*/
        request.jsonParser = new RestApiJsonParser<List<AccountBalance>>() {
            @Override
            public List<AccountBalance> parseJson(JsonWrapper jsonWrapper) {
                List<AccountBalance> result = new LinkedList<>();
                JsonWrapperArray dataArray = jsonWrapper.getJsonArray("data");

                /**remove Lambda @ 2022-07-02*/
                dataArray.forEach(new Handler<JsonWrapper>() {
                    @Override
                    public void handle(JsonWrapper item) {
                        AccountBalance element = new AccountBalance();
                        element.setAsset(item.getString("asset"));
                        element.setBalance(item.getBigDecimal("balance"));
                        element.setWithdrawAvailable(item.getBigDecimal("withdrawAvailable"));
                        result.add(element);
                    }
                });
                return result;
            }
        };
        return request;
    }

}
