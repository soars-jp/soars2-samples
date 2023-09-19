package jp.soars.tutorials.sample06;

import jp.soars.core.TAgent;
import jp.soars.core.TRole;
import jp.soars.core.TRule;
import jp.soars.core.TSpot;

/**
 * 父親役割
 * @author nagakane
 */
public final class TRoleOfFather extends TRole {

    /** 健康状態決定ルール */
    private static final String RULE_NAME_OF_DETERMINING_HEALTH = "DeterminingHealth";

    /** 9時に自宅から会社に移動するルール名 */
    private static final String RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY_9 = "MoveFromHomeToCompany9";

    /** 10時に自宅から会社に移動するルール名 */
    private static final String RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY_10 = "MoveFromHomeToCompany10";

    /** 11時に自宅から会社に移動するルール名 */
    private static final String RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY_11 = "MoveFromHomeToCompany11";

    /** 会社から自宅に移動するルール名 */
    private static final String RULE_NAME_OF_MOVE_FROM_COMPANY_TO_HOME = "MoveFromCompanyToHome";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param home 自宅
     * @param company 会社
     */
    public TRoleOfFather(TAgent owner, TSpot home, TSpot company) {
        // 親クラスのコンストラクタを呼び出す．
        // 以下の2つの引数は省略可能で，その場合デフォルト値で設定される．
        // 第3引数:この役割が持つルール数 (デフォルト値 10)
        // 第4引数:この役割が持つ子役割数 (デフォルト値 5)
        super(ERoleName.Father, owner, 3, 0);

        // 役割が持つルールの登録
        // 健康状態決定ルール．6:00:00/健康状態決定ステージに定時実行ルールとして予約する．病人になる確率は25%(0.25)とする．
        new TRuleOfDeterminingHealth(RULE_NAME_OF_DETERMINING_HEALTH, this, 0.25, home)
                .setTimeAndStage(6, 0, 0, EStage.DeterminingHealth);

        // 会社から自宅に移動するルール．予約はTRuleOfMoveFromHomeToCompanyで相対時刻指定で行われる．
        TRule ruleOfReturnHome = new TRuleOfAgentMoving(RULE_NAME_OF_MOVE_FROM_COMPANY_TO_HOME, this, company, home);

        // 確率的に自宅から会社に移動するルール．9:00:00, 10:00:00, 11:00:00/エージェント移動ステージに定時実行ルールとして予約する．
        new TRuleOfStochasticallyAgentMovingOnWeekdays(RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY_9,
                this, 0.5, home, company, ruleOfReturnHome, "8:00:00", EStage.AgentMoving)
                .setTimeAndStage(9, 0, 0, EStage.AgentMoving);

        new TRuleOfStochasticallyAgentMovingOnWeekdays(RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY_10,
                this, 0.6, home, company, ruleOfReturnHome, "8:00:00", EStage.AgentMoving)
                .setTimeAndStage(10, 0, 0, EStage.AgentMoving);

        new TRuleOfStochasticallyAgentMovingOnWeekdays(RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY_11,
                this, 1.0, home, company, ruleOfReturnHome, "8:00:00", EStage.AgentMoving)
                .setTimeAndStage(11, 0, 0, EStage.AgentMoving);
    }
}
