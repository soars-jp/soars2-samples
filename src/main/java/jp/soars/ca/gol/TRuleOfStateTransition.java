package jp.soars.ca.gol;

import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TRole;
import jp.soars.core.TRule;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

/**
 * 状態遷移ルール
 * @author nagakane
 */
public final class TRuleOfStateTransition extends TRule {

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     */
    public TRuleOfStateTransition(String name, TRole owner) {
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
        ((TRoleOfStateTransition) getRole(ERoleName.StateTransition)).stateTransition();; // 状態遷移
    }
}
