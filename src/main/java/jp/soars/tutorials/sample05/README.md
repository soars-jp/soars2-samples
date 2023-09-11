前：
次：
TODO:
# sample05:役割のアクティブ制御 <!-- omit in toc -->

- [シナリオとシミュレーション条件](#シナリオとシミュレーション条件)
- [シミュレーション定数の定義](#シミュレーション定数の定義)
- [ルールの定義](#ルールの定義)
  - [TRuleOfStochasticallyLeaveHome:確率的に自宅から会社に移動するルール](#truleofstochasticallyleavehome確率的に自宅から会社に移動するルール)
  - [TRuleOfReturnHome:会社から自宅に移動するルール](#truleofreturnhome会社から自宅に移動するルール)
- [役割の定義](#役割の定義)
  - [TRoleOfFather:父親役割](#troleoffather父親役割)
- [メインクラスの定義](#メインクラスの定義)


## シナリオとシミュレーション条件

以下のシナリオを考える．

- 3人の父親(Father1, Father2, Father3)は，それぞれ自宅(Home1, Home2, Home3)を持つ．
- 父親は，平日(土日以外)は50%の確率で9時，30%の確率で10時，20%の確率で11時に自宅から同じ会社(Company)に移動する．
- 父親は，出社して8時間後にそれぞれの自宅に移動する．
- 父親は，休日(土日)は会社に移動せずそれぞれの自宅にいる．
- 父親は，6時に25%の確率で病人になる．
- 病人は，10時に自宅から病院(Hospital)に移動する．
- 病人は，病院に移動して2時間後にそれぞれの自宅に移動する．

シミュレーション条件

- エージェント : Father(3)
- スポット : Home(3), Company(1), Hospital(1)
- ステージ : DeterminingHealth, AgentMoving
- 時刻ステップ間隔：1時間 / step
- シミュレーション期間：7日間


## シミュレーション定数の定義

sample03に加えて，曜日を表すenumクラスEDayを定義する．
`EDay.java`
```java
public enum EDay {
    /** 日曜日 */
    Sunday,
    /** 月曜日 */
    Monday,
    /** 火曜日 */
    Tuesday,
    /** 水曜日 */
    Wednesday,
    /** 木曜日 */
    Thursday,
    /** 金曜日 */
    Friday,
    /** 土曜日 */
    Saturday
}
```
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

### TRuleOfStochasticallyLeaveHome:確率的に自宅から会社に移動するルール

sample03のTRuleOfStochasticallyLeaveHomeを拡張する．
現在時刻の日付を7で割った余りで曜日を計算し，土曜日か日曜日の場合は会社に移動しないように変更する．

`TRuleOfStochasticallyLeaveHome.java`
```java
public final class TRuleOfStochasticallyLeaveHome extends TAgentRule {

    /** 会社から自宅に移動するルール */
    private final TRuleOfReturnHome fReturnHomeRule;

    /** 会社から自宅に移動するルールを実行するまでの時間間隔 */
    private final TTime fIntervalTimeReturnHome;

    /** 会社から自宅に移動するルールの発火時刻計算用 */
    private final TTime fTimeOfReturnHome;

    /** 会社から自宅に移動するルールを実行するステージ */
    private final Enum<?> fStageOfReturnHome;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     * @param returnHomeRule 会社から自宅に移動するルール
     * @param intervalTimeReturnHome 会社から自宅に移動するルールを実行するまでの時間間隔
     * @param stageOfReturnHome 会社から自宅に移動するルールを実行するステージ
     */
    public TRuleOfStochasticallyLeaveHome(String name, TRole owner,
            TRuleOfReturnHome returnHomeRule, String intervalTimeReturnHome, Enum<?> stageOfReturnHome) {
        // 親クラスのコンストラクタを呼び出す．
        super(name, owner);
        fReturnHomeRule = returnHomeRule;
        fIntervalTimeReturnHome = new TTime(intervalTimeReturnHome);
        fTimeOfReturnHome = new TTime();
        fStageOfReturnHome = stageOfReturnHome;
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

        // 日付を7で割ったあまり番目の曜日を取得．
        // どの曜日が何番になっているかはenumの定義順による．
        EDay day = EDay.values()[currentTime.getDay() % 7];
        if (day != EDay.Sunday && day != EDay.Saturday) { // 土日でない場合は会社に移動して，会社から自宅に移動するルールが発火するように登録．
            TRoleOfFather role = (TRoleOfFather) getOwnerRole(); // 父親役割(このルールを持っている役割)を取得
            if (isAt(role.getHome())) { // 自宅にいる場合
                // 会社に移動する
                moveTo(role.getCompany());
                // 移動ルールが正常に実行されたことをデバッグ情報としてルールログに出力
                appendToDebugInfo("success", debugFlag);

                // 現在時刻にインターバルを足した時刻を会社から自宅に移動するルールの発火時刻とする．
                fTimeOfReturnHome.copyFrom(currentTime).add(fIntervalTimeReturnHome);
                // 会社から自宅に移動するルールを臨時実行ルールとして登録．
                fReturnHomeRule.setTimeAndStage(fTimeOfReturnHome.getDay(), fTimeOfReturnHome.getHour(),
                        fTimeOfReturnHome.getMinute(), fTimeOfReturnHome.getSecond(), fStageOfReturnHome);
            } else { // 自宅にいない場合
                // 移動ルールが実行されなかったことをデバッグ情報としてルールログに出力
                appendToDebugInfo("fail", debugFlag);
            }
        }

        // 次の日の9時(50%)，10時(30%)，11時(20%)のエージェント移動ステージに自分自身を再予約する．
        double p = getRandom().nextDouble(); // [0, 1]のdouble
        int hour = -1;
        if (p <= 0.5) {
            hour = 9; // 50%
        } else if (p <= 0.8) {
            hour = 10; // 30%
        } else {
            hour = 11; // 20%
        }
        setTimeAndStage(currentTime.getDay() + 1, hour, 0, 0, EStage.AgentMoving);
        // 設定された時刻をデバッグ情報としてルールログに出力
        appendToDebugInfo(" next time = " + hour, debugFlag);
    }
}
```

### TRuleOfReturnHome:会社から自宅に移動するルール

sample03と同じ．

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
        TRoleOfFather role = (TRoleOfFather) getOwnerRole(); // 父親役割(このルールを持っている役割)を取得
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

### TRoleOfFather:父親役割

sample03と同じ．

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

        // 自宅にいるならば，会社に移動する．9時(50%)，10時(30%)，11時(20%)のエージェント移動ステージに臨時実行ルールとして予約する．
        // 初日以降は，ルール自身が臨時実行ルールとして翌日の実行時間に再予約する．
        double p = getRandom().nextDouble(); // [0, 1]のdouble
        int hour = -1;
        if (p <= 0.5) {
            hour = 9; // 50%
        } else if (p <= 0.8) {
            hour = 10; // 30%
        } else {
            hour = 11; // 20%
        }
        new TRuleOfStochasticallyLeaveHome(RULE_NAME_OF_LEAVE_HOME, this, returnHomeRule, "8:00:00", EStage.AgentMoving)
                .setTimeAndStage(0, hour, 0, 0, EStage.AgentMoving);
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

sample03のメインクラスのログ出力ディレクトリを変更する．
また，スポットログに曜日を出力するように変更する．

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
        String pathOfLogDir = "logs" + File.separator + "tutorials" + File.separator + "sample04"; // ログディレクトリ
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
        List<TSpot> homes = spotManager.createSpots(ESpotType.Home, noOfHomes); // Homeスポットを生成．(Home1, Home2, Home3)
        TSpot company = spotManager.createSpot(ESpotType.Company); // Companyスポットを1つ生成．(Company)

        // *************************************************************************************************************
        // エージェント作成
        //   - Father(3)
        //     - 初期スポットは Home
        //     - 父親役割を持つ．
        // *************************************************************************************************************

        int noOfFathers = noOfHomes; // 父親の数は家の数と同じ．
        List<TAgent> fathers = agentManager.createAgents(EAgentType.Father, noOfFathers); // Fatherエージェントを生成．(Father1, Father2, Father3)
        for (int i = 0; i < noOfFathers; ++i) {
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
        PrintWriter spotLogPW = new PrintWriter(new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "spot_log.csv")));
        // スポットログのカラム名出力
        spotLogPW.print("CurrentTime,Day");
        for (TAgent father : fathers) {
            spotLogPW.print("," + father.getName());
        }
        spotLogPW.println();

        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        while (ruleExecutor.executeStep()) {
            // 標準出力に現在時刻を表示する
            System.out.println(ruleExecutor.getCurrentTime());

            // スポットログ出力
            spotLogPW.print(ruleExecutor.getCurrentTime());
            spotLogPW.print("," + EDay.values()[ruleExecutor.getCurrentTime().getDay() % 7]);
            for (TAgent father : fathers) {
                spotLogPW.print("," + father.getCurrentSpotName());
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

前：
次：
TODO:
