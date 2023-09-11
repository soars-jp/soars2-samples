package jp.soars.samples.sample12;

import java.util.List;
import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TAgentRule;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

/**
 * ランダム移動ルール
 * @author nagakane
 */
public final class TRuleOfRandomMoving extends TAgentRule {

    /** 目的地のスポットタイプ． */
    private final Enum<?> fDestinationSpotType;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールを持つ役割
     * @param destinationType 移動する候補地のスポットタイプ
     */
    public TRuleOfRandomMoving(String name, TRole owner, Enum<?> destinationSpotType) {
        super(name, owner);
        fDestinationSpotType = destinationSpotType;
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

        List<TSpot> destinationCandidates = spotManager.getSpots(fDestinationSpotType); // 移動先スポット候補リスト
        TSpot destination = destinationCandidates.get(getRandom().nextInt(destinationCandidates.size())); // 移動先スポット

        // スポットに定員があるなら，仮移動エージェントとして追加
        TRoleOfSpotWithCapacity role = (TRoleOfSpotWithCapacity) destination.getRole(ERoleName.SpotWithCapacity);
        if (role != null) {
            role.addTemporaryAgent(getAgent(), getCurrentSpot());
        }

        moveTo(destination); // 移動
        appendToDebugInfo(destination.getName(), debugFlag); // デバッグ情報
    }
}
