package com.wxx.nuclear_system;

import com.binance.client.*;
import com.binance.client.exception.BinanceApiException;
import com.binance.client.model.enums.*;
import com.binance.client.model.event.CandlestickEvent;
import com.binance.client.model.market.ExchangeInfoEntry;
import com.binance.client.model.trade.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**TODO 【心不死，则道不生】
 * TODO 作者:吴相兴
 * TODO 仅限个人学习使用
 * TODO 系统名称已更新为 【核武器】
 * TODO 下面的说明是从0到1的详细迭代。
 * TODO 由于内容太多，无法覆盖所有迭代细节，所以此说明文档不再更新，只在最后更新改动说明 @2022-08-09
 * **/

/**
 * TODO：综述 【核武器诞生】
 * TODO：【融合6大主系统，若干小系统，合而为一】
 *      1   空军速率监控系统
 *      2   2%自动止损系统
 *      3   动态止盈系统
 *      4   3段采样自动补仓系统
 *      5   速率系统与币安客户端的进程间通讯系统
 *      6   一键光速切换系统
 *      7   行情响铃服务系统
 *      8   语音播报系统
 *      9   不可抗力抵抗系统
 *      10  全频段自动补仓系统
 *      11  账户倍率限制系统
 *      12  账户做多限制系统
 *      13  大行情订单限制系统
 *      14  瀑布提醒系统
 *      15  动态补仓曲线系统
 *      16  移动止盈止损系统
 *      17  等等等，实现了人类绝对无法完成的操作。
 *      18   终成核武器。
 * **/

/***
 * TODO:吴相兴 实现的【空军速率系统】简介：
 * 初始功能（迭代过程中部分功能可能发生变化）
        1 速率算法实现:只分析1分钟K线即可
        2 本代码解决了币安服务器的限制
        3 本代码合并了蚂蚁爬坡检测系统
        4 本代码结合了无时间限制检测系统
        5 本代码结合了离线监控系统
        6 本代码结合了多窗口分段展示系统
        7 本代码结合了分段分析K线系统
        8 本代码结合了1 3 5 15 不同时间K线检测系统
        9 本代码结合了动态倍率系统
        10 本代码结合了动态监控数值系统
        11 由于本代码极限考虑性能，而速率系统只需要分析1分钟K线，所以K线周期设置代码删掉，需无时间限制系统去用旧版代码
        12 KLine实时分析系统，采用Websocket接口接收数据流，将实时性能最大化，禁止使用币安提供的其他方式获取。

        13 TODO:空军速率系统数据更新记录：

             TODO:第3版@【2022 06 13】。
             1 移除蚂蚁监控系统
             2 时间限制从5秒改为4秒，相关定义也已移除。
             3 其他数据保持不变。

             TODO:第2版生产力数据：【@ 2022 06 05】   时间限制为5秒。
             1.0		采样基准为0.3
             2.0		采样基准为0.9
             3.0		采样基准为2.0

             TODO:第1版生产力数据：【before 2022 06 05】   时间限制为7秒。
             1 1.0		采样基准为0.4
             1 2.0		采样基准为1.0
             1 3.0		采样基准为2.0

             TODO:简单解释
             三级监控。以上面为例
             1 从0.4到  1.0						小于5秒，说明在插针
             2 从1.0 到 2.0 之间的速度				小于5秒，说明在插针
             3 从 2.0到 3.0 之间的速度				小于5秒，说明在插针
 ***/



/**TODO 吴相兴 实现的【全自动止盈止损系统】简介
 * 【融合在第三部分监控窗口中显示。。】
 * TODO
 *      1   这个就是2分钟止盈止损系统的完美实现。。
 *      2   此系统可最大限度的降低黑天鹅事件的损失。
 *      3   此系统的核心使命，是要解决那1%的情绪波动所带来的损失。
 *      4   最大允许亏损总仓位动态更新2%。
 *      5   比速率系统还复杂
 *      6   集成并实现了K线监控分析系统
 *      7   集成并实现了自动止盈系统
 *      8   集成并实现了自动止损系统
 *      9   集成并实现了一键平仓系统
 *      10  集成并实现了离线监控系统
 *      11  集成并实现了动态止盈系统
 *      12  集成并实现了2%自动止损系统
 *      13  集成并实现了2根K线自动止损系统
 *      14  解决了币安SDK WebSocket断线重连问题
 *      15  解决ListenKey过期
 *      16  解决了币安SDK V2接口未适配问题
 *      17  解决了币安SDK 多个接口报错问题
 *      18  所有逻辑和算法均最大限度考虑性能和稳定性以及可靠性。
 *      19  KLine实时分析系统，采用Websocket接口接收数据流，将实时性能最大化，禁止使用币安提供的其他方式获取。
 *      20  通过实现以上所有功能，最终才实现了 2分钟 止盈止损系统。
 *      21  TODO: 注意这套系统的所有数据精确计算，注意区分是不是百分比。
 *      22  TODO：由于订单任何时刻都可能发生，所以本系统启动之后除了腾讯服务器炸裂之外不允许停止运行。
 *      23  TODO：不可抗力考虑：使用linux screen登录，即使本地断网，系统运行不会受到任何影响。
 ***/

/**TODO 其余系统介绍，在README.md或者手稿或者代码注释中
 *  包含所有思路，从0到1的所有构建，都在手稿里面，嗯。
 *  思路和灵感太多，只看一个地方无法完全了解。
 * **/



/**TODO：名词解释
 * Position         :仓位
 * SHORT            :做空
 * LONG             :做多
 * SHORT    SELL    :  开空
 * SHORT    BUY     :  平空
 * LONG     BUY     :  做多
 * LONG     SELL    :  平多
 * **/


/***
 * TODO:系统简介
 *      1   分3部分，9个窗口
 *      2   9个窗口分别监控三个分段的波动
 *      3   前3个窗口还集成了自动补仓系统
 *      4   第2部分窗口的最后一个窗口集成了动态倍率系统
 *      5   第3部分窗口集成了2%自动止损系统
 *      6   第3部分窗口接受任意时刻的数字输入，动态更新止盈
 *      7   第1部分窗口接受任意时刻的输入，手动开关自动谷仓系统
 *      8   进程间通讯系统正则表达式  \[ (.*?) ] 只匹配 [ 币种 ] ,比如[ DOGEUSDT ] ，前后带一个空格，SecureCRTY也进行这样匹配高亮。
 *      9   监控梯度，1-3窗口1%-2%，3-6窗口>3%  6-9窗口去掉监控，嗯。自从添加时间参数之后，其实1-3窗口可以显示全部信息
 *              但是使用的时候还是要输入，以区分不同部分窗口，1    3   5 即可
 *      10  所有往核武器里面合并的代码，一定要经过严格的测试，嗯。
 *      11  所有的架构设计的时候要考虑【同一份代码是运行于不同的进程当中，这一精妙绝伦的构思。】
 *      12  所有一切功能实现和代码的添加，都要以绝对不能降低补仓系统性能为前提。因为1毫秒的补仓位置差距，可能带来0.1%x10=1%的经济损失。

 *TODO: 更新【2022-07-09】
 *      1   前3个窗口改为 全频段监控系统
 *      2   4-6窗口为 原来的3段采样补仓系统
 *      3   第7-9 仍然为自动止损系统
 *      4   7-9窗口为无法中断的自动止损系统
 *      5   1-6 现已可中断，嗯。
 *      6   第7窗口，添加自动更新配置系统。
 *      7   第8窗口，添加倍率限制系统。
 *      8   第7-9窗口，添加做多限制系统。多单限制系统是跟随币种，币种在哪个进程就在哪个进程平仓
 *      9   其余不变
 *
 *      TODO：EXTRA
 *             TODO:全频段高频快速撸小单算法的曲线。
 *  *          TODO：不能使用2次幂
 *  *          TODO：不能使用3次幂
 *  *          TODO：就是用混合幂，严格限制仓位，快速出入，然后出现意外手动止损，嗯。
 *  *          TODO：目前综合效果最佳的是从第2次开始补仓严格限制补仓3次的混合幂。
 *  *          TODO：根据不同的行情，切换不同的曲线，切换不同的首单，甚至关闭补仓，灵活应对，嗯。
 *
 *TODO: 更新【2022-07-23】
 *      1   前3个窗口改为 带波动率+时间戳的 真全频段监控 补仓系统 （可以完整监控一个币从0.5开始快速波动到无穷大涨幅之间的所有数据）
 *      2   4-6窗口为 波动率监控 补仓系统 ，已去掉 3段采样系统
 *      3   4-6窗口 已支持真正的 不同行情 不同参数。输入 涨幅1.5波动率2.5  进行动态修正参数。
 *      4   第7-9 仍然为自动止损系统
 *      5   7-9窗口为无法中断的自动止损系统
 *      6   1-6 现已可中断，嗯。
 *      7   第7窗口，添加自动更新配置系统。
 *      8   第8窗口，添加倍率限制系统。
 *      9   第7-9窗口，添加做多限制系统。多单限制系统是跟随币种，币种在哪个进程就在哪个进程平仓
 *      10   第7-9窗口，现在支持开启做多，输入 开启做多 关闭做多 切换

 *TODO: 更新【2022-07-25】
 *      1   7-9已开启做多支持，输入 开启做多 关闭做多 切换，默认关闭
 *      2   7-9已可关闭2分钟系统，输入 关闭2分钟系统 开启2分钟系统 进行切换
 *      3   7-9已移除倍率限制，倍率限制现已单独放到永久后台进行限制，永久运行。【经过测试，已实现完整24小时，所有进程没有任何报错，嗯，倍率切换过500次以上了，嗯。】
 *      4   现已真正支持超大数量币种下单，使用递归彻底解决问题。
 *
 *TODO: 更新【2022-08-09】
 *      1   1-3 为波动率补仓系统+动态倍率系统+自动复利系统
 *      2   4-6 为 自动止损+移动止损+移动止盈系统
 *
 *TODO：使用步骤：
 *
 *  TODO：@2022-07-27更新
 *  1   开启核武器补仓     【已默认关闭】     开启复利    【已默认关闭】
 *  2   关闭2分钟系统     【就是要手动输入】
 *  3   开启做多            【就是要手动输入】
 *  4   涨幅1.5波动率2.5     【就是要手动输入】
 *  5   关闭区间限制          【可选，默认是开启的】
 *
 *   *  TODO：@2022-07-28更新
 *  *  1   核武器已删除，只剩终极反物质武器。        输入1 和 2 启动系统
 *  *  2   关闭2分钟系统          【就是要手动输入】
 *  *  3   开启做多              【就是要手动输入】
 *  *  4   涨幅1.5波动率2.5      【就是要手动输入】
 *  *  5   关闭区间限制          【可选，默认是开启的】
 *
 * TODO：@2022-08-09更新
 *  *   2分钟系统默认已删除，用移动止盈止损+0.5%自动止损完美代替。
 *  *   可动态设置 止盈回调
 *  1   开启做多              【就是要手动输入】
 *  2   涨幅1.5波动率2.5      【就是要手动输入】
 *  3   关闭区间限制          【可选，默认是开启的】
 *
 *
 * TODO：@2022-08-10更新
 *  *   可动态设置 止盈回调
 *  *   已默认开启做多
 *  *   区间限制默认开启
 *  *   【每个进程一个独立文件】
 *  1   涨幅1.5波动率2.5      【就是要手动输入】
 *
 * * TODO：@2022-10-10更新
 *  *  *   已合并为3个进程
 *  *  *   可动态更新所有配置
 *  *  *   可动态设置补仓类型
 *  *  *   波动率监控
 *
 *  TODO：@2022-11-05更新
 *      这里未记录更新所有迭代过程，内容太多
 *      更多内容在文档笔记中。
 * ***/



