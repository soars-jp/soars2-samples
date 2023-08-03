package jp.soars.samples.sample12;

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
import jp.soars.utils.random.ICRandom;

/**
 * メインクラス．
 * @author nagakane
 */
public class TMain {

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

        String simulationStart = "0/00:00:00"; // シミュレーション開始時刻
        String simulationEnd = "0/00:00:100"; // シミュレーション終了時刻
        String tick = "00:00:01"; // １ステップの時間間隔
        List<Enum<?>> stages = List.of(EStage.AgentMoving1, EStage.AgentMoving2); // ステージリスト
        Set<Enum<?>> agentTypes = new HashSet<>(); // 全エージェントタイプ
        Set<Enum<?>> spotTypes = new HashSet<>(); // 全スポットタイプ
        Collections.addAll(agentTypes, EAgentType.values()); // EAgentType に登録されているエージェントタイプをすべて追加
        Collections.addAll(spotTypes, ESpotType.values()); // ESpotType に登録されているスポットタイプをすべて追加
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes); // ビルダー作成

        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目．
        // *************************************************************************************************************

        // 並列化するステージを登録．第1引数はスレッド数，第2引数以降は並列実行するステージ．
        int noOfThreads = 4;
        builder.setParallelizationStages(noOfThreads, EStage.AgentMoving1, EStage.AgentMoving2);

        // 定期実行ステージを登録．第1引数は対象ステージ，第2引数は実行開始時間．第3引数は実行間隔．
        builder.setPeriodicallyExecutedStage(EStage.AgentMoving1, "0/00:00:00", "00:00:01");
        builder.setPeriodicallyExecutedStage(EStage.AgentMoving2, "0/00:00:00", "00:00:01");

        // マスター乱数発生器のシード値設定
        long seed = 0L; // シード値
        builder.setRandomSeed(seed); // シード値設定

        // ログ出力設定
        String pathOfLogDir = "logs" + File.separator + "sample12"; // ログディレクトリ
        builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv") // ルールログ出力設定
               .setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv"); // ランタイムログ出力設定

        // ルールログのデバッグ情報出力設定
        builder.setRuleDebugMode(ERuleDebugMode.LOCAL); // ローカル設定に従う

        // グローバル共有変数の初期値を設定する．
        builder.setInitialValueOfGlobalSharedVariableSet(TRuleOfAgentMoving.FAILED_MOVE_COUNT, Integer.valueOf(0));

        // *************************************************************************************************************
        // TSOARSBuilderの最適化設定項目．
        // *************************************************************************************************************

        // 臨時実行ルールの配列を保持するマップの初期サイズを指定する．今回は臨時実行ルールはないため0に設定してメモリを節約する．
        builder.setExpectedSizeOfTemporaryRulesMap(0);

        // ある時刻のステージに登録されるルールの配列の初期サイズを指定する．
        // ある時刻のAgentMovingステージに登録されるルールの数は父親の数と同じ．
        int noOfAgents = 1000; // 父親の数
        builder.setExpectedNoOfRulesPerStage(EStage.AgentMoving1, noOfAgents);
        builder.setExpectedNoOfRulesPerStage(EStage.AgentMoving2, noOfAgents);

        // 削除されるオブジェクトを一時的に保持するリストの初期サイズを指定する．今回はオブジェクトの削除はないため0に設定してメモリを節約する．
        builder.setExpectedNoOfDeletedObjects(0);

        // エージェントタイプごとにエージェントを保持するリストの初期サイズを指定する．父親の生成数分のリストを確保する．
        builder.setExpectedNoOfAgents(EAgentType.Agent, noOfAgents);

        // レイヤーとスポットタイプごとにスポットを保持するリストの初期サイズを指定する．レイヤーを入力しない場合は，デフォルトレイヤーが指定される．
        int noOfSpots = 3; // スポット数
        builder.setExpectedNoOfSpots(ESpotType.Spot, noOfSpots);

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
        //   - Spot1
        //   - Spot2
        //     - 定員 10
        //   - Spot3
        // *************************************************************************************************************

        TSpot spot1 = spotManager.createSpot(ESpotType.Spot, "Spot1", 0, noOfAgents);
        TSpot spot2 = spotManager.createSpot(ESpotType.Spot, "Spot2", 0, 10);
        TSpot spot3 = spotManager.createSpot(ESpotType.Spot, "Spot3", 0, noOfAgents);
        // スポットの定員を設定
        spot2.setCapacity(10);

        // *************************************************************************************************************
        // エージェント作成
        //   - Agent エージェントを1000
        //     - 初期スポットは Spot1 スポット
        //     - 役割としてエージェント役割を持つ．
        // *************************************************************************************************************

        List<TAgent> agents = agentManager.createAgents(EAgentType.Agent, noOfAgents, 0);
        for (int i = 0; i < agents.size(); ++i) {
            TAgent agent = agents.get(i); // i番目のエージェントを取り出す．
            agent.initializeCurrentSpot(spot1); // 初期位置を Spot1 に設定する

            new TRoleOfAgent(agent, spot1, spot2, spot3); // エージェント役割設定．
            agent.activateRole(ERoleName.Agent); // エージェント役割をアクティブ化する．
        }

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        //   - 各時刻でエージェントの現在位置のログをとる (スポットログ, spot_log.csv)
        //   - 各時刻でスポットに滞在するエージェント数のログをとる (エージェントカウントログ, agent_count_log.csv)
        // *************************************************************************************************************

        // スポットログ用PrintWriter
        PrintWriter spotLogPW = new PrintWriter(new BufferedWriter(
                new FileWriter(pathOfLogDir + File.separator + "spot_log.csv")));
        // スポットログのカラム名出力
        spotLogPW.print("CurrentTime");
        for (TAgent agent : agents) {
            spotLogPW.print("," + agent.getName());
        }
        spotLogPW.println();

        // エージェントカウントログ用 PrintWriter
        PrintWriter agentCountLogPW = new PrintWriter(new BufferedWriter(
                new FileWriter(pathOfLogDir + File.separator + "agent_count_log.csv")));
        // エージェントカウントログのカラム名出力
        agentCountLogPW.println("CurrentTime," + spot1.getName() + "," + spot2.getName() + "," + spot3.getName());

        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        while (ruleExecutor.executeStep()) { // 1ステップ分のルールを実行
            // 標準出力に現在時刻を表示する
            System.out.println(ruleExecutor.getCurrentTime());

            // スポットログ出力
            spotLogPW.print(ruleExecutor.getCurrentTime());
            for (TAgent agent : agents) {
                spotLogPW.print("," + agent.getCurrentSpotName());
            }
            spotLogPW.println();

            // エージェントカウントログ出力
            agentCountLogPW.println(ruleExecutor.getCurrentTime() + "," +
                    spot1.getNoOfAgents() + "," + spot2.getNoOfAgents() + "," + spot3.getNoOfAgents());
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown(); // ルール実行器を終了する
        spotLogPW.close(); // スポットログを終了する
        agentCountLogPW.close(); // エージェントカウントログを終了する

        // 移動に失敗した回数を表示
        System.out.println("Failed move count: " + globalSharedVariableSet.get(TRuleOfAgentMoving.FAILED_MOVE_COUNT));
    }
}
