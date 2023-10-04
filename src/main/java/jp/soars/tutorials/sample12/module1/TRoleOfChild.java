package jp.soars.tutorials.sample12.module1;

import jp.soars.core.TAgent;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;

/**
 * 子ども役割
 * @author nagakane
 */
public final class TRoleOfChild extends TRole {

    /** 自宅から学校に移動するルール名 */
    public static final String RULE_NAME_OF_MOVE_FROM_HOME_TO_SCHOOL = "MoveFromHomeToSchool";

    /** 学校から自宅に移動するルール名 */
    public static final String RULE_NAME_OF_MOVE_FROM_SCHOOL_TO_HOME = "MoveFromSchoolToHome";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param home 自宅
     * @param school 学校
     */
    public TRoleOfChild(TAgent owner, TSpot home, TSpot school) {
        // 親クラスのコンストラクタを呼び出す．
        // 以下の2つの引数は省略可能で，その場合デフォルト値で設定される．
        // 第3引数:この役割が持つルール数 (デフォルト値 10)
        // 第4引数:この役割が持つ子役割数 (デフォルト値 5)
        super(EModule1RoleName.Child, owner, 2, 0);

        // 役割が持つルールの登録
        // 自宅から学校に移動するルール．8:00:00/エージェント移動ステージに定時実行ルールとして予約する．
        new TRuleOfAgentMovingOnWeekdays(RULE_NAME_OF_MOVE_FROM_HOME_TO_SCHOOL, this, home, school)
                .setTimeAndStage(8, 0, 0, EModule1Stage.AgentMoving);

        // 学校から自宅に移動するルール．15:00:00/エージェント移動ステージに定時実行ルールとして予約する．
        new TRuleOfAgentMoving(RULE_NAME_OF_MOVE_FROM_SCHOOL_TO_HOME, this, school, home)
                .setTimeAndStage(15, 0, 0, EModule1Stage.AgentMoving);
    }
}
