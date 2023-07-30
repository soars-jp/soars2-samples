package jp.soars.samples.sample04;

import jp.soars.core.TAgent;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;

/**
 * 子供役割
 * @author nagakane
 */
public final class TRoleOfChild extends TRole {

    /** 家を出発するルール名 */
    public static final String RULE_NAME_OF_LEAVE_HOME = "LeaveHome";

    /** 家に帰るルール名 */
    public static final String RULE_NAME_OF_RETURN_HOME = "ReturnHome";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param home 自宅
     * @param school 学校
     */
    public TRoleOfChild(TAgent owner, TSpot home, TSpot school) {
        super(ERoleName.Child, owner, 2, 1);

        // 自宅にいるなら学校に移動する
        new TRuleOfAgentMoving(RULE_NAME_OF_LEAVE_HOME, this, home, school)
                .setTimeAndStage(8, 0, 0, EStage.AgentMoving);

        // 学校にいるなら自宅に移動する
        new TRuleOfAgentMoving(RULE_NAME_OF_RETURN_HOME, this, school, home)
                .setTimeAndStage(15, 0, 0, EStage.AgentMoving);
    }
}
