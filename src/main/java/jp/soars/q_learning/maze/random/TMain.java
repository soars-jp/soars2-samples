package jp.soars.q_learning.maze.random;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.soars.core.TAgent;
import jp.soars.core.TAgentManager;
import jp.soars.core.TRuleExecutor;
import jp.soars.core.TSOARSBuilder;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.enums.ERuleDebugMode;
import jp.soars.modules.onolab.cell.T2DCellSpaceMap;
import jp.soars.utils.random.ICRandom;

/**
 * メインクラス
 *
 * @author nagakane
 */
public class TMain {

    /**
     * シミュレーションのメインループの標準出力部分は Ubuntu22.04 以外での動作確認をしていないため，環境に合わせて適宜変更してほしい．
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        // *************************************************************************************************************
        // TSOARSBuilderの必須設定項目
        // - simulationStart:シミュレーション開始時刻
        // - simulationEnd:シミュレーション終了時刻
        // - tick:1ステップの時間間隔
        // - stages:使用するステージリスト(実行順)
        // - agentTypes:使用するエージェントタイプ集合
        // - spotTypes:使用するスポットタイプ集合
        // *************************************************************************************************************

        String simulationStart = "0/00:00:00";
        String simulationEnd = "0/00:5:00";
        String tick = "00:00:01";
        List<Enum<?>> stages = List.of(EStage.AgentAction);
        Set<Enum<?>> agentTypes = new HashSet<>();
        Collections.addAll(agentTypes, EAgentType.values());
        Set<Enum<?>> spotTypes = new HashSet<>();
        Collections.addAll(spotTypes, ESpotType.values());
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes);

        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目．
        // *************************************************************************************************************

        // 定期実行ステージ設定
        builder.setPeriodicallyExecutedStage(EStage.AgentAction, simulationStart, tick);

        // ログ出力設定
        String pathOfLogDir = "logs" + File.separator + "q_learning" + File.separator + "maze" + File.separator
                + "random";
        builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv");
        builder.setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv");

        // ルールログのデバッグ情報出力設定
        builder.setRuleDebugMode(ERuleDebugMode.LOCAL);

        // 以下，最適化設定
        // 空間のサイズ -> 穴掘り法で迷路作成するので 奇数 x 奇数
        int width = 19;
        int hight = 9;
        int noOfSpots = width * hight;

        builder.setExpectedNoOfAgents(EAgentType.Agent, 1);
        builder.setExpectedNoOfSpots(ESpotType.Cell, noOfSpots);
        builder.setRulesNotShuffledBeforeExecuted(EStage.AgentAction);
        builder.setExpectedNoOfRulesPerStage(EStage.AgentAction, 1);
        builder.setExpectedSizeOfTemporaryRulesMap(0);
        builder.setExpectedNoOfDeletedObjects(0);

        // *************************************************************************************************************
        // TSOARSBuilderでシミュレーションに必要なインスタンスの作成と取得．
        // *************************************************************************************************************

        builder.build();
        TRuleExecutor ruleExecutor = builder.getRuleExecutor();
        TAgentManager agentManager = builder.getAgentManager();
        TSpotManager spotManager = builder.getSpotManager();
        ICRandom random = builder.getRandom();

        // *************************************************************************************************************
        // スポット作成
        // *************************************************************************************************************

        List<TSpot> cells = spotManager.createSpots(ESpotType.Cell, noOfSpots, 2, 0);
        // 原点を基準に正の座標かつ，トーラスではないセル空間を作成．
        T2DCellSpaceMap map = new T2DCellSpaceMap(cells, 0, width - 1, 0, hight - 1, false, false);

        // 穴掘り法による迷路作成．デフォルトで(1, 1), (width - 2, hight - 2)がスタートとゴール．
        boolean[][] maze = TMazeGenerator.generate2DMaze(width, hight, random);

        // 迷路セル役割設定
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < hight; ++j) {
                if (maze[i][j]) {
                    new TRoleOfMazeCell(map.getCell(i, j), EMazeCellType.Aisle);
                } else {
                    new TRoleOfMazeCell(map.getCell(i, j), EMazeCellType.Wall);
                }
            }
        }
        // スタートとゴールはセルタイプ変更
        ((TRoleOfMazeCell) map.getCell(1, 1).getRole(ERoleName.MazeCell)).setMazeCellType(EMazeCellType.Start);
        ((TRoleOfMazeCell) map.getCell(width - 2, hight - 2).getRole(ERoleName.MazeCell))
                .setMazeCellType(EMazeCellType.Goal);

        // *************************************************************************************************************
        // エージェント作成
        // *************************************************************************************************************

        TAgent agent = agentManager.createAgent(EAgentType.Agent, 1);
        agent.initializeCurrentSpot(map.getCell(1, 1));
        TRoleOfAgent agentRole = new TRoleOfAgent(agent, 1, 1);
        agent.activateRole(ERoleName.Agent);

        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        do {
            // 画面クリア
            System.out.print("\033[H\033[2J");
            System.out.flush();
            // 画面表示
            System.out.print(ruleExecutor.getCurrentTime());
            System.out.print("\n行動:");
            System.out.print(agentRole.getAgentAction());
            System.out.print("\n状態:(");
            System.out.print(agentRole.getState()[0]);
            System.out.print(", ");
            System.out.print(agentRole.getState()[1]);
            System.out.print(")\n報酬:");
            System.out.println(agentRole.getReword());
            for (int y = map.getUpperBoundY(), lenY = map.getLowerBoundY(); lenY <= y; --y) {
                for (int x = map.getLowerBoundX(), lenX = map.getUpperBoundX(); x <= lenX; ++x) {
                    TSpot spot = map.getCell(x, y);
                    if (spot.getAgents().size() != 0) { // エージェントがいるセル
                        System.out.print("👦");
                        continue;
                    }
                    EMazeCellType mazeCellType = ((TRoleOfMazeCell) spot.getRole(ERoleName.MazeCell)).getMazeCellType();
                    if (mazeCellType == EMazeCellType.Wall) {
                        System.out.print("⬛︎");
                    } else if (mazeCellType == EMazeCellType.Aisle) {
                        System.out.print("⬜︎");
                    } else if (mazeCellType == EMazeCellType.Start) {
                        System.out.print("🟦");
                    } else if (mazeCellType == EMazeCellType.Goal) {
                        System.out.print("🟥");
                    }
                }
                System.out.println();
            }
            // ディレイ 500ms
            Thread.sleep(500);
        } while (ruleExecutor.executeStep());

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown();
    }
}
