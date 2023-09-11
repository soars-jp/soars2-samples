
# sample03：確率的なルールの定義 <!-- omit in toc -->

- [シナリオとシミュレーション条件](#シナリオとシミュレーション条件)
- [シミュレーション定数の定義](#シミュレーション定数の定義)
- [ルールの定義](#ルールの定義)
  - [エージェント移動ルール](#エージェント移動ルール)
  - [確率的エージェント移動ルール](#確率的エージェント移動ルール)
- [役割の定義](#役割の定義)
  - [父親役割](#父親役割)
- [メインクラスの定義](#メインクラスの定義)


## シナリオとシミュレーション条件

以下のシナリオを考える．

- 3人の父親(Father1, Father2, Father3)は，それぞれ自宅(Home1, Home2, Home3)を持つ．
- 3人の父親は，50%の確率で9時，30%の確率で10時，20%の確率で11時に自宅から同じ会社(Company)に移動する．
- 3人の父親は，出社して8時間後にそれぞれの自宅に移動する．

シミュレーション条件

- エージェント : Father(3)
- スポット : Home(3), Company(1)
- ステージ : AgentMoving
- 時刻ステップ間隔：1時間 / step
- シミュレーション期間：7日間


## シミュレーション定数の定義

sample03では以下の定数を定義する．

- エージェントタイプの定義
- スポットタイプの定義
- ステージの定義
- 役割名の定義


```java
public enum EAgentType {
    /** 父親 */
    Father
}
```

```java
public enum ESpotType {
    /** 自宅 */
    Home,
    /** 会社 */
    Company
}
```

```java
public enum EStage {
    /** エージェント移動ステージ */
    AgentMoving
}
```

```java
public enum ERoleName {
    /** 父親役割 */
    Father
}
```

## ルールの定義

sample03では以下のルールを定義する．

- TRuleOfAgentMoving：エージェント移動ルール
- TRuleOfStochasticallyAgentMoving：確率的エージェント移動ルール

### エージェント移動ルール

sample02のエージェント移動ルールと同様．

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

TRuleOfAgentMovingを拡張して，確率的エージェント移動ルールTRuleOfStochasticallyAgentMovingを定義する．
コンストラクタで移動確率を引数としてとり，エージェントが出発地にいるかつ移動確率条件が満たされたら移動先に移動し，
次のルールを臨時実行ルールとしてスケジュールする．

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

## 役割の定義

sample03では以下の役割を定義する．

- TRoleOfFather：父親役割

### 父親役割

父親役割は，家を出発地，会社を目的地とするエージェント移動ルールLeaveHomeと，
会社を出発地，家を目的地とする確率的エージェント移動ルールStochasticallyLeaveHomeから構成される．

シナリオの50%の確率で9時，30%の確率で10時，20%の確率で11時に自宅から同じ会社(Company)に移動を表現するために，
自宅にいるかつ，確率条件が満たされた時のみエージェントが移動するTRuleOfStochasticallyAgentMovingを利用する．

- まず，9時に自宅から会社に0.5の確率で移動するルールを定義する．
- 次に，10時に自宅から会社に0.6の確率で移動するルールを定義する．このルールは9時に自宅から会社に移動していないかつ，0.6の確率でエージェントが移動するルールとなるため，総じて0.5*0.6=0.3の確率でエージェントが移動するルールとなる．
- 次に，11時に自宅から会社に1.0の確率で移動するルールを定義する．このルールは9時,10時に自宅から会社に移動していないかつ，1.0の確率でエージェントが移動するルールとなるため，総じて0.5*0.4*1.0=0.2の確率でエージェントが移動するルールとなる．

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

## メインクラスの定義

sample02との差分はログ出力先ディレクトリを変更したのみである．

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
        String pathOfLogDir = "logs" + File.separator + "sample03"; // ログディレクトリ
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
        // *************************************************************************************************************

        int noOfHomes = 3; // 家の数
        List<TSpot> homes = spotManager.createSpots(ESpotType.Home, noOfHomes); // Homeスポットを生成．(Home1, Home2, ...)
        TSpot company = spotManager.createSpot(ESpotType.Company); // Companyスポットを生成．(Company)

        // *************************************************************************************************************
        // エージェント作成
        //   - Father エージェントを3つ
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
