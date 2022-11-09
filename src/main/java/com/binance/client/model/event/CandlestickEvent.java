package com.binance.client.model.event;

import com.binance.client.constant.BinanceApiConstants;
import com.wxx.nuclear_system.NuclerSystem;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class CandlestickEvent {
    /*****日志系统***
     * @2022-08-10
     * */
    private static final Logger log = LoggerFactory.getLogger(CandlestickEvent.class);

    private String eventType;

    private Long eventTime;

    private String symbol;

    /**添加一个【做多】补仓次数
     * 更新区分做多和做空
     * @2022-08-10
     * */
    private int longMarginCallTimes;

    private Long startTime;

    private Long closeTime;

    private String interval;

    private Long firstTradeId;

    private Long lastTradeId;

    private BigDecimal open;

    private BigDecimal close;

    private BigDecimal high;

    private BigDecimal low;

    /**添加一个增长率
     * 当前K线数据基于当前K线开始时候的增长率，比如3.0%，就是当前K线增长3.0%
     * @2022-0720
     * */
    private BigDecimal upRate;

    /**添加一个增长率
     * 当前这个数据基于上一个数据，计算出来的 瞬时波动率。就是 2.3% / s
     * 这个值就是 pumpRate
     * @2022-0722
     * */
    private BigDecimal pumpRate;

    /** 从这个数据开始往后，分析模型数据
     * @2022-0730
     * */
    private boolean startAnalysisModel;

    private BigDecimal volume;

    private Long numTrades;

    private Boolean isClosed;

    private BigDecimal quoteAssetVolume;

    private BigDecimal takerBuyBaseAssetVolume;

    private BigDecimal takerBuyQuoteAssetVolume;

    private Long ignore;

    /**开始补仓之后，已经过的KLine数目
     * @2022-08-30
     * */
    private int kLineElipsedCountAfterStartMarginCall;

    /**是否已停止补仓：开始补仓之后，经过多少根K之后，判断不能再补仓的标志
     * @2022-08-30
     * */
    private boolean isStopMarginCall;

    /**@2022-10-03
     *开始进行K线计数
     *  用来判断停止补仓，和恢复补仓
     * */
    private boolean startCountingKLineElipsed;

    /**
     * @2022-10-30 蚂蚁计数
     * */
    private int antCount;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getEventTime() {
        return eventTime;
    }

    public void setEventTime(Long eventTime) {
        this.eventTime = eventTime;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getLongMarginCallTimes() {
        return longMarginCallTimes;
    }

    public void setLongMarginCallTimes(int longMarginCallTimes) {
        this.longMarginCallTimes = longMarginCallTimes;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Long closeTime) {
        this.closeTime = closeTime;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public Long getFirstTradeId() {
        return firstTradeId;
    }

    public void setFirstTradeId(Long firstTradeId) {
        this.firstTradeId = firstTradeId;
    }

    public Long getLastTradeId() {
        return lastTradeId;
    }

    public void setLastTradeId(Long lastTradeId) {
        this.lastTradeId = lastTradeId;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public void setOpen(BigDecimal open) {
        this.open = open;
    }

    public BigDecimal getClose() {
        return close;
    }

    public void setClose(BigDecimal close) {
        this.close = close;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public void setHigh(BigDecimal high) {
        this.high = high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public void setLow(BigDecimal low) {
        this.low = low;
    }

    public BigDecimal getUpRate() {
        return upRate;
    }

    public void setUpRate(BigDecimal upRate) {
        this.upRate = upRate;
    }

    public BigDecimal getPumpRate() {
        return pumpRate;
    }

    public void setPumpRate(BigDecimal pumpRate) {
        this.pumpRate = pumpRate;
    }

    public boolean isStartAnalysisModel() {
        return startAnalysisModel;
    }

    public void setStartAnalysisModel(boolean startAnalysisModel) {
        this.startAnalysisModel = startAnalysisModel;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    public Long getNumTrades() {
        return numTrades;
    }

    public void setNumTrades(Long numTrades) {
        this.numTrades = numTrades;
    }

    public Boolean getIsClosed() {
        return isClosed;
    }

    public void setIsClosed(Boolean isClosed) {
        this.isClosed = isClosed;
    }

    public BigDecimal getQuoteAssetVolume() {
        return quoteAssetVolume;
    }

    public void setQuoteAssetVolume(BigDecimal quoteAssetVolume) {
        this.quoteAssetVolume = quoteAssetVolume;
    }

    public BigDecimal getTakerBuyBaseAssetVolume() {
        return takerBuyBaseAssetVolume;
    }

    public void setTakerBuyBaseAssetVolume(BigDecimal takerBuyBaseAssetVolume) {
        this.takerBuyBaseAssetVolume = takerBuyBaseAssetVolume;
    }

    public BigDecimal getTakerBuyQuoteAssetVolume() {
        return takerBuyQuoteAssetVolume;
    }

    public void setTakerBuyQuoteAssetVolume(BigDecimal takerBuyQuoteAssetVolume) {
        this.takerBuyQuoteAssetVolume = takerBuyQuoteAssetVolume;
    }

    public Long getIgnore() {
        return ignore;
    }

    public void setIgnore(Long ignore) {
        this.ignore = ignore;
    }

    public void update(BigDecimal open,BigDecimal close,BigDecimal upRate,long eventTime){
        this.open = open;
        this.close = close;
        this.upRate = upRate;
        this.eventTime = eventTime;
    }

    public int getkLineElipsedCountAfterStartMarginCall() {
        return kLineElipsedCountAfterStartMarginCall;
    }

    public void setkLineElipsedCountAfterStartMarginCall(int kLineElipsedCountAfterStartMarginCall) {
        this.kLineElipsedCountAfterStartMarginCall = kLineElipsedCountAfterStartMarginCall;
    }

    public boolean isStopMarginCall() {
        return isStopMarginCall;
    }

    public void setStopMarginCall(boolean stopMarginCall) {
        isStopMarginCall = stopMarginCall;
    }

    public boolean isStartCountingKLineElipsed() {
        return startCountingKLineElipsed;
    }

    public void setStartCountingKLineElipsed(boolean startCountingKLineElipsed) {
        this.startCountingKLineElipsed = startCountingKLineElipsed;
    }

    public int getAntCount() {
        return antCount;
    }

    public void setAntCount(int antCount) {
        this.antCount = antCount;
    }

    /**TODO：核心思想就是两条：
     * TODO：    1 机会正确，开始计数，达到指定K线，停止补仓
     * TODO：    2 机会错误，开启其他币种继续补仓。
     *
     * 打印是用来测试的，生产全部去掉，多线程打印，很乱的，并且那个Symbol获取不到的，因为没有赋值，嗯。在外面合适的地方轻微打印一下就行了
     * */
    public void updateKlineElipsedCount(final String currency,
                                        final int ALLOW_MARGINCALL_KLINE,
                                        final int ALLOW_MARGINCALL_CD,
                                        final int ALLOW_OTHER_MARGINCALL_KLINE){

        /** TODO：步骤1：正确机会，开始累计 K线*/
        if( isStartCountingKLineElipsed() ) {
            kLineElipsedCountAfterStartMarginCall = kLineElipsedCountAfterStartMarginCall + 1;
            //每一根K结束都会打印，所以关掉，嗯
            //log.error("[" + currency +"]" + "[已过K线]" + kLineElipsedCountAfterStartMarginCall);
        }

        /**TODO：步骤2：机会不对，开启其他币种补仓恩。
         * TODO：如果 补仓了，但是没有开始K线计数，说明机会不对。
         * 因为如果补仓了，还可能是正确机会的第2根，第3根K，嗯。。。。
         * 所以如果计数没增加，然后还补仓了，那么一定是假机会，嗯。。。
         *
         * @2022-10-03
         *  假机会重置放这里比较好。首先不能放在实时判断那里，因为太多了。
         *  一分钟只需要执行一次。放在K线结束那里也没问题。放这里效率会更高？代码更简洁？？
         *  kLineElipsedCountAfterStartMarginCall == 0  和  !isStartCountingKLineElipsed()
         *  思考一下，是不是完全一样。在这里是完全一样的，在外面 用那个方法，恩
         * */
        if( !isStartCountingKLineElipsed() && (longMarginCallTimes > 0) ){
            NuclerSystem.resetToAllowOtherCurrencyMargincall();
            //log.error("[" + currency +"]" + "[假机会，将继续开启所有币种补仓]");
        }

        /**TODO：步骤3：开始根据经过的K线数，进行不同的处理，恩。
         * */
        /**放在外面设置标志位，一定没有任何问题，并且这种算法，一定要用 >= ，光用 = 不行*/
        if( kLineElipsedCountAfterStartMarginCall >= ALLOW_MARGINCALL_KLINE
                && kLineElipsedCountAfterStartMarginCall  < ALLOW_MARGINCALL_CD ){
            /** 关闭当前币种补仓。有个问题，这里需要一直设置？？？ 这里一直设置没问题 */
            isStopMarginCall = true;
            if( kLineElipsedCountAfterStartMarginCall == ALLOW_MARGINCALL_KLINE ) {
                log.error("[" + currency +"]" + "[已关闭补仓] cd " + ALLOW_MARGINCALL_CD + " min");
            }

            /**TODO:开启其他币种补仓，这里有个问题，卧槽，这是在子线程中，嗯。。。
             * TODO：这里有个问题：这里不能一直设置。。。所以这里要用 = 卧槽。。。是否会出现 ！= ？？
             * 经过仔细，缜密的详细的分析，判断：只要出现过一次符合条件的，那么一定会逐根计数，那么一定会走到，不存在跳跃的情况，嗯。。
             * */
            if( kLineElipsedCountAfterStartMarginCall == ALLOW_OTHER_MARGINCALL_KLINE ){
                NuclerSystem.resetToAllowOtherCurrencyMargincall();
                log.error("[" + currency +"]" + "[已开启其他币种补仓]");
            }

        }else if( kLineElipsedCountAfterStartMarginCall  >= ALLOW_MARGINCALL_CD ){
            /** 达到冷却时间，开启当前币种K线计数器 */
            startCountingKLineElipsed = false;
            /** 达到冷却时间，开启当前币种补仓 */
            isStopMarginCall = false;
            /**达到冷却时间，重置K线统计计数*/
            kLineElipsedCountAfterStartMarginCall = 0;
            log.error("[" + currency +"]" + "[冷却结束，已重新开启补仓]");
        }
    }

    /**TODO:K线结束的时候重置补仓状态
     * TODO:[!!!!!!!!!!!!]这个 upRate 一定不能重置，卧槽。。。导致，特么瞬时 波动率 ，上百。。。。。。。
     * */
    public void initMarginCallStatusAtKLineClose(){
        setLongMarginCallTimes(0);
        setStartAnalysisModel(false);
        /**TODO：一定不能重置，卧槽*/
        //setUpRate(BigDecimal.ZERO);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE).append("eventType", eventType)
                .append("eventTime", eventTime).append("symbol", symbol).append("startTime", startTime)
                .append("closeTime", closeTime).append("symbol", symbol).append("interval", interval)
                .append("firstTradeId", firstTradeId).append("lastTradeId", lastTradeId).append("open", open)
                .append("close", close).append("high", high).append("low", low).append("volume", volume)
                .append("numTrades", numTrades).append("isClosed", isClosed)
                .append("quoteAssetVolume", quoteAssetVolume).append("takerBuyBaseAssetVolume", takerBuyBaseAssetVolume)
                .append("takerBuyQuoteAssetVolume", takerBuyQuoteAssetVolume).append("ignore", ignore).toString();
    }

}