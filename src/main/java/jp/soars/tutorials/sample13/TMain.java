package jp.soars.tutorials.sample13;

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
import jp.soars.core.TRole;
import jp.soars.core.TRule;
import jp.soars.core.TRuleExecutor;
import jp.soars.core.TSOARSBuilder;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.enums.ERuleDebugMode;
import jp.soars.tutorials.sample13.module1.EDay;
import jp.soars.tutorials.sample13.module1.EModule1RoleName;
import jp.soars.tutorials.sample13.module1.EModule1Stage;
import jp.soars.tutorials.sample13.module1.TRoleOfChild;
import jp.soars.tutorials.sample13.module1.TRoleOfFather;
import jp.soars.tutorials.sample13.module2.EModule2Stage;
import jp.soars.tutorials.sample13.module2.TRoleOfDeterminingHealth;
import jp.soars.tutorials.sample13.module2.TRoleOfSickPerson;
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
        List<Enum<?>> stages = List.of(EModule2Stage.DeterminingHealth,
                                       EModule2Stage.AgentMoving,
                                       EModule1Stage.AgentMoving,
                                       EModule2Stage.RecoveringFromSick);
        Set<Enum<?>> agentTypes = new HashSet<>();
        Collections.addAll(agentTypes, EAgentType.values());
        Set<Enum<?>> spotTypes = new HashSet<>();
        Collections.addAll(spotTypes, ESpotType.values());
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes);

        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目
        // *************************************************************************************************************

        // module1とmodule2のAgentMovingステージをマージして同一のステージとみなす
        // このとき第1引数で指定したステージに統一される．
        builder.mergeStages(EModule2Stage.AgentMoving, EModule1Stage.AgentMoving);

        // マスター乱数発生器のシード値設定
        long seed = 0L;
        builder.setRandomSeed(seed);

        // ルールログとランタイムログの出力設定
        String pathOfLogDir = "logs" + File.separator + "tutorials" + File.separator + "sample13";
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
        //   - School:School
        //   - Hospital:Hospital
        // *************************************************************************************************************

        int noOfHomes = 3; // 家の数
        List<TSpot> homes = spotManager.createSpots(ESpotType.Home, noOfHomes);
        TSpot company = spotManager.createSpot(ESpotType.Company);
        TSpot school = spotManager.createSpot(ESpotType.School);
        TSpot hospital = spotManager.createSpot(ESpotType.Hospital);

        // *************************************************************************************************************
        // エージェント作成
        //   - Father:Father1, Father2, Father3
        //     - 初期スポット:Home
        //     - 役割:健康状態決定役割，父親役割，病人役割
        //   - Child:Child1, Child2, Child3
        //     - 初期スポット:Home
        //     - 役割:健康状態決定役割，子ども役割，病人役割
        // *************************************************************************************************************

        int noOfFathers = noOfHomes; // 父親の数は家の数と同じ．
        List<TAgent> fathers = agentManager.createAgents(EAgentType.Father, noOfFathers);
        for (int i = 0; i < noOfFathers; ++i) {
            TAgent father = fathers.get(i); // i番目の父親エージェント
            TSpot home = homes.get(i); // i番目の父親エージェントの自宅
            father.initializeCurrentSpot(home); // 初期スポットを自宅に設定
            TRole roleOfCommon = new TRoleOfDeterminingHealth(father, home, EModule1RoleName.Father); // 健康状態決定役割を作成
            TRole roleOfFather = new TRoleOfFather(father, home, company); // 父親役割を作成
            new TRoleOfSickPerson(father, home, hospital, 2, EModule1RoleName.Father); // 病人役割を作成
            roleOfFather.addChildRole(roleOfCommon); // 健康状態決定役割を父親役割の子役割に設定
            father.activateRole(EModule1RoleName.Father); // 父親役割をアクティブ化

            // 父親役割のルール上書き
            // 上書きの流れは，元のルールの削除 -> 新しいルールの登録．
            // 元のルールを削除しない場合もルール名が同じであれば上書きはされるが，ワーニングメッセージが表示される．
            // ワーニングメッセージは TSOARSBuilder の設定でOFFにできるが，他のワーニングメッセージも出力されなくなるため注意．

            // 登録されている，確率的に自宅から会社に移動するルールを削除
            roleOfFather.removeRule(TRoleOfFather.RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY_9);
            roleOfFather.removeRule(TRoleOfFather.RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY_10);
            roleOfFather.removeRule(TRoleOfFather.RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY_11);

            // 登録されている，会社から自宅に移動するルールを取得
            TRule ruleOfReturnHome = roleOfFather.getRule(TRoleOfFather.RULE_NAME_OF_MOVE_FROM_COMPANY_TO_HOME);

            // 新しい確率的に自宅から会社に移動するルール．9:00:00, 10:00:00, 11:00:00/エージェント移動ステージに定時実行ルールとして予約する．
            new TRuleOfStochasticallyAgentMoving(TRoleOfFather.RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY_9,
                    roleOfFather, 0.5, home, company, ruleOfReturnHome, "8:00:00", EModule1Stage.AgentMoving)
                    .setTimeAndStage(9, 0, 0, EModule1Stage.AgentMoving);

            new TRuleOfStochasticallyAgentMoving(TRoleOfFather.RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY_10,
                    roleOfFather, 0.6, home, company, ruleOfReturnHome, "8:00:00", EModule1Stage.AgentMoving)
                    .setTimeAndStage(10, 0, 0, EModule1Stage.AgentMoving);

            new TRuleOfStochasticallyAgentMoving(TRoleOfFather.RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY_11,
                    roleOfFather, 1.0, home, company, ruleOfReturnHome, "8:00:00", EModule1Stage.AgentMoving)
                    .setTimeAndStage(11, 0, 0, EModule1Stage.AgentMoving);
        }

        int noOfChildren = noOfHomes; // 子どもの数は家の数と同じ．
        List<TAgent> children = agentManager.createAgents(EAgentType.Child, noOfChildren);
        for (int i = 0; i < noOfChildren; ++i) {
            TAgent child = children.get(i); // i番目の子どもエージェント
            TSpot home = homes.get(i); // i番目の子どもエージェントの自宅
            child.initializeCurrentSpot(home); // 初期スポットを自宅に設定
            TRole roleOfCommon = new TRoleOfDeterminingHealth(child, home, EModule1RoleName.Child); // 健康状態決定役割を作成
            TRole roleOfChild = new TRoleOfChild(child, home, school); // 子ども役割を作成
            new TRoleOfSickPerson(child, home, hospital, 3, EModule1RoleName.Child); // 病人役割を作成
            roleOfChild.addChildRole(roleOfCommon); // 健康状態決定役割を子ども役割の子役割に設定
            child.activateRole(EModule1RoleName.Child); // 子ども役割をアクティブ化

            // 子ども役割のルール上書き
            // 時間変更したい場合は，TRuleのresetTimeAndStageメソッドで時間とステージの設定を削除した後，再登録する．
            // 登録されている，学校から自宅に移動するルールを取得
            TRule agentMovingRule = roleOfChild.getRule(TRoleOfChild.RULE_NAME_OF_MOVE_FROM_SCHOOL_TO_HOME);
            agentMovingRule.resetTimeAndStage();
            agentMovingRule.setTimeAndStage(17, 0, 0, EModule1Stage.AgentMoving);
        }

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        //   - スポットログ:各時刻での各エージェントの現在位置ログ
        // *************************************************************************************************************

        // スポットログ用PrintWriter
        PrintWriter spotLogPW = new PrintWriter(new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "spot_log.csv")));
        // スポットログのカラム名出力
        spotLogPW.print("CurrentTime,Day");
        for (TAgent agent : agentManager.getAgents()) {
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
            spotLogPW.print(",");
            spotLogPW.print(EDay.values()[ruleExecutor.getCurrentTime().getDay() % 7]);
            for (TAgent agent : agentManager.getAgents()) {
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
    }
}
