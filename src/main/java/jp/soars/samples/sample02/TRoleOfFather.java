package jp.soars.samples.sample02;

import jp.soars.core.TAgent;
import jp.soars.core.TRole;
import jp.soars.core.TRule;
import jp.soars.core.TSpot;
import jp.soars.core.TTime;

/**
 * 父親役割
 * @author nagakane
 */
public final class TRoleOfFather extends TRole {

    /** 家を出発するルール名 */
    private static final String RULE_NAME_OF_LEAVE_HOME = "LeaveHome";

    /** 家に帰るルール名 */
    private static final String RULE_NAME_OF_RETURN_HOME = "ReturnHome";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param home 自宅
     * @param company 会社
     */
    public TRoleOfFather(TAgent owner, TSpot home, TSpot company) {
        super(ERoleName.Father, owner, 2, 0);

        // 会社にいるならば自宅に移動する．スケジューリングは，RULE_NAME_OF_LEAVE_HOMEで行われる．
        TRule ruleOfReturnHome = new TRuleOfAgentMoving(RULE_NAME_OF_RETURN_HOME, this, company, home);

        // 自宅にいるならば会社に移動し，32時間後のエージェント移動ステージにruleOfReturnHomeをスケジュールする．
        // 毎日9時，エージェントステージに発火するように予約する．
        new TRuleOfAgentMoving(RULE_NAME_OF_LEAVE_HOME, this, home, company,
                ruleOfReturnHome, new TTime("0/32:00:00"), EStage.AgentMoving)
                .setTimeAndStage(9, 0, 0, EStage.AgentMoving);
    }
}
