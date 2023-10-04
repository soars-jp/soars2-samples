package jp.soars.tutorials.sample15;

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
import jp.soars.core.enums.ERuntimeLogKey;
import jp.soars.utils.csv.TCSimpleCsvData;
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

        // 並列化設定．第1引数はスレッド数，第2引数以降は並列実行するステージ．
        int noOfThreads = 4;
        builder.setParallelizationStages(noOfThreads, EStage.AgentMoving);

        // エージェント移動ステージを毎時刻ルールが実行される定期実行ステージとして登録
        builder.setPeriodicallyExecutedStage(EStage.AgentMoving, simulationStart, tick);

        // マスター乱数発生器のシード値設定
        long seed = 0L;
        builder.setRandomSeed(seed);

        // ランタイムログの出力設定
        String pathOfLogDir = "logs" + File.separator + "tutorials" + File.separator + "sample15";
        builder.setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv");

        // 最適化設定

        // ルールログは小規模実験でのバグチェックが主な目的であり，大規模実験では出力しないことを推奨
        // builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv");

        // デフォルトでルールを実行前にシャッフルする仕様になっており，シャッフルしないように設定(ステージ毎)．
        builder.setRulesNotShuffledBeforeExecuted(EStage.AgentMoving);

        // シミュレーションで作成するエージェント数を設定．配列の初期サイズに利用する．
        int noOfAgents = 100000; // エージェント数
        builder.setExpectedNoOfAgents(EAgentType.Agent, noOfAgents);

        // シミュレーションで作成するスポット数を設定．配列の初期サイズ確保に利用する．
        int noOfSpots = 10000; // スポット数
        builder.setExpectedNoOfSpots(ESpotType.Spot, noOfSpots);

        // あるステージに登録されるルール数を設定．配列の初期サイズ確保に利用する．
        // エージェント移動ステージに登録されるルール数はエージェント数と一致．
        builder.setExpectedNoOfRulesPerStage(EStage.AgentMoving, noOfAgents);

        // 臨時実行ルールの配列を保存するリストの初期サイズ設定．
        // このシミュレーションでは臨時実行ルールを利用しないため0．
        builder.setExpectedSizeOfTemporaryRulesMap(0);

        // 動的削除されるオブジェクトを一時保存するためのリストの初期サイズ設定．
        // このシミュレーションではオブジェクトの動的削除を利用しないため0．
        builder.setExpectedNoOfDeletedObjects(0);

        // ルールログのデバッグ情報出力設定，LOCALでfalseにするよりも，OFFの方が高速．
        builder.setRuleDebugMode(ERuleDebugMode.OFF);

        // ワーニングメッセージ出力設定．出力しない．
        builder.setWarningFlag(false);

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
        //   - Spot:Spot1-Spot10000
        // *************************************************************************************************************

        List<TSpot> spots = spotManager.createSpots(ESpotType.Spot, noOfSpots);

        // *************************************************************************************************************
        // エージェント作成
        //   - Agent:Agent1-Agent100000
        //     - 初期スポット:Spot
        //     - 役割:エージェント役割
        // *************************************************************************************************************

        List<TAgent> agents = agentManager.createAgents(EAgentType.Agent, noOfAgents);
        for (int i = 0; i < noOfAgents; ++i) {
            TAgent agent = agents.get(i); // i番目のエージェント
            TSpot spot = spots.get(random.nextInt(noOfSpots));
            agent.initializeCurrentSpot(spot); // 初期スポット設定
            new TRoleOfAgent(agent); // エージェント役割作成
            agent.activateRole(ERoleName.Agent); // エージェント役割をアクティブ化
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
            }
            spotLogPW.println();
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown();
        spotLogPW.close();

        // ランタイムログからルールの総実行時間を集計して表示
        TCSimpleCsvData csv = new TCSimpleCsvData(pathOfLogDir + File.separator + "runtime_log.csv");
        long totalRuleExecutionTime  = 0L;
        while (csv.readLine()) {
            totalRuleExecutionTime += Long.parseLong(csv.getElement(ERuntimeLogKey.ExecutionTimeInNanoSec.toString()));
        }
        System.out.println("総ルール実行時間：" + String.format("%.2f", totalRuleExecutionTime / 1000_000_000.0) + "[秒]");
        csv.close();
    }
}
