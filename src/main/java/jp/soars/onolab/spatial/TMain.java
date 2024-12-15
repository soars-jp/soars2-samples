package jp.soars.onolab.spatial;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.soars.core.TAgent;
import jp.soars.core.TAgentManager;
import jp.soars.core.TRuleExecutor;
import jp.soars.core.TSOARSBuilder;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.enums.ERuleDebugMode;
import jp.soars.modules.onolab.spatial.ESpatialSpotModuleRoleName;
import jp.soars.modules.onolab.spatial.T2DCoordinate;
import jp.soars.modules.onolab.spatial.T2DSpace;
import jp.soars.modules.onolab.spatial.TRoleOf2DCoordinateAgent;
import jp.soars.modules.onolab.spatial.TRoleOf2DCoordinateSpot;
import jp.soars.utils.random.ICRandom;

/**
 * メインクラス
 * @author nagakane
 */
public class TMain {

    public static void main(String[] args) throws IOException {
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
        String simulationEnd = "7/00:00:00";
        String tick = "1:00:00";
        List<Enum<?>> stages = List.of(EStage.AgentMoving);
        Set<Enum<?>> agentTypes = new HashSet<>();
        Collections.addAll(agentTypes, EAgentType.values());
        Set<Enum<?>> spotTypes = new HashSet<>();
        Collections.addAll(spotTypes, ESpotType.values());
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes);

        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目
        // *************************************************************************************************************

        // エージェント移動ステージを毎時刻ルールが実行される定期実行ステージとして登録
        builder.setPeriodicallyExecutedStage(EStage.AgentMoving, simulationStart, tick);

        // マスター乱数発生器のシード値設定
        long seed = 0L;
        builder.setRandomSeed(seed);

        // ルールログとランタイムログの出力設定
        String pathOfLogDir = "logs" + File.separator + "spatial";
        builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv");
        builder.setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv");

        // ルールログのデバッグ情報出力設定
        builder.setRuleDebugMode(ERuleDebugMode.LOCAL);

        // *************************************************************************************************************
        // TSOARSBuilderでシミュレーションに必要なインスタンスの作成と取得
        // *************************************************************************************************************

        builder.build();
        TRuleExecutor ruleExecutor = builder.getRuleExecutor();
        TAgentManager agentManager = builder.getAgentManager();
        TSpotManager spotManager = builder.getSpotManager();
        ICRandom random = builder.getRandom();
        Map<String, Object> globalSharedVariableSet = builder.getGlobalSharedVariableSet();

        // *************************************************************************************************************
        // スポット作成
        //   - Spot:Spot1-Spot10
        // *************************************************************************************************************

        int noOfSpots = 10; // スポットの数
        // スポット内座標の範囲指定
        double lowerBoundX = -5.0;
        double upperBoundX = 5.0;
        double lowerBoundY = -5.0;
        double upperBoundY = 5.0;
        boolean isToroidal = true; // トーラスにするか？
        List<TSpot> spots = spotManager.createSpots(ESpotType.Spot, noOfSpots);
        for (int i = 0; i < noOfSpots; ++i) {
            TSpot spot = spots.get(i); // i番目のスポット
            new TRoleOf2DCoordinateSpot(spot, lowerBoundX, upperBoundX, lowerBoundY, upperBoundY, isToroidal); // 2次元座標スポット役割作成
        }

        // *************************************************************************************************************
        // エージェント作成
        //   - Agent:Agent1-Agent20
        //     - 初期スポット:Spot
        //     - 役割:エージェント役割
        // *************************************************************************************************************

