package jp.soars.onolab.transportation.sample02;

import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TAgentRule;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

public final class TRuleOfAgentMoving extends TAgentRule {

    /** 出発地 */
    private final TSpot fSource;

    /** 目的地 */
    private final TSpot fDestination;

    /** 次のルールを実行するまでの時間 */
    private TTime fTimeToNextRule;

    /** 次のルールを実行するステージ */
    private Enum<?> fStageOfNextRule;

    /** 次に実行するルール名 */
    private String fNextRule;

    /** 次の実行時刻を計算するためのワークメモリ */
    private TTime fNextTime;

    /**
     * コンストラクタ
     *
     * @param name        ルール名
     * @param owner       このルールをもつ役割
     * @param source      出発地
     * @param destination 目的地
     */
    public TRuleOfAgentMoving(String name, TRole owner, TSpot source, TSpot destination) {
        super(name, owner);
        fSource = source;
        fDestination = destination;
        fTimeToNextRule = null;
        fStageOfNextRule = null;
        fNextRule = null;
    }

    /**
     * コンストラクタ
     *
     * @param name        ルール名
     * @param owner       このルールをもつ役割
     * @param source      出発地
     * @param destination 目的地
     */
    public TRuleOfAgentMoving(String name, TRole owner, TSpot source, TSpot destination, TTime timeToNextRule,
            Enum<?> stageOfNextRule, String nextRule) {
        // 親クラスのコンストラクタを呼び出す．
        super(name, owner);
        fSource = source;
        fDestination = destination;
        fTimeToNextRule = timeToNextRule;
        fStageOfNextRule = stageOfNextRule;
        fNextRule = nextRule;
        fNextTime = new TTime();

    }

    /**
     * ルールを実行する．
     *
     * @param currentTime           現在時刻
     * @param currentStage          現在ステージ
     * @param spotManager           スポット管理
     * @param agentManager          エージェント管理
     * @param globalSharedVariables グローバル共有変数集合
     */
    @Override
    public final void doIt(TTime currentTime, Enum<?> currentStage, TSpotManager spotManager,
            TAgentManager agentManager, Map<String, Object> globalSharedVariables) {
        // エージェントが出発地にいるならば，目的地に移動する．
        boolean debugFlag = true;
        if (isAt(fSource)) {
            moveTo(fDestination); // 目的地へ移動する．
            if (fNextRule != null) {// 次に実行するルールが定義されていたら
                fNextTime.copyFrom(currentTime).add(fTimeToNextRule);
                getRule(fNextRule).setTimeAndStage(fNextTime.getDay(), fNextTime.getHour(), fNextTime.getMinute(),
                        fNextTime.getSecond(), fStageOfNextRule);
            }
            appendToDebugInfo("success", debugFlag);
        } else {
            appendToDebugInfo("fail", debugFlag);
        }
    }

}
