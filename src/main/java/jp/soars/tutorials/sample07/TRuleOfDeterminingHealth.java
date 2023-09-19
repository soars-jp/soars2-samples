package jp.soars.tutorials.sample07;

import java.util.Map;

import jp.soars.core.TAgent;
import jp.soars.core.TAgentManager;
import jp.soars.core.TAgentRule;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

/**
 * 健康状態決定ルール
 * @author nagakane
 */
public final class TRuleOfDeterminingHealth extends TAgentRule {

    /** 病気になる確率[0, 1] */
    private final double fProbability;

    /** 自宅 */
    private final TSpot fHome;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールを持つ役割
     * @param probability 病気になる確率[0, 1]
     * @param home 自宅
     */
    public TRuleOfDeterminingHealth(String name, TRole owner, double probability, TSpot home) {
        super(name, owner);
        if (probability < 0.0 || 1.0 < probability) {
            throw new RuntimeException("Invalid probability. Probability value must be between 0 and 1.");
        }
        fProbability = probability;
        fHome = home;
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
        // 自宅にいる場合，確率に従って父親役割か子ども役割を非アクティブ化して病人役割をアクティブ化する．
        boolean debugFlag = true;
        if (isAt(fHome)) {
            if (getRandom().nextDouble() <= fProbability) {
                TAgent owner = getAgent();
                if (owner.getType() == EAgentType.Father) {
                    owner.deactivateRole(ERoleName.Father);
                } else if (owner.getType() == EAgentType.Child) {
                    owner.deactivateRole(ERoleName.Child);
                } else {
                    throw new RuntimeException("Unexpected agent type.");
                }
                owner.activateRole(ERoleName.SickPerson);
                appendToDebugInfo("get sick.", debugFlag);
            } else {
                appendToDebugInfo("Don't get sick. (probability)", debugFlag);
            }
        } else {
            appendToDebugInfo("Don't get sick. (spot)", debugFlag);
        }
    }
}
