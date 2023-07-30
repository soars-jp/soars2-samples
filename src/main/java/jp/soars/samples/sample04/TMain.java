package jp.soars.samples.sample04;

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
import jp.soars.core.TRuleExecutor;
import jp.soars.core.TSOARSBuilder;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;
import jp.soars.utils.random.ICRandom;

/**
 * メインクラス
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
        List<Enum<?>> stages = List.of(EStage.DeterminingHealth, EStage.AgentMoving); // ステージリスト
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
        String pathOfLogDir = "logs" + File.separator + "sample04"; // ログディレクトリ
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
        //   - Home スポットを3つ
        //   - Company スポットを1つ
        //   - School スポットを1つ
        //   - Hospital スポットを1つ
        // *************************************************************************************************************

        int noOfHomes = 3; // 家の数
        List<TSpot> homes = spotManager.createSpots(ESpotType.Home, noOfHomes); // Homeスポットを生成．(Home1, Home2, ...)
        TSpot company = spotManager.createSpot(ESpotType.Company); // Companyスポットを生成．(Company)
        TSpot school = spotManager.createSpot(ESpotType.School); // Schoolスポットを生成．(School)
        TSpot hospital = spotManager.createSpot(ESpotType.Hospital); // Hospitalスポットを生成．(Hospital)

        // *************************************************************************************************************
        // エージェント作成
        //   - Father エージェントを3つ
        //     - 初期スポットは Home スポット
        //     - 役割として父親役割，共通役割，病人役割を持つ．
        //   - Child エージェントを3つ
        //     - 初期スポットは Home スポット
        //     - 役割として子供役割，共通役割，病人役割を持つ．
        // *************************************************************************************************************

        int noOfFathers = noOfHomes; // 父親の数は家の数と同じ．
        List<TAgent> fathers = agentManager.createAgents(EAgentType.Father, noOfFathers); // Fatherエージェントを生成．(Father1, Father2, ...)
        for (int i = 0; i < fathers.size(); ++i) {
            TAgent father = fathers.get(i); // i番目のエージェントを取り出す．
            TSpot home = homes.get(i); // i番目のエージェントの自宅を取り出す．
            father.initializeCurrentSpot(home); // 初期位置を自宅に設定する．

            TRole commonRole = new TRoleOfCommon(father, home, ERoleName.Father); // 共通役割を生成する．
            TRole fatherRole = new TRoleOfFather(father, home, company); // 父親役割を生成する．
            new TRoleOfSickPerson(father, home, hospital, new TTime("2:00:00"), ERoleName.Father); // 病人役割を生成する．治療時間は2時間とする．
            fatherRole.addChildRole(commonRole); // 共通役割を父親役割の子役割として登録する．これにより共通役割のアクティブ状態は父親役割のアクティブ状態と同じになる．
            father.activateRole(ERoleName.Father); // 父親役割をアクティブ化する．
        }

        int noOfChildren = noOfHomes; // 子供の数は家の数と同じ．
        List<TAgent> children = agentManager.createAgents(EAgentType.Child, noOfChildren); // Childエージェントを生成．(Child1, Child2, ...)
        for (int i = 0; i < children.size(); ++i) {
            TAgent child = children.get(i);// i番目のエージェントを取り出す．
            TSpot home = homes.get(i); // i番目のエージェントの自宅を取り出す．
            child.initializeCurrentSpot(home); // 初期位置を自宅に設定する．

            TRole commonRole = new TRoleOfCommon(child, home, ERoleName.Child); // 共通役割を生成する．
            TRole childRole = new TRoleOfChild(child, home, school); // 子供役割を生成する．
            new TRoleOfSickPerson(child, home, hospital, new TTime("3:00:00"), ERoleName.Child); // 病人役割を生成する．治療時間は3時間とする．
            childRole.addChildRole(commonRole); // 共通役割を子供役割の子役割として登録する．これにより共通役割のアクティブ状態は子供役割のアクティブ状態と同じになる．
            child.activateRole(ERoleName.Child);// 子供役割をアクティブ化する
        }

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        //   - 各時刻でエージェントの現在位置のログをとる (スポットログ, spot_log.csv)
        // *************************************************************************************************************

        // スポットログ用PrintWriter
        PrintWriter spotLogPW = new PrintWriter(new BufferedWriter(
                new FileWriter(pathOfLogDir + File.separator + "spot_log.csv")));
        // スポットログのカラム名出力
        spotLogPW.print("CurrentTime");
        for(TAgent agent : agentManager.getAgents()){
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
            for (TAgent agent : agentManager.getAgents()) {
                spotLogPW.print("," + agent.getCurrentSpotName());
            }
            spotLogPW.println();
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown(); // ルール実行器を終了する
        spotLogPW.close(); // スポットログを終了する
    }
}
