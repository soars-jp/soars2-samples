package jp.soars.samples.sample08;

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
 * 逐次実行
 * @author nagakane
 */
public class TMainSequential {

    public static void main(String[] args) throws IOException {
        // *************************************************************************************************************
        // TSOARSBuilderの必須設定項目．
        //   - simulationStart: シミュレーション開始時刻
        //   - simulationEnd: シミュレーション終了時刻
        //   - tick: 1ステップの時間間隔
        //   - stages: 使用するステージリスト
        //   - agentTypes: 使用するエージェントタイプ集合
        //   - spotTypes: 使用するスポットタイプ集合
        // *************************************************************************************************************

        long startTimeOfSimulation = System.nanoTime(); // シミュレーション開始時刻
        String simulationStart = "0/00:00:00"; // シミュレーション開始時刻
        String simulationEnd = "7/00:00:00"; // シミュレーション終了時刻
        String tick = "1:00:00"; // １ステップの時間間隔
        List<Enum<?>> stages = List.of(EStage.AgentMoving); // ステージリスト
        Set<Enum<?>> agentTypes = new HashSet<>(); // 全エージェントタイプ
        Set<Enum<?>> spotTypes = new HashSet<>(); // 全スポットタイプ
        Collections.addAll(agentTypes, EAgentType.values()); // EAgentType に登録されているエージェントタイプをすべて追加
        Collections.addAll(spotTypes, ESpotType.values()); // ESpotType に登録されているスポットタイプをすべて追加
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes); // ビルダー作成

        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目．
        // *************************************************************************************************************

        // マスター乱数発生器のシード値設定
        long seed = 0L; // シード値
        builder.setRandomSeed(seed); // シード値設定

        // ログ出力設定
        String pathOfLogDir = "logs" + File.separator + "sample08" + File.separator + "sequential"; // ログディレクトリ
        builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv") // ルールログ出力設定
               .setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv"); // ランタイムログ出力設定

        // ルールログのデバッグ情報出力設定
        builder.setRuleDebugMode(ERuleDebugMode.LOCAL); // ローカル設定に従う

        // *************************************************************************************************************
        // TSOARSBuilderの最適化設定項目．
        // *************************************************************************************************************

        // 臨時実行ルールの配列を保持するマップの初期サイズを指定する．今回は臨時実行ルールはないため0に設定してメモリを節約する．
        builder.setExpectedSizeOfTemporaryRulesMap(0);

        // ある時刻のステージに登録されるルールの配列の初期サイズを指定する．
        // ある時刻のAgentMovingステージに登録されるルールの数は父親の数と同じ．
        int noOfFathers = 100000; // 父親の数
        builder.setExpectedNoOfRulesPerStage(EStage.AgentMoving, noOfFathers);

        // 削除されるオブジェクトを一時的に保持するリストの初期サイズを指定する．今回はオブジェクトの削除はないため0に設定してメモリを節約する．
        builder.setExpectedNoOfDeletedObjects(0);

        // エージェントタイプごとにエージェントを保持するリストの初期サイズを指定する．父親の生成数分のリストを確保する．
        builder.setExpectedNoOfAgents(EAgentType.Father, noOfFathers);

        // レイヤーとスポットタイプごとにスポットを保持するリストの初期サイズを指定する．レイヤーを入力しない場合は，デフォルトレイヤーが指定される．
        int noOfHomes = noOfFathers; // 家の数は父親の数と同じ．
        int noOfCompanies = 100; // 会社の数
        builder.setExpectedNoOfSpots(ESpotType.Home, noOfFathers);
        builder.setExpectedNoOfSpots(ESpotType.Company, noOfCompanies);

        // *************************************************************************************************************
        // TSOARSBuilderでシミュレーションに必要なインスタンスの作成と取得．
        // *************************************************************************************************************

        builder.build(); // インスタンスのビルド
        TRuleExecutor ruleExecutor = builder.getRuleExecutor(); // ルール実行器
        TAgentManager agentManager = builder.getAgentManager(); // エージェント管理
        TSpotManager spotManager = builder.getSpotManager(); // スポット管理
        ICRandom random = builder.getRandom(); // マスター乱数発生器
        Map<String, Object> globalSharedVariableSet = builder.getGlobalSharedVariableSet(); // グローバル共有変数集合

        // *************************************************************************************************************
        // スポット作成
        //   - Home スポットを10万
        //   - Company スポットを100
        // *************************************************************************************************************

        // TSpotManager の最適化設定項目を利用する．createSpotsメソッドの以下の引数を指定する
        // 第3引数: スポットが持つ役割の数
        // 第4引数: スポットに滞在するエージェント数の予測値
        long startTimeOfSpotInitialization = System.nanoTime(); // スポット初期化開始時刻
        // Home スポット生成．役割数0，エージェント数の予測値1
        List<TSpot> homes = spotManager.createSpots(ESpotType.Home, noOfHomes, 0, 1);
        // Company スポット生成．役割数0，エージェント数の予測値10万/100
        List<TSpot> companies = spotManager.createSpots(ESpotType.Company, noOfCompanies, 0, noOfFathers / noOfCompanies);
        long timeOfSpotInitialization = System.nanoTime() - startTimeOfSpotInitialization; // スポット初期化時間

        // *************************************************************************************************************
        // エージェント作成
        //   - Father エージェントを10万
        //     - 初期スポットは Home スポット
        //     - 役割として父親役割を持つ．
        // *************************************************************************************************************

