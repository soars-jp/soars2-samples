前：
次：
TODO:

# sample04:曜日概念の導入 <!-- omit in toc -->

- [シナリオとシミュレーション条件](#シナリオとシミュレーション条件)
- [シミュレーション定数の定義](#シミュレーション定数の定義)
- [ルールの定義](#ルールの定義)
  - [TRuleOfStochasticallyMoveFromHomeToCompanyOnWeekdays:平日に確率的に自宅から会社に移動するルール](#truleofstochasticallymovefromhometocompanyonweekdays平日に確率的に自宅から会社に移動するルール)
  - [TRuleOfMoveFromCompanyToHome:会社から自宅に移動するルール](#truleofmovefromcompanytohome会社から自宅に移動するルール)
- [役割の定義](#役割の定義)
  - [TRoleOfFather:父親役割](#troleoffather父親役割)
- [メインクラスの定義](#メインクラスの定義)


## シナリオとシミュレーション条件

以下のシナリオを考える．

- 3人の父親(Father1, Father2, Father3)は，それぞれ自宅(Home1, Home2, Home3)を持つ．
- 父親は，平日(土日以外)は50%の確率で9時，30%の確率で10時，20%の確率で11時に自宅から同じ会社(Company)に移動する．
- 父親は，会社に移動してから8時間後に会社からそれぞれの自宅に移動する．
- 父親は，休日(土日)は会社に移動せず自宅にいる．

シミュレーション条件

- エージェント : Father(3)
- スポット : Home(3), Company(1)
- ステージ : AgentMoving
- 時刻ステップ間隔：1時間 / step
- シミュレーション期間：7日間

## シミュレーション定数の定義

sample03に追加して，曜日を表すenumクラスEDayを新たに定義する．

`EDay.java`

```Java
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

```Java
public enum EAgentType {
    /** 父親 */
    Father
}
```

`ESpotType.java`

```Java
public enum ESpotType {
    /** 自宅 */
    Home,
    /** 会社 */
    Company
}
```

`EStage.java`

```Java
public enum EStage {
    /** エージェント移動ステージ */
    AgentMoving
}
```

`ERoleName.java`

```Java
public enum ERoleName {
    /** 父親役割 */
    Father
}
```

## ルールの定義
### TRuleOfStochasticallyMoveFromHomeToCompanyOnWeekdays:平日に確率的に自宅から会社に移動するルール

sample03のTRuleOfStochasticallyMoveFromHomeToCompanyを拡張する．
現在時刻の日付を7で割った余りで曜日を計算し，土日の場合は会社に移動せず，会社から自宅に移動するルールの予約も行わないように変更する．

`TRuleOfStochasticallyMoveFromHomeToCompanyOnWeekdays.java`

```Java
public final class TRuleOfStochasticallyMoveFromHomeToCompanyOnWeekdays extends TAgentRule {

    /** 移動確率[0, 1] */
    private final double fProbability;

    /** 会社から自宅に移動するルール */
    private final TRule fRuleOfReturnHome;

    /** 会社から自宅に移動するルールを実行するまでの時間間隔 */
    private final TTime fIntervalTimeOfReturnHome;

    /** 会社から自宅に移動するルールの発火時刻計算用 */
    private final TTime fTimeOfReturnHome;

    /** 会社から自宅に移動するルールを実行するステージ */
    private final Enum<?> fStageOfReturnHome;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     * @param probability 移動確率[0, 1]
     * @param ruleOfReturnHome 会社から自宅に移動するルール
     * @param intervalTimeOfReturnHome 会社から自宅に移動するルールを実行するまでの時間間隔
     * @param stageOfReturnHome 会社から自宅に移動するルールを実行するステージ
     */
    public TRuleOfStochasticallyMoveFromHomeToCompanyOnWeekdays(String name, TRole owner, double probability,
            TRule ruleOfReturnHome, String intervalTimeOfReturnHome, Enum<?> stageOfReturnHome) {
        // 親クラスのコンストラクタを呼び出す．
        super(name, owner);
        if (probability < 0.0 || 1.0 < probability) {
            throw new RuntimeException("Invalid probability. Probability value must be between 0 and 1.");
        }
        fProbability = probability;
        fRuleOfReturnHome = ruleOfReturnHome;
        fIntervalTimeOfReturnHome = new TTime(intervalTimeOfReturnHome);
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
        // エージェントが平日(土日以外)に自宅にいるかつ移動確率を満たしたならば，会社に移動する．
        boolean debugFlag = true;
        // 現在時刻の日を7で割った余りで曜日を取得．どの曜日が何番になっているかはenumの定義順による．
        EDay day = EDay.values()[currentTime.getDay() % 7];
        if (day != EDay.Sunday && day != EDay.Saturday) {
            TRoleOfFather role = (TRoleOfFather) getOwnerRole();
            if (isAt(role.getHome())) {
                if (getRandom().nextDouble() <= fProbability) {
                    moveTo(role.getCompany());
                    appendToDebugInfo("success", debugFlag);

                    // 会社から自宅に移動するルールの発火時刻を計算して臨時実行ルールとして予約する．
                    fTimeOfReturnHome.copyFrom(currentTime).add(fIntervalTimeOfReturnHome);
                    fRuleOfReturnHome.setTimeAndStage(fTimeOfReturnHome.getDay(), fTimeOfReturnHome.getHour(),
                            fTimeOfReturnHome.getMinute(), fTimeOfReturnHome.getSecond(), fStageOfReturnHome);
                } else {
                    appendToDebugInfo("fail (probability)", debugFlag);
                }
            } else {
                appendToDebugInfo("fail (spot)", debugFlag);
            }
        } else {
            appendToDebugInfo("fail (day)", debugFlag);
        }
    }
}
```

### TRuleOfMoveFromCompanyToHome:会社から自宅に移動するルール

sample03と同じ．

`TRuleOfMoveFromCompanyToHome.java`

```Java
public final class TRuleOfMoveFromCompanyToHome extends TAgentRule {

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     */
    public TRuleOfMoveFromCompanyToHome(String name, TRole owner) {
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
        // エージェントが会社にいるならば，自宅に移動する．
        boolean debugFlag = true;
        TRoleOfFather role = (TRoleOfFather) getOwnerRole();
        if (isAt(role.getCompany())) {
            moveTo(role.getHome());
            appendToDebugInfo("success", debugFlag);
        } else {
            appendToDebugInfo("fail", debugFlag);
        }
    }
}
```

## 役割の定義
### TRoleOfFather:父親役割

sample03と同じ．

`TRoleOfFather.java`

```Java
public final class TRoleOfFather extends TRole {

    /** 自宅 */
    private final TSpot fHome;

    /** 会社 */
    private final TSpot fCompany;

    /** 9時に自宅から会社に移動するルール名 */
    private static final String RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY_9 = "MoveFromHomeToCompany9";

    /** 10時に自宅から会社に移動するルール名 */
    private static final String RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY_10 = "MoveFromHomeToCompany10";

    /** 11時に自宅から会社に移動するルール名 */
    private static final String RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY_11 = "MoveFromHomeToCompany11";

    /** 会社から自宅に移動するルール名 */
    private static final String RULE_NAME_OF_MOVE_FROM_COMPANY_TO_HOME = "MoveFromCompanyToHome";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param home 自宅
     * @param company 会社
     */
    public TRoleOfFather(TAgent owner, TSpot home, TSpot company) {
        // 親クラスのコンストラクタを呼び出す．
        // 以下の2つの引数は省略可能で，その場合デフォルト値で設定される．
        // 第3引数:この役割が持つルール数 (デフォルト値 10)
        // 第4引数:この役割が持つ子役割数 (デフォルト値 5)
        super(ERoleName.Father, owner, 2, 0);

        fHome = home;
        fCompany = company;

        // 役割が持つルールの登録
        // 会社から自宅に移動するルール．予約はTRuleOfMoveFromHomeToCompanyで相対時刻指定で行われる．
        TRule ruleOfReturnHome = new TRuleOfMoveFromCompanyToHome(RULE_NAME_OF_MOVE_FROM_COMPANY_TO_HOME, this);

        // 確率的に自宅から会社に移動するルール．9:00:00, 10:00:00, 11:00:00/エージェント移動ステージに定時実行ルールとして予約する．
        // 移動確率をそれぞれ 9:00:00 -> 0.5, 10:00:00 -> 0.6, 11:00:00 ->1.0 に設定する．これによって，
        //  9時に移動する確率は，0.5 = 50%
        // 10時に移動する確率は，9時に移動していない かつ 0.6 = (1.0 - 0.5) * 0.6 = 30%
        // 11時に移動する確率は，9時に移動していない かつ 10時に移動していない かつ 1.0 = (1.0 - 0.5) * (1.0 - 0.6) * 1.0 = 20%
        new TRuleOfStochasticallyMoveFromHomeToCompanyOnWeekdays(RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY_9,
                this, 0.5, ruleOfReturnHome, "8:00:00", EStage.AgentMoving)
                .setTimeAndStage(9, 0, 0, EStage.AgentMoving);

        new TRuleOfStochasticallyMoveFromHomeToCompanyOnWeekdays(RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY_10,
                this, 0.6, ruleOfReturnHome, "8:00:00", EStage.AgentMoving)
                .setTimeAndStage(10, 0, 0, EStage.AgentMoving);

        new TRuleOfStochasticallyMoveFromHomeToCompanyOnWeekdays(RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY_11,
                this, 1.0, ruleOfReturnHome, "8:00:00", EStage.AgentMoving)
                .setTimeAndStage(11, 0, 0, EStage.AgentMoving);
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

```Java
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
        List<Enum<?>> stages = List.of(EStage.AgentMoving);
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
        String pathOfLogDir = "logs" + File.separator + "tutorials" + File.separator + "sample04";
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
        // *************************************************************************************************************

        int noOfHomes = 3; // 家の数
        List<TSpot> homes = spotManager.createSpots(ESpotType.Home, noOfHomes);
        TSpot company = spotManager.createSpot(ESpotType.Company);

        // *************************************************************************************************************
        // エージェント作成
        //   - Father:Father1, Father2, Father3
        //     - 初期スポット:Home
        //     - 役割:父親役割
        // *************************************************************************************************************

        int noOfFathers = noOfHomes; // 父親の数は家の数と同じ．
        List<TAgent> fathers = agentManager.createAgents(EAgentType.Father, noOfFathers);
        for (int i = 0; i < noOfFathers; ++i) {
            TAgent father = fathers.get(i); // i番目の父親エージェント
            TSpot home = homes.get(i); // i番目の父親エージェントの自宅
            father.initializeCurrentSpot(home); // 初期スポットを自宅に設定
            new TRoleOfFather(father, home, company); // 父親役割を作成
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
```


前：
次：
TODO:
