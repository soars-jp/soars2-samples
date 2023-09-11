<!-- omit in toc -->
# sample04：子役割と役割の変更

- [シナリオとシミュレーション条件](#シナリオとシミュレーション条件)
- [シミュレーション定数の定義](#シミュレーション定数の定義)
- [ルールの定義](#ルールの定義)
  - [エージェント移動ルール](#エージェント移動ルール)
  - [確率的エージェント移動ルール](#確率的エージェント移動ルール)
  - [健康状態決定ルール](#健康状態決定ルール)
  - [回復ルール](#回復ルール)
- [役割の定義](#役割の定義)
  - [共通役割](#共通役割)
  - [父親役割](#父親役割)
  - [子供役割](#子供役割)
  - [病人役割](#病人役割)
- [メインクラスの定義](#メインクラスの定義)


## シナリオとシミュレーション条件

以下のシナリオを考える．

- 3人の父親(Father1, Father2, Father3)は，それぞれ自宅(Home1, Home2, Home3)を持つ．
- 3人の父親は，50%の確率で9時，30%の確率で10時，20%の確率で11時に自宅から同じ会社(Company)に移動する．
- 3人の父親は，出社して8時間後にそれぞれの自宅に移動する．
- 3人の子供(Child1, Child2, Child3)は，それぞれ自宅(Home1, Home2, Home3)を持つ．
- 3人の子供は，8時に自宅から同じ学校(School)に移動する．
- 3人の子供は，15時に学校からそれぞれの自宅に移動する．
- 父親と子供は，6時に25%の確率で病気になる．
- 病人は，10時に自宅から病院(Hospital)に移動する．
- 病人は，父親の場合2時間，子供の場合3時間，病院で治療を受けた後，自宅に戻り病気が治る．

シミュレーション条件

- エージェント : Father(3), Child(3)
- スポット : Home(3), Company(1), School(1), Hospital(1)
- ステージ : DeterminingHealth, AgentMoving
- 時刻ステップ間隔：1時間 / step
- シミュレーション期間：7日間


## シミュレーション定数の定義

sample04では以下の定数を定義する．

- エージェントタイプの定義
- スポットタイプの定義
- ステージの定義
- 役割名の定義


```java
public enum EAgentType {
    /** 父親 */
    Father,
    /** 子供 */
    Child
}
```

```java
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

```java
public enum EStage {
    /** 健康状態決定ステージ */
    DeterminingHealth,
    /** エージェント移動ステージ */
    AgentMoving
}
```

```java
public enum ERoleName {
    /** 父親役割 */
    Father,
    /** 子供役割 */
    Child,
    /** 共通役割 */
    Common,
    /** 病人役割 */
    SickPerson
}
```

## ルールの定義

sample04では以下のルールを定義する．

- TRuleOfAgentMoving：エージェント移動ルール
- TRuleOfStochasticallyAgentMoving：確率的エージェント移動ルール
- TRuleOfDeterminingHealth：健康状態決定ルール
- TRuleOfRecoveringFromSick：回復ルール

### エージェント移動ルール

sample03のエージェント移動ルールと同様．

`TRuleOfAgentMoving.java`

```java
public final class TRuleOfAgentMoving extends TAgentRule {

    /** 出発地 */
    private final TSpot fSource;

    /** 目的地 */
    private final TSpot fDestination;

    /** 次に実行するルール */
    private final TRule fNextRule;

    /** 次のルールを実行するまでの時間間隔 */
    private final TTime fIntervalTimeToNextRule;

    /** 次のルールの発火時刻計算用 */
    private final TTime fTimeOfNextRule;

    /** 次のルールを実行するステージ */
    private final Enum<?> fStageOfNextRule;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     * @param source 出発地
     * @param destination 目的地
     * @param nextRule 次に実行するルール．
     * @param intervalTimeToNextRule 次のルールを実行するまでの時間間隔
     * @param stageOfNextRule 次のルールを実行するステージ
     */
    public TRuleOfAgentMoving(String name, TRole owner, TSpot source, TSpot destination,
            TRule nextRule, TTime intervalTimeToNextRule, Enum<?> stageOfNextRule) {
        // 親クラスのコンストラクタを呼び出す
        super(name, owner);
        fSource = source;
        fDestination = destination;
        fNextRule = nextRule;
        fIntervalTimeToNextRule = intervalTimeToNextRule;
        fTimeOfNextRule = new TTime();
        fStageOfNextRule = stageOfNextRule;
    }

    /**
     * コンストラクタ
     * 次に実行するルールを登録しない場合に使用する．エージェント移動のみのルールとなる．
     * @param name ルール名
     * @param owner このルールをもつ役割
     * @param source 出発地
     * @param destination 目的地
     */
    public TRuleOfAgentMoving(String name, TRole owner, TSpot source, TSpot destination) {
        this(name, owner, source, destination, null, null, null);
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
        if (isAt(fSource)) { // 出発地にいるなら
            moveTo(fDestination); // 目的地に移動する
            // 出発地と目的地をデバッグ情報として出力
            appendToDebugInfo("move from " + fSource.getName() + " to " + fDestination.getName(), debugFlag);

            if (fNextRule != null) { // 次に実行するルールが定義されていたら
                // 現在時刻にインターバルを足した時刻を次のルールの発火時刻とする．
                fTimeOfNextRule.copyFrom(currentTime)
                               .add(fIntervalTimeToNextRule);
                // 次に実行するルールを臨時実行ルールとしてスケジュール
                fNextRule.setTimeAndStage(fTimeOfNextRule.getDay(), fTimeOfNextRule.getHour(),
                        fTimeOfNextRule.getMinute(), fTimeOfNextRule.getSecond(), fStageOfNextRule);
            }
        } else { // 移動しない場合
            appendToDebugInfo("no move (wrong spot)", debugFlag);
        }
    }
}
```

### 確率的エージェント移動ルール

sample03の確率的エージェント移動ルールと同様．

`TRuleOfStochasticallyAgentMoving.java`

```java
public final class TRuleOfStochasticallyAgentMoving extends TAgentRule {

    /** 出発地 */
    private final TSpot fSource;

    /** 目的地 */
    private final TSpot fDestination;

    /** 次に実行するルール */
    private final TRule fNextRule;

    /** 次のルールを実行するまでの時間間隔 */
    private final TTime fIntervalTimeToNextRule;

    /** 次のルールの発火時刻計算用 */
    private final TTime fTimeOfNextRule;

    /** 次のルールを実行するステージ */
    private final Enum<?> fStageOfNextRule;

    /** 移動確率 */
    private final double fProbability;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     * @param source 出発地
     * @param destination 目的地
     * @param nextRule 次に実行するルール
     * @param intervalTimeToNextRule 次のルールを実行するまでの時間間隔
     * @param stageOfNextRule 次のルールを実行するステージ
     * @param probability 移動確率
     */
    public TRuleOfStochasticallyAgentMoving(String name, TRole owner, TSpot source, TSpot destination,
            TRule nextRule, TTime intervalTimeToNextRule, Enum<?> stageOfNextRule, double probability) {
        super(name, owner);
        fSource = source;
        fDestination = destination;
        fNextRule = nextRule;
        fIntervalTimeToNextRule = intervalTimeToNextRule;
        fTimeOfNextRule = new TTime();
        fStageOfNextRule = stageOfNextRule;
        fProbability = probability;
    }

    /**
     * コンストラクタ
     * 次に実行するルールを登録しない場合に使用する．エージェント移動のみのルールとなる．
     * @param name ルール名
     * @param owner このルールをもつ役割
     * @param source 出発地
     * @param destination 目的地
     * @param probability 移動確率
     */
    public TRuleOfStochasticallyAgentMoving(String name, TRole owner, TSpot source, TSpot destination, double probability) {
        this(name, owner, source, destination, null, null, null, probability);
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
        if (isAt(fSource)) { // 出発地にいるなら
            if (getRandom().nextDouble() <= fProbability) { // 移動確率条件が満たされたら
                moveTo(fDestination); // 目的地に移動する
                // 出発地と目的地をデバッグ情報として出力
                appendToDebugInfo("move from " + fSource.getName() + " to " + fDestination.getName(), debugFlag);

                if (fNextRule != null) { // 次に実行するルールが定義されていたら
                    // 現在時刻にインターバルを足した時刻を次のルールの発火時刻とする．
                    fTimeOfNextRule.copyFrom(currentTime)
                                .add(fIntervalTimeToNextRule);
                    // 次に実行するルールを臨時実行ルールとしてスケジュール
                    fNextRule.setTimeAndStage(fTimeOfNextRule.getDay(), fTimeOfNextRule.getHour(),
                            fTimeOfNextRule.getMinute(), fTimeOfNextRule.getSecond(), fStageOfNextRule);
                }
            } else {
                appendToDebugInfo("no move (probability)", debugFlag);
            }
        } else { // 移動しない場合
            appendToDebugInfo("no move (wrong spot)", debugFlag);
        }
    }
}
```

### 健康状態決定ルール

健康状態決定ルールは，スポット条件と確率条件が満たされた場合に役割を切り替える．
病人役割に切り替える時にディアクティブ化する役割を非アクティブ化して病人役割をアクティブ化する．
父親役割と子供役割には共通役割が子役割として登録されており，共通役割のアクティブ状態も一緒に変わる点に注意．

`TRuleOfDeterminingHealth.java`

```java
public final class TRuleOfDeterminingHealth extends TAgentRule {

    /** 発火スポット条件 */
    private final TSpot fSpot;

    /** 病気になる確率 */
    private final double fProbability;

    /** 病人役割に切り替える時にディアクティブ化される役割 */
    private final Enum<?> fDeactivatedRole;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールを持つ役割
     * @param spot 発火スポット
     * @param probability 病気になる確率
     * @param deactivatedRole 病人役割に切り替える時にディアクティブ化される役割
     */
    public TRuleOfDeterminingHealth(String name, TRole owner, TSpot spot, double probability, Enum<?> deactivatedRole) {
        super(name, owner);
        fSpot = spot;
        fProbability = probability;
        fDeactivatedRole = deactivatedRole;
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
        if (isAt(fSpot)) {
            if (getRandom().nextDouble() <= fProbability) { // スポット条件および確率条件が満たされたら
                appendToDebugInfo("get sick.", debugFlag);

                // 父親の場合は父親役割，子供の場合は子供役割を無効化する．
                if (fDeactivatedRole != null) {
                    getAgent().deactivateRole(fDeactivatedRole);
                    appendToDebugInfo(" deactivate:" + fDeactivatedRole.toString(), debugFlag);
                }
                // 病人役割を有効化する．
                getAgent().activateRole(ERoleName.SickPerson);
                appendToDebugInfo(" activate:" + ERoleName.SickPerson.toString(), debugFlag);
            } else {
                appendToDebugInfo("Don't get sick (probability)", debugFlag);
            }
        } else { // 移動しない場合
            appendToDebugInfo("Don't get sick (wrong spot)", debugFlag);
        }
    }
}
```

### 回復ルール

健康状態決定ルールは，病院にいる場合に自宅に移動して役割を切り替える．
病人役割をディアクティブ化して病人役割から回復するときに切り替える役割をアクティブ化する．
父親役割と子供役割には共通役割が子役割として登録されており，共通役割のアクティブ状態も一緒に変わる点に注意．

`TRuleOfRecoveringFromSick.java`

```java
public final class TRuleOfRecoveringFromSick extends TAgentRule {

    /** 自宅 */
    private final TSpot fHome;

    /** 病院 */
    private final TSpot fHospital;

    /** 病人役割から回復するときにアクティブ化される役割 */
    private final Enum<?> fActivatedRole;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     * @param home 自宅
     * @param hospital 病院
     * @param activatedRole 病人役割から回復するときにアクティブ化される役割
     */
    public TRuleOfRecoveringFromSick(String name, TRole owner, TSpot home, TSpot hospital, Enum<?> activatedRole) {
        super(name, owner);
        fHome = home;
        fHospital = hospital;
        fActivatedRole = activatedRole;
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
        if (isAt(fHospital)) { // 病院にいるなら
            moveTo(fHome); // 自宅へ移動する
            appendToDebugInfo("recovering from sick.", debugFlag);

            // 病人役割を無効化する．
            getAgent().deactivateRole(ERoleName.SickPerson);
            appendToDebugInfo(" deactivate:" + ERoleName.SickPerson.toString(), debugFlag);

            // アクティブ化する役割が設定されている場合はアクティブ化
            if (fActivatedRole != null) {
                getAgent().activateRole(fActivatedRole);
                appendToDebugInfo(" activate:" + fActivatedRole.toString(), debugFlag);
            }
        } else {
            appendToDebugInfo("not recovering (wrong spot)", debugFlag);
        }
    }
}
```

## 役割の定義

sample04では以下の役割を定義する．

- TRoleOfCommon：共通役割
- TRoleOfFather：父親役割
- TRoleOfChild：子供役割
- TRoleOfSickPerson：病人役割

### 共通役割

共通役割は，6時に25%の確率で病気になり，役割を病人役割に変更する．
共通役割は，父親と子供に共通するルールをもつ役割であり，父親役割と子供役割の子役割として登録される．
共通役割を父親役割と子供役割の子役割にすることで，父親役割と子供役割をアクティブ化，非アクティブ化したときに共通役割のアクティブ状態も一緒に変化する．

`TRoleOfCommon.java`

```java
public final class TRoleOfCommon extends TRole {

    /** 健康状態決定ルール名 */
    public static final String RULE_NAME_OF_DETERMINING_HEALTH = "DeterminingHealth";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param home 自宅
     * @param deactivatedRole 病人役割に切り替える時に非アクティブ化される役割
     */
    public TRoleOfCommon(TAgent owner, TSpot home, Enum<?> deactivatedRole) {
        super(ERoleName.Common, owner, 1, 0);

        // 6時，健康状態決定ステージ，自宅において，25%の確率で病気になる．
        new TRuleOfDeterminingHealth(RULE_NAME_OF_DETERMINING_HEALTH, this, home, 0.25, deactivatedRole)
                .setTimeAndStage(6, 0, 0, EStage.DeterminingHealth);
    }
}
```

### 父親役割

sample03の父親役割と同様．

`TRoleOfFather.java`

```java
public final class TRoleOfFather extends TRole {

    /** ９時に確率的に家を出発するルール名 */
    public static final String RULE_NAME_OF_STOCHASTICALLY_LEAVE_HOME_9 = "StochasticallyLeaveHome9";

    /** １０時に確率的に家を出発するルール名 */
    public static final String RULE_NAME_OF_STOCHASTICALLY_LEAVE_HOME_10 = "StochasticallyLeaveHome10";

    /** １１時に確率的に家を出発するルール名 */
    public static final String RULE_NAME_OF_STOCHASTICALLY_LEAVE_HOME_11 = "StochasticallyLeaveHome11";

    /** 家に帰るルール名 */
    public static final String RULE_NAME_OF_RETURN_HOME = "ReturnHome";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param home 自宅
     * @param company 会社
     */
    public TRoleOfFather(TAgent owner, TSpot home, TSpot company) {
        super(ERoleName.Father, owner, 4, 0);

        // 会社にいるならば自宅に移動する．スケジューリングは，RULE_NAME_OF_STOCHASTICALLY_LEAVE_HOMEで行われる．
        TRule ruleOfReturnHome = new TRuleOfAgentMoving(RULE_NAME_OF_RETURN_HOME, this, company, home);

        // 自宅にいるならば会社に移動し，8時間後のエージェント移動ステージにruleOfReturnHomeをスケジュールする．
        // 9時，10時，11時のエージェント移動ステージにそれぞれのRULE_NAME_OF_STOCHASTICALLY_LEAVE_HOMEが発火するように予約する．
        // TRuleOfStochasticallyAgentMoving は自宅にいる場合のみ会社に移動するため，
        // まず，9時に発火するルールは，自宅にいる場合に0.5の確率で会社に移動する．
        // 次に，10時に発火するルールは，9時に会社に移動していないかつ，0.6の確率で会社に移動するため，0.5*0.6=0.3の確率で会社に移動するルールとなる．
        // 次に，11時に発火するルールは，10時に会社に移動していないかつ，1.0の確率で会社に移動するため，0.5*0.4*1.0=0.2の確率で会社に移動するルールとなる．
        new TRuleOfStochasticallyAgentMoving(RULE_NAME_OF_STOCHASTICALLY_LEAVE_HOME_9, this, home, company,
                ruleOfReturnHome, new TTime("8:00:00"), EStage.AgentMoving, 0.5)
                .setTimeAndStage(9, 0, 0, EStage.AgentMoving);
        new TRuleOfStochasticallyAgentMoving(RULE_NAME_OF_STOCHASTICALLY_LEAVE_HOME_10, this, home, company,
                ruleOfReturnHome, new TTime("8:00:00"), EStage.AgentMoving, 0.6)
                .setTimeAndStage(10, 0, 0, EStage.AgentMoving);
        new TRuleOfStochasticallyAgentMoving(RULE_NAME_OF_STOCHASTICALLY_LEAVE_HOME_11, this, home, company,
                ruleOfReturnHome, new TTime("8:00:00"), EStage.AgentMoving, 1.0)
                .setTimeAndStage(11, 0, 0, EStage.AgentMoving);
    }
}
```

### 子供役割

子供役割は毎日8時に自宅から学校，15時に学校から自宅に移動するルールを持つ．

`TRoleOfChild.java`

```java
public final class TRoleOfChild extends TRole {

    /** 家を出発するルール名 */
    public static final String RULE_NAME_OF_LEAVE_HOME = "LeaveHome";

    /** 家に帰るルール名 */
    public static final String RULE_NAME_OF_RETURN_HOME = "ReturnHome";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param home 自宅
     * @param school 学校
     */
    public TRoleOfChild(TAgent owner, TSpot home, TSpot school) {
        super(ERoleName.Child, owner, 2, 1);

        // 自宅にいるなら学校に移動する
        new TRuleOfAgentMoving(RULE_NAME_OF_LEAVE_HOME, this, home, school)
                .setTimeAndStage(8, 0, 0, EStage.AgentMoving);

        // 学校にいるなら自宅に移動する
        new TRuleOfAgentMoving(RULE_NAME_OF_RETURN_HOME, this, school, home)
                .setTimeAndStage(15, 0, 0, EStage.AgentMoving);
    }
}
```

### 病人役割

病人役割は，10時に自宅から病院に移動し，治療時間が過ぎた後に自宅に戻って病気から回復するルールを持つ．

`TRoleOfSickPerson.java`

```java
public final class TRoleOfSickPerson extends TRole {

    /** 病院に行くルール名 */
    public static final String RULE_NAME_OF_GO_HOSPITAL = "GoHospital";

    /** 病気から回復するルール名 */
    public static final String RULE_NAME_OF_RECOVERING_FROM_SICK = "RecoveringFromSick";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param home 自宅
     * @param hospital 病院
     * @param treatmentTime 治療時間
     * @param activatedRole 病人役割から回復するときにアクティブ化される役割
     */
    public TRoleOfSickPerson(TAgent owner, TSpot home, TSpot hospital, TTime treatmentTime, Enum<?> activatedRole) {
        super(ERoleName.SickPerson, owner, 2, 0);

        // 病気から回復して帰宅する．スケジューリングは，RULE_NAME_OF_GO_HOSPITALで行われる．
        TRule ruleOfRecoveringFromSick = new TRuleOfRecoveringFromSick(RULE_NAME_OF_RECOVERING_FROM_SICK, this,
                home, hospital, activatedRole);

        // 10時に自宅から病院に移動する．診察時間が過ぎたあと，ruleOfRecoveringFromSickを臨時実行ルールとしてスケジューリングする．
        new TRuleOfAgentMoving(RULE_NAME_OF_GO_HOSPITAL, this, home, hospital,
                ruleOfRecoveringFromSick, treatmentTime, EStage.AgentMoving)
                .setTimeAndStage(10, 0, 0, EStage.AgentMoving);
    }
}
```

## メインクラスの定義

共通役割の子役割としての登録には，TRole.addChildRoleメソッドを使用する．

`TMain.java`

```java
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
```
