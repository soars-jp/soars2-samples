package jp.soars.samples.sample12;

import java.util.List;
import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TRole;
import jp.soars.core.TRule;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

public final class TRuleOfCheckCapacity extends TRule {

    /** スポットの定員条件を満たすかどうかを表すグローバル共有変数のキー */
    public static final String KEY_NAME_OF_SPOT_CAPACITY_CONDITION_MET = "isCapacityConditionMet";

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールを持つ役割
     */
    public TRuleOfCheckCapacity(String name, TRole owner) {
        super(name, owner);
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
        List<TSpot> spots = spotManager.getSpots();
        for (TSpot spot : spots) {
            // 定員があるかつ，スポットにいるエージェント数が定員よりも多いなら -> 定員条件を満たさない．
            TRoleOfSpotWithCapacity role = (TRoleOfSpotWithCapacity) spot.getRole(ERoleName.SpotWithCapacity);
            if (role != null && role.getCapacity() < spot.getNoOfAgents()) {
                globalSharedVariables.replace(KEY_NAME_OF_SPOT_CAPACITY_CONDITION_MET, false);
                appendToDebugInfo("false", debugFlag); // デバッグ情報
                return;
            }
        }
        // 全てのスポットが定員条件を満たしている
        globalSharedVariables.replace(KEY_NAME_OF_SPOT_CAPACITY_CONDITION_MET, true);
        appendToDebugInfo("true", debugFlag); // デバッグ情報
    }
}
