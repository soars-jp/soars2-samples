前：[sample12:モジュール合成](src/main/java/jp/soars/tutorials/sample12/)

次：[sample14:ルールの並列実行](src/main/java/jp/soars/tutorials/sample14/)

# sample13:ルールの上書きと追加 <!-- omit in toc -->

sample13ではモジュールで定義されている役割が持つルールの一部の動作のみ変更したい場合に便利な機能として，
ルールの上書き機能について解説する．上書きはメインクラスで行う．

- [module1](#module1)
  - [シミュレーション定数の定義](#シミュレーション定数の定義)
  - [ルールの定義](#ルールの定義)
    - [TRuleOfAgentMoving:エージェント移動ルール](#truleofagentmovingエージェント移動ルール)
    - [TRuleOfAgentMovingOnWeekdays:平日エージェント移動ルール](#truleofagentmovingonweekdays平日エージェント移動ルール)
    - [TRuleOfStochasticallyAgentMovingOnWeekdays:平日に確率的にエージェント移動するルール](#truleofstochasticallyagentmovingonweekdays平日に確率的にエージェント移動するルール)
  - [役割の定義](#役割の定義)
    - [TRoleOfFather:父親役割](#troleoffather父親役割)
    - [TRoleOfChild:子ども役割](#troleofchild子ども役割)
- [module2](#module2)
  - [シミュレーション定数の定義](#シミュレーション定数の定義-1)
  - [ルールの定義](#ルールの定義-1)
    - [TRuleOfAgentMoving:エージェント移動ルール](#truleofagentmovingエージェント移動ルール-1)
    - [TRuleOfDeterminingHealth:健康状態決定ルール](#truleofdetermininghealth健康状態決定ルール)
    - [TRuleOfRecoveringFromSick:病気から回復するルール](#truleofrecoveringfromsick病気から回復するルール)
  - [役割の定義](#役割の定義-1)
    - [TRoleOfDeterminingHealth:健康状態決定役割](#troleofdetermininghealth健康状態決定役割)
    - [TRoleOfSickPerson:病人役割](#troleofsickperson病人役割)
- [モジュール合成](#モジュール合成)
  - [シミュレーション定数の定義](#シミュレーション定数の定義-2)
  - [ルールの定義](#ルールの定義-2)
    - [TRuleOfStochasticallyAgentMoving:確率的にエージェント移動するルール](#truleofstochasticallyagentmoving確率的にエージェント移動するルール)
  - [メインクラスの定義](#メインクラスの定義)


## シナリオとシミュレーション条件

以下のシナリオを考える．

sample12から変更するシナリオ
変更前
- 父親は，平日(土日以外)は50%の確率で9時，30%の確率で10時，20%の確率で11時に自宅から同じ会社(Company)に移動する．
- 父親は，休日(土日)は会社に移動せず自宅にいる．
- 子どもは，平日(土日以外)は15時に学校からそれぞれの自宅に移動する．
変更後
- 父親は，50%の確率で9時，30%の確率で10時，20%の確率で11時に自宅から同じ会社(Company)に移動する．
- 子どもは，平日(土日以外)は17時に学校からそれぞれの自宅に移動する．

変更しないシナリオ
- 3人の父親(Father1, Father2, Father3)は，それぞれ自宅(Home1, Home2, Home3)を持つ．
- 父親は，会社に移動してから8時間後に会社からそれぞれの自宅に移動する．
- 3人の子ども(Child1, Child2, Child3)は，それぞれ自宅(Home1, Home2, Home3)を持つ．
- 子どもは，平日(土日以外)は8時に自宅から同じ学校(School)に移動する．
- 子どもは，休日(土日)は学校に移動せず自宅にいる．
- 父親と子どもは，6時に自宅にいる場合に25%の確率で病気になる．
- 病人は，10時に自宅から病院(Hospital)に移動する．
- 病人は，父親の場合12時，子どもの場合13時に病院からそれぞれの自宅に移動して病気が治る．

シミュレーション条件

- エージェント : Father(3), Child(3)
- スポット : Home(3), Company(1), School(1) Hospital(1)
- ステージ : DeterminingHealth, AgentMoving, RecoveringFromSick
- 時刻ステップ間隔：1時間 / step
- シミュレーション期間：7日間

# module1

sample12と同じ．

## シミュレーション定数の定義

`EModule1Stage.java`

```Java
public enum EModule1Stage {
    /** エージェント移動ステージ */
    AgentMoving
}
```

`EModule1RoleName.java`

```Java
public enum EModule1RoleName {
    /** 父親役割 */
    Father,
    /** 子ども役割 */
    Child
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

### TRuleOfAgentMovingOnWeekdays:平日エージェント移動ルール

`TRuleOfAgentMovingOnWeekdays.java`

```Java
public final class TRuleOfAgentMovingOnWeekdays extends TAgentRule {

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
    public TRuleOfAgentMovingOnWeekdays(String name, TRole owner, TSpot source, TSpot destination) {
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
        // エージェントが平日(土日以外)に出発地にいるならば，目的地に移動する．
        boolean debugFlag = true;
        EDay day = EDay.values()[currentTime.getDay() % 7];
        if (day != EDay.Sunday && day != EDay.Saturday) {
            if (isAt(fSource)) {
                moveTo(fDestination);
                appendToDebugInfo("success", debugFlag);
            } else {
                appendToDebugInfo("fail (spot)", debugFlag);
            }
        } else {
            appendToDebugInfo("fail (day)", debugFlag);
        }
    }
}
```

### TRuleOfStochasticallyAgentMovingOnWeekdays:平日に確率的にエージェント移動するルール

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

## 役割の定義

### TRoleOfFather:父親役割

`TRoleOfFather.java`

```Java
public final class TRoleOfFather extends TRole {

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
        super(ERoleName.Father, owner, 4, 0);

        // 役割が持つルールの登録
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

### TRoleOfChild:子ども役割

`TRoleOfChild.java`

```Java
public final class TRoleOfChild extends TRole {

    /** 自宅から学校に移動するルール名 */
    public static final String RULE_NAME_OF_MOVE_FROM_HOME_TO_SCHOOL = "MoveFromHomeToSchool";

    /** 学校から自宅に移動するルール名 */
    public static final String RULE_NAME_OF_MOVE_FROM_SCHOOL_TO_HOME = "MoveFromSchoolToHome";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param home 自宅
     * @param school 学校
     */
    public TRoleOfChild(TAgent owner, TSpot home, TSpot school) {
        // 親クラスのコンストラクタを呼び出す．
        // 以下の2つの引数は省略可能で，その場合デフォルト値で設定される．
        // 第3引数:この役割が持つルール数 (デフォルト値 10)
        // 第4引数:この役割が持つ子役割数 (デフォルト値 5)
        super(ERoleName.Child, owner, 2, 0);

        // 役割が持つルールの登録
        // 自宅から学校に移動するルール．8:00:00/エージェント移動ステージに定時実行ルールとして予約する．
        new TRuleOfAgentMovingOnWeekdays(RULE_NAME_OF_MOVE_FROM_HOME_TO_SCHOOL, this, home, school)
                .setTimeAndStage(8, 0, 0, EStage.AgentMoving);

        // 学校から自宅に移動するルール．15:00:00/エージェント移動ステージに定時実行ルールとして予約する．
        new TRuleOfAgentMoving(RULE_NAME_OF_MOVE_FROM_SCHOOL_TO_HOME, this, school, home)
                .setTimeAndStage(15, 0, 0, EStage.AgentMoving);
    }
}
```

# module2

sample12と同じ．

## シミュレーション定数の定義

`EModule2Stage.java`

```Java
public enum EModule2Stage {
    /** 健康状態決定ステージ */
    DeterminingHealth,
    /** エージェント移動ステージ */
    AgentMoving,
    /** 病気回復ステージ */
    RecoveringFromSick
}
```

`EModule2RoleName.java`

```Java
public enum EModule2RoleName {
    /** 健康状態決定役割 */
    DeterminingHealth,
    /** 病人役割 */
    SickPerson
}
```

## ルールの定義

### TRuleOfAgentMoving:エージェント移動ルール

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

### TRuleOfDeterminingHealth:健康状態決定ルール

`TRuleOfDeterminingHealth.java`

```Java
public final class TRuleOfDeterminingHealth extends TAgentRule {

    /** 病気になる確率[0, 1] */
    private final double fProbability;

    /** 自宅 */
    private final TSpot fHome;

    /** 病気になった時に非アクティブ化する役割名 */
    private final Enum<?> fOriginalRoleName;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールを持つ役割
     * @param probability 病気になる確率[0, 1]
     * @param home 自宅
     * @param originalRoleName 元の役割．病気になった時に非アクティブ化する．
     */
    public TRuleOfDeterminingHealth(String name, TRole owner, double probability, TSpot home, Enum<?> originalRoleName) {
        super(name, owner);
        if (probability < 0.0 || 1.0 < probability) {
            throw new RuntimeException("Invalid probability. Probability value must be between 0 and 1.");
        }
        fProbability = probability;
        fHome = home;
        fOriginalRoleName = originalRoleName;
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
        // 自宅にいる場合，確率に従って父親役割か子ども役割を非アクティブ化して病人役割をアクティブ化する．
        boolean debugFlag = true;
        if (isAt(fHome)) {
            if (getRandom().nextDouble() <= fProbability) {
                getAgent().deactivateRole(fOriginalRoleName);
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

`TRuleOfRecoveringFromSick.java`

```Java
public final class TRuleOfRecoveringFromSick extends TAgentRule {

    /** 病気から回復した時にアクティブ化する役割名 */
    private final Enum<?> fOriginalRoleName;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     * @param originalRoleName 元の役割．病気から回復した時にアクティブ化する．
     */
    public TRuleOfRecoveringFromSick(String name, TRole owner, Enum<?> originalRoleName) {
        // 親クラスのコンストラクタを呼び出す．
        super(name, owner);
        fOriginalRoleName = originalRoleName;
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
        // 病人役割を非アクティブ化して父親役割か子ども役割をアクティブ化する．
        getAgent().deactivateRole(ERoleName.SickPerson);
        getAgent().activateRole(fOriginalRoleName);
    }
}
```

## 役割の定義

### TRoleOfDeterminingHealth:健康状態決定役割

`TRoleOfDeterminingHealth.java`

```Java
public final class TRoleOfDeterminingHealth extends TRole {

    /** 健康状態決定ルール名 */
    public static final String RULE_NAME_OF_DETERMINING_HEALTH = "DeterminingHealth";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param home 自宅
     * @param originalRoleName 元の役割．病気になった時に非アクティブ化する．
     */
    public TRoleOfDeterminingHealth(TAgent owner, TSpot home, Enum<?> originalRoleName) {
        super(ERoleName.DeterminingHealth, owner, 1, 0);

        // 健康状態決定ルール．6:00:00/健康状態決定ステージに定時実行ルールとして予約する．病人になる確率は25%(0.25)とする．
        new TRuleOfDeterminingHealth(RULE_NAME_OF_DETERMINING_HEALTH, this, 0.25, home, originalRoleName)
                .setTimeAndStage(6, 0, 0, EStage.DeterminingHealth);
    }
}
```

### TRoleOfSickPerson:病人役割

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
     * @param home 自宅
     * @param hospital 病院
     * @param medicHour 診察時間(病院滞在時間)
     * @param originalRoleName 元の役割．病気から回復した時にアクティブ化する．
     */
    public TRoleOfSickPerson(TAgent owner, TSpot home, TSpot hospital, int medicHour, Enum<?> originalRoleName) {
        super(ERoleName.SickPerson, owner, 3, 0);

        // 役割が持つルールの登録
        // 自宅から病院に移動するルール．10:00:00/エージェント移動ステージに定時実行ルールとして予約する．
        new TRuleOfAgentMoving(RULE_NAME_OF_MOVE_FROM_HOME_TO_HOSPITAL, this, home, hospital)
                .setTimeAndStage(10, 0, 0, EStage.AgentMoving);

        // 病院から自宅に移動するルール．(12,13):00:00/エージェント移動ステージに定時実行ルールとして予約する．
        new TRuleOfAgentMoving(RULE_NAME_OF_MOVE_FROM_HOSPITAL_TO_HOME, this, hospital, home)
                .setTimeAndStage(10 + medicHour, 0, 0, EStage.AgentMoving);

        // 病気から回復するルール．(12,13):00:00/病気回復ステージに定時実行ルールとして予約する．
        new TRuleOfRecoveringFromSick(RULE_NAME_OF_RECOVERING_FROM_SICK, this, originalRoleName)
                .setTimeAndStage(10 + medicHour, 0, 0, EStage.RecoveringFromSick);
    }
}
```

# モジュール合成

## シミュレーション定数の定義

`EAgentType.java`

```Java
public enum EAgentType {
    /** 父親 */
    Father,
    /** 子ども */
    Child
}
```

`ESpotType.java`

```Java
public enum ESpotType {
    /** 自宅 */
    Home,
    /** 会社 */
    Company,
    /** 学校 */
    School,
    /** 病院 */
    Hospital
}
```

## ルールの定義

### TRuleOfStochasticallyAgentMoving:確率的にエージェント移動するルール

確率的にエージェント移動するルールは，module1の平日に確率的にエージェント移動するルールから平日判定をなくしたルールである．

`TRuleOfStochasticallyAgentMoving.java`

```Java
public final class TRuleOfStochasticallyAgentMoving extends TAgentRule {

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
    public TRuleOfStochasticallyAgentMoving(String name, TRole owner, double probability, TSpot source,
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
    }
}
```

## メインクラスの定義

sample13では，父親役割，子ども役割を作成した後にルールの上書きを実行している．

父親役割の場合，ルールのインスタンスを変更する必要があるため，父親役割に登録されている元のルールを役割クラスのremoveRuleメソッドで削除した後に，
新たにルールを作成している．この時，元のルールを削除しなくても同じ名前でルールを登録しようとした場合には上書きが実行されるが，
ワーニングメッセージが表示される．
このワーニングメッセージは TSOARSBuilder の設定でOFFにできるが，他のワーニングメッセージも出力されなくなるため注意が必要．

子ども役割の場合，定時実行ルールの時間設定を変更すればよい．
この場合は，ルールクラスのresetTimeAndStageメソッドでルールの登録を削除した後，再登録すればよい．

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
```


前：[sample12:モジュール合成](src/main/java/jp/soars/tutorials/sample12/)

次：[sample14:ルールの並列実行](src/main/java/jp/soars/tutorials/sample14/)
