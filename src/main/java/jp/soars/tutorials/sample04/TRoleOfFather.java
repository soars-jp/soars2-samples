package jp.soars.samples.sample04;

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

    /** ９時に確率的に家を出発するルール名 */
    public static final String RULE_NAME_OF_STOCHASTICALLY_LEAVE_HOME_9 = "StochasticallyLeaveHome9";

    /** １０時に確率的に家を出発するルール名 */
    public static final String RULE_NAME_OF_STOCHASTICALLY_LEAVE_HOME_10 = "StochasticallyLeaveHome10";

    /** １１時に確率的に家を出発するルール名 */
    public static final String RULE_NAME_OF_STOCHASTICALLY_LEAVE_HOME_11 = "StochasticallyLeaveHome11";

    /** 家に帰るルール名 */
    public static final String RULE_NAME_OF_RETURN_HOME = "ReturnHome";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param home 自宅
     * @param company 会社
     */
    public TRoleOfFather(TAgent owner, TSpot home, TSpot company) {
        super(ERoleName.Father, owner, 4, 1);

        // 会社にいるならば自宅に移動する．スケジューリングは，RULE_NAME_OF_STOCHASTICALLY_LEAVE_HOMEで行われる．
        TRule ruleOfReturnHome = new TRuleOfAgentMoving(RULE_NAME_OF_RETURN_HOME, this, company, home);

        // 自宅にいるならば会社に移動し，8時間後のエージェント移動ステージにruleOfReturnHomeをスケジュールする．
        // 9時，10時，11時のエージェント移動ステージにそれぞれのRULE_NAME_OF_STOCHASTICALLY_LEAVE_HOMEが発火するように予約する．
        // TRuleOfStochasticallyAgentMoving は自宅にいる場合のみ会社に移動するため，
        // まず，9時に発火するルールは，自宅にいる場合に0.5の確率で会社に移動する．
        // 次に，10時に発火するルールは，9時に会社に移動していないかつ，0.6の確率で会社に移動するため，0.5*0.6=0.3の確率で会社に移動するルールとなる．
        // 次に，11時に発火するルールは，10時に会社に移動していないかつ，1.0の確率で会社に移動するため，0.5*0.4*1.0=0.2の確率で会社に移動するルールとなる．
        new TRuleOfStochasticallyAgentMoving(RULE_NAME_OF_STOCHASTICALLY_LEAVE_HOME_9, this, home, company,
                ruleOfReturnHome, new TTime("8:00:00"), EStage.AgentMoving, 0.5)
                .setTimeAndStage(9, 0, 0, EStage.AgentMoving);
        new TRuleOfStochasticallyAgentMoving(RULE_NAME_OF_STOCHASTICALLY_LEAVE_HOME_10, this, home, company,
                ruleOfReturnHome, new TTime("8:00:00"), EStage.AgentMoving, 0.6)
                .setTimeAndStage(10, 0, 0, EStage.AgentMoving);
        new TRuleOfStochasticallyAgentMoving(RULE_NAME_OF_STOCHASTICALLY_LEAVE_HOME_11, this, home, company,
                ruleOfReturnHome, new TTime("8:00:00"), EStage.AgentMoving, 1.0)
                .setTimeAndStage(11, 0, 0, EStage.AgentMoving);
    }
}
