package jp.soars.tutorials.sample16;

import java.util.Map;

import jp.soars.core.TAgent;
import jp.soars.core.TAgentManager;
import jp.soars.core.TAgentRule;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

/**
 * オブジェクト作成ルール．
 * @author nagakane
 */
public final class TRuleOfCreatingSpotAndAgent extends TAgentRule {

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールを持つ役割
     */
    public TRuleOfCreatingSpotAndAgent(String name, TRole owner) {
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
        // 新たなスポットの作成
        TSpot newSpot = spotManager.createSpots(ESpotType.Dummy, 1).get(0);
        appendToDebugInfo("created spot:" + newSpot.getName(), debugFlag);

        // 新たなエージェントの作成
        TAgent newAgent = agentManager.createAgents(EAgentType.Dummy, 1).get(0);
        appendToDebugInfo(" created agent:" + newAgent.getName(), debugFlag);
    }
}
