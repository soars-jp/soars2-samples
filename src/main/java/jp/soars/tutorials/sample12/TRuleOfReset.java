package jp.soars.samples.sample12;

import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TRole;
import jp.soars.core.TRule;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

public final class TRuleOfReset extends TRule {

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールを持つ役割
     */
    public TRuleOfReset(String name, TRole owner) {
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
        ((TRoleOfSpotWithCapacity) getRole(ERoleName.SpotWithCapacity)).getTemporaryAgents().clear();
    }
}
