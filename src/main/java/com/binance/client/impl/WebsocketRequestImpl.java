package com.binance.client.impl;

import com.binance.client.SubscriptionErrorHandler;
import com.binance.client.SubscriptionListener;
import com.binance.client.impl.utils.Channels;
import com.binance.client.impl.utils.Handler;
import com.binance.client.impl.utils.JsonWrapper;
import com.binance.client.model.enums.CandlestickInterval;
import com.binance.client.model.event.CandlestickEvent;


class WebsocketRequestImpl {

    WebsocketRequestImpl() {
    }

    WebsocketRequest<CandlestickEvent> subscribeCandlestickEvent(String symbol, CandlestickInterval interval,
            SubscriptionListener<CandlestickEvent> subscriptionListener,
            SubscriptionErrorHandler errorHandler) {
        InputChecker.checker()
                .shouldNotNull(symbol, "symbol")
                .shouldNotNull(subscriptionListener, "listener");
        WebsocketRequest<CandlestickEvent> request = new WebsocketRequest<>(subscriptionListener, errorHandler);
        request.name = "***Candlestick for " + symbol + "***";

        /**remove Lambda @ 2022-07-02*/
        request.connectionHandler = new Handler<WebSocketConnection>() {
            @Override
            public void handle(WebSocketConnection connection) {
                connection.send(Channels.candlestickChannel(symbol, interval));
            }
        };

        /**remove Lambda @ 2022-07-02*/
        request.jsonParser = new RestApiJsonParser<CandlestickEvent>() {
            @Override
            public CandlestickEvent parseJson(JsonWrapper jsonWrapper) {
                CandlestickEvent result = new CandlestickEvent();
                result.setEventType(jsonWrapper.getString("e"));
                result.setEventTime(jsonWrapper.getLong("E"));
                result.setSymbol(jsonWrapper.getString("s"));
                JsonWrapper jsondata = jsonWrapper.getJsonObject("k");
                result.setStartTime(jsondata.getLong("t"));
                result.setCloseTime(jsondata.getLong("T"));
                result.setSymbol(jsondata.getString("s"));
                result.setInterval(jsondata.getString("i"));
                result.setFirstTradeId(jsondata.getLong("f"));
                result.setLastTradeId(jsondata.getLong("L"));
                result.setOpen(jsondata.getBigDecimal("o"));
                result.setClose(jsondata.getBigDecimal("c"));
                result.setHigh(jsondata.getBigDecimal("h"));
                result.setLow(jsondata.getBigDecimal("l"));
                result.setVolume(jsondata.getBigDecimal("v"));
                result.setNumTrades(jsondata.getLong("n"));
                result.setIsClosed(jsondata.getBoolean("x"));
                result.setQuoteAssetVolume(jsondata.getBigDecimal("q"));
                result.setTakerBuyBaseAssetVolume(jsondata.getBigDecimal("V"));
                result.setTakerBuyQuoteAssetVolume(jsondata.getBigDecimal("Q"));
                result.setIgnore(jsondata.getLong("B"));
                return result;
            }
        };
        return request;
    }


}
