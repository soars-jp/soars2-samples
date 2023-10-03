package jp.soars.tutorials.sample12.module1;

import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TAgentRule;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

/**
 * 平日エージェント移動ルール
 * @author nagakane
 */
public final class TRuleOfAgentMovingOnWeekdays extends TAgentRule {

    /** 出発地 */
    private final TSpot fSource;

    /** 目的地 */
    private final TSpot fDestination;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     * @param source 出発地
     * @param destination 目的地
     */
    public TRuleOfAgentMovingOnWeekdays(String name, TRole owner, TSpot source, TSpot destination) {
        // 親クラスのコンストラクタを呼び出す．
        super(name, owner);
        fSource = source;
        fDestination = destination;
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
        // エージェントが平日(土日以外)に出発地にいるならば，目的地に移動する．
        boolean debugFlag = true;
        EDay day = EDay.values()[currentTime.getDay() % 7];
        if (day != EDay.Sunday && day != EDay.Saturday) {
            if (isAt(fSource)) {
                moveTo(fDestination);
                appendToDebugInfo("success", debugFlag);
            } else {
                appendToDebugInfo("fail (spot)", debugFlag);
            }
        } else {
            appendToDebugInfo("fail (day)", debugFlag);
        }
    }
}
