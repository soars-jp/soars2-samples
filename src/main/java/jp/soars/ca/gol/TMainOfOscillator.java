package jp.soars.ca.gol;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.soars.core.TAgent;
import jp.soars.core.TAgentManager;
import jp.soars.core.TRuleExecutor;
import jp.soars.core.TSOARSBuilder;
import jp.soars.core.enums.ERuleDebugMode;

/**
 * メインクラス
 * @author nagakane
 */
public class TMainOfOscillator {

    /**
     * ライフゲームのいくつかの振動子のシミュレーション
     * 作者実行環境 Ubuntu22.04 以外で実行確認していないのであしからず．(特に標準出力画面をクリアする部分)
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        // *************************************************************************************************************
        // TSOARSBuilderの必須設定項目．
        //   - simulationStart: シミュレーション開始時刻
        //   - simulationEnd: シミュレーション終了時刻
        //   - tick: 1ステップの時間間隔
        //   - stages: 使用するステージリスト
        //   - agentTypes: 使用するエージェントタイプ集合
        //   - spotTypes: 使用するスポットタイプ集合
        // *************************************************************************************************************

        String simulationStart = "0/00:00:00"; // シミュレーション開始時刻
        String simulationEnd = "0/00:05:00"; // シミュレーション終了時刻
        String tick = "00:00:01"; // １ステップの時間間隔
        List<Enum<?>> stages = List.of(EStage.CalculateNextState, EStage.UpdateCell); // ステージリスト
        Set<Enum<?>> agentTypes = new HashSet<>(); // 全エージェントタイプ
        Set<Enum<?>> spotTypes = new HashSet<>(); // 全スポットタイプ
        Collections.addAll(agentTypes, EAgentType.values()); // EAgentType に登録されているエージェントタイプをすべて追加
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes); // ビルダー作成

        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目．
        // *************************************************************************************************************

        // 定期実行ステージ設定
        builder.setPeriodicallyExecutedStage(EStage.CalculateNextState, simulationStart, tick)
               .setPeriodicallyExecutedStage(EStage.UpdateCell, simulationStart, tick);

        // 並列化設定
        int noOfThreads = 10;
        builder.setParallelizationStages(noOfThreads, EStage.CalculateNextState, EStage.UpdateCell);

        // ログ出力設定
        String pathOfLogDir = "logs" + File.separator + "ca" + File.separator + "gol"; // ログディレクトリ
        builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv") // ルールログ出力設定
               .setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv"); // ランタイムログ出力設定

        // ルールのシャッフルオフ
        builder.setRulesNotShuffledBeforeExecuted(EStage.CalculateNextState)
               .setRulesNotShuffledBeforeExecuted(EStage.UpdateCell);

        // ルールログのデバッグ情報出力設定
        builder.setRuleDebugMode(ERuleDebugMode.LOCAL); // ローカル設定に従う

        // 空間のサイズ
        int width = 60;
        int hight = 30;
        int noOfAgents = (width + 2) * (hight + 2); // 領域の外側に番人が必要なことに注意
        int noOfUpdatedAgents = width * hight; // 状態のアップデートが実行されるエージェント数．(番人以外)

        // 以下最適化設定
        builder.setExpectedNoOfAgents(EAgentType.Cell, noOfAgents);
        builder.setExpectedNoOfRulesPerStage(EStage.CalculateNextState, noOfUpdatedAgents);
        builder.setExpectedNoOfRulesPerStage(EStage.UpdateCell, noOfUpdatedAgents);
        builder.setFlagOfCreatingRandomForEachAgent(false);
        builder.setExpectedSizeOfTemporaryRulesMap(0);
        builder.setExpectedNoOfDeletedObjects(0);

        // *************************************************************************************************************
        // TSOARSBuilderでシミュレーションに必要なインスタンスの作成と取得．
        // *************************************************************************************************************

        builder.build(); // インスタンスのビルド
        TRuleExecutor ruleExecutor = builder.getRuleExecutor(); // ルール実行器
        TAgentManager agentManager = builder.getAgentManager(); // エージェント管理

        // *************************************************************************************************************
        // エージェント作成
        // *************************************************************************************************************

        List<TAgent> agents = agentManager.createAgents(EAgentType.Cell, noOfAgents, 1);
        List<TAgent> updatedAgents = new ArrayList<>(noOfUpdatedAgents); // アップデートされるエージェントリスト
        int width2 = width + 2;
        int tmp1 = (width + 2) * (hight + 1);
        for (int i = 0; i < noOfAgents; ++i) {
            if (i % width2 == 0 || (i + 1) % width2 == 0 || i <= width || tmp1 < i) { // 番人の条件
                TAgent agent = agents.get(i);
                // 役割生成
                new TRoleOfCell(agent, null);
            } else { // 番人ではない
                TAgent agent = agents.get(i);
                updatedAgents.add(agent);
                List<TAgent> neighborhoods = new ArrayList<>(8);
                // 上段
                int tmp = i - width2;
                neighborhoods.add(agents.get(tmp - 1));
                neighborhoods.add(agents.get(tmp));
                neighborhoods.add(agents.get(tmp + 1));
                // 左右
                neighborhoods.add(agents.get(i - 1));
                neighborhoods.add(agents.get(i + 1));
                // 下段
                tmp = i + width2;
                neighborhoods.add(agents.get(tmp - 1));
                neighborhoods.add(agents.get(tmp));
                neighborhoods.add(agents.get(tmp + 1));

                // 役割生成
                new TRoleOfCell(agent, neighborhoods);
                agent.activateRole(ERoleName.Cell);
            }
        }

        // パルサーを構成するためのマッピング
        int[][] pulsar = new int[][]
                {{1,1,1,1,1},
                 {1,0,0,0,1}};
        // (13, 8) を基準に上記のマッピングをコピー． 1 -> LIFE とする
        int ul = 13 * width + 8; // (13, 8)のインデックス
        for (int h = 0, len1 = pulsar.length; h < len1; ++h) {
            int[] indexes = pulsar[h];
            int sIndex = ul + h * width; // マッピング1行の開始インデックス
            for (int w = 0, len2 = indexes.length; w < len2; ++w) {
                if (pulsar[h][w] == 1) {
                    ((TRoleOfCell) updatedAgents.get(sIndex + w).getRole(ERoleName.Cell)).setState(EState.LIFE);
                }
            }
        }

        // 銀河を構成するためのマッピング
        int[][] galaxy = new int[][]
                {{1,1,0,1,1,1,1,1,1},
                 {1,1,0,1,1,1,1,1,1},
                 {1,1,0,0,0,0,0,0,0},
                 {1,1,0,0,0,0,0,1,1},
                 {1,1,0,0,0,0,0,1,1},
                 {1,1,0,0,0,0,0,1,1},
                 {0,0,0,0,0,0,0,1,1},
                 {1,1,1,1,1,1,0,1,1},
                 {1,1,1,1,1,1,0,1,1}};
        // (9, 25) を基準に上記のマッピングをコピー． 1 -> LIFE とする
        ul = 9 * width + 25; // (9, 25)のインデックス
        for (int h = 0, len1 = galaxy.length; h < len1; ++h) {
            int[] indexes = galaxy[h];
            int sIndex = ul + h * width; // マッピング1行の開始インデックス
            for (int w = 0, len2 = indexes.length; w < len2; ++w) {
                if (galaxy[h][w] == 1) {
                    ((TRoleOfCell) updatedAgents.get(sIndex + w).getRole(ERoleName.Cell)).setState(EState.LIFE);
                }
            }
        }

        // タンブラーを構成するためのマッピング
        int[][] tumbler = new int[][]
                {{0,1,1,0,1,1,0},
                 {0,1,1,0,1,1,0},
                 {0,0,1,0,1,0,0},
                 {1,0,1,0,1,0,1},
                 {1,0,1,0,1,0,1},
                 {1,1,0,0,0,1,1}};
        // (10, 45) を基準に上記のマッピングをコピー． 1 -> LIFE とする
        ul = 10 * width + 45; // (10, 45)のインデックス
        for (int h = 0, len1 = tumbler.length; h < len1; ++h) {
            int[] indexes = tumbler[h];
            int sIndex = ul + h * width; // マッピング1行の開始インデックス
            for (int w = 0, len2 = indexes.length; w < len2; ++w) {
                if (tumbler[h][w] == 1) {
                    ((TRoleOfCell) updatedAgents.get(sIndex + w).getRole(ERoleName.Cell)).setState(EState.LIFE);
                }
            }
        }
        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        do {
            // 画面クリア
            System.out.print("\033[H\033[2J");
            System.out.flush();
            // 画面表示
            System.out.println(ruleExecutor.getCurrentTime());
            for (int i = 0; i < noOfUpdatedAgents; ++i) {
                if (((TRoleOfCell) updatedAgents.get(i).getRole(ERoleName.Cell)).getState() == EState.LIFE) {
                    System.out.print("⬛︎");
                } else {
                    System.out.print("⬜︎");
                }
                if ((i + 1) % width == 0) {
                    System.out.println();
                }
            }
            // ディレイ 100ms
            Thread.sleep(100);
        } while (ruleExecutor.executeStep()); // 1ステップ分のルールを実行

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown(); // ルール実行器を終了する
    }
}
