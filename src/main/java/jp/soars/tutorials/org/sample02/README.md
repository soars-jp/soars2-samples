前：[sample01:最も簡単なプログラム](src/main/java/jp/soars/tutorials/sample01/)
次：[sample03:確率的なルールの定義](src/main/java/jp/soars/tutorials/sample03/)

# sample02：臨時実行ルールによる相対時刻指定 <!-- omit in toc -->

- [シナリオとシミュレーション条件](#シナリオとシミュレーション条件)
- [シミュレーション定数の定義](#シミュレーション定数の定義)
- [ルールの定義](#ルールの定義)
  - [自宅から会社に移動するルール](#自宅から会社に移動するルール)
  - [会社から自宅に移動するルール](#会社から自宅に移動するルール)
- [役割の定義](#役割の定義)
  - [父親役割](#父親役割)
- [メインクラスの定義](#メインクラスの定義)


## シナリオとシミュレーション条件

以下のシナリオを考える．

- 3人の父親(Father1, Father2, Father3)は，それぞれ自宅(Home1, Home2, Home3)を持つ．
- 3人の父親は，9時に自宅から同じ会社(Company)に移動する．
- 3人の父親は，出社して32時間後にそれぞれの自宅に移動する．

シミュレーション条件

- エージェント : Father(3)
- スポット : Home(3), Company(1)
- ステージ : AgentMoving
- 時刻ステップ間隔：1時間 / step
- シミュレーション期間：7日間

## シミュレーション定数の定義

`EAgentType.java`
```java
public enum EAgentType {
    /** 父親 */
    Father
}
```
`ESpotType.java`
```java
public enum ESpotType {
    /** 自宅 */
    Home,
    /** 会社 */
    Company
}
```
`EStage.java`
```java
public enum EStage {
    /** エージェント移動ステージ */
    AgentMoving
}
```
`ERoleName.java`
```java
public enum ERoleName {
    /** 父親役割 */
    Father
}
```

## ルールの定義

- TRuleOfLeaveHome：自宅から会社に移動するルール
- TRuleOfReturnHome：会社から自宅に移動するルール

### 自宅から会社に移動するルール

sample01の自宅から会社に移動するルールを拡張して定義する．
会社から自宅に移動するルール，会社から自宅に移動するルールを実行するまでの時間間隔，会社から自宅に移動するルールを実行するステージを指定して，
会社から自宅に移動するルールを自宅から会社に移動するルールが実行された32時間後に実行されるように臨時実行ルールとして登録する．

`TRuleOfLeaveHome.java`
```java
public final class TRuleOfLeaveHome extends TAgentRule {

    /** 会社から自宅に移動するルール */
    private final TRuleOfReturnHome fReturnHomeRule;

    /** 会社から自宅に移動するルールを実行するまでの時間間隔 */
    private final TTime fIntervalTimeToReturnHomeRule;

    /** 会社から自宅に移動するルールの発火時刻計算用 */
    private final TTime fTimeOfReturnHomeRule;

    /** 会社から自宅に移動するルールを実行するステージ */
    private final Enum<?> fStageOfReturnHomeRule;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     * @param returnHomeRule 会社から自宅に移動するルール
     * @param intervalTimeToReturnHomeRule 会社から自宅に移動するルールを実行するまでの時間間隔
     * @param stageOfReturnHomeRule 会社から自宅に移動するルールを実行するステージ
     */
    public TRuleOfLeaveHome(String name, TRole owner,
            TRuleOfReturnHome returnHomeRule, String intervalTimeToReturnHomeRule, Enum<?> stageOfReturnHomeRule) {
        // 親クラスのコンストラクタを呼び出す．
        super(name, owner);
        fReturnHomeRule = returnHomeRule;
        fIntervalTimeToReturnHomeRule = new TTime(intervalTimeToReturnHomeRule);
        fTimeOfReturnHomeRule = new TTime();
        fStageOfReturnHomeRule = stageOfReturnHomeRule;
    }

    /**
     * ルールを実行する．
     * @param currentTime 現在時刻
     * @param currentStage 現在ステージ
     * @param spotManager スポット管理
     * @param agentManager エージェント管理
     * @param globalSharedVariables グローバル共有変数集合
     */
    @Override
    public final void doIt(TTime currentTime, Enum<?> currentStage, TSpotManager spotManager,
            TAgentManager agentManager, Map<String, Object> globalSharedVariables) {
        boolean debugFlag = true; // デバッグ情報出力フラグ
        TRoleOfFather role = (TRoleOfFather) getRole(ERoleName.Father); // エージェントに登録されている父親役割を取得
        if (isAt(role.getHome())) { // 自宅にいる場合
            // 会社に移動する
            moveTo(role.getCompany());
            // 移動ルールが正常に実行されたことをデバッグ情報としてルールログに出力
            appendToDebugInfo("success", debugFlag);

            // 現在時刻にインターバルを足した時刻を会社から自宅に移動するルールの発火時刻とする．
            fTimeOfReturnHomeRule.copyFrom(currentTime).add(fIntervalTimeToReturnHomeRule);
            // 会社から自宅に移動するルールを臨時実行ルールとして登録．
            fReturnHomeRule.setTimeAndStage(fTimeOfReturnHomeRule.getDay(), fTimeOfReturnHomeRule.getHour(),
                    fTimeOfReturnHomeRule.getMinute(), fTimeOfReturnHomeRule.getSecond(), fStageOfReturnHomeRule);
        } else { // 自宅にいない場合
            // 移動ルールが実行されなかったことをデバッグ情報としてルールログに出力
            appendToDebugInfo("fail", debugFlag);
        }
    }
}
```

### 会社から自宅に移動するルール

sample01と同様．

`TRuleOfReturnHome.java`
```java
public final class TRuleOfReturnHome extends TAgentRule {

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     */
    public TRuleOfReturnHome(String name, TRole owner) {
        // 親クラスのコンストラクタを呼び出す．
        super(name, owner);
    }

    /**
     * ルールを実行する．
     * @param currentTime 現在時刻
     * @param currentStage 現在ステージ
     * @param spotManager スポット管理
     * @param agentManager エージェント管理
     * @param globalSharedVariables グローバル共有変数集合
     */
    @Override
    public final void doIt(TTime currentTime, Enum<?> currentStage, TSpotManager spotManager,
            TAgentManager agentManager, Map<String, Object> globalSharedVariables) {
        boolean debugFlag = true; // デバッグ情報出力フラグ
        TRoleOfFather role = (TRoleOfFather) getRole(ERoleName.Father); // エージェントに登録されている父親役割を取得
        if (isAt(role.getCompany())) { // 会社にいる場合
            // 自宅に移動する
            moveTo(role.getHome());
            // 移動ルールが正常実行されたことをデバッグ情報としてルールログに出力
            appendToDebugInfo("success", debugFlag);
        } else { // 会社にいない場合
            // 移動ルールが実行されなかったことをデバッグ情報としてルールログに出力
            appendToDebugInfo("fail", debugFlag);
        }
    }
}
```

## 役割の定義

- TRoleOfFather：父親役割

### 父親役割

父親役割は会社から自宅に移動するルールのスケジューリングを行わない．
自宅から会社に移動するルールを実行した32時間後のエージェント移動ステージに会社から自宅に移動するルールが臨時実行ルールとしてスケジューリングされる．
また，24時間以上の時間をTTimeで指定する際は，dd/hh:mm:ss形式で指定する必要があるため，"0/32:00:00"となっていることに注意する必要がある．
もし"hh:mm:ss"形式で"32:00:00"と指定した場合は，"08:00:00"と解釈される．

`TRoleOfFather.java`
```java
public final class TRoleOfFather extends TRole {

    /** 自宅 */
    private final TSpot fHome;

    /** 会社 */
    private final TSpot fCompany;

    /** 家を出発するルール名 */
    private static final String RULE_NAME_OF_LEAVE_HOME = "LeaveHome";

    /** 家に帰るルール名 */
    private static final String RULE_NAME_OF_RETURN_HOME = "ReturnHome";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param home 自宅
     * @param company 会社
     */
    public TRoleOfFather(TAgent owner, TSpot home, TSpot company) {
        // 親クラスのコンストラクタを呼び出す．
        // 以下の2つの引数は省略可能で，その場合デフォルト値で設定される．
        // 第3引数 : この役割が持つルール数 (デフォルト値 10)
        // 第4引数 : この役割が持つ子役割数 (デフォルト値 5)
        super(ERoleName.Father, owner, 2, 0);

        fHome = home;
        fCompany = company;

        // 役割が持つルールの登録
        // 会社にいるならば，自宅に移動する．スケジューリングはTRuleOfLeaveHomeの中で行われる．
        TRuleOfReturnHome returnHomeRule = new TRuleOfReturnHome(RULE_NAME_OF_RETURN_HOME, this);

        // 自宅にいるならば，会社に移動する．毎日9時，エージェント移動ステージに発火するように予約する．
        // 32:00:00ではなく0/32:00:00と表記する点に注意．32:00:00はTTimeクラスで8:00:00と解釈され，32時間後を表現できない．
        new TRuleOfLeaveHome(RULE_NAME_OF_LEAVE_HOME, this, returnHomeRule, "0/32:00:00", EStage.AgentMoving)
                .setTimeAndStage(9, 0, 0, EStage.AgentMoving);
    }

    /**
     * 自宅を返す．
     * @return 自宅
     */
    public final TSpot getHome() {
        return fHome;
    }

    /**
     * 会社を返す．
     * @return 会社
     */
    public final TSpot getCompany() {
        return fCompany;
    }
}
```

## メインクラスの定義

sample01との差分はログ出力先ディレクトリを変更したのみである．

`TMain.java`

```java
public class TMain {

    public static void main(String[] args) throws IOException {
        // *************************************************************************************************************
        // TSOARSBuilderの必須設定項目．
        //   - simulationStart: シミュレーション開始時刻
        //   - simulationEnd: シミュレーション終了時刻
        //   - tick: 1ステップの時間間隔
        //   - stages: 使用するステージリスト(実行順)
        //   - agentTypes: 使用するエージェントタイプ集合
        //   - spotTypes: 使用するスポットタイプ集合
        // *************************************************************************************************************

        String simulationStart = "0/00:00:00"; // シミュレーション開始時刻
        String simulationEnd = "7/00:00:00"; // シミュレーション終了時刻
        String tick = "1:00:00"; // １ステップの時間間隔
        List<Enum<?>> stages = List.of(EStage.AgentMoving); // ステージリスト
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
        String pathOfLogDir = "logs" + File.separator + "tutorials" + File.separator + "sample02"; // ログディレクトリ
        builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv") // ルールログ出力設定
               .setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv"); // ランタイムログ出力設定

        // ルールログのデバッグ情報出力設定
        builder.setRuleDebugMode(ERuleDebugMode.LOCAL); // ローカル設定に従う

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
        //   - Home(3)
        //   - Company(1)
        // *************************************************************************************************************

        int noOfHomes = 3; // 家の数
        List<TSpot> homes = spotManager.createSpots(ESpotType.Home, noOfHomes); // Homeスポットを生成．(Home1, Home2, ...)
        TSpot company = spotManager.createSpot(ESpotType.Company); // Companyスポットを生成．(Company)

        // *************************************************************************************************************
        // エージェント作成
        //   - Father(3)
        //     - 初期スポットは Home スポット
        //     - 役割として父親役割を持つ．
        // *************************************************************************************************************

        int noOfFathers = noOfHomes; // 父親の数は家の数と同じ．
        List<TAgent> fathers = agentManager.createAgents(EAgentType.Father, noOfFathers); // Fatherエージェントを生成．(Father1, Father2, ...)
        for (int i = 0; i < fathers.size(); ++i) {
            TAgent father = fathers.get(i); // i番目のエージェントを取り出す．
            TSpot home = homes.get(i); // i番目のエージェントの自宅を取り出す．
            father.initializeCurrentSpot(home); // 初期位置を自宅に設定する．
            new TRoleOfFather(father, home, company); // 父親役割を生成する．
            father.activateRole(ERoleName.Father); // 父親役割をアクティブ化する．
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
        for (TAgent agent : fathers) {
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
            for (TAgent a : fathers) {
                spotLogPW.print("," + a.getCurrentSpotName());
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
```

前：[sample01:最も簡単なプログラム](src/main/java/jp/soars/tutorials/sample01/)
次：[sample03:確率的なルールの定義](src/main/java/jp/soars/tutorials/sample03/)
