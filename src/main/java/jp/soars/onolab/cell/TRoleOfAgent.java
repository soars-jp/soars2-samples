package jp.soars.onolab.cell;

import jp.soars.core.TAgent;
import jp.soars.core.TRole;

/**
 * エージェント役割
 * @author nagakane
 */
public class TRoleOfAgent extends TRole {

    /** ランダム移動ルール名 */
    private static final String RULE_NAME_OF_RANDOM_MOVING = "RandomMoving";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     */
    public TRoleOfAgent(TAgent owner) {
        super(ERoleName.Agent, owner, 1, 0);

        new TRuleOfAgentRandomMoving(RULE_NAME_OF_RANDOM_MOVING, this)
                .setStage(EStage.AgentMoving);
    }
}
