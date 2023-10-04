前：[sample05-1:役割のアクティブ制御](src/main/java/jp/soars/tutorials/sample05_1/)

次：[sample06:子役割による役割のアクティブ制御](src/main/java/jp/soars/tutorials/sample06/)


# sample05-2:ルールの汎化 <!-- omit in toc -->

sample05-2ではシミュレーションシナリオはsample05-1と同じだが，
エージェントの移動ルールを移動前スポットと移動先スポットの組み合わせごとに新しく定義していくのは面倒なので，
エージェントの移動として汎用的に使えるルールを定義する．
具体的には，ルールに出発地と目的地を持たせ，ルールをnewするときにそれぞれのスポットを渡すように変更する．
この方法のメリットは，同じようなルールをいくつも定義しなくても良くなる点，役割からスポットを受け取るという操作がない分若干高速．
デメリットは，例えば自宅スポットを変更したい場合に全てのルールの自宅スポットを変更する必要がある点，メモリ使用量が増える点である．
シミュレーションの内容・規模や使用可能な計算資源に応じて最適なモデルの実装方法は変わる．

- [シナリオとシミュレーション条件](#シナリオとシミュレーション条件)
- [シミュレーション定数の定義](#シミュレーション定数の定義)
- [ルールの定義](#ルールの定義)
  - [TRuleOfAgentMoving:エージェント移動ルール](#truleofagentmovingエージェント移動ルール)
  - [TRuleOfStochasticallyAgentMovingOnWeekdays:平日に確率的にエージェント移動するルール](#truleofstochasticallyagentmovingonweekdays平日に確率的にエージェント移動するルール)
  - [TRuleOfDeterminingHealth:健康状態決定ルール](#truleofdetermininghealth健康状態決定ルール)
  - [TRuleOfRecoveringFromSick:病気から回復するルール](#truleofrecoveringfromsick病気から回復するルール)
- [役割の定義](#役割の定義)
  - [TRoleOfFather:父親役割](#troleoffather父親役割)
  - [TRoleOfSickPerson:病人役割](#troleofsickperson病人役割)
- [メインクラスの定義](#メインクラスの定義)


## シナリオとシミュレーション条件

以下のシナリオを考える．

- 3人の父親(Father1, Father2, Father3)は，それぞれ自宅(Home1, Home2, Home3)を持つ．
- 父親は，平日(土日以外)は50%の確率で9時，30%の確率で10時，20%の確率で11時に自宅から同じ会社(Company)に移動する．
- 父親は，会社に移動してから8時間後に会社からそれぞれの自宅に移動する．
- 父親は，休日(土日)は会社に移動せず自宅にいる．
- 父親は，6時に自宅にいる場合に25%の確率で病気になる．
- 病人は，10時に自宅から病院(Hospital)に移動する．
- 病人は，12時に病院からそれぞれの自宅に移動して病気が治る．

シミュレーション条件

- エージェント : Father(3)
- スポット : Home(3), Company(1), Hospital(1)
- ステージ : DeterminingHealth, AgentMoving, RecoveringFromSick
- 時刻ステップ間隔：1時間 / step
- シミュレーション期間：7日間

## シミュレーション定数の定義

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
    Company,
    /** 病院 */
    Hospital
}
```

`EStage.java`

```Java
public enum EStage {
    /** 健康状態決定ステージ */
    DeterminingHealth,
    /** エージェント移動ステージ */
    AgentMoving,
    /** 病気回復ステージ */
    RecoveringFromSick
}
```

`ERoleName.java`

```Java
public enum ERoleName {
    /** 父親役割 */
    Father,
    /** 病人役割 */
    SickPerson
}
```

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

## ルールの定義

### TRuleOfAgentMoving:エージェント移動ルール

エージェント移動ルールは出発地と目的地をコンストラクタで受け取ることで，2スポット間の移動を定義する．
これによって，sample05-1のTRuleOfMoveFromCompanyToHome, TRuleOfMoveFromHomeToHospital, TRuleOfMoveFromHospitalToHome
の3つをエージェント移動ルールで代用できる．

`TRuleOfAgentMoving.java`

```Java
public final class TRuleOfAgentMoving extends TAgentRule {

    /** 出発地 */
    private final TSpot fSource;

    /** 目的地 */
    private final TSpot fDestination;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     * @param source 出発地
     * @param destination 目的地
     */
    public TRuleOfAgentMoving(String name, TRole owner, TSpot source, TSpot destination) {
        // 親クラスのコンストラクタを呼び出す．
        super(name, owner);
        fSource = source;
        fDestination = destination;
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
        // エージェントが出発地にいるならば，目的地に移動する．
        boolean debugFlag = true;
        if (isAt(fSource)) {
            moveTo(fDestination);
            appendToDebugInfo("success", debugFlag);
        } else {
            appendToDebugInfo("fail", debugFlag);
        }
    }
}
```

### TRuleOfStochasticallyAgentMovingOnWeekdays:平日に確率的にエージェント移動するルール

sample05-1のTRuleOfStochasticallyAgentMovingOnWeekdaysを変更する．
コンストラクタで出発地と目的地として受け取るように変更する．

`TRuleOfStochasticallyAgentMovingOnWeekdays.java`

```Java
public final class TRuleOfStochasticallyAgentMovingOnWeekdays extends TAgentRule {

    /** 移動確率[0, 1] */
    private final double fProbability;

    /** 出発地 */
    private final TSpot fSource;

    /** 目的地 */
    private final TSpot fDestination;

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
    public TRuleOfStochasticallyAgentMovingOnWeekdays(String name, TRole owner, double probability, TSpot source,
            TSpot destination, TRule ruleOfReturnHome, String intervalTimeOfReturnHome, Enum<?> stageOfReturnHome) {
        // 親クラスのコンストラクタを呼び出す．
        super(name, owner);
        if (probability < 0.0 || 1.0 < probability) {
            throw new RuntimeException("Invalid probability. Probability value must be between 0 and 1.");
        }
        fProbability = probability;
        fSource = source;
        fDestination = destination;
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
        // エージェントが平日(土日以外)に出発地にいるかつ移動確率を満たしたならば，目的地に移動する．
        boolean debugFlag = true;
        EDay day = EDay.values()[currentTime.getDay() % 7];
        if (day != EDay.Sunday && day != EDay.Saturday) {
            if (isAt(fSource)) {
                if (getRandom().nextDouble() <= fProbability) {
                    moveTo(fDestination);
                    appendToDebugInfo("success", debugFlag);

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

### TRuleOfDeterminingHealth:健康状態決定ルール

sample05-1のTRuleOfDeterminingHealthを変更する．
自宅スポットをコンストラクタで受け取るように変更する．

`TRuleOfDeterminingHealth.java`

```Java
public final class TRuleOfDeterminingHealth extends TAgentRule {

    /** 病気になる確率[0, 1] */
    private final double fProbability;

    /** 自宅 */
    private final TSpot fHome;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールを持つ役割
     * @param probability 病気になる確率[0, 1]
     * @param home 自宅
     */
    public TRuleOfDeterminingHealth(String name, TRole owner, double probability, TSpot home) {
        super(name, owner);
        if (probability < 0.0 || 1.0 < probability) {
            throw new RuntimeException("Invalid probability. Probability value must be between 0 and 1.");
        }
        fProbability = probability;
        fHome = home;
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
        // 自宅にいる場合，確率に従って父親役割を非アクティブ化して病人役割をアクティブ化する．
        boolean debugFlag = true;
        if (isAt(fHome)) {
            if (getRandom().nextDouble() <= fProbability) {
                getAgent().deactivateRole(ERoleName.Father);
                getAgent().activateRole(ERoleName.SickPerson);
                appendToDebugInfo("get sick.", debugFlag);
            } else {
                appendToDebugInfo("Don't get sick. (probability)", debugFlag);
            }
        } else {
            appendToDebugInfo("Don't get sick. (spot)", debugFlag);
        }
    }
}
```

### TRuleOfRecoveringFromSick:病気から回復するルール

sample05-1と同じ．

`TRuleOfRecoveringFromSick.java`

```Java
public final class TRuleOfRecoveringFromSick extends TAgentRule {

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     */
    public TRuleOfRecoveringFromSick(String name, TRole owner) {
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
        // 病人役割を非アクティブ化して父親役割をアクティブ化する．
        getAgent().deactivateRole(ERoleName.SickPerson);
        getAgent().activateRole(ERoleName.Father);
    }
}
```

## 役割の定義
### TRoleOfFather:父親役割

sample05-1のTRoleOfFatherを変更する．
汎用ルールを使用するように変更する．
また，自宅と会社スポットは父親役割は持たずルールに設定する．

`TRoleOfFather.java`

```Java
public final class TRoleOfFather extends TRole {

    /** 健康状態決定ルール名 */
    public static final String RULE_NAME_OF_DETERMINING_HEALTH = "DeterminingHealth";

    /** 9時に自宅から会社に移動するルール名 */
    public static final String RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY_9 = "MoveFromHomeToCompany9";

    /** 10時に自宅から会社に移動するルール名 */
    public static final String RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY_10 = "MoveFromHomeToCompany10";

    /** 11時に自宅から会社に移動するルール名 */
    public static final String RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY_11 = "MoveFromHomeToCompany11";

    /** 会社から自宅に移動するルール名 */
    public static final String RULE_NAME_OF_MOVE_FROM_COMPANY_TO_HOME = "MoveFromCompanyToHome";

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
        super(ERoleName.Father, owner, 5, 0);

        // 役割が持つルールの登録
        // 健康状態決定ルール．6:00:00/健康状態決定ステージに定時実行ルールとして予約する．病人になる確率は25%(0.25)とする．
        new TRuleOfDeterminingHealth(RULE_NAME_OF_DETERMINING_HEALTH, this, 0.25, home)
                .setTimeAndStage(6, 0, 0, EStage.DeterminingHealth);

        // 会社から自宅に移動するルール．予約はTRuleOfMoveFromHomeToCompanyで相対時刻指定で行われる．
        TRule ruleOfReturnHome = new TRuleOfAgentMoving(RULE_NAME_OF_MOVE_FROM_COMPANY_TO_HOME, this, company, home);

        // 確率的に自宅から会社に移動するルール．9:00:00, 10:00:00, 11:00:00/エージェント移動ステージに定時実行ルールとして予約する．
        new TRuleOfStochasticallyAgentMovingOnWeekdays(RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY_9,
                this, 0.5, home, company, ruleOfReturnHome, "8:00:00", EStage.AgentMoving)
                .setTimeAndStage(9, 0, 0, EStage.AgentMoving);

        new TRuleOfStochasticallyAgentMovingOnWeekdays(RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY_10,
                this, 0.6, home, company, ruleOfReturnHome, "8:00:00", EStage.AgentMoving)
                .setTimeAndStage(10, 0, 0, EStage.AgentMoving);

        new TRuleOfStochasticallyAgentMovingOnWeekdays(RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY_11,
                this, 1.0, home, company, ruleOfReturnHome, "8:00:00", EStage.AgentMoving)
                .setTimeAndStage(11, 0, 0, EStage.AgentMoving);
    }
}
```

### TRoleOfSickPerson:病人役割

sample05-1のTRoleOfSickPersonを変更する．
汎用ルールを使用するように変更する．
また，自宅と病院スポットをコンストラクタで受け取り，ルールに設定する．

`TRoleOfSickPerson.java`

```Java
public final class TRoleOfSickPerson extends TRole {

    /** 病気から回復するルール名 */
    public static final String RULE_NAME_OF_RECOVERING_FROM_SICK = "RecoveringFromSick";

    /** 自宅から病院に移動するルール名 */
    public static final String RULE_NAME_OF_MOVE_FROM_HOME_TO_HOSPITAL = "MoveFromHomeToHospital";

    /** 病院から自宅に移動するルール名 */
    public static final String RULE_NAME_OF_MOVE_FROM_HOSPITAL_TO_HOME = "MoveFromHospitalToHome";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     */
    public TRoleOfSickPerson(TAgent owner, TSpot home, TSpot hospital) {
        super(ERoleName.SickPerson, owner, 3, 0);

        // 役割が持つルールの登録
        // 自宅から病院に移動するルール．10:00:00/エージェント移動ステージに定時実行ルールとして予約する．
        new TRuleOfAgentMoving(RULE_NAME_OF_MOVE_FROM_HOME_TO_HOSPITAL, this, home, hospital)
                .setTimeAndStage(10, 0, 0, EStage.AgentMoving);

        // 病院から自宅に移動するルール．12:00:00/エージェント移動ステージに定時実行ルールとして予約する．
        new TRuleOfAgentMoving(RULE_NAME_OF_MOVE_FROM_HOSPITAL_TO_HOME, this, hospital, home)
                .setTimeAndStage(12, 0, 0, EStage.AgentMoving);

        // 病気から回復するルール．12:00:00/病気回復ステージに定時実行ルールとして予約する．
        new TRuleOfRecoveringFromSick(RULE_NAME_OF_RECOVERING_FROM_SICK, this)
                .setTimeAndStage(12, 0, 0, EStage.RecoveringFromSick);
    }
}
```

## メインクラスの定義

sample05-1のメインクラスのログ出力ディレクトリを変更する．
また，病人役割に自宅と病院スポットを渡すように変更する．

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
        String pathOfLogDir = "logs" + File.separator + "tutorials" + File.separator + "sample05_2";
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
            new TRoleOfSickPerson(father, home, hospital); // 病人役割を作成
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


前：[sample05-1:役割のアクティブ制御](src/main/java/jp/soars/tutorials/sample05_1/)

次：[sample06:子役割による役割のアクティブ制御](src/main/java/jp/soars/tutorials/sample06/)
