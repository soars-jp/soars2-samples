package jp.soars.ca.gol;

import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TRole;
import jp.soars.core.TRule;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;
import jp.soars.modules.onolab.space.ESpaceRoleName;
import jp.soars.modules.onolab.space.TRoleOf2DCell;

/**
 * 近傍エージェントの状態チェックと次の状態決定ルール
 * @author nagakane
 */
public final class TRuleOfCalculateNextState extends TRule {

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     */
    public TRuleOfCalculateNextState(String name, TRole owner) {
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
        int noOfAgentsAlive = 0; // 生きているエージェントの数
        // 2次元セル役割を取得
        TRoleOf2DCell roleOf2DCell = (TRoleOf2DCell) getRole(ESpaceRoleName.Cell);
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                if (x == 0 && y == 0) { // (0, 0)は自分自身なのでスキップ
                    continue;
                }
                // 近傍セルの TRoleOfStateTransition 役割から現在の状態取得．
                // ordinal() は DEATH -> 0, LIFE -> 1
                noOfAgentsAlive += ((TRoleOfStateTransition) roleOf2DCell
                        .getNeighborhood(x, y)
                        .getRole(ERoleName.StateTransition))
                        .getState()
                        .ordinal();
            }
        }

        boolean debugFlag = true; // デバッグ情報出力フラグ
        TRoleOfStateTransition role = (TRoleOfStateTransition) getRole(ERoleName.StateTransition);
        EState state = role.getState();
        appendToDebugInfo("state:" + state + " noOfAgentsAlive:" + noOfAgentsAlive, debugFlag);
        // ライフゲームの状態遷移定義
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
