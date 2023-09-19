package jp.soars.tutorials.sample07;

import java.util.Map;

import jp.soars.core.TAgent;
import jp.soars.core.TAgentManager;
import jp.soars.core.TAgentRule;
import jp.soars.core.TRole;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

/**
 * 病気から回復するルール
 * @author nagakane
 */
public final class TRuleOfRecoveringFromSick extends TAgentRule {

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     */
    public TRuleOfRecoveringFromSick(String name, TRole owner) {
        // 親クラスのコンストラクタを呼び出す．
        super(name, owner);
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
        // 病人役割を非アクティブ化して父親役割か子ども役割をアクティブ化する．
        TAgent owner = getAgent();
        owner.deactivateRole(ERoleName.SickPerson);
        if (owner.getType() == EAgentType.Father) {
            owner.activateRole(ERoleName.Father);
        } else if (owner.getType() == EAgentType.Child) {
            owner.activateRole(ERoleName.Child);
        } else {
            throw new RuntimeException("Unexpected agent type.");
        }
    }
}
