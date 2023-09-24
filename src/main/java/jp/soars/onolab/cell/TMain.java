package jp.soars.onolab.cell;

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
 * @author nagakane
 */
public class TMain {

    /**
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
        List<Enum<?>> stages = List.of(EStage.AgentMoving);
        Set<Enum<?>> agentTypes = new HashSet<>();
        Collections.addAll(agentTypes, EAgentType.values());
        Set<Enum<?>> spotTypes = new HashSet<>();
        Collections.addAll(spotTypes, ESpotType.values());
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes);

        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目
        // *************************************************************************************************************

        // 定期実行ステージ設定
        builder.setPeriodicallyExecutedStage(EStage.AgentMoving, simulationStart, tick);

        // マスター乱数発生器のシード値設定
        long seed = 0L;
        builder.setRandomSeed(seed);

        // ルールログとランタイムログの出力設定
        String pathOfLogDir = "logs" + File.separator + "onolab" + File.separator + "cell";
        builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv");
        builder.setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv");

        // ルールログのデバッグ情報出力設定
        builder.setRuleDebugMode(ERuleDebugMode.OFF);

        // 以下，最適化設定
        // 空間のサイズ
        int width = 30;
        int hight = 15;
        int noOfSpots = width * hight;

        builder.setExpectedNoOfAgents(EAgentType.Agent, 1);
        builder.setExpectedNoOfSpots(ESpotType.Cell, noOfSpots);
        builder.setRulesNotShuffledBeforeExecuted(EStage.AgentMoving);
        builder.setExpectedNoOfRulesPerStage(EStage.AgentMoving, 1);
        builder.setExpectedSizeOfTemporaryRulesMap(0);
        builder.setExpectedNoOfDeletedObjects(0);

        // *************************************************************************************************************
        // TSOARSBuilderでシミュレーションに必要なインスタンスの作成と取得
        // *************************************************************************************************************

        builder.build();
        TRuleExecutor ruleExecutor = builder.getRuleExecutor();
        TAgentManager agentManager = builder.getAgentManager();
        TSpotManager spotManager = builder.getSpotManager();
        ICRandom random = builder.getRandom();

        // *************************************************************************************************************
        // スポット作成
        // *************************************************************************************************************

        List<TSpot> cells = spotManager.createSpots(ESpotType.Cell, noOfSpots, 1, 1);

        // onolab-cell-module でスポットによる2次元セル空間を構築．
        // T2DCellSpaceMap を作成した時点で，各スポットには TRoleOf2DCell が設定され，これは内部にムーア近傍にあるセルへの参照を持つ．
        // 第4引数はx軸方向，第5引数はy軸方向をトーラスのように繋げるかを指定する．(デフォルトでtrueなのでトーラスにしたい場合は入力しなくても良い)
        T2DCellSpaceMap map = new T2DCellSpaceMap(cells, width, hight, true, true);

        // *************************************************************************************************************
        // エージェント作成
        // *************************************************************************************************************

        TAgent agent = agentManager.createAgent(EAgentType.Agent, 1);
        agent.initializeCurrentSpot(cells.get(random.nextInt(cells.size())));
        new TRoleOfAgent(agent);
        agent.activateRole(ERoleName.Agent);

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
                    if (map.getCell(x, y).getAgents().size() == 0) {
                        System.out.print("⬜︎");
                    } else {
                        System.out.print("👦");
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
