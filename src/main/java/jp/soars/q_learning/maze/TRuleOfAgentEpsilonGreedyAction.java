package jp.soars.q_learning.maze;

import java.util.HashMap;
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
 * エージェントの行動をGreedyに選択する
 *
 * @author nishikubo
 */
public class TRuleOfAgentEpsilonGreedyAction extends TAgentRule {

    // Q関数
    private Map<int[], Map<EAgentAction, Double>> fQMap;

    // 状態行動対への訪問回数
    private Map<int[], Map<EAgentAction, Integer>> fAlphaMap;

    // 確率 ε
    private double fEpsilon;

    // 割引率
    private double fGamma;

    /**
     * コンストラクタ
     *
     * @param name  ルール名
     * @param owner このルールを持つ役割
     */
    public TRuleOfAgentEpsilonGreedyAction(String name, TRole owner) {
        this(name, owner, 0.6, 0.99);
    }

    /**
     * コンストラクタ
     *
     * @param name    ルール名
     * @param owner   このルールを持つ役割
     * @param epsilon 確率 ε
     * @param gamma   割引率
     */
    public TRuleOfAgentEpsilonGreedyAction(String name, TRole owner, double epsilon, double gamma) {
        super(name, owner);
        fQMap = new HashMap<>();
        fAlphaMap = new HashMap<>();
        fEpsilon = epsilon;
        fGamma = gamma;
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
        // 穴掘り法で作成した迷路なので，一回で2マス移動する．
        EAgentAction[] actions = EAgentAction.values();

        // 報酬は ゴール 100, 通路 0, 壁 -1
        TRoleOfEpsilonGreedyAgent role = (TRoleOfEpsilonGreedyAgent) getOwnerRole();
        int[] state = role.getState();

        // Q学習のための初期化
        fQMap.putIfAbsent(state, new HashMap<>());
        fAlphaMap.putIfAbsent(state, new HashMap<>());
        for (EAgentAction a : actions) {
            fQMap.get(state).putIfAbsent(a, 0.0);
            fAlphaMap.get(state).putIfAbsent(a, 0);
        }

        EAgentAction action;
        if (getRandom().nextDouble() < fEpsilon) {
            // 確率εでランダムな行動を選択
            action = actions[getRandom().nextInt(actions.length)];
        } else {
            // 最良の行動を選択
            action = fQMap.get(state).entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
        }
        role.setAgentAction(action);

        int reward = 0; // 報酬
        int done = 0; // 終了判定

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
            reward = -1;
            done = 1;
        } else {
            // 2マス先スポット
            TSpot spot2 = null;
            // 1マス先が壁ではないので移動可能なことは確定．-> 状態書き換え
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
                done = 1;
                reward = 100;
            } else { // 2マス先が通路 -> 報酬 0
                done = 0;
                reward = 0;
            }
            // エージェント移動
            moveTo(spot2);
        }
        role.setReward(reward); // 報酬の獲得
        // 次の状態
        int[] nextState = role.getState();
        // 次の状態行動対のQ関数の初期化
        fQMap.putIfAbsent(nextState, new HashMap<>());
        for (EAgentAction a : actions) {
            fQMap.get(nextState).putIfAbsent(a, 0.0);
        }
        // 状態行動対への訪問回数をインクリメント
        fAlphaMap.get(state).put(action, fAlphaMap.get(state).get(action) + 1);
        // ステップサイズ
        double alpha = 1.0 / (double) fAlphaMap.get(state).get(action);
        // Q関数の更新
        fQMap.get(state).put(action,
                fQMap.get(state).get(action) + alpha * ((double) reward + fGamma * (double) (1 - done)
                        * fQMap.get(nextState).entrySet().stream().max(Map.Entry.comparingByValue()).get()
                                .getValue()
                        - fQMap.get(state).get(action)));

    }
}
