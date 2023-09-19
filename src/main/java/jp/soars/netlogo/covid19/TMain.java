package jp.soars.netlogo.covid19;

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
import jp.soars.netlogo.covid19.agent.TRoleOfCOVID19;
import jp.soars.netlogo.covid19.agent.TRoleOfResident;
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
        String simulationEnd = "180/00:00:00";
        String tick = "1:00:00";
        List<Enum<?>> stages = List.of();
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
        String pathOfLogDir = "logs" + File.separator + "tutorials" + File.separator + "sample01";
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
        //   - Home(36)
        //   - ClassRoom(1)
        //   - Gym(1)
        //   - Office(1)
        //   - Hospital(1)
        //   - Grave(1)
        // *************************************************************************************************************

        int noOfHomes = 36;
        int noOfClassRoom = 1;
        int noOfGym = 1;
        int noOfOffice = 1;
        int noOfHospital = 1;
        int noOfGrave = 1;
        List<TSpot> homes = spotManager.createSpots(ESpotType.Home, noOfHomes, , );
        TSpot classRoom = spotManager.createSpot(ESpotType.ClassRoom, , );
        TSpot gym = spotManager.createSpot(ESpotType.ClassRoom, , );
        TSpot office = spotManager.createSpot(ESpotType.ClassRoom, , );
        TSpot hospital = spotManager.createSpot(ESpotType.ClassRoom, , );
        TSpot grave = spotManager.createSpot(ESpotType.ClassRoom, , );

        // *************************************************************************************************************
        // エージェント作成
        //  - Resident
        // *************************************************************************************************************

        int noOfResidents = 100; // 住人人数
        List<TAgent> residents = agentManager.createAgents(EAgentType.Resident, noOfResidents, );
        for (TAgent resident : residents) {
            TSpot home = homes.get(random.nextInt(noOfHomes));
            double p = random.nextDouble();
            EAgeGroup ageGroup;
            if (p <= 0.2) {
                ageGroup = EAgeGroup.Young;
            } else if (p <= 0.6) {
                ageGroup = EAgeGroup.Middle;
            } else {
                ageGroup = EAgeGroup.Old;
            }

            new TRoleOfCOVID19(resident);
            new TRoleOfResident(resident, ageGroup, home);

            resident.initializeCurrentSpot(home);
        }

        // 初期感染者を設定．
        int noOfInitialInfected = 5;
        // 非復元抽出．WithoutCopy付きは引数に入力したリストの順番が変わるので注意(破壊的操作をする)．
        // 順番を変えたくない場合は chooseWithoutReplacement メソッドを利用する．
        List<TAgent> patients = random.chooseWithoutReplacementWithoutCopy(residents, noOfInitialInfected);
        for (TAgent patient : patients) {
            TRoleOfCOVID19 role = (TRoleOfCOVID19) patient.getRole(ERoleName.COVID19);
            role.setInfected(true);
            role.setDiseaseLevel(EDiseaseLevel.LATENT_NO_CONTAGION);
        }

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        // *************************************************************************************************************


        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        // 1ステップ分のルールを実行 (ruleExecutor.executeStage()で1ステージ毎に実行することもできる)
        // 実行された場合:true，実行されなかった(終了時刻)場合は:falseが帰ってくるため，while文で回すことができる．
        while (ruleExecutor.executeStep()) {
            // 標準出力に現在時刻を表示する
            System.out.println(ruleExecutor.getCurrentTime());
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown();
    }
}
