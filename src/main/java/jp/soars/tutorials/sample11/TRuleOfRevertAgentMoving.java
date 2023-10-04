package jp.soars.tutorials.sample11;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jp.soars.core.TAgent;
import jp.soars.core.TAgentManager;
import jp.soars.core.TRole;
import jp.soars.core.TRule;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

/**
 * エージェントを移動前スポットに戻すルール
 * @author nagakane
 */
public final class TRuleOfRevertAgentMoving extends TRule {

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールを持つ役割
     */
    public TRuleOfRevertAgentMoving(String name, TRole owner) {
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
        TRoleOfSpotWithCapacity role = (TRoleOfSpotWithCapacity) getOwnerRole();
        TSpot owner = (TSpot) role.getOwner();

        // 戻すエージェントの数を計算して，定員オーバーしている場合はエージェントを戻す
        int noOfReverts = owner.getNoOfAgents() - role.getCapacity();
        if (0 < noOfReverts) {
            // 直前に移動してきたエージェントと移動前スポットのマップからランダムに選択
            Map<TAgent, TSpot> preMovementSpotMap = role.getPreMovementSpotMap();
            List<Entry<TAgent, TSpot>> selectedAgents = getRandom()
                    .chooseWithoutReplacement(preMovementSpotMap.entrySet(), noOfReverts);

            // エージェントを戻して，マップから削除
            for (Entry<TAgent, TSpot> entry : selectedAgents) {
                entry.getKey().moveTo(entry.getValue());
                preMovementSpotMap.remove(entry.getKey());
            }
        }
    }
}
