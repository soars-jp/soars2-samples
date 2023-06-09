package jp.soars.samples.sample10;

import jp.soars.core.TAgent;
import jp.soars.core.TRole;

/**
 * エージェント役割
 * @author nagakane
 */
public final class TRoleOfAgent extends TRole {

    /** ランダムに移動する */
    public static final String RULE_NAME_OF_RANDOM_MOVEING = "RandomMoving";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     */
    public TRoleOfAgent(TAgent owner) {
        super(ERoleName.Agent, owner, 1, 0);

        // ステージルールとして登録
        new TRuleOfRandomMoving(RULE_NAME_OF_RANDOM_MOVEING, this)
                .setStage(EStage.AgentMoving);
    }
}
