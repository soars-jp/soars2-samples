package jp.soars.samples.sample07;

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
        String simulationEnd = "7/00:00:00"; // シミュレーション終了時刻
        String tick = "1:00:00"; // １ステップの時間間隔
        List<Enum<?>> stages = List.of(EStage.DynamicAdd, EStage.DynamicDelete); // ステージリスト
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
        String pathOfLogDir = "logs" + File.separator + "sample07"; // ログディレクトリ
        builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv") // ルールログ出力設定
               .setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv"); // ランタイムログ出力設定

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
        //   - Dummy スポットを5つ
        // *************************************************************************************************************

        int noOfDummies = 5; // ダミーの数
        spotManager.createSpots(ESpotType.Dummy, noOfDummies);

        // *************************************************************************************************************
        // エージェント作成
        //   - Creator エージェントを3つ
        //     - 役割としてクリエイター役割を持つ．
        //   - Killer エージェントを3つ
        //     - 役割としてキラー役割を持つ．
        //   - Dummy エージェントを5つ
        // *************************************************************************************************************

        // Creator エージェントを作成
        int noOfCreators = 3;
        List<TAgent> creators = agentManager.createAgents(EAgentType.Creator, noOfCreators);
        for (int i = 0; i < creators.size(); ++i) {
            TAgent creator = creators.get(i); // i番目のCreatorを取り出す．
            new TRoleOfCreator(creator); // クリエイター役割を生成する．
            creator.activateRole(ERoleName.Creator); // クリエイター役割をアクティブ化する．
        }

        // Killer エージェントを作成
        int noOfKillers = 3;
        List<TAgent> killers = agentManager.createAgents(EAgentType.Killer, noOfKillers);
        for (int i = 0; i < killers.size(); ++i) {
            TAgent killer = killers.get(i); // i番目のKillerを取り出す．
            new TRoleOfKiller(killer); // キラー役割を生成する．
            killer.activateRole(ERoleName.Killer); // キラー役割をアクティブ化する．
        }

        // Dummy エージェントを作成
        agentManager.createAgents(EAgentType.Dummy, noOfDummies);

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        //   - 各時刻で全エージェントのエージェント名のログをとる (エージェントログ, agent_log.txt)
        //   - 各時刻で全スポットのスポット名のログをとる (スポットログ, spot_log.txt)
        // *************************************************************************************************************

        // エージェントログ用PrintWriter
        PrintWriter agentLogPW = new PrintWriter(new BufferedWriter(
                new FileWriter(pathOfLogDir + File.separator + "agent_log.txt")));
        // スポットログ用PrintWriter
        PrintWriter spotLogPW = new PrintWriter(new BufferedWriter(
                new FileWriter(pathOfLogDir + File.separator + "spot_log.txt")));

        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        while (ruleExecutor.executeStep()) { // 1ステップ分のルールを実行
            // 標準出力に現在時刻を表示する
            System.out.println(ruleExecutor.getCurrentTime());

            // ログ出力
            agentLogPW.print(ruleExecutor.getCurrentTime());
            for (TAgent agent : agentManager.getAgents()) {
                agentLogPW.print("," + agent.getName());
            }
            agentLogPW.println();

            spotLogPW.print(ruleExecutor.getCurrentTime());
            for (TSpot spot : spotManager.getSpots()) {
                spotLogPW.print("," + spot.getName());
            }
            spotLogPW.println();
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown(); // ルール実行器を終了する
        agentLogPW.close();// エージェントログを終了する
        spotLogPW.close(); // スポットログを終了する
    }
}
