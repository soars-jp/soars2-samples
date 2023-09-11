package jp.soars.tutorials.sample14;

import jp.soars.core.TAgent;
import jp.soars.core.TRole;
import jp.soars.core.TRule;
import jp.soars.core.TSpot;
import jp.soars.core.TTime;

/**
 * 病人役割
 * @author nagakane
 */
public final class TRoleOfSickPerson extends TRole {

    /** 病院に行くルール名 */
    public static final String RULE_NAME_OF_GO_HOSPITAL = "GoHospital";

    /** 病気から回復するルール名 */
    public static final String RULE_NAME_OF_RECOVERING_FROM_SICK = "RecoveringFromSick";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param home 自宅
     * @param hospital 病院
     * @param treatmentTime 治療時間
     * @param activatedRole 病人役割から回復するときにアクティブ化される役割
     */
    public TRoleOfSickPerson(TAgent owner, TSpot home, TSpot hospital, TTime treatmentTime, Enum<?> activatedRole) {
        super(ERoleName.SickPerson, owner, 2, 0);

        // 病気から回復して帰宅する．スケジューリングは，RULE_NAME_OF_GO_HOSPITALで行われる．
        TRule ruleOfRecoveringFromSick = new TRuleOfRecoveringFromSick(RULE_NAME_OF_RECOVERING_FROM_SICK, this,
                home, hospital, activatedRole);

        // 10時に自宅から病院に移動する．診察時間が過ぎたあと，ruleOfRecoveringFromSickを臨時実行ルールとしてスケジューリングする．
        new TRuleOfAgentMoving(RULE_NAME_OF_GO_HOSPITAL, this, home, hospital,
                ruleOfRecoveringFromSick, treatmentTime, EStage.AgentMoving)
                .setTimeAndStage(10, 0, 0, EStage.AgentMoving);
    }
}