        // TAgentManager の最適化設定項目を利用する．createAgentsメソッドの以下の引数を指定する
        // 第3引数: スポットが持つ役割の数
        long startTimeOfAgentInitialization = System.nanoTime(); // エージェント初期化開始時刻
        // Father エージェント生成．役割数1
        List<TAgent> fathers = agentManager.createAgents(EAgentType.Father, noOfFathers, 1);
        for (int i = 0, n = fathers.size(); i < n; ++i) {
            TAgent father = fathers.get(i); // i番目のエージェントを取り出す．
            TSpot home = homes.get(i); // i番目のエージェントの自宅を取り出す．
            TSpot company = companies.get(i % noOfCompanies); // 会社に均等に割り振る．
            father.initializeCurrentSpot(home); // 初期位置を自宅に設定する．

            new TRoleOfCell(father, home, company); // 父親役割を生成する．
            father.activateRole(ERoleName.Father); // 父親役割をアクティブ化する．
        }
        long timeOfAgentInitialization = System.nanoTime() - startTimeOfAgentInitialization; // エージェント初期化時間

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        //   - 各時刻でエージェントの現在位置のログをとる (スポットログ, spot_log.csv)
        // *************************************************************************************************************

        // スポットログ用PrintWriter
        PrintWriter spotLogPW = new PrintWriter(new BufferedWriter(
                new FileWriter(pathOfLogDir + File.separator + "spot_log.csv")));
        // スポットログのカラム名出力
        spotLogPW.print("CurrentTime");
        for (TAgent agent : fathers) {
            spotLogPW.print("," + agent.getName());
        }
        spotLogPW.println();

        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        while (ruleExecutor.executeStep()) { // 1ステップ分のルールを実行
            // 標準出力に現在時刻を表示する
            System.out.println(ruleExecutor.getCurrentTime());

            // スポットログ出力
            spotLogPW.print(ruleExecutor.getCurrentTime());
            for (TAgent a : fathers) {
                spotLogPW.print("," + a.getCurrentSpotName());
            }
            spotLogPW.println();
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown(); // ルール実行器を終了する
        spotLogPW.close(); // スポットログを終了する
        long timeOfSimulation = System.nanoTime() - startTimeOfSimulation; // シミュレーション実行時間

        // *************************************************************************************************************
        // シミュレーションのパフォーマンス集計
        // *************************************************************************************************************

        PrintWriter simulationLogPW = new PrintWriter(new BufferedWriter(
                new FileWriter(pathOfLogDir + File.separator + "simulationInfo.txt")));
        // シミュレーション基本情報
        simulationLogPW.println("sample08: Simulation of " + noOfHomes + " migrations. [sequential execution]");
        simulationLogPW.println("Simulation period = " + simulationEnd);
        simulationLogPW.println("Time per step = " + tick);
        simulationLogPW.println("Random seed = " + seed);
        System.out.println("===========================================================================");
        System.out.println("sample08: Simulation of " + noOfHomes + " migrations. [sequential execution]");
        System.out.println("Simulation period = " + simulationEnd);
        System.out.println("Time per step = " + tick);
        System.out.println("Random seed = " + seed);
        // シミュレーション総実行時間
        simulationLogPW.println("Total simulation run time = " + timeOfSimulation + "[ns] (" + (timeOfSimulation / 1_000_000) + "[ms])");
        System.out.println("Total simulation run time = " + timeOfSimulation + "[ns] (" + (timeOfSimulation / 1_000_000) + "[ms])\n");
        // 総スポット数とスポット初期化時間
        simulationLogPW.println("Total number of spots = " + spotManager.getSpotDB().size());
        simulationLogPW.println("Spot initialization time = " + timeOfSpotInitialization + "[ns] (" + (timeOfSpotInitialization / 1_000_000) + "[ms])");
        System.out.println("Total number of spots = " + spotManager.getSpotDB().size());
        System.out.println("Spot initialization time = " + timeOfSpotInitialization + "[ns] (" + (timeOfSpotInitialization / 1_000_000) + "[ms])\n");
        // 総エージェント数とエージェント初期化時間
        simulationLogPW.println("Total number of agents = " + agentManager.getAgentDB().size());
        simulationLogPW.println("Agent initialization time = " + timeOfAgentInitialization + "[ns] (" + (timeOfAgentInitialization / 1_000_000) + "[ms])");
        System.out.println("Total number of agents = " + agentManager.getAgentDB().size());
        System.out.println("Agent initialization time = " + timeOfAgentInitialization + "[ns] (" + (timeOfAgentInitialization / 1_000_000) + "[ms])\n");
        // 総実行ルール数とルール実行時間
        // ランタイムログを取得
        TCSimpleCsvData csv = new TCSimpleCsvData(pathOfLogDir + File.separator + "runtime_log.csv");
        long noOfExecutedRules = 0; // 総実行ルール数
        long ruleExecutionTime = 0; // 総実行時間 [ns]
        while (csv.readLine()) {
            noOfExecutedRules += csv.getElementAsLong(ERuntimeLogKey.NoOfExecutedRules.toString());
            ruleExecutionTime += csv.getElementAsLong(ERuntimeLogKey.ExecutionTimeInNanoSec.toString());
        }
        csv.close();

        simulationLogPW.println("Total number of executed rules = " + noOfExecutedRules);
        simulationLogPW.println("Rule execution Time = " + ruleExecutionTime + "[ns] (" + (ruleExecutionTime / 1_000_000) + "[ms])");
        simulationLogPW.close();
        System.out.println("Total number of executed rules = " + noOfExecutedRules);
        System.out.println("Rule execution Time = " + ruleExecutionTime + "[ns] (" + (ruleExecutionTime / 1_000_000) + "[ms])");
        System.out.println("===========================================================================");
    }
}
