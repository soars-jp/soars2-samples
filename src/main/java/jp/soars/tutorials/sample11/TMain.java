package jp.soars.tutorials.sample11;

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
        List<Enum<?>> stages = List.of(EStage.AgentMoving,
                                       EStage.RevertAgentMoving,
                                       EStage.Reset);
        Set<Enum<?>> agentTypes = new HashSet<>();
        Collections.addAll(agentTypes, EAgentType.values());
        Set<Enum<?>> spotTypes = new HashSet<>();
        Collections.addAll(spotTypes, ESpotType.values());
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes);

        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目
        // *************************************************************************************************************

        // 定期実行ステージの登録
        builder.setPeriodicallyExecutedStage(EStage.RevertAgentMoving, simulationStart, tick);
        builder.setPeriodicallyExecutedStage(EStage.Reset, simulationStart, tick);

        // マスター乱数発生器のシード値設定
        long seed = 0L;
        builder.setRandomSeed(seed);

        // ルールログとランタイムログの出力設定
        String pathOfLogDir = "logs" + File.separator + "tutorials" + File.separator + "sample11";
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
        //   - Spot:Spot1-Spot10 (定員あり)
        //   - Spot:Spot11-Spot20 (定員なし)
        // *************************************************************************************************************

        int noOfSpotsWithCapacity = 10; // 定員ありスポットの数
        int capacity = 10; // 定員
        List<TSpot> spotsWithCapacity = spotManager.createSpots(ESpotType.Spot, noOfSpotsWithCapacity);
        for (TSpot spot : spotsWithCapacity) {
            new TRoleOfSpotWithCapacity(spot, capacity); // 定員役割を設定
            spot.activateRole(ERoleName.SpotWithCapacity); // 定員役割をアクティベート
        }

        int noOfSpots = 10; // 定員なしスポットの数
        List<TSpot> spots = spotManager.createSpots(ESpotType.Spot, noOfSpots);

        // *************************************************************************************************************
        // エージェント作成
        //   - Agent:Agent1-Agent1000
        //     - 初期スポット:定員なしスポットからランダムに選択
        //     - 役割:エージェント役割
        // *************************************************************************************************************

        int noOfAgents = 1000; // エージェントの数はスポットの数と同じ．
        List<TAgent> agents = agentManager.createAgents(EAgentType.Agent, noOfAgents);
        for (TAgent agent : agents) {
            agent.initializeCurrentSpot(spots.get(random.nextInt(noOfSpots))); // 初期スポット設定
            new TRoleOfAgent(agent); // エージェント役割作成
            agent.activateRole(ERoleName.Agent); // エージェント役割をアクティブ化
        }

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        //   - エージェント数ログ:各時刻で各スポットにいるエージェント数
        // *************************************************************************************************************

        // エージェント数ログ用PrintWriter
        PrintWriter noOfAgentsLogPW = new PrintWriter(new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "number_of_agents_in_spot.csv")));
        // エージェント数ログのカラム名出力
        noOfAgentsLogPW.print("CurrentTime,CurrentStage");
        for (TSpot spot : spotManager.getSpots()) {
            noOfAgentsLogPW.print(',');
            noOfAgentsLogPW.print(spot.getName());
        }
        noOfAgentsLogPW.println();

        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        // RevertAgentMoving, Reset ステージを非アクティブ化
        ruleExecutor.deactivateStage(EStage.RevertAgentMoving);
        ruleExecutor.deactivateStage(EStage.Reset);

        // 1ステージ分のルールを実行 (ruleExecutor.executeStep()で1ステップ毎に実行することもできる)
        // 実行された場合:true，実行されなかった(終了時刻)場合は:falseが帰ってくるため，while文で回すことができる．
        while (ruleExecutor.executeStage()) {
            // 標準出力に現在時刻，ステージを表示する
            System.out.println(ruleExecutor.getCurrentTime() + " : " + ruleExecutor.getCurrentStage());

            // エージェント数ログ出力
            noOfAgentsLogPW.print(ruleExecutor.getCurrentTime());
            noOfAgentsLogPW.print(',');
            noOfAgentsLogPW.print(ruleExecutor.getCurrentStage());
            for (TSpot spot : spotManager.getSpots()) {
                noOfAgentsLogPW.print(',');
                noOfAgentsLogPW.print(spot.getNoOfAgents());
            }
            noOfAgentsLogPW.println();

            if (ruleExecutor.getCurrentStage() == EStage.AgentMoving) { // AgentMoving ステージ実行後
                // ルールが実行されている場合は，RevertAgentMoving, Reset ステージをアクティブ化
                if (0 < ruleExecutor.getNoOfExecutedRules()) {
                    ruleExecutor.activateStage(EStage.RevertAgentMoving);
                    ruleExecutor.activateStage(EStage.Reset);
                }
            } else if (ruleExecutor.getCurrentStage() == EStage.RevertAgentMoving) { // RevertAgentMoving ステージ実行後
                // 定員ありスポットの定員条件が満たされているかをチェックして，満たされていない場合は RevertAgentMoving ステージにロールバック
                boolean capacityOver = false;
                for (TSpot spot : spotsWithCapacity) {
                    TRoleOfSpotWithCapacity role = (TRoleOfSpotWithCapacity) spot.getRole(ERoleName.SpotWithCapacity);
                    if (role.getCapacity() < spot.getNoOfAgents()) { // 定員オーバー
                        capacityOver = true;
                        break;
                    }
                }
                if (capacityOver) {
                    ruleExecutor.rollbackStage(EStage.RevertAgentMoving);
                }
            } else if (ruleExecutor.getCurrentStage() == EStage.Reset) { // Reset ステージ実行後
                // RevertAgentMoving, Reset ステージを非アクティブ化
                ruleExecutor.deactivateStage(EStage.RevertAgentMoving);
                ruleExecutor.deactivateStage(EStage.Reset);
            }
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown();
        noOfAgentsLogPW.close();
    }
}