public class NuclerSystem {

    /**TODO：币种列表文件*/
    private static final String USDTSymbolsFileName = "usdtSymbols_ALL.txt";
    /**TODO：币种列表文件*/



    /**补仓系统的配置文件*/
    private static String systemParamsConfigFile = new File(".") + File.separator + "hedgingSystemConfig.properties";
    private static final String USDTSymbolsFile = new File(".") + File.separator + USDTSymbolsFileName;
    /**TODO:最简单的数组，最高效。经过简单测试，ArrayList耗时是数组的差不多10倍*/
    private static String[] USDTSymbols;
    private static ArrayList<String> USDTSymbolsArray = new ArrayList<>();

    /******key*******/
    public static String API_KEY;
    public static String SECRET_KEY;
    private static String apiKeyConfigFile = new File(".") + File.separator + "apiConfig.properties";

    /*****日志系统****/
    private static final Logger log = LoggerFactory.getLogger(NuclerSystem.class);



    /**************************************【常量】***********************************************/
    /**
     * TODO:【常量】
     * TODO:【必须放到这里，跨类引用，存在着明显的性能损耗】
     * TODO:【命名总规则，名字的意义，就是数值的真正大小】
     * TODO：【主要混淆点，在于，不同算法对这个数据的处理，有的算法，自己乘了100了。
     *          所以，让输入端看起来可能有点奇怪，嗯。
     *          所以，输入端统一做适配即可】
     *
     *      百分比：后缀 _percent，就是百分比的真实数据
     *          举例 12% 就是0.12  所以   BigDecimal_12_0_percent = new BigDecimal("0.12");
     *      常量： 后缀 就是常量
     *          举例 0.5  就是 BigDecimal_0_5 = new BigDecimal("0.5");
     *      比率百分比：中间即携带rate，也携带percent，以数值结尾
     *          举例 涨幅比例 1.0% ,就是 比值 = 1.010   pump_rate_percent_1_0 = new BigDecimal("1.010" );
     * */

    /**
     * 就是数字的BigDecimal形式表示
     * */
    private static final BigDecimal BigDecimal_PERCENT = new BigDecimal("0.01");
    private static final BigDecimal BigDecimal_0_12 = new BigDecimal("0.12");
    private static final BigDecimal BigDecimal_0_9 = new BigDecimal("0.9");
    private static final BigDecimal BigDecimal_1_5 = new BigDecimal("1.5");
    private static final BigDecimal BigDecimal_2_0 = new BigDecimal("2.0");
    private static final BigDecimal BigDecimal_10 = BigDecimal.TEN;
    private static final BigDecimal BigDecimal_100 = new BigDecimal("100");
    private static final BigDecimal BigDecimal_1000 = new BigDecimal("1000");

    /** 常量
     * 就是百分比形式
     * 3_0 = 3.0
     * 30_0 = 30
     * ***/
    private static final BigDecimal BigDecimal_12_0_percent = BigDecimal_0_12;       //12%


    /**
     * 涨幅百分比比率：注意命名中 同时带了 rate 和 percent，嗯。
     * 涨幅比例 0.1% ,就是 比值 = 1.001
     * 涨幅比例 1.0% ,就是 比值 = 1.010
     * 直接写死，不需要计算。
     * */
    private static final BigDecimal pump_rate_percent_0_0 = BigDecimal.ONE;
    private static final BigDecimal pump_rate_percent_1_0 = new BigDecimal("1.010" );
    private static final BigDecimal pump_rate_percent_3_5 = new BigDecimal("1.035" );
    private static final BigDecimal pump_rate_percent_4_5 = new BigDecimal("1.045" );
    private static final BigDecimal pump_rate_percent_5_5 = new BigDecimal("1.055" );
    private static final BigDecimal pump_rate_percent_6_5 = new BigDecimal("1.065" );
    private static final BigDecimal pump_rate_percent_7_0 = new BigDecimal("1.070" );
    private static final BigDecimal pump_rate_percent_8_0 = new BigDecimal("1.080" );
    private static final BigDecimal pump_rate_percent_10_0 = new BigDecimal("1.100" );




    /**************************************【监控系统相关数据结构】***********************************************/

    /**服务器*/
    private static final SubscriptionClient client = SubscriptionClient.create();
    private static final RequestOptions options = new RequestOptions();
    private static SyncRequestClient syncRequestClient;
    /***系统启动时间**/
    private static long systemStartTime = 0l;


    /**判断币种的监控还活着   1 代表活着*/
    private static final ConcurrentHashMap<String,Integer> offLineMonitor = new ConcurrentHashMap<String,Integer>();
    private static long lastCheckTime = 0;
    private static long lastPrintTime = 0;
    private static final long checkTimeInterval = 3 * 60 * 1000; // 毫秒，3分钟检测一次
    private static final long printTimeInterval = 30 * 60 * 1000; // 毫秒，30分钟打印一次

    /**用来提醒开启播报系统*/
    private static boolean broadcastSystemStartup = false;
    /**用来提醒开始补仓*/
    private static boolean startMargincallStart = false;
    /**用来提醒更新监控数值*/
    private static boolean monitorValueUpdate = false;

    /**蚂蚁爬坡标准：8次算，可配置*/
    private static volatile int ANT_STANDARD = 8;


    /*****************************************【补仓系统共享数据结构】**************************************
     * 最大10个工作线程，完全足够了。因为下单是瞬时的，嗯。
     * @2022-10-07 从20改成10，完全足够，恩。
     * ****/
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /****币种下单数量精度
     * KEY:大写
     * ****/
    private static final ConcurrentHashMap<String,Long> AllSymbolQuantityPrecision = new ConcurrentHashMap<>();

    /** 通过命令行控制的自动补仓开关，默认关闭，防止刚启动参数还没初始化直接补裂开**/
    private static boolean enableAutoMarginCall = false;

    /**TODO：【重要】订单数量限制，同一时间只进行一个订单补仓。仅在手动打开补仓的地方进行重置。*/
    private static String currentMarginCallCurrency = null;

    /** 通过命令行控制的 区间限制*/
    private static boolean enableSectionLimit = true;

    /**大瀑布监控
     * 多次机会发现，只要 > 7.0% ，不需要管速率，就是真正的大事件，嗯。不到7还不够，嗯。
     * 测试用0.1就行了
     * */
    private static volatile BigDecimal WATERFALL_MONITORVALUE = pump_rate_percent_7_0; //pump_rate_0_1_percent;//测试

    /**BTC ETH 只要1分钟下跌大于1.0% 就是 大情绪，嗯。并且这俩是联动明显，所以只要有一个就要提醒，嗯。*/
    private static final BigDecimal BTC_ETH_WATERFALL_MONITORVALUE = pump_rate_percent_1_0;


    /**TODO：账户总仓位
     * 初始化为10u
     */
    private static BigDecimal ___POSITION_USDTs___ = BigDecimal_10;

    /**上次更新 保存的 仓位值。
     * 此值唯一作用：【用来限制 手动输入 仓位的上限，不影响其余所有逻辑。】
     * 所以此值是动态变化的，根据不断变化的仓位 可以 额外按照一定的比例进行变化。
     * 给个默认值，防止手速太快，刚启动就更新首单的极限情况会出现，然后就会空指针，，
     * 由于热身系统的存在，所以此值，一定会被更新到。即使一整天都没有符合条件的订单出现。
     * 所以，当发生仓位更新的时候，这个值和动态首单值要同时更新。然后此值是用来限制 手动输入 的范围。
     * */
    private static BigDecimal LAST___POSITION_USDTs___ = ___POSITION_USDTs___;


    /**补仓倍率**/
    private static volatile int MARGINCALL_LEVERAGE = 10;


    /***TODO:停止补仓的相关数据结构/

     /**TODO: 当前K线涨幅超过多少，停止补仓
     * 2022-08-30已更新为6.5%，嗯
     * */
    private static volatile BigDecimal STOP_MARGINCALL_KLINE_PUMPRATE = pump_rate_percent_6_5;

    /**TODO：达到这个涨幅之后，币种停止补仓
     * 开始进行停止补仓统计的 有效涨幅
     *这个停止补仓的有效涨幅 跟 前面有一个 危险涨幅停止补仓是一个吗？？
     *完全不是一回事，这里可以先在2.0%补仓，然后再拉到30%，嗯，只要当前K最后的涨幅 > 5%即可。而前面那个是在任何时刻，当大于危险涨幅的时候停止补仓，嗯。
     *
     * @2022-09-09 更新为 8 从 5
     * */
    private static volatile BigDecimal STOP_MARGINCALL_CURRENCY_PUMPRATE = pump_rate_percent_8_0;   //  正常是8.0，测试用 0.2

    /*** 最大允许补仓 K线 ：3根*/
    private static volatile int ALLOW_MARGINCALL_KLINE = 3;  // 正常是3 根K，测试用2根
    /** 最小补仓冷却时间：24小时 = 1440 分钟*/
    private static volatile int ALLOW_MARGINCALL_CD_MIN = 24 * 60; // 测试用 10分钟

    /** 当前币种补仓完毕之后，允许其他币种开始补仓的冷却时间*/
    private static volatile int ALLOW_OTHER_MARGINCALL_KLINE =  ALLOW_MARGINCALL_KLINE  + 5; // 正常是 + 5 分钟，测试用 + 2



    /*****************************************[4段阶梯补仓数据结构]***************************************************/

    /** TODO：区间限制思想
     * 涨幅1%到2%之间的仓位 x 0.5 就是半仓
     * 1-2			50%
     * 2-3			60%
     * 3-4			70%
     * 4-5			80%
     * 5-6			90%
     * >6			100%
     *
     * example:
     * 1.72%		3.22%/s
     * 3.22 X 100 = 322 /2 = 161 usd / 2 = 80.5 usd
     * 哦，这个加的作用是限制一下小幅波动超高波动率的时候的仓位，卧槽。比如
     * 1.72%	突然来了一个8%/s，但是没有停止
     * 但是他又涨到了3.2%	波动率为5.6%/s，之前发生了一次这种情况。
     * 那么不限制1-2之间的仓位的话，均价在特么1-2之间，嗯。
     *
     * 上面的是直接的只做空仓位。
     * 现在改成对冲的限制
     * 0%-3%    x 1.5 倍
     * 3%-4%    x 1.0 倍
     * 4%-5%    x 0.8 倍
     * >5%      x 0.5 倍
     *
     * @2022-10-24
     *  已更新为 直接 总仓位百分比进行补仓。
     * */

    /**
     * TODO：单次补仓仓位限制，系统刚启动的时候设置为10u，小心刚上来懵逼
     * 初始化为10u
     * */
    private static volatile BigDecimal EVERYTIME_MARGINCALL_POSITION_LIMIT = new BigDecimal("10");
    private static volatile BigDecimal EVERYTIME_MARGINCALL_POSITION_LIMIT_PERCENT = BigDecimal_0_9;

