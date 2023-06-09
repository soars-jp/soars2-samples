package jp.soars.samples.sample07;

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

    /** デバッグ情報として削除したエージェント名を出力する */
    private String fDeletedSpot;

    /** デバッグ情報として削除したスポット名を出力する */
    private String fDeletedAgent;

    /**
     * 削除ルール
     * @param name ルール名
     * @param owner このルールを持つ役割
     */
    public TRuleOfDeletingSpotAndAgent(String name, TRole owner) {
        super(name, owner);
        fDeletedSpot = "";
        fDeletedAgent = "";
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
        // ダミースポットをランダムに１つ削除
        List<TSpot> dummySpots = spotManager.getSpots(ESpotType.Dummy); // スポット管理からダミースポットのリストを取得
        TSpot spot = null;
        do {
            spot = dummySpots.get(getRandom().nextInt(dummySpots.size())); // 削除されるスポットをランダムに選択
        } while (!spot.getAgents().isEmpty()); // エージェントがいるスポットを消そうとするとエラーとなるため，その場合は再選択
        fDeletedSpot = spot.getName();
        spotManager.deleteSpot(spot);

        // ダミーエージェントをランダムに１つ削除
        List<TAgent> dummyAgents = agentManager.getAgents(EAgentType.Dummy); // エージェント管理からダミーエージェントのリストを取得
        TAgent agent = dummyAgents.get(getRandom().nextInt(dummyAgents.size())); // 削除されるエージェントをランダムに選択
        fDeletedAgent = agent.getName();
        agentManager.deleteAgent(agent);
    }

    /**
     * ルールログで表示するデバッグ情報．
     * @return デバッグ情報
     */
    @Override
    public String debugInfo() {
        String str = "spot:" + fDeletedSpot + " agent:" + fDeletedAgent;
        fDeletedSpot = "";
        fDeletedAgent = "";
        return str;
    }
}
