package jp.soars.samples.sample06;

import jp.soars.core.TAgent;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;
import jp.soars.core.TTime;

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
     * @param home 自宅
     */
    public TRoleOfAgent(TAgent owner, TSpot home) {
        super(ERoleName.Agent, owner, 1, 0);

        // 8時から18時までの間，1-3時間ごとにランダム移動するルールを設定する
        new TRuleOfRandomMoving(RULE_NAME_OF_RANDOM_MOVING, this, home, ESpotType.Spot, new TTime("18:00:00"))
                .setTimeAndStage(8, 0, 0, EStage.AgentMoving);
    }
}
