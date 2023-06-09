package jp.soars.samples.sample11;

import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TAgentRule;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;
import jp.soars.samples.sample11.module3.ERoleName;

/**
 * 健康状態決定ルール
 * @author nagakane
 */
public final class TRuleOfDeterminingHealth extends TAgentRule {

    /** 発火スポット条件 */
    private final TSpot fSpot;

    /** 病気になる確率 */
    private final double fProbability;

    /** 病人役割に切り替える時にディアクティブ化する役割 */
    private final Enum<?> fDeactivateRole;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールを持つ役割
     * @param spot 発火スポット
     * @param probability 病気になる確率
     * @param deactivateRole 病人役割に切り替える時にディアクティブ化する役割
     */
    public TRuleOfDeterminingHealth(String name, TRole owner, TSpot spot, double probability, Enum<?> deactivateRole) {
        super(name, owner);
        fSpot = spot;
        fProbability = probability;
        fDeactivateRole = deactivateRole;
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
        if (isAt(fSpot) && (getRandom().nextDouble() <= fProbability)) {// スポット条件および確率条件が満たされたら
            // 父親の場合は父親役割，子どもの場合は子ども役割を無効化する．
            if (fDeactivateRole != null) {
                getAgent().deactivateRole(fDeactivateRole);
            }
            // 病人役割を有効化する．
            getAgent().activateRole(ERoleName.SickPerson);
        }
    }
}
