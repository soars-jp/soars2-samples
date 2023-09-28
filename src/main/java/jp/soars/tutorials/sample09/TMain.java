package jp.soars.tutorials.sample09;

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
        //   - layers:使用するレイヤー集合
        //   - defaultLayer:デフォルトレイヤー
        // *************************************************************************************************************

        String simulationStart = "0/00:00:00";
        String simulationEnd = "7/00:00:00";
        String tick = "1:00:00";
        List<Enum<?>> stages = List.of(EStage.AgentMoving);
        Set<Enum<?>> agentTypes = new HashSet<>();
        Collections.addAll(agentTypes, EAgentType.values());
        Set<Enum<?>> spotTypes = new HashSet<>();
        Collections.addAll(spotTypes, ESpotType.values());
        Set<Enum<?>> layers = new HashSet<>();
        Collections.addAll(layers, ELayer.values());
        ELayer defaultLayer = ELayer.Real;
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes, layers, defaultLayer);

        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目
        // *************************************************************************************************************

        // エージェント移動ステージを毎時刻ルールが実行される定期実行ステージとして登録
        builder.setPeriodicallyExecutedStage(EStage.AgentMoving, simulationStart, tick);

        // マスター乱数発生器のシード値設定
        long seed = 0L;
        builder.setRandomSeed(seed);

        // ルールログとランタイムログの出力設定
        String pathOfLogDir = "logs" + File.separator + "tutorials" + File.separator + "sample09";
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
        //   - Spot:Spot1-Spot10 (Real)
        //   - Spot:Spot11-Spot20 (SNS)
        // *************************************************************************************************************

        int noOfSpots = 10; // スポットの数
        List<TSpot> realSpots = spotManager.createSpots(ESpotType.Spot, noOfSpots, ELayer.Real);
        List<TSpot> snsSpots = spotManager.createSpots(ESpotType.Spot, noOfSpots, ELayer.SNS);

        // *************************************************************************************************************
        // エージェント作成
        //   - Agent:Agent1-Agent10
        //     - 初期スポット:Spot
        //     - 役割:エージェント役割
        // *************************************************************************************************************

        int noOfAgents = noOfSpots; // エージェントの数はスポットの数と同じ．
        List<TAgent> agents = agentManager.createAgents(EAgentType.Agent, noOfAgents);
        for (int i = 0; i < noOfAgents; ++i) {
            TAgent agent = agents.get(i); // i番目のエージェント
            TSpot realSpot = realSpots.get(i); // i番目の現実スポット
            TSpot snsSpot = snsSpots.get(i); // i番目のSNSスポット
            agent.initializeCurrentSpot(realSpot); // 現実の初期スポット設定
            agent.initializeCurrentSpot(snsSpot); // SNSの初期スポット設定
            new TRoleOfAgent(agent); // エージェント役割作成
            agent.activateRole(ERoleName.Agent); // エージェント役割をアクティブ化
        }

        // *************************************************************************************************************
        // グローバル共有変数集合の初期値設定
        // *************************************************************************************************************

        globalSharedVariableSet.put(TGlobalSharedVariableSetKey.MOVE, 0);
        globalSharedVariableSet.put(TGlobalSharedVariableSetKey.NO_MOVE, 0);

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        //   - グローバル共有変数集合ログ
        // *************************************************************************************************************

        // グローバル共有変数集合ログ用PrintWriter
        PrintWriter gsbsPW = new PrintWriter(new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "global_shared_variable_log.csv")));
        // グローバル共有変数集合ログのカラム名出力
        gsbsPW.print("CurrentTime,");
        gsbsPW.print(TGlobalSharedVariableSetKey.MOVE);
        gsbsPW.print(',');
        gsbsPW.println(TGlobalSharedVariableSetKey.NO_MOVE);

        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        // 1ステップ分のルールを実行 (ruleExecutor.executeStage()で1ステージ毎に実行することもできる)
        // 実行された場合:true，実行されなかった(終了時刻)場合は:falseが帰ってくるため，while文で回すことができる．
        while (ruleExecutor.executeStep()) {
            // 標準出力に現在時刻を表示する
            System.out.println(ruleExecutor.getCurrentTime());

            // グローバル共有変数集合ログ出力
            gsbsPW.print(ruleExecutor.getCurrentTime());
            gsbsPW.print(',');
            gsbsPW.print(globalSharedVariableSet.get(TGlobalSharedVariableSetKey.MOVE));
            gsbsPW.print(',');
            gsbsPW.println(globalSharedVariableSet.get(TGlobalSharedVariableSetKey.NO_MOVE));
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown();
        gsbsPW.close();
    }
}
