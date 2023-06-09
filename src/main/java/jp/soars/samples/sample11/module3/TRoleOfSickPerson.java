package jp.soars.samples.sample11.module3;

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
     * @param medicTTime 診察時間
     * @param activateRole 病人役割から回復するときに切り替える役割
     */
    public TRoleOfSickPerson(TAgent owner, TSpot home, TSpot hospital, TTime medicTTime, Enum<?> activateRole) {
        super(ERoleName.SickPerson, owner, 2, 0);

        // 病気から回復して帰宅する．スケジューリングは，RULE_NAME_OF_GO_HOSPITALで行われる．
        TRule ruleOfRecoveringFromSick = new TRuleOfRecoveringFromSick(RULE_NAME_OF_RECOVERING_FROM_SICK, this,
                home, hospital, activateRole);

        // 10時に自宅から病院に移動する．診察時間が過ぎたあと，ruleOfRecoveringFromSickを臨時実行ルールとしてスケジューリングする．
        new TRuleOfAgentMoving(RULE_NAME_OF_GO_HOSPITAL, this, home, hospital,
                ruleOfRecoveringFromSick, medicTTime, EStage.AgentMoving)
                .setTimeAndStage(10, 0, 0, EStage.AgentMoving);
    }
}
