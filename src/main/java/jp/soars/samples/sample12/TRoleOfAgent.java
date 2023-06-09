package jp.soars.samples.sample12;

import jp.soars.core.TAgent;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;

/**
 * エージェント役割
 * @author nagakane
 */
public final class TRoleOfAgent extends TRole {

    /** エージェント移動ルール1 */
    public static final String RULE_NAME_OF_AGENT_MOVEING1 = "AgentMoving1";

    /** エージェント移動ルール2 */
    public static final String RULE_NAME_OF_AGENT_MOVEING2 = "AgentMoving2";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param source 初期スポット
     * @param stopover 中継地
     * @param destination 目的地
     */
    public TRoleOfAgent(TAgent owner, TSpot source, TSpot stopover, TSpot destination) {
        super(ERoleName.Agent, owner, 1, 0);

        // stopoverに定員があるため，stopoverから先にエージェントを移動させて，その後stopoverへの移動を定義する．
        new TRuleOfAgentMoving(RULE_NAME_OF_AGENT_MOVEING1, this, stopover, destination)
                .setStage(EStage.AgentMoving1);

        new TRuleOfAgentMoving(RULE_NAME_OF_AGENT_MOVEING2, this, source, stopover)
                .setStage(EStage.AgentMoving2);
    }
}
