package jp.soars.tutorials.sample04;

import jp.soars.core.TAgent;
import jp.soars.core.TRole;
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
        // 親クラスのコンストラクタを呼び出す．
        // 以下の2つの引数は省略可能で，その場合デフォルト値で設定される．
        // 第3引数 : この役割が持つルール数 (デフォルト値 10)
        // 第4引数 : この役割が持つ子役割数 (デフォルト値 5)
        super(ERoleName.Father, owner, 2, 0);

        fHome = home;
        fCompany = company;

        // 役割が持つルールの登録
        // 会社にいるならば，自宅に移動する．スケジューリングはTRuleOfLeaveHomeの中で行われる．
        TRuleOfMoveFromCompanyToHome returnHomeRule = new TRuleOfMoveFromCompanyToHome(RULE_NAME_OF_RETURN_HOME, this);

        // 自宅にいるならば，会社に移動する．9時(50%)，10時(30%)，11時(20%)のエージェント移動ステージに臨時実行ルールとして予約する．
        // 初日以降は，ルール自身が臨時実行ルールとして翌日の実行時間に再予約する．
        double p = getRandom().nextDouble(); // [0, 1]のdouble
        int hour = -1;
        if (p <= 0.5) {
            hour = 9; // 50%
        } else if (p <= 0.8) {
            hour = 10; // 30%
        } else {
            hour = 11; // 20%
        }
        new TRuleOfStochasticallyMoveFromCompanyToHome(RULE_NAME_OF_LEAVE_HOME, this, returnHomeRule, "8:00:00", EStage.AgentMoving)
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
