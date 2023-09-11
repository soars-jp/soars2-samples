package jp.soars.ca.gol;

import java.util.List;
import java.util.Map;

import jp.soars.core.TAgent;
import jp.soars.core.TAgentManager;
import jp.soars.core.TRole;
import jp.soars.core.TRule;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

/**
 * 近傍エージェントの状態チェックと次の状態決定ルール
 * @author nagakane
 */
public final class TRuleOfCalculateNextState extends TRule {

    /** ムーア近傍エージェントリスト */
    private final List<TAgent> fNeighborhoods;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     * @param neighborhoods ムーア近傍にいるエージェントリスト．
     */
    public TRuleOfCalculateNextState(String name, TRole owner, List<TAgent> neighborhoods) {
        // 親クラスのコンストラクタを呼び出す．
        super(name, owner);
        fNeighborhoods = neighborhoods;
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
        int noOfAgentsAlive = 0; // 生きているエージェントの数
        for (TAgent agent : fNeighborhoods) {
            // ordinal() は DEATH -> 0, LIFE -> 1
            noOfAgentsAlive += ((TRoleOfCell) agent.getRole(ERoleName.Cell)).getState().ordinal();
        }

        boolean debugFlag = true; // デバッグ情報出力フラグ
        TRoleOfCell role = (TRoleOfCell) getRole(ERoleName.Cell);
        EState state = role.getState();
        appendToDebugInfo("state:" + state + " noOfAgentsAlive:" + noOfAgentsAlive, debugFlag);
        if (state == EState.DEATH) { // 自分自身が死んでいる場合
            if (noOfAgentsAlive == 3) {
                role.setNextState(EState.LIFE);
                appendToDebugInfo(" nextState:" + EState.LIFE, debugFlag);
            } else {
                role.setNextState(EState.DEATH);
                appendToDebugInfo(" nextState:" + EState.DEATH, debugFlag);
            }
        } else { // 自分自身が生きている場合
            if (noOfAgentsAlive == 2 || noOfAgentsAlive == 3) {
                role.setNextState(EState.LIFE);
                appendToDebugInfo(" nextState:" + EState.LIFE, debugFlag);
            } else {
                role.setNextState(EState.DEATH);
                appendToDebugInfo(" nextState:" + EState.DEATH, debugFlag);
            }
        }
    }
}
