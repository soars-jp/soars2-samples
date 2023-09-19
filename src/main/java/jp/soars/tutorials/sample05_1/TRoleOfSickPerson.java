package jp.soars.tutorials.sample05_1;

import jp.soars.core.TAgent;
import jp.soars.core.TRole;

/**
 * 病人役割
 * @author nagakane
 */
public final class TRoleOfSickPerson extends TRole {

    /** 病気から回復するルール名 */
    public static final String RULE_NAME_OF_RECOVERING_FROM_SICK = "RecoveringFromSick";

    /** 自宅から病院に移動するルール名 */
    private static final String RULE_NAME_OF_MOVE_FROM_HOME_TO_HOSPITAL = "MoveFromHomeToHospital";

    /** 病院から自宅に移動するルール名 */
    private static final String RULE_NAME_OF_MOVE_FROM_HOSPITAL_TO_HOME = "MoveFromHospitalToHome";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     */
    public TRoleOfSickPerson(TAgent owner) {
        super(ERoleName.SickPerson, owner, 3, 0);

        // 役割が持つルールの登録
        // 自宅から病院に移動するルール．10:00:00/エージェント移動ステージに定時実行ルールとして予約する．
        new TRuleOfMoveFromHomeToHospital(RULE_NAME_OF_MOVE_FROM_HOME_TO_HOSPITAL, this)
                .setTimeAndStage(10, 0, 0, EStage.AgentMoving);

        // 病院から自宅に移動するルール．12:00:00/エージェント移動ステージに定時実行ルールとして予約する．
        new TRuleOfMoveFromHospitalToHome(RULE_NAME_OF_MOVE_FROM_HOSPITAL_TO_HOME, this)
                .setTimeAndStage(12, 0, 0, EStage.AgentMoving);

        // 病気から回復するルール．12:00:00/病気回復ステージに定時実行ルールとして予約する．
        new TRuleOfRecoveringFromSick(RULE_NAME_OF_RECOVERING_FROM_SICK, this)
                .setTimeAndStage(12, 0, 0, EStage.RecoveringFromSick);
    }
}
