package jp.soars.samples.sample09;

import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TAgentRule;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

/**
 * 集計ルール
 * @author nagakane
 */
public final class TRuleOfAggregation extends TAgentRule {

    /** 自宅 */
    private TSpot fHome;

    /** 会社 */
    private TSpot fCompany;

    /** [グローバル共有変数で集計するキー] 自宅 */
    public static final String HOME_KEY = "Home";

    /** [グローバル共有変数で集計するキー] 会社 */
    public static final String COMPANY_KEY = "Company";

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     * @param home 自宅
     * @param company 職場
     */
    public TRuleOfAggregation(String name, TRole owner, TSpot home, TSpot company){
        super(name, owner);
        fHome = home;
        fCompany = company;
    }

    /**
     * ルールを実行する．
     * @param currentTime 現在時刻
     * @param currentStage 現在ステージ
     * @param spotManager スポット管理
     * @param agentManager エージェント管理
     * @param globalSharedVariables グローバル共有変数集合
     */
    @Override
    public final void doIt(TTime currentTime, Enum<?> currentStage, TSpotManager spotManager,
            TAgentManager agentManager, Map<String, Object> globalSharedVariables) {
        // 自分がいる場所をグローバル共有変数に報告する．
        // ConcurrentHashMapのcomputeメソッドはアトミックであることが保証されており，並列化ステージで使用できる．
        if (isAt(fHome)) {
            globalSharedVariables.compute(HOME_KEY, (k, v) -> v = (long) v + 1);
        } else if (isAt(fCompany)) {
            globalSharedVariables.compute(COMPANY_KEY, (k, v) -> v = (long) v + 1);
        }
    }
}
