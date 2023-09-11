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
import jp.soars.modules.onolab.space.T2DCellSpaceMap;

/**
 * メインクラス
 * @author nagakane
 */
public class TMainOfGliderGun {

    /**
     * ライフゲームのグライダー銃パターンのシミュレーション
     * 作者実行環境 Ubuntu22.04 以外で実行確認していないのであしからず．(特に標準出力画面をクリアする部分)
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
        String simulationEnd = "0/00:3:00"; // シミュレーション終了時刻
        String tick = "00:00:01"; // １ステップの時間間隔
        List<Enum<?>> stages = List.of(EStage.CalculateNextState, EStage.StateTransition); // ステージリスト
        Set<Enum<?>> agentTypes = new HashSet<>(); // 全エージェントタイプ
        Set<Enum<?>> spotTypes = new HashSet<>(); // 全スポットタイプ
        Collections.addAll(spotTypes, ESpotType.values()); // ESpotType に登録されているスポットタイプをすべて追加
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes); // ビルダー作成

        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目．
        // *************************************************************************************************************

        // 定期実行ステージ設定
        builder.setPeriodicallyExecutedStage(EStage.CalculateNextState, simulationStart, tick)
               .setPeriodicallyExecutedStage(EStage.StateTransition, simulationStart, tick);

        // ログ出力設定
        String pathOfLogDir = "logs" + File.separator + "ca" + File.separator + "gol" + File.separator + "glider_gun"; // ログディレクトリ
        builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv") // ルールログ出力設定
               .setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv"); // ランタイムログ出力設定

        // ルールのシャッフルオフ
        builder.setRulesNotShuffledBeforeExecuted(EStage.CalculateNextState)
               .setRulesNotShuffledBeforeExecuted(EStage.StateTransition);

        // ルールログのデバッグ情報出力設定
        builder.setRuleDebugMode(ERuleDebugMode.LOCAL); // ローカル設定に従う

        // 空間のサイズ
        int width = 60;
        int hight = 30;
        int noOfSpots = width * hight;

        // 以下最適化設定
        builder.setExpectedNoOfSpots(ESpotType.Cell, noOfSpots);
        builder.setExpectedNoOfRulesPerStage(EStage.CalculateNextState, noOfSpots);
        builder.setExpectedNoOfRulesPerStage(EStage.StateTransition, noOfSpots);
        builder.setFlagOfCreatingRandomForEachSpot(false);
        builder.setExpectedSizeOfTemporaryRulesMap(0);
        builder.setExpectedNoOfDeletedObjects(0);

        // *************************************************************************************************************
        // TSOARSBuilderでシミュレーションに必要なインスタンスの作成と取得．
        // *************************************************************************************************************

        builder.build(); // インスタンスのビルド
        TRuleExecutor ruleExecutor = builder.getRuleExecutor(); // ルール実行器
        TSpotManager spotManager = builder.getSpotManager(); // スポット管理

        // *************************************************************************************************************
        // スポット作成
        // *************************************************************************************************************

        List<TSpot> cells = spotManager.createSpots(ESpotType.Cell, noOfSpots, 2, 0); // セル生成

        // onolab space-module のスポットによる2次元セル空間を構築．(デフォルトでトーラス)
        // T2DCellSpaceMap を作成した時点で，各スポットには TRoleOf2DCell が設定され，これは内部にムーア近傍にあるセルへの参照を持つ．
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
        } while (ruleExecutor.executeStep()); // 1ステップ分のルールを実行

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown(); // ルール実行器を終了する
    }
}
