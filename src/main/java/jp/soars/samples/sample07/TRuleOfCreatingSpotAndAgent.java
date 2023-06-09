package jp.soars.samples.sample07;

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

    /** デバッグ情報として作成したエージェント名を出力する */
    private String fCreatedSpot;

    /** デバッグ情報として作成したスポット名を出力する */
    private String fCreatedAgent;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールを持つ役割
     */
    public TRuleOfCreatingSpotAndAgent(String name, TRole owner) {
        super(name, owner);
        fCreatedSpot = "";
        fCreatedAgent = "";
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
        // 新たなスポットの作成
        TSpot newSpot = spotManager.createSpots(ESpotType.Dummy, 1).get(0);
        fCreatedSpot = newSpot.getName();

        // 新たなエージェントの作成
        TAgent newAgent = agentManager.createAgents(EAgentType.Dummy, 1).get(0);
        fCreatedAgent = newAgent.getName();
    }

    /**
     * ルールログで表示するデバッグ情報．
     * @return デバッグ情報
     */
    @Override
    public String debugInfo() {
        String str = "spot:" + fCreatedSpot + " agent:" + fCreatedAgent;
        fCreatedSpot = "";
        fCreatedAgent = "";
        return str;
    }
}
