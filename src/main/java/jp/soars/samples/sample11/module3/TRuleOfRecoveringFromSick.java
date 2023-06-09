package jp.soars.samples.sample11.module3;

import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TAgentRule;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

/**
 * 病気から回復して，帰宅するルール
 * @author nagakane
 */
public final class TRuleOfRecoveringFromSick extends TAgentRule {

    /** 自宅 */
    private final TSpot fHome;

    /** 病院 */
    private final TSpot fHospital;

    /** 病人役割から回復するときに切り替える役割 */
    private final Enum<?> fActivateRole;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     * @param home 自宅
     * @param hospital 病院
     * @param activateRole 病人役割から回復するときに切り替える役割
     */
    public TRuleOfRecoveringFromSick(String name, TRole owner, TSpot home, TSpot hospital, Enum<?> activateRole) {
        super(name, owner);
        fHome = home;
        fHospital = hospital;
        fActivateRole = activateRole;
    }

    /**
     * ルールを実行する．
     * @param currentTime 現在時刻
     * @param currentStage 現在ステージ
     * @param spotManager スポット管理
     * @param agentManager エージェント管理
     * @param globalSharedVariables グローバル共有変数集合
     */
    @Override
    public final void doIt(TTime currentTime, Enum<?> currentStage, TSpotManager spotManager,
            TAgentManager agentManager, Map<String, Object> globalSharedVariables) {
        if (isAt(fHospital)) { // 病院にいるなら
            moveTo(fHome); // 自宅へ移動する

            // 病人役割を無効化する．
            getAgent().deactivateRole(ERoleName.SickPerson);
            // アクティブ化する役割が設定されている場合はアクティブ化
            if (fActivateRole != null) {
                getAgent().activateRole(fActivateRole);
            }
        }
    }
}