        int noOfAgents = 20; // エージェント数
        List<TAgent> agents = agentManager.createAgents(EAgentType.Agent, noOfAgents);
        for (int i = 0; i < noOfAgents; ++i) {
            TAgent agent = agents.get(i); // i番目のエージェント
            TSpot spot = spots.get(i % noOfSpots); // スポット
            agent.initializeCurrentSpot(spot); // 初期スポット設定

            new TRoleOfAgent(agent); // エージェント役割作成
            agent.activateRole(ERoleName.Agent); // エージェント役割をアクティブ化

            TRoleOf2DCoordinateAgent role = new TRoleOf2DCoordinateAgent(agent, spotManager); // 2次元座標利用エージェント役割作成
            // スポット内座標の初期化．現在スポットが設定されているかつ，スポットがスポット内座標役割を持っている必要がある点に注意．
            role.moveInCurrentSpot(spotManager, random);
        }

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        //   - スポットログ:各時刻での各エージェントの現在位置ログ
        // *************************************************************************************************************

        // スポットログ用PrintWriter
        PrintWriter spotLogPW = new PrintWriter(new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "spot_log.csv")));
        // スポットログのカラム名出力
        spotLogPW.print("CurrentTime");
        for (TAgent agent : agents) {
            spotLogPW.print(',');
            spotLogPW.print(agent.getName());
        }
        spotLogPW.println();

        // 各時刻におけるエージェント同士の距離のログ
        PrintWriter distanceLogPW = new PrintWriter(new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "distance_log.txt")));


        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        // 1ステップ分のルールを実行 (ruleExecutor.executeStage()で1ステージ毎に実行することもできる)
        // 実行された場合:true，実行されなかった(終了時刻)場合は:falseが帰ってくるため，while文で回すことができる．
        while (ruleExecutor.executeStep()) {
            // 標準出力に現在時刻を表示する
            System.out.println(ruleExecutor.getCurrentTime());

            // スポットログ出力
            spotLogPW.print(ruleExecutor.getCurrentTime());
            for (TAgent agent : agents) {
                spotLogPW.print(',');
                spotLogPW.print(agent.getCurrentSpotName());
                spotLogPW.print(':');
                spotLogPW.print(((TRoleOf2DCoordinateAgent) agent.getRole(ESpatialSpotModuleRoleName.TwoDimensionalCoordinateAgent))
                        .getCoordinate(spotManager));
            }
            spotLogPW.println();

            // 距離ログ出力
            distanceLogPW.println(ruleExecutor.getCurrentTime());
            for (TSpot spot : spots) {
                distanceLogPW.println(spot.getName());
                for (TAgent agent: spot.getAgents()) { // 各エージェントの座標を最初に出力
                    distanceLogPW.print("  ");
                    distanceLogPW.print(agent.getName());
                    distanceLogPW.print(' ');
                    distanceLogPW.println(((TRoleOf2DCoordinateAgent) agent.getRole(ESpatialSpotModuleRoleName.TwoDimensionalCoordinateAgent))
                            .getCoordinate(spotManager));
                }
                distanceLogPW.println("  Distance:");
                for (TAgent agent1: spot.getAgents()) { // 各エージェントの距離を出力
                    for (TAgent agent2: spot.getAgents()) {
                        if (agent1.equals(agent2)) {
                            continue;
                        }
                        T2DCoordinate coordinate1 = ((TRoleOf2DCoordinateAgent) agent1.getRole(ESpatialSpotModuleRoleName.TwoDimensionalCoordinateAgent))
                                .getCoordinate(spotManager);
                        T2DCoordinate coordinate2 = ((TRoleOf2DCoordinateAgent) agent2.getRole(ESpatialSpotModuleRoleName.TwoDimensionalCoordinateAgent))
                                .getCoordinate(spotManager);
                        T2DSpace space = ((TRoleOf2DCoordinateAgent) agent1.getRole(ESpatialSpotModuleRoleName.TwoDimensionalCoordinateAgent))
                                .get2DSpaceOfCurrentSpot(spotManager);
                        double distance = space.distance(coordinate1, coordinate2);
                        distanceLogPW.print("    ");
                        distanceLogPW.print(agent1.getName());
                        distanceLogPW.print("-");
                        distanceLogPW.print(agent2.getName());
                        distanceLogPW.print(" ");
                        distanceLogPW.println(distance);
                    }
                }
            }
            distanceLogPW.println("==================================================================================================");
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown();
        spotLogPW.close();
        distanceLogPW.close();
    }
}
