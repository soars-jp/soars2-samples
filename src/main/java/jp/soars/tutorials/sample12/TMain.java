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
        String simulationEnd = "7/00:00:00"; // シミュレーション終了時刻
        String tick = "1:00:00"; // １ステップの時間間隔
        List<Enum<?>> stages = List.of(EStage.AgentMoving,
                                       EStage.RevertAgentMoving,
                                       EStage.Check,
                                       EStage.Reset); // ステージリスト
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
        String pathOfLogDir = "logs" + File.separator + "sample12"; // ログディレクトリ
        builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv") // ルールログ出力設定
               .setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv"); // ランタイムログ出力設定

        // ルールログのデバッグ情報出力設定
        builder.setRuleDebugMode(ERuleDebugMode.LOCAL); // ローカル設定に従う

        // ステージ実行ルールの設定
        builder.setPeriodicallyExecutedStage(EStage.RevertAgentMoving, simulationStart, tick);
        builder.setPeriodicallyExecutedStage(EStage.Check, simulationStart, tick);
        builder.setPeriodicallyExecutedStage(EStage.Reset, simulationStart, tick);

        // *************************************************************************************************************
        // TSOARSBuilderでシミュレーションに必要なインスタンスの作成と取得．
        // *************************************************************************************************************

        builder.build(); // インスタンスのビルド
        TRuleExecutor ruleExecutor = builder.getRuleExecutor(); // ルール実行器
        TAgentManager agentManager = builder.getAgentManager(); // エージェント管理
        TSpotManager spotManager = builder.getSpotManager(); // スポット管理
        ICRandom random = builder.getRandom(); // マスター乱数発生器
        Map<String, Object> globalSharedVariableSet = builder.getGlobalSharedVariableSet(); // グローバル共有変数集合

        globalSharedVariableSet.put(TRuleOfCheckCapacity.KEY_NAME_OF_SPOT_CAPACITY_CONDITION_MET, true);

        // *************************************************************************************************************
        // スポット作成
        //   - Spot スポット 20つ (内10つは定員あり)
        //   - Agency スポット 1つ
        // *************************************************************************************************************

        int noOfSpotsWithCapacity = 10; // 定員ありスポットの数
        int capacity = 10; // 定員
        List<TSpot> spots = spotManager.createSpots(ESpotType.Spot, noOfSpotsWithCapacity);
        for (TSpot spot : spots) {
            new TRoleOfSpotWithCapacity(spot, capacity); // 定員役割を設定
            spot.activateRole(ERoleName.SpotWithCapacity); // 定員役割をアクティベート
        }

        int noOfSpots = 10; // 定員なしスポットの数
        spots = spotManager.createSpots(ESpotType.Spot, noOfSpots);

        TSpot agency = spotManager.createSpot(ESpotType.Agency); // エージェンシースポット
        new TRoleOfAgency(agency);
        agency.activateRole(ERoleName.Agency);

        // *************************************************************************************************************
        // エージェント作成
        //   - Agent エージェントを1000つ
        //     - 初期スポットは定員のない Spot スポットからランダムに選択
        //     - 役割としてエージェント役割を持つ．
        // *************************************************************************************************************

        int noOfAgents = 1000; // エージェント数
        List<TAgent> agents = agentManager.createAgents(EAgentType.Agent, noOfAgents);
        for (TAgent agent : agents) {
            agent.initializeCurrentSpot(spots.get(random.nextInt(spots.size()))); // 初期位置は定員なしスポット
            new TRoleOfAgent(agent); // エージェント役割を生成する．
            agent.activateRole(ERoleName.Agent); // エージェント役割をアクティブ化する．
        }

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        //   - 各時刻でスポットにいるエージェント数のログをとる
        // *************************************************************************************************************

        // エージェントログ用PrintWriter
        PrintWriter agentLogPW = new PrintWriter(new BufferedWriter(
                new FileWriter(pathOfLogDir + File.separator + "agent_log.csv")));
        // エージェントログのカラム名出力
        agentLogPW.print("CurrentTime,CurrentStage");
        for (TSpot spot : spotManager.getSpots()) {
            agentLogPW.print("," + spot.getName());
        }
        agentLogPW.println();

        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        while (ruleExecutor.executeStage()) { // 1ステージ分のルールを実行
            // 標準出力に現在時刻，ステージを表示する
            System.out.println(ruleExecutor.getCurrentTime() + " : " + ruleExecutor.getCurrentStage());

            // スポットログ出力
            agentLogPW.print(ruleExecutor.getCurrentTime() + "," + ruleExecutor.getCurrentStage());
            for (TSpot spot : spotManager.getSpots()) {
                agentLogPW.print("," + spot.getNoOfAgents());
            }
            agentLogPW.println();

            // エージェント移動ステージ実行後
            if (ruleExecutor.getCurrentStage() == EStage.AgentMoving) {
                if (0 < ruleExecutor.getNoOfExecutedRules()) { // エージェント移動が発生した場合
                    // 整合性チェック用のステージをアクティブ化
                    ruleExecutor.activateStage(EStage.RevertAgentMoving);
                    ruleExecutor.activateStage(EStage.Check);
                    ruleExecutor.activateStage(EStage.Reset);
                } else { // エージェント移動が発生していない場合
                    // 整合性チェック用のステージを非アクティブ化
                    ruleExecutor.deactivateStage(EStage.RevertAgentMoving);
                    ruleExecutor.deactivateStage(EStage.Check);
                    ruleExecutor.deactivateStage(EStage.Reset);
                }
            }

            // チェックステージ実行後に，チェック結果によってステージを戻す．
            if (ruleExecutor.getCurrentStage() == EStage.Check &&
                    !((boolean) globalSharedVariableSet.get(TRuleOfCheckCapacity.KEY_NAME_OF_SPOT_CAPACITY_CONDITION_MET))) {
                // checkステージ実行後から2ステージ戻すと，RevertAgentMoving，Checkステージを
                // 定員条件を満たしたとチェックされるまで繰り返すことになる．
                ruleExecutor.rollbackStage(EStage.RevertAgentMoving);
            }

            // リセットステージで整合性フラグを true に戻す．
            if (ruleExecutor.getCurrentStage() == EStage.Reset) {
                globalSharedVariableSet.put(TRuleOfCheckCapacity.KEY_NAME_OF_SPOT_CAPACITY_CONDITION_MET, true);
            }
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown(); // ルール実行器を終了する
        agentLogPW.close(); // スポットログを終了する
    }
}
