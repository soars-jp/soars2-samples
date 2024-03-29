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
public class TMainOfGliderGun {

    /**
     * ライフゲームのグライダー銃パターンのシミュレーション
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
        String pathOfLogDir = "logs" + File.separator + "ca" + File.separator + "gol" + File.separator + "glider_gun";
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

        // グライダー銃を構成するためのマッピング
        int[][] gliderGun = new int[][]
                {{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0},
                 {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0},
                 {0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1},
                 {0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1},
                 {1,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                 {1,1,0,0,0,0,0,0,0,0,1,0,0,0,1,0,1,1,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0},
                 {0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0},
                 {0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                 {0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}};
        // 左上を基準に上記のマッピングをコピー． 1 -> LIFE とする
        for (int i = 0, lenY = gliderGun.length, y = map.getUpperBoundY(); i < lenY; ++i, --y) {
            for (int j = 0, lenX = gliderGun[i].length, x = map.getLowerBoundX(); j < lenX; ++j, ++x) {
                if (gliderGun[i][j] == 1) {
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