    /**4个区间的补仓占总仓位的百分比，可配置
     * 由于这个区间值有可能调整，所以不能用之前的带willMarginCallUSDTs_AT_0p_3p这种命名方式否则会改动配置文件
     * */
    /**默认值 1%*/
    private static final BigDecimal WILL_MARGINCALLUSDTS_PERCENT_DEFAULT = BigDecimal_PERCENT;
    private static volatile BigDecimal WILL_MARGINCALLUSDTS_AT_SECTION_1_PERCENT = WILL_MARGINCALLUSDTS_PERCENT_DEFAULT;
    private static volatile BigDecimal WILL_MARGINCALLUSDTS_AT_SECTION_2_PERCENT = WILL_MARGINCALLUSDTS_PERCENT_DEFAULT;
    private static volatile BigDecimal WILL_MARGINCALLUSDTS_AT_SECTION_3_PERCENT = WILL_MARGINCALLUSDTS_PERCENT_DEFAULT;
    private static volatile BigDecimal WILL_MARGINCALLUSDTS_AT_SECTION_4_PERCENT = WILL_MARGINCALLUSDTS_PERCENT_DEFAULT;

    /**4个区间的补仓实际数值，可配置
     *      * 刚启动的时候，从配置文件中读取到 总仓位 和 上面的百分比，然后计算出下面的实际的补仓数值。
     *      * 并在每次总仓位更新的时候更新实际补仓数值，恩。
     * */
    /**默认值 10个u*/
    private static final BigDecimal WILL_MARGINCALLUSDTS_DEFAULT = BigDecimal_10;
    private static volatile BigDecimal WILL_MARGINCALLUSDTS_AT_SECTION_1 = WILL_MARGINCALLUSDTS_DEFAULT;
    private static volatile BigDecimal WILL_MARGINCALLUSDTS_AT_SECTION_2 = WILL_MARGINCALLUSDTS_DEFAULT;
    private static volatile BigDecimal WILL_MARGINCALLUSDTS_AT_SECTION_3 = WILL_MARGINCALLUSDTS_DEFAULT;
    private static volatile BigDecimal WILL_MARGINCALLUSDTS_AT_SECTION_4 = WILL_MARGINCALLUSDTS_DEFAULT;



    /*****************************************[真波动率监控系统相关数据结构]*******************************************/

    /**上一个K线数据事件*/
    private static ConcurrentHashMap<String, CandlestickEvent> lastPerpetualCandlestickEvent = new ConcurrentHashMap<>();
    /** 补仓次数限制 **/
    private static volatile int SYSTEM_MARGINCALL_COUNT_LIMIT = 1;


    /**
     * TODO：带百分比的命名，注意
     * 1.008 = 1 + 0.8%，所以名字带 percent 后缀，percent就是多两个0，或者说除以100，嗯，够严谨。
     * */
    /**TODO：【最终决定补仓参数1 ：涨幅】最小敏感 振幅 百分比 就是K线涨幅 > PUMP_RATE_PERCENT_ATLEAST 。 刚启动设置为10% */
    private static BigDecimal PUMP_RATE_PERCENT_ATLEAST = pump_rate_percent_10_0;  // 涨幅 最小监控 。默认值搞大，别上来直接补爆了。
    /**最小波动率监控 敏感 数值。 波动幅度/每秒  目前是直接写死 5.0 。刚启动设置为15%/s*/
    private static final String PUMP_RATE_PERCENT_PERSECOND_ATLEAST_DEFAULT = "15.0"; //  波动率最小监控 。默认值搞大，别上来直接补爆了。
    /**TODO：【最终决定补仓参数2 ：波动率】最小波动率监控 敏感数值 【百分比】。 保留4位小数完全足够 */
    private static BigDecimal PUMP_RATE_PERCENT_PERSECOND_ATLEAST_PERCENT = new BigDecimal(PUMP_RATE_PERCENT_PERSECOND_ATLEAST_DEFAULT).divide(BigDecimal_100).setScale(4,BigDecimal.ROUND_DOWN);



    /***************************************[复利系统相关数据结构]*********************************************
     * 复利系统的save和load要结合着看，嗯。
     * */
    /**延迟30秒启动 首单更新 系统(测试用5秒)
     * 这个值主要是在第一次环境安装好，第一次启动启动的时候，要先等更新进程先更新数据，所以延迟一下。
     * 30s应该是完全足够了啊。运行一次之后，这个可以更新为1秒，设置不加延迟，直接更新，因为仓位信息，昨天已经更新到文件中无数次了。
     * */
    private static final long loadUSDTsConfigSystemStartDelay = 500; //30 * 1000;
    /**首单读取更新间隔 5分钟【跟保存保持一致就行，太快没有意义，因为没保存。。】 (测试用3秒)*/
    private static final long loadUSDTsConfigSystemInterval = 6 * 60 * 1000;

    /**首单自动更新开关
     * 输入close/CLOSE 关闭自动更新首单
     * 输入open/OPEN 开启自动更新首单
     * */
    private static boolean autoUpdateUSDTSConfig = true;
    /**
     * USDT仓位配置文件，在当前目录中创建一个配置文件。
     * */
    private static String usdtsPositionConfigFile = new File(".") + File.separator + "usdts.properties";;





    /**************************************************【启动核武器】**********************************************************/
    public static void main(String ... args){
        /**
         * TODO:启动要注意先后顺序，绝大部分系统应该在第5步之后启动。
         * ***/

        /******第1步 ：记录系统启动时间**/
        recordSystemStartTime();

        /******第2步 ：从配置文件中加载key**/
        loadApiKeyFromConfigFileAndInitClient();

        /******第3步 ：加载系统配置**/
        loadSystemParamsFromConfigFile(true);

        /******第4步 ：加载币种列表**/
        loadUSDTSymbolsFromConfigFile();

        /******第5步 ： 判断当前启动的是哪个系统。加以醒目的提醒，防止系统没有正确启动，造成重大损失*/
        showWhichSystemStart(5000);

        /******第6步 ： 离线监控系统初始化****/
        initOfflineMonitorSystem();

        /******第7步 ： 初始化所有币种的下单数量精度****/
        initAllSymbolsQuantityPrecision();

        /******第8步 ： 波动率监控系统初始化****/
        initRealPumpRateMonitorSystemInfoMap();

        /******第9步 ：启动K线实时分析系统**/
        startAnalysisKLineSystem();

        /******第10步 ：启动做空 平多 热身系统：加快系统响应速度，并检验前面的系统是否启动正常 **/
        startPostOrderWarmupSystem();

        /******第11步 ：启动动态配置系统**/
        startDynamicConfigSystem();

        /******第12步 ：启动复利系统**/
        startUpdateUSDTsConfigAndCheckServer();

        /******第13步 ：确认系统一切OK**/
        confirmSystemEverythingIsOk();
    }

