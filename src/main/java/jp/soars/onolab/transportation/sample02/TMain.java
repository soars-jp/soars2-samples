package jp.soars.onolab.transportation.sample02;

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
import jp.soars.modules.onolab.simple.transportation.ETransportationStage;
import jp.soars.modules.onolab.simple.transportation.TTransportationAndStationManager;
import jp.soars.utils.random.ICRandom;

public class TMain {
    public static void main(String[] args) throws IOException {

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
        String simulationEnd = "2/00:00:00";
        String tick = "0:01:00";
        List<Enum<?>> stages = List.of(ETransportationStage.NewTransportation, EStage.AgentMoving,
                ETransportationStage.TransportationArriving, ETransportationStage.AgentMoving,
                ETransportationStage.TransportationLeaving, ETransportationStage.DeleteTransportation);
        Set<Enum<?>> agentTypes = new HashSet<>();
        Collections.addAll(agentTypes, EAgentType.values());
        Set<Enum<?>> spotTypes = new HashSet<>();
        Collections.addAll(spotTypes, ESpotType.values());
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes);

        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目
        // *************************************************************************************************************

        // マスター乱数発生器のシード値設定
        long seed = 0L;
        builder.setRandomSeed(seed);

        // ルールログとランタイムログの出力設定
        String pathOfLogDir = "logs" + File.separator + "onolab" + File.separator + "transportation" + File.separator
                + "sample02";
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
        // - Home:Home
        // - Company:Company
        // - MidWay:Mid
        // *************************************************************************************************************

        int noOfHomes = 5; // 家の数
        List<TSpot> homes = spotManager.createSpots(ESpotType.Home, noOfHomes);
        TSpot company = spotManager.createSpot(ESpotType.Company);
        TSpot mid = spotManager.createSpot(ESpotType.Mid);

        // *************************************************************************************************************
        // 駅スポットと乗り物スポット作成
        // *************************************************************************************************************

        String PathOfTransportationLogDir = "src/main/java/jp/soars/onolab/transportation/transportationDB";
        TTransportationAndStationManager transportationAndStationManager = new TTransportationAndStationManager(
                PathOfTransportationLogDir, spotManager, ESpotType.Transportation, ESpotType.Station, true);

        // *************************************************************************************************************
        // エージェント作成
        // - Father:Father
        // - 初期スポット:Home
        // - 役割:父親役割
        // *************************************************************************************************************

        int noOfFathers = noOfHomes; // 父親の数は家の数と同じ．
        List<TAgent> fathers = agentManager.createAgents(EAgentType.Father, noOfFathers);
        // それぞれの父親はstation2,station3,station4,..から通勤する
        for (int i = 0; i < noOfFathers; ++i) {
            TAgent father = fathers.get(i); // i番目の父親エージェント
            TSpot home = homes.get(i); // i番目の父親エージェントの自宅
            father.initializeCurrentSpot(home); // 初期スポットを自宅に設定
            TSpot srcStationSpot = spotManager.getSpotDB().get("station" + (i + 2));
            TSpot dstStationSpot = spotManager.getSpotDB().get("station8");
            new TRoleOfFather(father, home, company, mid, srcStationSpot, dstStationSpot, "line1");
            father.activateRole(ERoleName.Father);
        }

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        // - スポットログ:各時刻での各エージェントの現在位置ログ
        // *************************************************************************************************************

        // スポットログ用PrintWriter
        PrintWriter spotLogPW = new PrintWriter(
                new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "spot_log.csv")));
        // スポットログのカラム名出力
        spotLogPW.print("CurrentTime");
        for (TAgent father : fathers) {
            spotLogPW.print(',');
            spotLogPW.print(father.getName());
        }
        spotLogPW.println();

        // Transportation用PrintWriter
        PrintWriter transportationLogPW = new PrintWriter(
                new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "transportation_log.csv")));
        // スポットログのカラム名出力
        transportationLogPW.print("CurrentTime");
        for (TSpot transportation : transportationAndStationManager.getTransportationSpotList()) {
            transportationLogPW.print(",");
            transportationLogPW.print(transportation.getName());
        }
        transportationLogPW.println();

        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        // 1ステップ分のルールを実行 (ruleExecutor.executeStage()で1ステージ毎に実行することもできる)
        // 実行された場合:true，実行されなかった(終了時刻)場合は:falseが帰ってくるため，while文で回すことができる．
        while (ruleExecutor.executeStep()) {
            // スポットログ出力
            spotLogPW.print(ruleExecutor.getCurrentTime());
            for (TAgent father : fathers) {
                spotLogPW.print(',');
                spotLogPW.print(father.getCurrentSpotName());
            }
            spotLogPW.println();

            // Transportationログ出力
            transportationLogPW.print(ruleExecutor.getCurrentTime());
            for (TSpot transportation : transportationAndStationManager.getTransportationSpotList()) {
                transportationLogPW.print(',');
                if (transportationAndStationManager.isTransportationInService(transportation)) {
                    transportationLogPW
                            .print(transportationAndStationManager.getTransportationLocation(transportation));
                }
            }
            transportationLogPW.println();
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown();
        spotLogPW.close();
        transportationLogPW.close();
    }
}
