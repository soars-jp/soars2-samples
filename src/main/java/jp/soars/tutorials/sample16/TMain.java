package jp.soars.tutorials.sample16;

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
        List<Enum<?>> stages = List.of(EStage.DynamicAdd, EStage.DynamicDelete);
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

        // ランタイムログの出力設定
        String pathOfLogDir = "logs" + File.separator + "tutorials" + File.separator + "sample16";
        builder.setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv");
        builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv");

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
        //   - Dummy:Dummy1-Dummy5
        // *************************************************************************************************************

        int noOfDummies = 5;
        spotManager.createSpots(ESpotType.Dummy, noOfDummies);

        // *************************************************************************************************************
        // エージェント作成
        //   - Creator:Creator1-Creator3
        //     - 役割:クリエイター役割
        //   - Killer:Killer1-Killer3
        //     - 役割:キラー役割
        //   - Dummy:Dummy1-Dummy5
        // *************************************************************************************************************

        // Creator エージェントを作成
        int noOfCreators = 3;
        List<TAgent> creators = agentManager.createAgents(EAgentType.Creator, noOfCreators);
        for (int i = 0; i < noOfCreators; ++i) {
            TAgent creator = creators.get(i); // i番目のCreatorを取り出す．
            new TRoleOfCreator(creator); // クリエイター役割を生成する．
            creator.activateRole(ERoleName.Creator); // クリエイター役割をアクティブ化する．
        }

        // Killer エージェントを作成
        int noOfKillers = 3;
        List<TAgent> killers = agentManager.createAgents(EAgentType.Killer, noOfKillers);
        for (int i = 0; i < noOfKillers; ++i) {
            TAgent killer = killers.get(i); // i番目のKillerを取り出す．
            new TRoleOfKiller(killer); // キラー役割を生成する．
            killer.activateRole(ERoleName.Killer); // キラー役割をアクティブ化する．
        }

        // Dummy エージェントを作成
        agentManager.createAgents(EAgentType.Dummy, noOfDummies);

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        //   - スポットログ:各時刻で存在するスポット名の一覧 (txtファイル)
        //   - エージェントログ:各時刻で存在するエージェント名の一覧 (txtファイル)
        // *************************************************************************************************************

        PrintWriter agentLogPW = new PrintWriter(new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "agent_log.txt")));
        PrintWriter spotLogPW = new PrintWriter(new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "spot_log.txt")));

        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        // 1ステップ分のルールを実行 (ruleExecutor.executeStage()で1ステージ毎に実行することもできる)
        // 実行された場合:true，実行されなかった(終了時刻)場合は:falseが帰ってくるため，while文で回すことができる．
        while (ruleExecutor.executeStep()) {
            // 標準出力に現在時刻を表示する
            System.out.println(ruleExecutor.getCurrentTime());

            // ログ出力
            agentLogPW.print(ruleExecutor.getCurrentTime());
            for (TAgent agent : agentManager.getAgents()) {
                agentLogPW.print(",");
                agentLogPW.print(agent.getName());
            }
            agentLogPW.println();

            spotLogPW.print(ruleExecutor.getCurrentTime());
            for (TSpot spot : spotManager.getSpots()) {
                spotLogPW.print(",");
                spotLogPW.print(spot.getName());
            }
            spotLogPW.println();
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown();
        agentLogPW.close();
        spotLogPW.close();
    }
}
