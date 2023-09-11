package jp.soars.samples.sample09;

import jp.soars.core.TAgent;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;

/**
 * 父親役割
 * @author nagakane
 */
public final class TRoleOfFather extends TRole {
	/** 家を出発するルール名 */
    private static final String RULE_NAME_OF_LEAVE_HOME = "LeaveHome";

    /** 家に帰るルール名 */
    private static final String RULE_NAME_OF_RETURN_HOME = "ReturnHome";

	/** 集計ルール名 */
    public static final String RULE_NAME_OF_AGGREGATION = "Aggregation";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param home 自宅
     * @param company 会社
     */
    public TRoleOfFather(TAgent owner, TSpot home, TSpot company) {
        super(ERoleName.Father, owner, 3, 0);

        // 自宅にいるならば，会社に移動する．毎日9時，エージェント移動ステージに発火するように予約する．
        new TRuleOfMoveFromHomeToCompany(RULE_NAME_OF_LEAVE_HOME, this, home, company)
                .setTimeAndStage(9, 0, 0, EStage.AgentMoving);

        // 会社にいるならば，自宅に移動する．毎日17時，エージェント移動ステージに発火するように予約する．
        new TRuleOfMoveFromHomeToCompany(RULE_NAME_OF_RETURN_HOME, this, company, home)
                .setTimeAndStage(17, 0, 0, EStage.AgentMoving);

		// 集計ルールはステージ実行ルールとして予約．
        new TRuleOfAggregation(RULE_NAME_OF_AGGREGATION, this, home, company)
                .setStage(EStage.Aggregation);
    }
}
