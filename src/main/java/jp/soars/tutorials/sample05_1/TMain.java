package jp.soars.tutorials.sample05_1;

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
        List<Enum<?>> stages = List.of(EStage.DeterminingHealth,
                                       EStage.AgentMoving,
                                       EStage.RecoveringFromSick);
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
        String pathOfLogDir = "logs" + File.separator + "tutorials" + File.separator + "sample05_1";
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
        //   - Home:Home1, Home2, Home3
        //   - Company:Company
        //   - Hospital:Hospital
        // *************************************************************************************************************

        int noOfHomes = 3; // 家の数
        List<TSpot> homes = spotManager.createSpots(ESpotType.Home, noOfHomes);
        TSpot company = spotManager.createSpot(ESpotType.Company);
        TSpot hospital = spotManager.createSpot(ESpotType.Hospital);

        // *************************************************************************************************************
        // エージェント作成
        //   - Father:Father1, Father2, Father3
        //     - 初期スポット:Home
        //     - 役割:父親役割，病人役割
        // *************************************************************************************************************

        int noOfFathers = noOfHomes; // 父親の数は家の数と同じ．
        List<TAgent> fathers = agentManager.createAgents(EAgentType.Father, noOfFathers);
        for (int i = 0; i < noOfFathers; ++i) {
            TAgent father = fathers.get(i); // i番目の父親エージェント
            TSpot home = homes.get(i); // i番目の父親エージェントの自宅
            father.initializeCurrentSpot(home); // 初期スポットを自宅に設定
            new TRoleOfFather(father, home, company); // 父親役割を作成
            new TRoleOfSickPerson(father); // 病人役割を作成
            father.activateRole(ERoleName.Father); // 父親役割をアクティブ化
        }

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        //   - スポットログ:各時刻での各エージェントの現在位置ログ
        // *************************************************************************************************************

        // スポットログ用PrintWriter
        PrintWriter spotLogPW = new PrintWriter(new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "spot_log.csv")));
        // スポットログのカラム名出力
        spotLogPW.print("CurrentTime,Day");
        for (TAgent father : fathers) {
            spotLogPW.print(',');
            spotLogPW.print(father.getName());
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
            spotLogPW.print(",");
            spotLogPW.print(EDay.values()[ruleExecutor.getCurrentTime().getDay() % 7]);
            for (TAgent father : fathers) {
                spotLogPW.print(',');
                spotLogPW.print(father.getCurrentSpotName());
            }
            spotLogPW.println();
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown();
        spotLogPW.close();
    }
}
