package jp.soars.ca.gol;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.soars.core.TRuleExecutor;
import jp.soars.core.TSOARSBuilder;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.enums.ERuleDebugMode;
import jp.soars.modules.onolab.cell.T2DCellSpaceMap;

/**
 * メインクラス
 * @author nagakane
 */
public class TMainOfOscillator {

    /**
     * ライフゲームのいくつかの振動子のシミュレーション
     * シミュレーションのメインループの標準出力部分は Ubuntu22.04 以外での動作確認をしていないため，環境に合わせて適宜変更してほしい．
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        // *************************************************************************************************************
        // TSOARSBuilderの必須設定項目
        //   - simulationStart:シミュレーション開始時刻
        //   - simulationEnd:シミュレーション終了時刻
        //   - tick:1ステップの時間間隔
        //   - stages:使用するステージリスト(実行順)
        //   - agentTypes:使用するエージェントタイプ集合
        //   - spotTypes:使用するスポットタイプ集合
        // *************************************************************************************************************

        String simulationStart = "0/00:00:00";
        String simulationEnd = "0/00:3:00";
        String tick = "00:00:01";
        List<Enum<?>> stages = List.of(EStage.CalculateNextState, EStage.StateTransition);
        Set<Enum<?>> agentTypes = new HashSet<>();
        Set<Enum<?>> spotTypes = new HashSet<>();
        Collections.addAll(spotTypes, ESpotType.values());
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes);

        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目．
        // *************************************************************************************************************

        // 定期実行ステージ設定
        builder.setPeriodicallyExecutedStage(EStage.CalculateNextState, simulationStart, tick);
        builder.setPeriodicallyExecutedStage(EStage.StateTransition, simulationStart, tick);

        // ログ出力設定
        String pathOfLogDir = "logs" + File.separator + "ca" + File.separator + "gol" + File.separator + "oscillator";
        builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv");
        builder.setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv");

        // ルールログのデバッグ情報出力設定
        builder.setRuleDebugMode(ERuleDebugMode.LOCAL);

        // 以下，最適化設定
        // 空間のサイズ
        int width = 60;
        int hight = 30;
        int noOfSpots = width * hight;

        builder.setExpectedNoOfSpots(ESpotType.Cell, noOfSpots);
        builder.setRulesNotShuffledBeforeExecuted(EStage.CalculateNextState);
        builder.setRulesNotShuffledBeforeExecuted(EStage.StateTransition);
        builder.setExpectedNoOfRulesPerStage(EStage.CalculateNextState, noOfSpots);
        builder.setExpectedNoOfRulesPerStage(EStage.StateTransition, noOfSpots);
        builder.setExpectedSizeOfTemporaryRulesMap(0);
        builder.setExpectedNoOfDeletedObjects(0);

        // *************************************************************************************************************
        // TSOARSBuilderでシミュレーションに必要なインスタンスの作成と取得．
        // *************************************************************************************************************

        builder.build();
        TRuleExecutor ruleExecutor = builder.getRuleExecutor();
        TSpotManager spotManager = builder.getSpotManager();

        // *************************************************************************************************************
        // スポット作成
        // *************************************************************************************************************

        List<TSpot> cells = spotManager.createSpots(ESpotType.Cell, noOfSpots, 2, 0);
        T2DCellSpaceMap map = new T2DCellSpaceMap(cells, width, hight);

        // 状態遷移役割設定
        for (TSpot cell : cells) {
            new TRoleOfStateTransition(cell, EState.DEATH);
            cell.activateRole(ERoleName.StateTransition);
        }

        // パルサーを構成するためのマッピング
        int[][] pulsar = new int[][]
                {{1,1,1,1,1},
                 {1,0,0,0,1}};
        // 上記のマッピングをコピー． 1 -> LIFE とする
        for (int i = 0, lenY = pulsar.length, y = map.getUpperBoundY() - 13; i < lenY; ++i, --y) {
            for (int j = 0, lenX = pulsar[i].length, x = map.getLowerBoundX() + 8; j < lenX; ++j, ++x) {
                if (pulsar[i][j] == 1) {
                    ((TRoleOfStateTransition) map.getCell(x, y).getRole(ERoleName.StateTransition)).setState(EState.LIFE);
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
        // 上記のマッピングをコピー． 1 -> LIFE とする
        for (int i = 0, lenY = galaxy.length, y = map.getUpperBoundY() - 9; i < lenY; ++i, --y) {
            for (int j = 0, lenX = galaxy[i].length, x = map.getLowerBoundX() + 25; j < lenX; ++j, ++x) {
                if (galaxy[i][j] == 1) {
                    ((TRoleOfStateTransition) map.getCell(x, y).getRole(ERoleName.StateTransition)).setState(EState.LIFE);
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
        // 上記のマッピングをコピー． 1 -> LIFE とする
        for (int i = 0, lenY = tumbler.length, y = map.getUpperBoundY() - 10; i < lenY; ++i, --y) {
            for (int j = 0, lenX = tumbler[i].length, x = map.getLowerBoundX() + 45; j < lenX; ++j, ++x) {
                if (tumbler[i][j] == 1) {
                    ((TRoleOfStateTransition) map.getCell(x, y).getRole(ERoleName.StateTransition)).setState(EState.LIFE);
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
            for (int y = map.getUpperBoundY(), lenY = map.getLowerBoundY(); lenY <= y; --y) {
                for (int x = map.getLowerBoundX(), lenX = map.getUpperBoundX(); x <= lenX; ++x) {
                    if (((TRoleOfStateTransition) map.getCell(x, y).getRole(ERoleName.StateTransition)).isState(EState.LIFE)) {
                        System.out.print("⬛︎");
                    } else {
                        System.out.print("⬜︎");
                    }
                }
                System.out.println();
            }
            // ディレイ 100ms
            Thread.sleep(100);
        } while (ruleExecutor.executeStep());

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown();
    }
}
