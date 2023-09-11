package jp.soars.samples.sample12;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jp.soars.core.TAgent;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;

public final class TRoleOfSpotWithCapacity extends TRole {

    /** スポットの定員 */
    private final int fCapacity;

    /** 仮移動エージェントと移動前スポット集合 */
    private final Map<TAgent, TSpot> fTemporaryAgents;

    /** エージェントの移動を戻す */
    public static final String RULE_NAME_OF_REVERT_AGENT_MOVING = "RevertAgentMoving";

    /** 仮移動エージェントと移動前スポット集合をリセット */
    public static final String RULE_NAME_OF_RESET = "Reset";

    public TRoleOfSpotWithCapacity(TSpot owner, int capacity) {
        super(ERoleName.SpotWithCapacity, owner, 1, 0);
        if (capacity < 0) {
            throw new RuntimeException("The capacity of spot must be at least 0.");
        }
        fCapacity = capacity;
        fTemporaryAgents = new ConcurrentHashMap<>();

        new TRuleOfRevertAgentMoving(RULE_NAME_OF_REVERT_AGENT_MOVING, this)
                .setStage(EStage.RevertAgentMoving);

        new TRuleOfReset(RULE_NAME_OF_RESET, this)
                .setStage(EStage.Reset);
    }

    /**
     * 仮移動エージェントとして追加
     * @param agent エージェント
     * @param spot 移動前スポット
     */
    public final void addTemporaryAgent(TAgent agent, TSpot spot) {
        fTemporaryAgents.put(agent, spot);
    }

    /**
     * 仮移動エージェントと移動前スポット集合を返す．
     * @return 仮移動エージェントと移動前スポット集合
     */
    public final Map<TAgent, TSpot> getTemporaryAgents() {
        return fTemporaryAgents;
    }

    /**
     * スポットの定員を返す．
     * @return スポットの定員
     */
    public final int getCapacity() {
        return fCapacity;
    }
}
