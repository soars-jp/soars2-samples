package jp.soars.tutorials.sample12.module2;

import jp.soars.core.TAgent;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;

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
     * @param home 自宅
     * @param hospital 病院
     * @param medicHour 診察時間(病院滞在時間)
     * @param originalRoleName 元の役割．病気から回復した時にアクティブ化する．
     */
    public TRoleOfSickPerson(TAgent owner, TSpot home, TSpot hospital, int medicHour, Enum<?> originalRoleName) {
        super(EModule2RoleName.SickPerson, owner, 3, 0);

        // 役割が持つルールの登録
        // 自宅から病院に移動するルール．10:00:00/エージェント移動ステージに定時実行ルールとして予約する．
        new TRuleOfAgentMoving(RULE_NAME_OF_MOVE_FROM_HOME_TO_HOSPITAL, this, home, hospital)
                .setTimeAndStage(10, 0, 0, EModule2Stage.AgentMoving);

        // 病院から自宅に移動するルール．(12,13):00:00/エージェント移動ステージに定時実行ルールとして予約する．
        new TRuleOfAgentMoving(RULE_NAME_OF_MOVE_FROM_HOSPITAL_TO_HOME, this, hospital, home)
                .setTimeAndStage(10 + medicHour, 0, 0, EModule2Stage.AgentMoving);

        // 病気から回復するルール．(12,13):00:00/病気回復ステージに定時実行ルールとして予約する．
        new TRuleOfRecoveringFromSick(RULE_NAME_OF_RECOVERING_FROM_SICK, this, originalRoleName)
                .setTimeAndStage(10 + medicHour, 0, 0, EModule2Stage.RecoveringFromSick);
    }
}