    /**系统指示器**/
    private static void showWhichSystemStart(long delay){
        log.error("\n共有  " + USDTSymbols.length + "  个B需要监听");

        /**第1步 ： 判断当前启动的是哪个系统，并加以醒目的提醒，防止系统没有正确启动，造成重大损失*/
        if( USDTSymbolsFileName.equals("usdtSymbols_1.txt") ) {
            log.error("\n\n[只做多系统]\n");
        }

        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    private static boolean isMyNumeric(String str){
        if( str == null || str == "" || str.length() == 0 ){
            return false;
        }
        Pattern pattern = Pattern.compile("[0-9]*\\.?[0-9]+");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    /**
     * 接收并分析K线数据
     * 高性能核心逻辑
     * **/
    private static void startAnalysisKLineSystem(){
        try {

            for(String currency : USDTSymbols){
                log.error("创建 [" + currency + "] 价格监听器");

                client.subscribeCandlestickEvent(currency.toLowerCase()  , CandlestickInterval.ONE_MINUTE,
                    new SubscriptionListener<CandlestickEvent>() {
                        @Override
                        public void onReceive(CandlestickEvent event) {

                            try {

                                /**【TODO：置顶】
                                 * 【TODO：注意】以下代码流程，不允许中断，即，当前event，必须完整经历下面所有代码的解析。因此不允许任何return等逻辑，所以try也要最小业务catch
                                 * */

                                final BigDecimal upRate = event.getClose().divide( event.getOpen() ,4 , BigDecimal.ROUND_DOWN );//
                                final BigDecimal downRate = event.getOpen().divide( event.getClose() ,4 , BigDecimal.ROUND_DOWN );//
                                final CandlestickEvent lastCandlestickEvent = lastPerpetualCandlestickEvent.get(currency);




                                /**TODO *******************************@start 【第1部分 真波动率监控系统】 ********************************
                                 * 必须放在最前面，可以给全频段系统，提供波动率数值。
                                 * TODO:最高优先级
                                 * */


                                /**TODO：由于下面的流程中有一个地方，必须在合适的时候return，所以，整个逻辑必须拉出来放到一个方法里面去。
                                 * 不能中断一个数据后续的处理判断，嗯。
                                 * */
                                Entry_MarginCall(currency,upRate,lastCandlestickEvent,event);

                                /**TODO:这样就完美实现了 最高性能补仓 + 几乎不影响的 模型数据获取*/
                                /**当前K线涨幅 > pump_rate_0_8_percent %  瞬间波动率 > 最小波动率( 涨幅/每秒 )
                                 * TODO：在涨幅很小的时候，也可以触发，因为波动率是瞬时的。所以要对涨幅进行限制。
                                 * */
                                if (upRate.compareTo(PUMP_RATE_PERCENT_ATLEAST) > 0 && event.getPumpRate().compareTo(PUMP_RATE_PERCENT_PERSECOND_ATLEAST_PERCENT) > 0) {
                                    /**TODO：满足涨幅 + 波动率，设置标志位，开始分析 模型数据
                                     * 放这里，是实在没有办法的办法。
                                     * */
                                    lastCandlestickEvent.setStartAnalysisModel(true);
                                }
                                if( lastCandlestickEvent.isStartAnalysisModel() ){
                                    log.error("[ " + currency + " ]" + ": +" + (upRate.subtract(BigDecimal.ONE).multiply(BigDecimal_100).setScale(2, BigDecimal.ROUND_DOWN) + "% ") + event.getPumpRate().multiply(BigDecimal_100).setScale(2, BigDecimal.ROUND_DOWN) + "%/s");
                                }


                                /**TODO *******************************@start 【第2部分 无限补仓的停止系统】 ********************************
                                 * */
                                /**TODO： 补仓之后，如果同时达到了涨幅，就开始累计K线数
                                 * 这种逻辑就特么让他不断的去设置，如果用判断  !lastCandlestickEvent.isStartCountingKLineElipsed()
                                 * 这种方式，只会设置一次，恩。。万一程序卡住，多线程bug之类的出现一次错误，所有的一切都会紊乱，恩。。
                                 *
                                 * @2022-10-30
                                 *      达到涨幅，即使没有补仓，也要停止补仓，恩。。。
                                 *      比如你关闭状态下，有一个币爆拉了，或者直接超过了你的区间，那也需要停的！！！
                                 * */
                                if(  upRate.compareTo(STOP_MARGINCALL_CURRENCY_PUMPRATE) >= 0 ) {
                                    lastCandlestickEvent.setStartCountingKLineElipsed(true);
                                }


                                /**TODO *******************************@start 【第3部分 瀑布监控系统】 ********************************
                                 * 这个其实放哪里都行
                                 * TODO:低优先级
                                 * */

                                /** 瀑布监控：大于 (waterfall_monitorValue)% 的，比如5月13号晚上的KAVA
                                 *  不需要时间计算，只需要幅度即可。
                                 * */
                                if( downRate.compareTo(WATERFALL_MONITORVALUE) > 0 ){
                                    /**虽然下跌本来就是负的，自带 - 。但是为了让 播报系统正确识别，所以额外加一个 - ，这样显得更清晰，嗯。
                                     * */
                                    log.error("[ " + currency + " ]"  + " : -" +  (downRate.subtract(BigDecimal.ONE).multiply(BigDecimal_100).setScale(2, BigDecimal.ROUND_DOWN) + "%"));
                                }

                                if( currency.equalsIgnoreCase( "BTCUSDT" ) || currency.equalsIgnoreCase( "ETHUSDT" ) ){
                                    if( downRate.compareTo(BTC_ETH_WATERFALL_MONITORVALUE) > 0 ){
                                        log.error("[ " + currency + " ]"  + " : -" +  (downRate.subtract(BigDecimal.ONE).multiply(BigDecimal_100).setScale(2, BigDecimal.ROUND_DOWN) + "%"));
                                    }
                                }



                                /**
                                 * 每一个 K线结束事件，都要进行初始化，因为任何一根K线，都有可能触发。
                                 * **/
                                if( event.getIsClosed() ){
                                    /**TODO：更新并记录开始补仓之后，所经过的K线数*/
                                    lastCandlestickEvent.updateKlineElipsedCount(currency,ALLOW_MARGINCALL_KLINE, ALLOW_MARGINCALL_CD_MIN,ALLOW_OTHER_MARGINCALL_KLINE);
                                    /**TODO: K线结束初始化补仓状态*/
                                    lastCandlestickEvent.initMarginCallStatusAtKLineClose();

                                    /**TODO：设置在线标志位，1分钟设置一次，并且只在空闲的时候设置**/
                                    if( currentMarginCallCurrency == null ) {
                                        offLineMonitor.put(currency, 1);
                                    }

                                    /**蚂蚁爬坡识别*/
                                    if( upRate.compareTo(BigDecimal.ONE) > 0 ){
                                        lastCandlestickEvent.setAntCount( lastCandlestickEvent.getAntCount() + 1 );
                                        if( lastCandlestickEvent.getAntCount() >= ANT_STANDARD ){
                                            log.error("[" + currency + "] 正在爬坡");
                                        }
                                    }else{//重置
                                        lastCandlestickEvent.setAntCount(0);
                                    }
                                }


                            }catch(BinanceApiException e){
                                log.error("ErrorCode:" + e.getErrCode());
                                log.error("ErrorType:" + e.getErrType());
                                log.error("ErrorMessage:" + e.getMessage());
                                if( e.getErrCode().equals( "-1003" ) || e.getErrCode().equals("429") ){
                                    log.error("[请求太频繁，我日尼玛]");
                                    System.exit(-1);
                                }
                                if( e.getErrCode().equals("418") ){
                                    log.error("[卧槽，ip已被封]");
                                    System.exit(-1);
                                }
                            }catch (Exception e){

                            }

                        }
                    }

                    ,new SubscriptionErrorHandler() {//所以其实这个就没什么必要了，好像。。不过放着吧。。
                        @Override
                        public void onError(BinanceApiException e) {
                            log.error("ErrorCode:" + e.getErrCode());
                            log.error("ErrorType:" + e.getErrType());
                            log.error("ErrorMessage:" + e.getMessage());
                            if( e.getErrCode().equals( "-1003" ) || e.getErrCode().equals("429") ){
                                log.error("[请求太频繁，我日尼玛]");
                                System.exit(-1);
                            }
                            if( e.getErrCode().equals("418") ){
                                log.error("[卧槽，ip已被封]");
                                System.exit(-1);
                            }
                        }
                    }
                );

            }


        }catch (Throwable e){e.printStackTrace();}

    }



    /**TODO：由于下面的流程中有一个地方，必须在合适的时候return，所以，整个逻辑必须拉出来放到一个方法里面去。
     * 不能中断一个数据后续的处理判断，嗯。
     * TODO：从栈返回，肯定要进行堆上的new操作，所以如无必要，不要进行return对象操作，嗯。
     * */
    private static void Entry_MarginCall(final String currency, final BigDecimal upRate, CandlestickEvent lastCandlestickEvent, CandlestickEvent event){

        /**
         * TODO：新思路：用空间换时间
         * TODO：把需要的属性全都放到 这个 event 对象中去即可，这样少了一个类，好像没别的优势了。。。。。
         * */

        /**下面算法没问题，是性能最高的*/
        /**获取【做多】已补仓次数，只获取一次，后续直接使用，没必要再去get一次。*/
        int alreadyMarginCallTimes_LONG = lastCandlestickEvent.getLongMarginCallTimes();
        /**直接取拿那个全局的现成的，肯定比每个线程在栈里面再分配一个快啊，那个现成的，完全没有多线程问题，嗯。。读写位置不存在冲突*/
        final BigDecimal lastUpRate = lastCandlestickEvent.getUpRate();
        final long lastEventTime = lastCandlestickEvent.getEventTime().longValue();
        /**TODO:获取是否已达到最大允许补仓K线数*/
        final boolean isStopMarginCall = lastCandlestickEvent.isStopMarginCall();


        /**TODO:更新数值
         * TODO：上一个K线数据的数值每次都要更新，所以放前面
         * TODO：但是补仓数值，仅在 真正发生补仓的时候再去更新。
         * TODO：因为目前内存中任意时刻只有一个对象
         *
         * TODO：由于下面有一句return，并且更新数值应该在任何情况下都更新，并且获取上一个数据要在更新之前，所以。只能放这里，嗯。。
         * */
        lastCandlestickEvent.update(event.getOpen(), event.getClose(), upRate,event.getEventTime());


        /**TODO：所以所有一切的增长都是要按照百分比进行计算，嗯。。*/
        /** 直接相减 ，可以为 负数 ，就是 负增长
         *  所以是一个波动率，是一个比值，可以直接用 百分比显示，更直观，嗯。。
         * */
        BigDecimal upUpValue = upRate.subtract(lastUpRate);
        long upTime = event.getEventTime().longValue() - lastEventTime;


        /**TODO：
         * 换算成比较直观的 百分比/每秒，比如 2.3% / s ，这个就是非常直观。
         * 时间差是毫秒，所以x 1000
         * */
        BigDecimal pumpRate = upUpValue.multiply(BigDecimal_1000).divide(new BigDecimal(upTime), 4, BigDecimal.ROUND_DOWN);


        /**
         * TODO:所以这个在全频段系统中获取 波动率 的最高性能是直接设置这个event的一个属性即可，这样不涉及到new操作，拷贝构造等，只是当前这个event临时使用即可，嗯。
         * 所以event中再添加一个属性，直接就是计算出来的pumpRate。
         * 然后为了在任何时刻都可以获取到波动率。要将 补仓次数上限 return，放在 这个 设置 波动率之后，嗯，卧槽，可以。
         * 这样，上面K线那里就可以在任何时候 先 补仓，再打印数据，卧槽。。
         * 如果打印放在这函数里面，怎么弄，都无法 保证 以 最高性能补仓，总是会在补仓之前进行额外一个打印判断，甚至还要额外设置一个是否打印属性，嗯。
         * */
        event.setPumpRate(pumpRate);


        /**如果没开启补仓，直接返回，嗯。【这一句之前在补仓次数判断下面的，好像没有必要，要尽快返回，上面的逻辑就是任何时刻获取波动数据，嗯。】*/
        if( !enableAutoMarginCall ) return;

        /**不是当前正在补仓的币种，就没有必要进行下面的补仓判断了，嗯。。。*/
        if( currentMarginCallCurrency != null && !currentMarginCallCurrency.equals(currency) ){
            return;
        }


        /**判断补仓次数
         * */
        /**判断是否需要补仓,要 >= 啊
         * 即使不需要补仓，破新高值也需要更新，所以返回只能放在这里。
         * 对冲和做多其实都只需要判断做多的次数，因为都会设置
         * 但是只做空，只设置做空的次数
         * 下面的算法结构跟原来只有一个补仓方式的性能只差1个if判断，估计10纳秒，
         * */
        if (alreadyMarginCallTimes_LONG >= SYSTEM_MARGINCALL_COUNT_LIMIT) {
            log.error("[补仓次数已达上限]" + alreadyMarginCallTimes_LONG);
            return;
        }



        /**当前K线涨幅 > pump_rate_0_8_percent %  瞬间波动率 > 最小波动率( 涨幅/每秒 )
         * TODO：在涨幅很小的时候，也可以触发，因为波动率是瞬时的。所以要对涨幅进行限制。
         * */
        if (upRate.compareTo(PUMP_RATE_PERCENT_ATLEAST) > 0 && pumpRate.compareTo(PUMP_RATE_PERCENT_PERSECOND_ATLEAST_PERCENT) > 0) {

            /** 已达到最大允许补仓的K线数目
             *  放这里可以加一个打印，恩。放上面打印太多了
             * */
            if( isStopMarginCall ){
                log.error("[已关闭补仓]");
                return;
            }

            /**超过危险涨幅停止补仓*/
            if( upRate.compareTo(STOP_MARGINCALL_KLINE_PUMPRATE) > 0 ){
                log.error("[危险涨幅，停止补仓]");
                return;
            }


            /**TODO：满足涨幅 + 波动率，设置标志位，开始分析 模型数据，标志位不放这里，永远以最高极限性能进行补仓。
             * //lastCandlestickEvent.setStartAnalysisModel(true);
             * */

            /**
             * TODO：补仓USDT：直接放这里补仓就是了，先补仓，再打印。
             * TODO 计算补仓USTS
             * TODO：
             * 【所以，应该是 x 仓位，卧槽。。。这样就变成了复利，嗯。。。
             * 先用10000试试，，，卧槽。这个仓位，在配置文件中，只有系统第一次运行在新服务器上，可能第一次获取不到，以后直接动态更新在文件中，嗯。。
             * 】
             * 当前的波动率 x 10000 /  2
             * 1位小数即可，比如，0.5u
             * 举例： 0.022，就是波动2.2%，乘以10000 = 222，除以2 = 111.
             *
             *  2022-08-30
             * 改为 x 2 即  波动2.2%，乘以10000 = 222，乘以2 = 444.
             * 一般都是补仓2次左右，大机会最多也就4，5次，所以要继续加大仓位
             *
             * 改回原来的x5，即 波动2.2%，乘以10000 = 222，乘以5 = 1110.
             * 那么极限大机会，也就是特么X5，= 1110 x 5 = 5500，而10x，10000，可以开10 0000的，嗯。
             *
             * TODO: 经过分析发现，X2 仓位已经够大了，嗯。。。。
             * 之前是他妈的没加波动限制+补仓次数限制，导致300个订单，把手续费都吃完了，而且还没有按照机会的波动去补仓，嗯。。
             * 折衷一点，X4，嗯，先X4。哦，不能X5，之前虽然是X5了，但是总仓位是有10%限制的，嗯。。。。。而你现在虽然X2，但是总仓位限制也是12%，几乎没变，仓位估计没啥变化。
             * 就这样，卧槽。。。。所以其实已经不少了，嗯。。。。。
             *
             * 2022-10-24
             *  直接用仓位百分比即可。
             * */
            BigDecimal willMarginCallUSDTs = WILL_MARGINCALLUSDTS_DEFAULT;


            /**TODO:区间限制，对冲，做多，是先数量多，再数量少。做空恰好反过来，嗯。经典。
             * TODO：在补仓完毕之后加一个打印，看一下本次补仓的usdt数量，要在补仓逻辑之后打印，打印也是耗时的，嗯。
             * 如果没开区间限制，那么就啥也不干，跟之前所有逻辑一样。如果开了区间限制，将会按照设计好的仓位限制进行进一步的处理仓位，嗯。
             * */
            if( enableSectionLimit ){
                if ( upRate.compareTo(pump_rate_percent_3_5) < 0 ) {
                    willMarginCallUSDTs = WILL_MARGINCALLUSDTS_AT_SECTION_1;
                }else if ( upRate.compareTo(pump_rate_percent_3_5) >= 0 && upRate.compareTo(pump_rate_percent_4_5) < 0 ) {
                    willMarginCallUSDTs = WILL_MARGINCALLUSDTS_AT_SECTION_2;
                }else if ( upRate.compareTo(pump_rate_percent_4_5) >= 0 && upRate.compareTo(pump_rate_percent_5_5) < 0 ) {
                    willMarginCallUSDTs = WILL_MARGINCALLUSDTS_AT_SECTION_3;
                }else if ( upRate.compareTo(pump_rate_percent_5_5) >= 0 ) {
                    willMarginCallUSDTs = WILL_MARGINCALLUSDTS_AT_SECTION_4;
                }
            }


            /**TODO：计算每次补仓的仓位限制是否超标
             * 直接在配置更新那里算出来，不要在这里每次去计算，嗯。
             * */
            if (willMarginCallUSDTs.compareTo(EVERYTIME_MARGINCALL_POSITION_LIMIT) > 0) {
                willMarginCallUSDTs = EVERYTIME_MARGINCALL_POSITION_LIMIT;
                log.error("[!!!单次超限，已限制为]" + willMarginCallUSDTs);
            }

            /**TODO:计算出最终的补仓仓位*/
            willMarginCallUSDTs = willMarginCallUSDTs.multiply(new BigDecimal(MARGINCALL_LEVERAGE));


            /**TODO：开始补仓
             * 并更新补仓次数。
             * */

            /**计算补仓数量
             * 用要花费的USDTS除以当前价格，向上取整即可。
             * **/
            BigDecimal willMarginCallQuantity = willMarginCallUSDTs.divide(event.getClose(), AllSymbolQuantityPrecision.get(currency).intValue(), BigDecimal.ROUND_HALF_UP);

            /**
             * 更新补仓次数，这个地方可能存在一个问题，就是代码跑到这里，如果在++之前，又收到了一个合适的数据，就会多补仓一次。
             * 当然，按照币安的说法，是250ms 一个数据。但是之前有一次，有可能是币安推送了两遍那个数据。所以会补裂，卧槽。。
             * TODO:[所以应该直接先保存一下，然后++，然后后面的逻辑用保存的那个，卧槽，对不对，嗯。。。。。。]
             * TODO:不过问题不大，应该2ms就可以走到这里，所以这个++放在补仓上面，再提速1ms左右。
             * **/


            /**TODO：开始补仓
             * */


            /**
             *TODO：当真正补仓的瞬间，【补仓逻辑上一行】，设置保存正在补仓的币的名称。
             *此时假如正有其他的某个币也符合了波动，并且也已进入判断逻辑。但是走到补仓逻辑
             *之前进行判断，都会停止补仓，嗯。
             *  所以判断要放在2个地方，一个比较早的地方，一个紧靠着业务逻辑，嗯。。
             */
            /**如果全局标志中已有正在补仓的记录，但是当前的币种不是记录的币种，那么不会进行补仓
            也就是说，没有记录，或者等于正在补仓的币，可以继续进行逻辑判断，嗯。。*/
            if( currentMarginCallCurrency != null && !currentMarginCallCurrency.equals(currency) ){
                log.error("[同一时间只处理一个订单，当前订单] " + currency);
                return;
            }
            /**仅在真正要补仓的地方设置正在补仓的币的名称，即使发生了抛异常补仓失败，也要设置，发生这个的时候，会得到明显提醒，需要手动重新开启，嗯。*/
            currentMarginCallCurrency = currency;


            lastCandlestickEvent.setLongMarginCallTimes(++alreadyMarginCallTimes_LONG);
            longBuyMarginCall(currency, willMarginCallQuantity.toPlainString());
            log.error("[只做多补仓usdt] " + willMarginCallUSDTs.toPlainString());


            /**最原始 幅度/毫秒 显示，用来观察最原始信息
             * 打印逻辑跟之前一样，可以打印，但是补仓要进行判断
             * TODO:所以这句，符合监控，读出来。读出来的就是补仓的，嗯。
             * */
            log.error("[ " + currency + " ]" + ": +" + (upRate.subtract(BigDecimal.ONE).multiply(BigDecimal_100).setScale(2, BigDecimal.ROUND_DOWN) + "% ") + pumpRate.multiply(BigDecimal_100).setScale(2, BigDecimal.ROUND_DOWN) + "%/s");

        }

    }


    /**重置以允许其他币种补仓*/
    public static void resetToAllowOtherCurrencyMargincall(){
        currentMarginCallCurrency = null;
    }


    /**系统运行过程中手动指定某个币种停止补仓*/
    public static void manualStopCurrencyMargincall(final String currency){
        if( lastPerpetualCandlestickEvent.get(currency) != null ){
            lastPerpetualCandlestickEvent.get(currency).setStartCountingKLineElipsed(true);
            log.error("[" + currency + "] 已设置停止补仓");
        }else{
            log.error("[" + currency + "] 不存在");
        }
    }


    /***获取币种下单数量精度***/
    private static void initAllSymbolsQuantityPrecision() {

        /**-1003: Too many requests;
         * current limit of IP(xx.xxx.x.xxx) is 2400 requests per minute.
         * Please use the websocket for live updates to avoid polling the API.
         * */

        log.error("[开始获取币种下单数量精度]");

        try {
            long startTime = System.currentTimeMillis();
            List<ExchangeInfoEntry> symbols = syncRequestClient.getExchangeInformation().getSymbols();
            log.error("[从服务器获取币种下单数量精度耗时] " + (System.currentTimeMillis() - startTime));

            ExchangeInfoEntry tmp = null;

            for (int i = 0; i < symbols.size(); i++) {
                tmp = symbols.get(i);
                AllSymbolQuantityPrecision.put(tmp.getSymbol(), tmp.getQuantityPrecision());
            }

            if (AllSymbolQuantityPrecision.isEmpty()) {
                log.error("[!!!币种数量精度获取失败]");
                System.exit(-1);//精度获取失败，此为致命错误，进程退出
            }

        }catch(BinanceApiException e){
            log.error("ErrorCode:" + e.getErrCode());
            log.error("ErrorType:" + e.getErrType());
            log.error("ErrorMessage:" + e.getMessage());
            if( e.getErrCode().equals( "-1003" ) || e.getErrCode().equals("429") ){
                log.error("[请求太频繁，我日尼玛]");
                System.exit(-1);
            }
            if( e.getErrCode().equals("418") ){
                log.error("[卧槽，ip已被封]");
                System.exit(-1);
            }
        }
    }

    /***做多补仓**/
    private static void longBuyMarginCall(final String currency,final String amount){
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    long startTime1 = System.currentTimeMillis();
                    syncRequestClient.postOrder(currency, OrderSide.BUY, PositionSide.LONG, OrderType.MARKET, null,
                            amount, null, null, null, null, null, NewOrderRespType.RESULT);
                    long eclipseTime1 = System.currentTimeMillis() - startTime1;
                    log.error("[做多耗时] " + eclipseTime1);
                }catch(BinanceApiException e){
                    log.error("ErrorCode:" + e.getErrCode());
                    log.error("ErrorType:" + e.getErrType());
                    log.error("ErrorMessage:" + e.getMessage());
                    if( e.getErrCode().equals( "-1003" ) || e.getErrCode().equals("429") ){
                        log.error("[请求太频繁，我日尼玛]");
                        System.exit(-1);
                    }
                    if( e.getErrCode().equals("418") ){
                        log.error("[卧槽，ip已被封]");
                        System.exit(-1);
                    }
                }
            }
        });

    }



    /**记录系统启动时间**/
    private static void recordSystemStartTime(){
        systemStartTime = System.currentTimeMillis();
    }


    /**初始化检测器*/
    private static void initOfflineMonitorSystem(){
        //初始化为0
        for(int i = 0 ; i < USDTSymbols.length ; i++ ){
            offLineMonitor.put(USDTSymbols[i],0);
        }
        //检测初始化
        for(int i = 0 ; i < USDTSymbols.length ; i++ ){
            if( offLineMonitor.get(USDTSymbols[i] ) != 0 ){
                log.error("[离线监控器初始化失败]");
                System.exit(-1);
            }
        }
        log.error("[离线监控器初始化成功]");
    }

    /**确认系统一切正常，并已开始补仓*/
    private static void confirmSystemEverythingIsOk(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(;;){
                    try {
                        if( broadcastSystemStartup && monitorValueUpdate && startMargincallStart ){
                            break;
                        }
                        /**TODO: 确认波动系统已启动*/
                        if( !broadcastSystemStartup ) {
                            log.error("[第1步:请开启播报系统]");
                            log.error("[开启后请输入:播报系统已开启]\n");
                        }

                        /**TODO: 确认监控数值已更新*/
                        if( !monitorValueUpdate ) {
                            log.error("[第2步:请更新监控数值]");
                            log.error("[更新后请输入:监控数值已更新]\n");
                        }
                        /**TODO: 确认已开始补仓*/
                        if( !startMargincallStart ) {
                            log.error("[第3步:请开始补仓]");
                            log.error("[开启后请输入:已开始补仓]\n");
                        }

                        Thread.sleep(20 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                /**只要打印出来，检测线程就已结束，释放了资源*/
                log.error("[系统已正确启动，已开始补仓]");
            }
        }).start();
    }


    /** 启动服务器离线检测：币安服务器或者自己的服务器离线 **/
    private static void startCheckServerOffline(){
        if( currentMarginCallCurrency != null ){
            log.error("[忙碌，不进行离线检测]");
            return;
        }
        log.error("[开始进行离线检测]\n");
        lastCheckTime = systemStartTime;//这个不加，刚启动会全部打印一遍离线，其实刚好算检测，，先去掉吧。。
        lastPrintTime = systemStartTime;
        try {
            /**配置文件中更新属性之后5秒开始检查*/
            Thread.sleep(5 * 1000);

            if (System.currentTimeMillis() - lastCheckTime >= checkTimeInterval) {
                for (int i = 0; i < USDTSymbols.length; i++) {
                    if (offLineMonitor.get(USDTSymbols[i]) == 0) {
                        log.error("[!!!离线]  " + USDTSymbols[i]);
                        //System.exit(-1);
                    }
                    //重置为0，以继续检测。
                    offLineMonitor.put(USDTSymbols[i], 0);
                }
                /**记录检测时间*/
                lastCheckTime = System.currentTimeMillis();
            }

            if (System.currentTimeMillis() - lastPrintTime >= printTimeInterval) {
                /**打印一次系统运行时间**/
                printRunningTime(System.currentTimeMillis() - systemStartTime);
                lastPrintTime = System.currentTimeMillis();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**TODO:按行读取 币种 配置文件
     * TODO:同步等待读取，若读取失败，进程必须退出。
     * */
    private static void loadUSDTSymbolsFromConfigFile(){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(USDTSymbolsFile));
            String currency = null;
            while ((currency = reader.readLine()) != null) {
                //System.out.println(currency);
                if( currency.trim().endsWith("USDT") || currency.trim().endsWith("usdt") ) {
                    USDTSymbolsArray.add(currency.trim());
                }
            }

            if( USDTSymbolsArray.size() < 1 ){
                System.out.println("[未读取到有效币种 1]");
                System.exit(-1);
            }

            /**TODO：用转化出来的数组进行检测，只要检测通过，内存一定没问题*/
            USDTSymbols = USDTSymbolsArray.toArray(new String[USDTSymbolsArray.size()]);
            System.out.println("[币种加载完毕 共计] " + USDTSymbols.length);

            if( USDTSymbols.length < 1 ){
                System.out.println("[未读取到有效币种 2]");
                System.exit(-1);
            }
            if( USDTSymbols.length > 200 ){
                System.out.println("[币安规定最多订阅200个stream]");
                System.exit(-1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[USDT symbols文件读取失败 1]");
            System.exit(-1);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("[USDT symbols文件读取失败 2]");
                System.exit(-1);

            }
        }
    }


    /**加载api key*/
    private static void loadApiKeyFromConfigFileAndInitClient(){
        Properties properties = new Properties();
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(apiKeyConfigFile));
            properties.load(bis);
            bis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            /**TODO:加载系统配置*/
            /** 获取key对应的value值
             * 获取止损配置参数
             * */
            String api_key = properties.getProperty("api_key");
            String secret_key = properties.getProperty("secret_key");


            /**TODO:验证加载进来的系统配置*/
            if ( api_key.isEmpty() ) {
                log.error("[api_key 配置错误]");
                System.exit(-1);
            }

            /**TODO:验证加载进来的系统配置*/
            if ( secret_key.isEmpty() ) {
                log.error("[secret_key 配置错误]");
                System.exit(-1);
            }

            API_KEY = new StringBuilder(api_key).reverse().toString();
            SECRET_KEY = new StringBuilder(secret_key).reverse().toString();

            syncRequestClient = SyncRequestClient.create(API_KEY, SECRET_KEY,options);

            /**
             * TODO：api就不要打印出来了，卧槽
             * */
            log.error("\n[apiKey和客户端初始化完毕]\n");

        }catch (Exception e){
            log.error("[配置解析错误]");
            e.printStackTrace();
            System.exit(-1);
        }
    }


    /**加载补仓系统的系统配置文件
     * @param exit
     *  参数加载错误是否需要退出进程。
     *  在进程启动的时候，只要一个参数错误，即退出进程。
     *  后续内存中加载的时候，遇到错误，也不应该退出。
     * */
    private static void loadSystemParamsFromConfigFile(boolean exit){
        Properties properties = new Properties();
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(systemParamsConfigFile));
            properties.load(bis);
            bis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            /**TODO:加载系统配置*/
            /** 获取key对应的value值
             * 下面的左边的变量名和右边的属性名完全一样，直接复制，恩。
             * */
            String margincall_leverage = properties.getProperty("margincall_leverage");
            String everytime_margincall_position_limit_percent = properties.getProperty("everytime_margincall_position_limit_percent");

            String allow_margincall_kline = properties.getProperty("allow_margincall_kline");
            String allow_margincall_cd_min = properties.getProperty("allow_margincall_cd_min");
            String allow_other_margincall_kline = properties.getProperty("allow_other_margincall_kline");

            String stop_margincall_kline_pumprate = properties.getProperty("stop_margincall_kline_pumprate");
            String stop_margincall_currency_pumprate = properties.getProperty("stop_margincall_currency_pumprate");

            String waterfall_monitorvalue = properties.getProperty("waterfall_monitorvalue");

            /**更新4段补仓的数值*/
            String will_margincallusdts_at_section_1_percent = properties.getProperty("will_margincallusdts_at_section_1_percent");
            String will_margincallusdts_at_section_2_percent = properties.getProperty("will_margincallusdts_at_section_2_percent");
            String will_margincallusdts_at_section_3_percent = properties.getProperty("will_margincallusdts_at_section_3_percent");
            String will_margincallusdts_at_section_4_percent = properties.getProperty("will_margincallusdts_at_section_4_percent");

            String system_margincall_count_limit = properties.getProperty("system_margincall_count_limit");

            String ant_standard = properties.getProperty("ant_standard");


            /**TODO:验证加载进来的系统配置*/
            if (!isMyNumeric(margincall_leverage)) {
                log.error("[margincall_leverage 配置错误]");
                /**TODO：系统启动的时候一个参数配置错误，即系统需要退出*/
                if(exit) System.exit(-1);
            }

            if (!isMyNumeric(everytime_margincall_position_limit_percent)) {
                log.error("[everytime_margincall_position_limit_percent 配置错误]");
                /**TODO：系统启动的时候一个参数配置错误，即系统需要退出*/
                if(exit) System.exit(-1);
            }

            if (!isMyNumeric(allow_margincall_kline)) {
                log.error("[allow_margincall_kline 配置错误]");
                /**TODO：系统启动的时候一个参数配置错误，即系统需要退出*/
                if(exit) System.exit(-1);
            }

            if (!isMyNumeric(allow_margincall_cd_min)) {
                log.error("[allow_margincall_cd_min 配置错误]");
                /**TODO：系统启动的时候一个参数配置错误，即系统需要退出*/
                if(exit) System.exit(-1);
            }

            if (!isMyNumeric(allow_other_margincall_kline)) {
                log.error("[allow_other_margincall_kline 配置错误]");
                /**TODO：系统启动的时候一个参数配置错误，即系统需要退出*/
                if(exit) System.exit(-1);
            }

            if (!isMyNumeric(stop_margincall_kline_pumprate)) {
                log.error("[stop_margincall_kline_pumprate 配置错误]");
                /**TODO：系统启动的时候一个参数配置错误，即系统需要退出*/
                if(exit) System.exit(-1);
            }
            if( new BigDecimal(stop_margincall_kline_pumprate).compareTo( new BigDecimal("6.5") ) > 0 ){
                log.error("[stop_margincall_kline_pumprate 太大]");
                /**TODO：系统启动的时候一个参数配置错误，即系统需要退出*/
                if(exit) System.exit(-1);
            }

            if (!isMyNumeric(stop_margincall_currency_pumprate)) {
                log.error("[stop_margincall_currency_pumprate 配置错误]");
                /**TODO：系统启动的时候一个参数配置错误，即系统需要退出*/
                if(exit) System.exit(-1);
            }

            if (!isMyNumeric(waterfall_monitorvalue)) {
                log.error("[waterfall_monitorvalue 配置错误]");
                /**TODO：系统启动的时候一个参数配置错误，即系统需要退出*/
                if(exit) System.exit(-1);
            }

            if (!isMyNumeric(will_margincallusdts_at_section_1_percent)) {
                log.error("[will_margincallusdts_at_section_1_percent 配置错误]");
                /**TODO：系统启动的时候一个参数配置错误，即系统需要退出*/
                if(exit) System.exit(-1);
            }

            if (!isMyNumeric(will_margincallusdts_at_section_2_percent)) {
                log.error("[will_margincallusdts_at_section_2_percent 配置错误]");
                /**TODO：系统启动的时候一个参数配置错误，即系统需要退出*/
                if(exit) System.exit(-1);
            }

            if (!isMyNumeric(will_margincallusdts_at_section_3_percent)) {
                log.error("[will_margincallusdts_at_section_3_percent 配置错误]");
                /**TODO：系统启动的时候一个参数配置错误，即系统需要退出*/
                if(exit) System.exit(-1);
            }

            if (!isMyNumeric(will_margincallusdts_at_section_4_percent)) {
                log.error("[will_margincallusdts_at_section_4_percent 配置错误]");
                /**TODO：系统启动的时候一个参数配置错误，即系统需要退出*/
                if(exit) System.exit(-1);
            }

            if (!isMyNumeric(system_margincall_count_limit)) {
                log.error("[system_margincall_count_limit 配置错误]");
                /**TODO：系统启动的时候一个参数配置错误，即系统需要退出*/
                if(exit) System.exit(-1);
            }

            if (!isMyNumeric(ant_standard)) {
                log.error("[ant_standard 配置错误]");
                /**TODO：系统启动的时候一个参数配置错误，即系统需要退出*/
                if(exit) System.exit(-1);
            }


            /**TODO：更新计算并打印验证止损参数
             * 更新止损百分比的同时，不需要更新最大亏损数值！
             *      因为这个参数加载在所有程序之前，后续都是用到这个参数，嗯。
             *      后续即使动态更新了，也没事
             *          A 动态更新这个最大亏损百分比很少，几乎不会改！！！也就是在某一次程序启动之前可能会有所改动
             *          B 极限对冲需要动态更改最大亏损百分比的那种，仓位是特么直接写死的，嗯。。。也只需要改一个比例，嗯
             * */
            try {
                MARGINCALL_LEVERAGE = Integer.parseInt(margincall_leverage);
                if( MARGINCALL_LEVERAGE > 10 ){
                    log.error("[倍率上限为10，已限制为10]");
                    MARGINCALL_LEVERAGE = 10;
                }
            }catch (Exception e){
                /**TODO：系统启动的时候一个参数配置错误，即系统需要退出*/
                System.exit(-1);
                log.error("[倍率配置获取错误]");
            }

            EVERYTIME_MARGINCALL_POSITION_LIMIT_PERCENT = new BigDecimal(everytime_margincall_position_limit_percent).multiply(BigDecimal_PERCENT);

            if( EVERYTIME_MARGINCALL_POSITION_LIMIT_PERCENT.compareTo( BigDecimal_0_9 ) > 0 ){
                log.error("[单次补仓仓位太大，已限制为90%]");
                EVERYTIME_MARGINCALL_POSITION_LIMIT_PERCENT = BigDecimal_0_9;
            }


            try {
                ALLOW_MARGINCALL_KLINE = Integer.parseInt(allow_margincall_kline);
                if( ALLOW_MARGINCALL_KLINE > 3 ){
                    log.error("[最多允许补3根K线]");
                    ALLOW_MARGINCALL_KLINE = 3;
                }
            }catch (Exception e){
                log.error("[ALLOW_MARGINCALL_KLINE获取错误]");
                /**TODO：系统启动的时候一个参数配置错误，即系统需要退出*/
                if(exit) System.exit(-1);
            }

            try {
                ALLOW_MARGINCALL_CD_MIN = Integer.parseInt(allow_margincall_cd_min);
            }catch (Exception e){
                log.error("[allow_margincall_cd_min获取错误]");
                /**TODO：系统启动的时候一个参数配置错误，即系统需要退出*/
                if(exit) System.exit(-1);
            }

            try {
                ALLOW_OTHER_MARGINCALL_KLINE = Integer.parseInt(allow_other_margincall_kline);
            }catch (Exception e){
                log.error("[ALLOW_OTHER_MARGINCALL_KLINE获取错误]");
                /**TODO：系统启动的时候一个参数配置错误，即系统需要退出*/
                if(exit) System.exit(-1);
            }


            STOP_MARGINCALL_KLINE_PUMPRATE = BigDecimal.ONE.add( new BigDecimal(stop_margincall_kline_pumprate).multiply(BigDecimal_PERCENT) );
            STOP_MARGINCALL_CURRENCY_PUMPRATE = BigDecimal.ONE.add( new BigDecimal(stop_margincall_currency_pumprate).multiply(BigDecimal_PERCENT) );
            WATERFALL_MONITORVALUE = BigDecimal.ONE.add( new BigDecimal(waterfall_monitorvalue).multiply(BigDecimal_PERCENT) );

            /**TODO：通过更新4段补仓的百分比，来更新4段补仓的数值，注意配置文件中输入的是20=20%，要转换一下*/
            WILL_MARGINCALLUSDTS_AT_SECTION_1_PERCENT = new BigDecimal(will_margincallusdts_at_section_1_percent).multiply(BigDecimal_PERCENT);
            WILL_MARGINCALLUSDTS_AT_SECTION_2_PERCENT = new BigDecimal(will_margincallusdts_at_section_2_percent).multiply(BigDecimal_PERCENT);
            WILL_MARGINCALLUSDTS_AT_SECTION_3_PERCENT = new BigDecimal(will_margincallusdts_at_section_3_percent).multiply(BigDecimal_PERCENT);
            WILL_MARGINCALLUSDTS_AT_SECTION_4_PERCENT = new BigDecimal(will_margincallusdts_at_section_4_percent).multiply(BigDecimal_PERCENT);
            WILL_MARGINCALLUSDTS_AT_SECTION_1 = ___POSITION_USDTs___.multiply(WILL_MARGINCALLUSDTS_AT_SECTION_1_PERCENT);
            WILL_MARGINCALLUSDTS_AT_SECTION_2 = ___POSITION_USDTs___.multiply(WILL_MARGINCALLUSDTS_AT_SECTION_2_PERCENT);
            WILL_MARGINCALLUSDTS_AT_SECTION_3 = ___POSITION_USDTs___.multiply(WILL_MARGINCALLUSDTS_AT_SECTION_3_PERCENT);
            WILL_MARGINCALLUSDTS_AT_SECTION_4 = ___POSITION_USDTs___.multiply(WILL_MARGINCALLUSDTS_AT_SECTION_4_PERCENT);

            try {
                SYSTEM_MARGINCALL_COUNT_LIMIT = Integer.parseInt(system_margincall_count_limit);
            }catch (Exception e){
                log.error("[system_margincall_count_limit获取错误]");
                /**TODO：系统启动的时候一个参数配置错误，即系统需要退出*/
                if(exit) System.exit(-1);
            }

            try {
                ANT_STANDARD = Integer.parseInt(ant_standard);
            }catch (Exception e){
                log.error("[ant_standard获取错误]");
                /**TODO：系统启动的时候一个参数配置错误，即系统需要退出*/
                if(exit) System.exit(-1);
            }



            /**
             * TODO：只要打印正确，就一定没有问题，因为打印的时候读取的是更新之后的值
             * */
            log.error("\n____[验证系统配置]____");
            log.error("[更新 补仓倍率]                " + MARGINCALL_LEVERAGE );
            log.error("[更新 单次补仓仓位限制百分比]     " + (EVERYTIME_MARGINCALL_POSITION_LIMIT_PERCENT.multiply(BigDecimal_100) + "%") );
            log.error("[更新 允许补仓几根K线]           " + ALLOW_MARGINCALL_KLINE);
            log.error("[更新 补仓冷却时间(分钟)]        " + ALLOW_MARGINCALL_CD_MIN);
            log.error("[更新 开始其他币种补仓冷却(分钟)]  " + ALLOW_OTHER_MARGINCALL_KLINE);
            log.error("[更新 停止补仓的K线涨幅]         " + STOP_MARGINCALL_KLINE_PUMPRATE);
            log.error("[更新 停止补仓的币种涨幅]         " + STOP_MARGINCALL_CURRENCY_PUMPRATE);
            log.error("[更新 瀑布监控百分比]            " + WATERFALL_MONITORVALUE);
            log.error("[更新 补仓次数限制]             " + SYSTEM_MARGINCALL_COUNT_LIMIT);
            log.error("[更新 蚂蚁爬坡标准]             " + ANT_STANDARD);
            log.error("\n____[验证补仓配置]____");
            log.error("[更新 区间1补仓百分比]           " + WILL_MARGINCALLUSDTS_AT_SECTION_1_PERCENT.multiply(BigDecimal_100) + "%");
            log.error("[更新 区间2补仓百分比]           " + WILL_MARGINCALLUSDTS_AT_SECTION_2_PERCENT.multiply(BigDecimal_100) + "%");
            log.error("[更新 区间3补仓百分比]           " + WILL_MARGINCALLUSDTS_AT_SECTION_3_PERCENT.multiply(BigDecimal_100) + "%");
            log.error("[更新 区间4补仓百分比]           " + WILL_MARGINCALLUSDTS_AT_SECTION_4_PERCENT.multiply(BigDecimal_100) + "%");
            log.error("[更新 区间1补仓USDTs]            " + WILL_MARGINCALLUSDTS_AT_SECTION_1);
            log.error("[更新 区间2补仓USDTs]            " + WILL_MARGINCALLUSDTS_AT_SECTION_2);
            log.error("[更新 区间3补仓USDTs]            " + WILL_MARGINCALLUSDTS_AT_SECTION_3);
            log.error("[更新 区间4补仓USDTs]            " + WILL_MARGINCALLUSDTS_AT_SECTION_4);
            log.error("__________________\n");

        }catch (Exception e){
            log.error("[配置解析错误]");
            e.printStackTrace();
            /**TODO：系统启动的时候一个参数配置错误，即系统需要退出*/
            if(exit) System.exit(-1);
        }
    }


    /**加载仓位配置文件，并计算更新 打印 首单。
     * 并发访问没有问题，再说也没有并发访问。
     * 嗯。
     * */
    private static void loadPositionConfig(){
        if( currentMarginCallCurrency != null ){
            log.error("[忙碌中，不进行配置更新]");
            return;
        }

        Properties properties = new Properties();
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(usdtsPositionConfigFile));
            properties.load(bis);
            bis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 获取key对应的value值
        String usdtsUpdateCount = properties.getProperty("usdtsUpdateCount");
        String usdts = properties.getProperty("usdts");

        /**如果获取属性的时候，配置还没更新，就直接返回。*/
        if( !isMyNumeric(usdts) ){
            log.error("[未获取到属性]");
            return;
        }

        /**更新总仓位
         * 更新的同时要更新 仓位限制 + 3%仓位
         * */
        ___POSITION_USDTs___ = new BigDecimal(usdts).setScale(2,BigDecimal.ROUND_HALF_UP);
        LAST___POSITION_USDTs___ = ___POSITION_USDTs___;
        EVERYTIME_MARGINCALL_POSITION_LIMIT = ___POSITION_USDTs___.multiply(EVERYTIME_MARGINCALL_POSITION_LIMIT_PERCENT);

        /**根据最新仓位计算4段补仓数值*/
        WILL_MARGINCALLUSDTS_AT_SECTION_1 = ___POSITION_USDTs___.multiply(WILL_MARGINCALLUSDTS_AT_SECTION_1_PERCENT);
        WILL_MARGINCALLUSDTS_AT_SECTION_2 = ___POSITION_USDTs___.multiply(WILL_MARGINCALLUSDTS_AT_SECTION_2_PERCENT);
        WILL_MARGINCALLUSDTS_AT_SECTION_3 = ___POSITION_USDTs___.multiply(WILL_MARGINCALLUSDTS_AT_SECTION_3_PERCENT);
        WILL_MARGINCALLUSDTS_AT_SECTION_4 = ___POSITION_USDTs___.multiply(WILL_MARGINCALLUSDTS_AT_SECTION_4_PERCENT);

        /**
         * 按照上面的顺序写只要打印出来就一定没有问题，
         * */
        log.error("\n[更新仓位] " + ___POSITION_USDTs___.toPlainString() + " [次数] " + usdtsUpdateCount);
        log.error("[更新仓位限制" + EVERYTIME_MARGINCALL_POSITION_LIMIT_PERCENT.multiply(BigDecimal_100) + "%] " + EVERYTIME_MARGINCALL_POSITION_LIMIT.toPlainString());
        log.error("[更新 区间1补仓USDTs]  " + WILL_MARGINCALLUSDTS_AT_SECTION_1);
        log.error("[更新 区间2补仓USDTs]  " + WILL_MARGINCALLUSDTS_AT_SECTION_2);
        log.error("[更新 区间3补仓USDTs]  " + WILL_MARGINCALLUSDTS_AT_SECTION_3);
        log.error("[更新 区间4补仓USDTs]  " + WILL_MARGINCALLUSDTS_AT_SECTION_4 + "\n");

    }


    /**启动复利系统
     * 从配置文件中获取最新的仓位信息，并计算首单。
     * 所以是复利系统
     * */
    private static void startUpdateUSDTsConfigAndCheckServer() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if( autoUpdateUSDTSConfig ) {
                        loadPositionConfig();
                    }

                    startCheckServerOffline();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, loadUSDTsConfigSystemStartDelay, loadUSDTsConfigSystemInterval);
    }


    /**打印系统运行时间，防止假死**/
    private static void printRunningTime(final long milliseconds) {
        final long day = TimeUnit.MILLISECONDS.toDays(milliseconds);
        final long hours = TimeUnit.MILLISECONDS.toHours(milliseconds) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(milliseconds));
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds));

        ///log.error("milliseconds :-" + milliseconds);
        log.error(String.format("[只做多补仓系统]运行 %d天 %d小时 %d分", day, hours, minutes));
    }



    /**
     * 热身功能不需要try catch。
     * 如果热身功能出现问题，就抛异常，说明是致命错误，必须终止。
     *
     * **/
    private static void startPostOrderWarmupSystem(){

        /***使用狗币热身，足够稳定。100 DOGE ≈ 3 USDT @ 2022年6月11号
         * 等后续狗币价格变的很贵的时候，一定要小心。。。
         * ***/
        final String HOT_SPORT_CURRENCY = "DOGEUSDT";
        final String HOT_SPORT_AMOUNT = "100";

        /*** 做多热身，并验证做多功能是否正常 ****/
        long startTime1 = System.currentTimeMillis();
        Order postOrderHotSport_LONGBUY_ORDER = syncRequestClient.postOrder(HOT_SPORT_CURRENCY, OrderSide.BUY, PositionSide.LONG, OrderType.MARKET, null,
                HOT_SPORT_AMOUNT, null, null, null, null, null, NewOrderRespType.RESULT);
        long eclipseTime1 = System.currentTimeMillis() - startTime1;
        log.error("\n[做多热身耗时] " + eclipseTime1);

        //如果成交量为0，热身失败，此为致命错误，系统退出。
        if( postOrderHotSport_LONGBUY_ORDER == null || postOrderHotSport_LONGBUY_ORDER.getExecutedQty().compareTo(BigDecimal.ZERO) ==0 ){
            log.error("[!!!FATAL ERROR] 做多热身失败");
            System.exit(-1);
        }


        /*** 平多热身，并验证平多功能是否正常 ****/
        long startTime2 = System.currentTimeMillis();
        Order postOrderHotSport_LONG_SELL_ORDER = syncRequestClient.postOrder(HOT_SPORT_CURRENCY, OrderSide.SELL, PositionSide.LONG, OrderType.MARKET, null,
                HOT_SPORT_AMOUNT, null, null, null, null, null, NewOrderRespType.RESULT);
        long eclipseTime2 = System.currentTimeMillis() - startTime2;
        log.error("[平多热身耗时] " + eclipseTime2 );

        //如果成交量为0，热身失败，此为致命错误，系统退出。
        if( postOrderHotSport_LONG_SELL_ORDER == null || postOrderHotSport_LONG_SELL_ORDER.getExecutedQty().compareTo(BigDecimal.ZERO) ==0 ){
            log.error("[!!!FATAL ERROR] 平多热身失败");
            System.exit(-1);
        }

    }



    /**初始化真正的波动率监控系统
     * */
    private static void initRealPumpRateMonitorSystemInfoMap(){
        //初始化为0
        for(int i = 0 ; i < USDTSymbols.length ; i++ ){
            /**初始化这里后面要确认第一个数值，别搞出个瞬间 10% ，直接补仓了，嗯。。*/
            lastPerpetualCandlestickEvent.put(USDTSymbols[i],new CandlestickEvent());
            initRealPumpRateMonitorInfoMapStatus(USDTSymbols[i]);
        }
        //检测初始化
        for(int i = 0 ; i < USDTSymbols.length ; i++ ){
            if( !realPumpRateMonitorInfoMapStatusInitSuccess(USDTSymbols[i]) ){
                log.error("[自动止损系统订单状态初始化失败]");
                System.exit(-1);//只要有一个币初始化失败，就代表失败。。
            }
        }

        log.error("[波动率监控系统初始化成功]");
    }


    /**初始化真正的波动率监控系统状态**/
    private static void initRealPumpRateMonitorInfoMapStatus(String currency){

        lastPerpetualCandlestickEvent.get(currency).setEventTime(0l);//第一个数据设置为0，其余的不用设置
        lastPerpetualCandlestickEvent.get(currency).setOpen(BigDecimal.ONE);//初始化第一个数据设置为1,因为要被当成分母，所以要设置成1
        lastPerpetualCandlestickEvent.get(currency).setClose(BigDecimal.ZERO);//第一个数据设置为0
        lastPerpetualCandlestickEvent.get(currency).setLongMarginCallTimes(0);//第一个数据设置为0
        lastPerpetualCandlestickEvent.get(currency).setUpRate(BigDecimal.ZERO);//第一个数据设置为0
        lastPerpetualCandlestickEvent.get(currency).setStartAnalysisModel(false);//第一个数据设置为false
        lastPerpetualCandlestickEvent.get(currency).setkLineElipsedCountAfterStartMarginCall(0);//开始补仓之后所经过的K线 数目
        lastPerpetualCandlestickEvent.get(currency).setStopMarginCall(false);//开始补仓之后，经过多少K线之后，是否已停止补仓
        lastPerpetualCandlestickEvent.get(currency).setStartCountingKLineElipsed(false);//是否开始统计K线经过的根数
        lastPerpetualCandlestickEvent.get(currency).setAntCount(0);

    }

    /** 检测真正的波动率监控系统初始状态**/
    private static boolean realPumpRateMonitorInfoMapStatusInitSuccess(String currency){
        if( lastPerpetualCandlestickEvent.get( currency ) != null
                && lastPerpetualCandlestickEvent.get(currency).getEventTime() == 0
                && lastPerpetualCandlestickEvent.get(currency).getOpen().compareTo(BigDecimal.ONE) == 0
                && lastPerpetualCandlestickEvent.get(currency).getClose().compareTo(BigDecimal.ZERO) == 0
                && lastPerpetualCandlestickEvent.get(currency).getLongMarginCallTimes() == 0
                && lastPerpetualCandlestickEvent.get(currency).getUpRate().compareTo(BigDecimal.ZERO) == 0
                && lastPerpetualCandlestickEvent.get(currency).isStartAnalysisModel() == false
                && lastPerpetualCandlestickEvent.get(currency).getkLineElipsedCountAfterStartMarginCall() == 0
                && lastPerpetualCandlestickEvent.get(currency).isStopMarginCall() == false
                && lastPerpetualCandlestickEvent.get(currency).isStartCountingKLineElipsed() == false
                && lastPerpetualCandlestickEvent.get(currency).getAntCount() == 0){

            return true;
        }

        return false;
    }


    /**
     * 交互线程，一直等待键盘输入。
     * 并且代码输出的回车，不会被解析。
     * 功能1  动态修改补仓系统首单
     * 动能2  动态开关补仓系统
     * 功能3  动态设置止盈
     * 功能4  动态开关自动复利系统
     * 功能5  动态设置补仓曲线
     * **/
    private static void startDynamicConfigSystem(){

        log.error("[启动交互线程]");
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(;;) {

                    try {
                        /**这里每次都要新建一个对象
                         * 否则 ctrl + D 之后
                         * System.in 一直处于 EOF 状态
                         * autoProfitReader.next() 就等于不成立
                         * 所以出现死循环，嗯。。所以每次循环都要新建一个Scanner
                         */
                        String inputStr = new Scanner(System.in).next();

                        if (isMyNumeric(inputStr) && new BigDecimal(inputStr).compareTo(BigDecimal.ZERO) == 0) {
                            log.error("[暂不处理0]");
                            continue;
                        }

                        /**判断输入数值
                         * 如果输入是数字，并且大于0
                         * */
                        if (isMyNumeric(inputStr) && new BigDecimal(inputStr).compareTo(BigDecimal.ZERO) > 0) {
                            if(new BigDecimal(inputStr).compareTo( LAST___POSITION_USDTs___) > 0 ){
                                log.error("[仓位太大]\n");
                                continue;
                            }

                            BigDecimal last____POSITION_USDTs___ = ___POSITION_USDTs___;
                            BigDecimal last____REAL_PUMPMONITOR_EVERYTIME_MARGINCALL_POSITION_LIMIT = EVERYTIME_MARGINCALL_POSITION_LIMIT;

                            ___POSITION_USDTs___ = new BigDecimal(inputStr);
                            /**更新 仓位*/
                            EVERYTIME_MARGINCALL_POSITION_LIMIT = ___POSITION_USDTs___.multiply(EVERYTIME_MARGINCALL_POSITION_LIMIT_PERCENT);

                            log.error("[更新仓位] " + ___POSITION_USDTs___ + " [从] " + last____POSITION_USDTs___ + "\n");
                            log.error("[更新" + EVERYTIME_MARGINCALL_POSITION_LIMIT_PERCENT.multiply(BigDecimal_100) + "%" + "] " + EVERYTIME_MARGINCALL_POSITION_LIMIT + " [从] " + last____REAL_PUMPMONITOR_EVERYTIME_MARGINCALL_POSITION_LIMIT + "\n");

                        /**如果输入的不是数字。
                         * */
                        } else {

                            if (inputStr.equalsIgnoreCase("n")) {
                                enableAutoMarginCall = false;
                                log.error("\n[已关闭自动补仓]\n");
                                continue;
                            } else if (inputStr.equalsIgnoreCase("y")) {
                                enableAutoMarginCall = true;
                                /**TODO:极其重要的重置，只有重置，才能继续补仓。*/
                                currentMarginCallCurrency = null;
                                log.error("\n[已开启自动补仓]\n");
                                continue;
                            } else if (inputStr.equalsIgnoreCase("close")) {
                                autoUpdateUSDTSConfig = false;
                                log.error("\n[已关闭自动复利]\n");
                                continue;
                            } else if (inputStr.equalsIgnoreCase("open")) {
                                autoUpdateUSDTSConfig = true;
                                log.error("\n[已开启自动复利]\n");
                                continue;
                            }else if (inputStr.startsWith("涨幅") && inputStr.indexOf("波动率") >= 0 ) {

                                String zhangfu = inputStr.substring(2,inputStr.indexOf("波动率"));
                                String bodonglv = inputStr.substring(inputStr.indexOf("波动率")+3,inputStr.length());

                                if( !isMyNumeric(zhangfu) || !isMyNumeric(bodonglv) ){
                                    log.error("[请重新输入]");
                                    continue;
                                }

                                /**测试可用任意值，注释掉*/
//                                if( new BigDecimal(zhangfu).compareTo(BigDecimal_0_5) < 0 ){
//                                    log.error("[涨幅最小1.0% ]");
//                                    zhangfu = "0.5";
//                                }
//                                if( new BigDecimal(bodonglv).compareTo(BigDecimal_0_5) < 0 ){
//                                    log.error("[波动率最小0.5%/s]");
//                                    bodonglv = "0.5";
//                                }

                                BigDecimal last_PUMP_RATE_PERCENT_ATLEAST = PUMP_RATE_PERCENT_ATLEAST;
                                BigDecimal last_PUMP_RATE_PERCENT_PERSECOND_ATLEAST_PERCENT = PUMP_RATE_PERCENT_PERSECOND_ATLEAST_PERCENT;

                                /**更新涨幅*/
                                PUMP_RATE_PERCENT_ATLEAST = BigDecimal.ONE.add( new BigDecimal(zhangfu).divide( BigDecimal_100,4,BigDecimal.ROUND_DOWN) );
                                /**更新波动率*/
                                PUMP_RATE_PERCENT_PERSECOND_ATLEAST_PERCENT = new BigDecimal(bodonglv).divide(BigDecimal_100).setScale(4,BigDecimal.ROUND_DOWN);

                                //log.error("[你输入的是] [涨幅]"  + zhangfu + "%  [波动率] " + bodonglv + "%");
                                log.error("更新 [涨幅]" + PUMP_RATE_PERCENT_ATLEAST.subtract(BigDecimal.ONE).multiply(BigDecimal_100) + "% [从]" + last_PUMP_RATE_PERCENT_ATLEAST.subtract(BigDecimal.ONE).multiply(BigDecimal_100) + "%");
                                log.error("更新 [波动率]" + PUMP_RATE_PERCENT_PERSECOND_ATLEAST_PERCENT.multiply(BigDecimal_100) + "%" + " [从]" + last_PUMP_RATE_PERCENT_PERSECOND_ATLEAST_PERCENT.multiply(BigDecimal_100) + "%");

                            }else if (  ( inputStr.equals("关闭区间限制") || inputStr.equals("开启区间限制") )  ) {
                                if( inputStr.equals("关闭区间限制") ){
                                    enableSectionLimit = false;
                                    log.error("[已关闭区间限制]");
                                }else if( inputStr.equals("开启区间限制") ){
                                    enableSectionLimit = true;
                                    log.error("[已开启区间限制]");
                                }

                            }else if( inputStr.equals("播报系统已开启") ){
                                broadcastSystemStartup = true;
                                log.error("[已关闭播报提醒]");
                            }else if( inputStr.equals("监控数值已更新") ){
                                monitorValueUpdate = true;
                                log.error("[已关闭监控更新提醒]");
                            }else if( inputStr.equals("已开始补仓") ){
                                startMargincallStart = true;
                                log.error("[已关闭补仓提醒]");
                            }else if( inputStr.equals("更新系统配置") ){
                                log.error("\n[开始更新补仓系统配置]\n");
                                loadSystemParamsFromConfigFile(false);
                            }else if( inputStr.endsWith("关闭补仓") ){
                                String tmpCurrency = inputStr.substring(0,inputStr.indexOf("关闭补仓"));
                                if( !tmpCurrency.isEmpty() && ( tmpCurrency.endsWith("USDT") || tmpCurrency.endsWith("usdt") ) ){
                                    tmpCurrency = tmpCurrency.trim().toUpperCase();
                                    manualStopCurrencyMargincall(tmpCurrency.toUpperCase());
                                }else{
                                    System.out.println("请重新输入 格式:KLAYUSDT关闭补仓");
                                }
                            }else{
                                log.error("[非法输入]");
                            }


                        }

                        //catch掉，休想从这里逃出去，嗯。。
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }



}
