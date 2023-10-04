package jp.soars.tutorials.sample16;

import java.util.List;
import java.util.Map;

import jp.soars.core.TAgent;
import jp.soars.core.TAgentManager;
import jp.soars.core.TAgentRule;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

/**
 * オブジェクト削除ルール．
 * @author nagakane
 */
public final class TRuleOfDeletingSpotAndAgent extends TAgentRule {

    /**
     * 削除ルール
     * @param name ルール名
     * @param owner このルールを持つ役割
     */
    public TRuleOfDeletingSpotAndAgent(String name, TRole owner) {
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
        // ダミースポットをランダムに１つ選択して削除する．
        // 削除実行前に，スポットにエージェントがいないことを確認している．
        List<TSpot> dummySpots = spotManager.getSpots(ESpotType.Dummy);
        TSpot spot = null;
        do {
            spot = dummySpots.get(getRandom().nextInt(dummySpots.size()));
        } while (!spot.getAgents().isEmpty());
        spotManager.deleteSpot(spot);
        appendToDebugInfo("deleted spot:" + spot.getName(), debugFlag);

        // ダミーエージェントをランダムに１つ選択して削除する．
        List<TAgent> dummyAgents = agentManager.getAgents(EAgentType.Dummy);
        TAgent agent = dummyAgents.get(getRandom().nextInt(dummyAgents.size()));
        agentManager.deleteAgent(agent);
        appendToDebugInfo(" deleted agent:" + agent.getName(), debugFlag);
    }
}
