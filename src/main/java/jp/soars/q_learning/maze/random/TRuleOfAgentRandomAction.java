package jp.soars.q_learning.maze.random;

import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TAgentRule;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;
import jp.soars.modules.onolab.cell.ECellModuleRoleName;
import jp.soars.modules.onolab.cell.TRoleOf2DCell;

/**
 * エージェントの行動をランダムに選択する
 * 
 * @author nagakane
 */
public class TRuleOfAgentRandomAction extends TAgentRule {

    /**
     * コンストラクタ
     * 
     * @param name  ルール名
     * @param owner このルールを持つ役割
     */
    public TRuleOfAgentRandomAction(String name, TRole owner) {
        super(name, owner);
    }

    /**
     * ルールを実行する．
     * 
     * @param currentTime           現在時刻
     * @param currentStage          現在ステージ
     * @param spotManager           スポット管理
     * @param agentManager          エージェント管理
     * @param globalSharedVariables グローバル共有変数集合
     */
    @Override
    public final void doIt(TTime currentTime, Enum<?> currentStage, TSpotManager spotManager,
            TAgentManager agentManager, Map<String, Object> globalSharedVariables) {
        // ランダム行動選択．穴掘り法で作成した迷路なので，一回で2マス移動する．
        EAgentAction[] actions = EAgentAction.values();
        EAgentAction action = actions[getRandom().nextInt(actions.length)];

        // 報酬は ゴール 100, 通路 0, 壁 -1
        TRoleOfAgent role = (TRoleOfAgent) getOwnerRole();
        role.setAgentAction(action);

        // 1マス先スポット
        TSpot spot1 = null;
        switch (action) {
            case Up:
                spot1 = ((TRoleOf2DCell) getCurrentSpot().getRole(ECellModuleRoleName.Cell))
                        .getCellByRelativeCoordinates(0, 1);
                break;
            case Down:
                spot1 = ((TRoleOf2DCell) getCurrentSpot().getRole(ECellModuleRoleName.Cell))
                        .getCellByRelativeCoordinates(0, -1);
                break;
            case Right:
                spot1 = ((TRoleOf2DCell) getCurrentSpot().getRole(ECellModuleRoleName.Cell))
                        .getCellByRelativeCoordinates(1, 0);
                break;
            case Left:
                spot1 = ((TRoleOf2DCell) getCurrentSpot().getRole(ECellModuleRoleName.Cell))
                        .getCellByRelativeCoordinates(-1, 0);
                break;
        }
        // 1マス先が壁 -> 状態は変化なし，報酬 -1
        if (((TRoleOfMazeCell) spot1.getRole(ERoleName.MazeCell)).getMazeCellType() == EMazeCellType.Wall) {
            role.setReword(-1);
            return;
        }

        // 2マス先スポット
        TSpot spot2 = null;
        // 1マス先が壁ではないので移動可能なことは確定．-> 状態書き換え
        int[] state = role.getState();
        switch (action) {
            case Up:
                spot2 = ((TRoleOf2DCell) spot1.getRole(ECellModuleRoleName.Cell))
                        .getCellByRelativeCoordinates(0, 1);
                state[1] += 2;
                break;
            case Down:
                spot2 = ((TRoleOf2DCell) spot1.getRole(ECellModuleRoleName.Cell))
                        .getCellByRelativeCoordinates(0, -1);
                state[1] -= 2;
                break;
            case Right:
                spot2 = ((TRoleOf2DCell) spot1.getRole(ECellModuleRoleName.Cell))
                        .getCellByRelativeCoordinates(1, 0);
                state[0] += 2;
                break;
            case Left:
                spot2 = ((TRoleOf2DCell) spot1.getRole(ECellModuleRoleName.Cell))
                        .getCellByRelativeCoordinates(-1, 0);
                state[0] -= 2;
                break;
        }
        // 2マス先がゴール -> 報酬 100
        if (((TRoleOfMazeCell) spot2.getRole(ERoleName.MazeCell)).getMazeCellType() == EMazeCellType.Goal) {
            role.setReword(100);
        } else { // 2マス先が通路 -> 報酬 0
            role.setReword(0);
        }
        // エージェント移動
        moveTo(spot2);
    }
}
