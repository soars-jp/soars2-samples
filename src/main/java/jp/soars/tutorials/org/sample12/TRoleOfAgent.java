package jp.soars.samples.sample12;

import jp.soars.core.TAgent;
import jp.soars.core.TRole;

/**
 * エージェント役割
 * @author nagakane
 */
public final class TRoleOfAgent extends TRole {

    /** ランダムに移動する */
    public static final String RULE_NAME_OF_RANDOM_MOVING = "RandomMoving";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     */
    public TRoleOfAgent(TAgent owner) {
        super(ERoleName.Agent, owner, 1, 0);

        new TRuleOfRandomMoving(RULE_NAME_OF_RANDOM_MOVING, this, ESpotType.Spot)
                .setTimeAndStage(8, 0, 0, EStage.AgentMoving);
    }
}
