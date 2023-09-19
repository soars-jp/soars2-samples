package jp.soars.tutorials.sample05_2;

import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TAgentRule;
import jp.soars.core.TRole;
import jp.soars.core.TRule;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

/**
 * 確率的にエージェント移動するルール
 * @author nagakane
 */
public final class TRuleOfStochasticallyAgentMovingOnWeekdays extends TAgentRule {

    /** 移動確率[0, 1] */
    private final double fProbability;

    /** 出発地 */
    private final TSpot fSource;

    /** 目的地 */
    private final TSpot fDestination;

    /** 会社から自宅に移動するルール */
    private final TRule fRuleOfReturnHome;

    /** 会社から自宅に移動するルールを実行するまでの時間間隔 */
    private final TTime fIntervalTimeOfReturnHome;

    /** 会社から自宅に移動するルールの発火時刻計算用 */
    private final TTime fTimeOfReturnHome;

    /** 会社から自宅に移動するルールを実行するステージ */
    private final Enum<?> fStageOfReturnHome;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     * @param probability 移動確率[0, 1]
     * @param ruleOfReturnHome 会社から自宅に移動するルール
     * @param intervalTimeOfReturnHome 会社から自宅に移動するルールを実行するまでの時間間隔
     * @param stageOfReturnHome 会社から自宅に移動するルールを実行するステージ
     */
    public TRuleOfStochasticallyAgentMovingOnWeekdays(String name, TRole owner, double probability, TSpot source,
            TSpot destination, TRule ruleOfReturnHome, String intervalTimeOfReturnHome, Enum<?> stageOfReturnHome) {
        // 親クラスのコンストラクタを呼び出す．
        super(name, owner);
        if (probability < 0.0 || 1.0 < probability) {
            throw new RuntimeException("Invalid probability. Probability value must be between 0 and 1.");
        }
        fProbability = probability;
        fSource = source;
        fDestination = destination;
        fRuleOfReturnHome = ruleOfReturnHome;
        fIntervalTimeOfReturnHome = new TTime(intervalTimeOfReturnHome);
        fTimeOfReturnHome = new TTime();
        fStageOfReturnHome = stageOfReturnHome;
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
        // エージェントが平日(土日以外)に出発地にいるかつ移動確率を満たしたならば，目的地に移動する．
        boolean debugFlag = true;
        EDay day = EDay.values()[currentTime.getDay() % 7];
        if (day != EDay.Sunday && day != EDay.Saturday) {
            if (isAt(fSource)) {
                if (getRandom().nextDouble() <= fProbability) {
                    moveTo(fDestination);
                    appendToDebugInfo("success", debugFlag);

                    fTimeOfReturnHome.copyFrom(currentTime).add(fIntervalTimeOfReturnHome);
                    fRuleOfReturnHome.setTimeAndStage(fTimeOfReturnHome.getDay(), fTimeOfReturnHome.getHour(),
                            fTimeOfReturnHome.getMinute(), fTimeOfReturnHome.getSecond(), fStageOfReturnHome);
                } else {
                    appendToDebugInfo("fail (probability)", debugFlag);
                }
            } else {
                appendToDebugInfo("fail (spot)", debugFlag);
            }
        } else {
            appendToDebugInfo("fail (day)", debugFlag);
        }
    }
}
