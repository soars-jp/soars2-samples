package jp.soars.tutorials.sample13;

import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TAgentRule;
import jp.soars.core.TRole;
import jp.soars.core.TRule;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

/**
 * エージェント移動ルール
 * 移動した場合次に実行するルールを臨時実行ルールとして発火登録することができる．
 * @author nagakane
 */
public final class TRuleOfAgentMoving extends TAgentRule {

    /** 出発地 */
    private final TSpot fSource;

    /** 目的地 */
    private final TSpot fDestination;

    /** 次に実行するルール */
    private final TRule fNextRule;

    /** 次のルールを実行するまでの時間間隔 */
    private final TTime fIntervalTimeToNextRule;

    /** 次のルールの発火時刻計算用 */
    private final TTime fTimeOfNextRule;

    /** 次のルールを実行するステージ */
    private final Enum<?> fStageOfNextRule;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     * @param source 出発地
     * @param destination 目的地
     * @param nextRule 次に実行するルール．
     * @param intervalTimeToNextRule 次のルールを実行するまでの時間間隔
     * @param stageOfNextRule 次のルールを実行するステージ
     */
    public TRuleOfAgentMoving(String name, TRole owner, TSpot source, TSpot destination,
            TRule nextRule, TTime intervalTimeToNextRule, Enum<?> stageOfNextRule) {
        // 親クラスのコンストラクタを呼び出す
        super(name, owner);
        fSource = source;
        fDestination = destination;
        fNextRule = nextRule;
        fIntervalTimeToNextRule = intervalTimeToNextRule;
        fTimeOfNextRule = new TTime();
        fStageOfNextRule = stageOfNextRule;
    }

    /**
     * コンストラクタ
     * 次に実行するルールを登録しない場合に使用する．エージェント移動のみのルールとなる．
     * @param name ルール名
     * @param owner このルールをもつ役割
     * @param source 出発地
     * @param destination 目的地
     */
    public TRuleOfAgentMoving(String name, TRole owner, TSpot source, TSpot destination) {
        this(name, owner, source, destination, null, null, null);
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
        boolean debugFlag = true; // デバッグ情報出力フラグ
        if (isAt(fSource)) { // 出発地にいるなら
            moveTo(fDestination); // 目的地に移動する
            // 出発地と目的地をデバッグ情報として出力
            appendToDebugInfo("move from " + fSource.getName() + " to " + fDestination.getName(), debugFlag);

            if (fNextRule != null) { // 次に実行するルールが定義されていたら
                // 現在時刻にインターバルを足した時刻を次のルールの発火時刻とする．
                fTimeOfNextRule.copyFrom(currentTime)
                               .add(fIntervalTimeToNextRule);
                // 次に実行するルールを臨時実行ルールとしてスケジュール
                fNextRule.setTimeAndStage(fTimeOfNextRule.getDay(), fTimeOfNextRule.getHour(),
                        fTimeOfNextRule.getMinute(), fTimeOfNextRule.getSecond(), fStageOfNextRule);
            }
        } else { // 移動しない場合
            appendToDebugInfo("no move (wrong spot)", debugFlag);
        }
    }
}
