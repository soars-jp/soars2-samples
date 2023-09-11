package jp.soars.samples.sample12;

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
        TSpot owner = (TSpot) getOwnerRole().getOwner();
        TRoleOfSpotWithCapacity role = (TRoleOfSpotWithCapacity) getRole(ERoleName.SpotWithCapacity);
        int noOfReverts = owner.getNoOfAgents() - role.getCapacity();
        if (0 < noOfReverts) { // 定員オーバーしている場合
            // 仮移動エージェント集合
            Map<TAgent, TSpot> temporaryAgents = role.getTemporaryAgents();
            // 戻すエージェントを選択
            List<Entry<TAgent, TSpot>> agentSpotList = getRandom()
                    .chooseWithoutReplacement(temporaryAgents.entrySet(), noOfReverts);
            for (Entry<TAgent, TSpot> entry : agentSpotList) {
                entry.getKey().moveTo(entry.getValue()); // 移動
                temporaryAgents.remove(entry.getKey()); // 仮移動エージェント集合から削除
            }
        }
    }
}
