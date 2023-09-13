package jp.soars.tutorials.sample03;

import jp.soars.core.TAgent;
import jp.soars.core.TRole;
import jp.soars.core.TRule;
import jp.soars.core.TSpot;

/**
 * 父親役割
 * @author nagakane
 */
public final class TRoleOfFather extends TRole {

    /** 自宅 */
    private final TSpot fHome;

    /** 会社 */
    private final TSpot fCompany;

    /** 自宅から会社に移動するルール名 */
    private static final String RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY = "MoveFromHomeToCompany";

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
        super(ERoleName.Father, owner, 2, 0);

        fHome = home;
        fCompany = company;

        // 役割が持つルールの登録
        // 会社から自宅に移動するルール．予約はTRuleOfStochasticallyMoveFromHomeToCompanyで相対時刻指定で行われる．
        TRule ruleOfReturnHome = new TRuleOfMoveFromCompanyToHome(RULE_NAME_OF_MOVE_FROM_COMPANY_TO_HOME, this);

        // 自宅から会社に移動するルール．初日の9時(50%)，10時(30%)，11時(20%)/エージェント移動ステージに臨時実行ルールとして予約する．
        // 初日以降は，ルール自身が臨時実行ルールとして再予約する．
        double p = getRandom().nextDouble(); // [0, 1]のdouble
        int hour;
        if (p <= 0.5) {
            hour = 9; // 50%
        } else if (p <= 0.8) {
            hour = 10; // 30%
        } else {
            hour = 11; // 20%
        }
        new TRuleOfStochasticallyMoveFromHomeToCompany(RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY, this,
                ruleOfReturnHome, "8:00:00", EStage.AgentMoving)
                .setTimeAndStage(0, hour, 0, 0, EStage.AgentMoving);
    }

    /**
     * 自宅を返す．
     * @return 自宅
     */
    public final TSpot getHome() {
        return fHome;
    }

    /**
     * 会社を返す．
     * @return 会社
     */
    public final TSpot getCompany() {
        return fCompany;
    }
}
