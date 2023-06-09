<!-- omit in toc -->
# sample09：ステージ実行ルールとグローバル共有変数集合

- [シナリオとシミュレーション条件](#シナリオとシミュレーション条件)
- [シミュレーション定数の定義](#シミュレーション定数の定義)
- [ルールの定義](#ルールの定義)
  - [エージェント移動ルール](#エージェント移動ルール)
  - [集計ルール](#集計ルール)
- [役割の定義](#役割の定義)
  - [父親役割](#父親役割)
- [メインクラスの定義](#メインクラスの定義)


## シナリオとシミュレーション条件

以下のシナリオを考える．

- 10万人の父親は，それぞれ自宅を持つ．
- 10万人の父親は，9時に自宅から会社に移動する．
- 10万人の父親は，17時にそれぞれの自宅に移動する．
- 毎時刻，父親がいる場所がグローバル共有変数に集計される．グローバル共有変数には「その時刻までにすべてのエージェントが自宅または職場にいた時間の総計」が記録される．

シミュレーション条件

- エージェント : Father(10万)
- スポット : Home(10万), Company(100)
- ステージ : AgentMoving, Aggregation
- 時刻ステップ間隔：1時間 / step
- シミュレーション期間：7日間


## シミュレーション定数の定義

sample09では以下の定数を定義する．

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
    AgentMoving,
    /** 集計ステージ */
    Aggregation
}
```

```java
public enum ERoleName {
    /** 父親役割 */
    Father
}
```

## ルールの定義

sample09では以下のルールを定義する．

- TRuleOfAgentMoving：エージェント移動ルール
- TRuleOfAggregation：集計ルール

### エージェント移動ルール

sample01のエージェント移動ルールと同様．

`TRuleOfAgentMoving.java`

```java
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
        if (isAt(fSource)) { // 出発地にいるなら
            moveTo(fDestination); // 目的地に移動する
        }
    }

    /**
     * ルールログで表示するデバッグ情報．
     * @return デバッグ情報
     */
    @Override
    public final String debugInfo() {
        // 設定されている出発地と目的地をデバッグ情報として出力する．
        return fSource.getName() + ":" + fDestination.getName();
    }
}
```

### 集計ルール

集計ルールは，エージェントが位置するスポットをグローバル共有変数に報告して集計する．
集計ルールは，集計ステージにステージ実行ルールとして登録され，集計ステージは毎時刻ルールが実行されるように設定される．
また，グローバル共有変数集合の実体はConcurrentHashMapで，computeメソッドはアトミックであることが保証されており，
並列化ステージで使用できるため集計ステージを並列ステージとして設定する．

`TRuleOfAggregation.java`

```java
public final class TRuleOfAggregation extends TAgentRule {

    /** 自宅 */
    private TSpot fHome;

    /** 会社 */
    private TSpot fCompany;

    /** [グローバル共有変数で集計するキー] 自宅 */
    public static final String HOME_KEY = "Home";

    /** [グローバル共有変数で集計するキー] 会社 */
    public static final String COMPANY_KEY = "Company";

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     * @param home 自宅
     * @param company 職場
     */
    public TRuleOfAggregation(String name, TRole owner, TSpot home, TSpot company){
        super(name, owner);
        fHome = home;
        fCompany = company;
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
        // 自分がいる場所をグローバル共有変数に報告する．
        // ConcurrentHashMapのcomputeメソッドはアトミックであることが保証されており，並列化ステージで使用できる．
        if (isAt(fHome)) {
            globalSharedVariables.compute(HOME_KEY, (k, v) -> v = (long) v + 1);
        } else if (isAt(fCompany)) {
            globalSharedVariables.compute(COMPANY_KEY, (k, v) -> v = (long) v + 1);
        }
    }
}
```

## 役割の定義

sample09では以下の役割を定義する．

- TRoleOfFather：父親役割

### 父親役割

集計ルールはステージ実行ルールであるため，ステージのみ指定し実行時刻はステージの設定による．
今回は，集計ステージは毎時刻実行されるように設定されるため毎時刻の集計ステージでルールが実行される．

`TRoleOfFather.java`

```java
public final class TRoleOfFather extends TRole {
	/** 家を出発するルール名 */
    private static final String RULE_NAME_OF_LEAVE_HOME = "LeaveHome";

    /** 家に帰るルール名 */
    private static final String RULE_NAME_OF_RETURN_HOME = "ReturnHome";

	/** 集計ルール名 */
    public static final String RULE_NAME_OF_AGGREGATION = "Aggregation";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param home 自宅
     * @param company 会社
     */
    public TRoleOfFather(TAgent owner, TSpot home, TSpot company) {
        super(ERoleName.Father, owner, 3, 0);

        // 自宅にいるならば，会社に移動する．毎日9時，エージェント移動ステージに発火するように予約する．
        new TRuleOfAgentMoving(RULE_NAME_OF_LEAVE_HOME, this, home, company)
                .setTimeAndStage(9, 0, 0, EStage.AgentMoving);

        // 会社にいるならば，自宅に移動する．毎日17時，エージェント移動ステージに発火するように予約する．
        new TRuleOfAgentMoving(RULE_NAME_OF_RETURN_HOME, this, company, home)
                .setTimeAndStage(17, 0, 0, EStage.AgentMoving);

		// 集計ルールはステージ実行ルールとして予約．
        new TRuleOfAggregation(RULE_NAME_OF_AGGREGATION, this, home, company)
                .setStage(EStage.Aggregation);
    }
}
```

## メインクラスの定義

集計ステージは定期実行ステージとして登録される．実行開始時刻を 0/00:00:00 実行間隔を 1:00:00 と指定すれば1時間ごとにルールが実行される．
グローバル共有変数を使用する場合，TSOARSBuilderにキーと初期値を渡すことでインスタンスが作成される．

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

        long startTimeOfSimulation = System.nanoTime(); // シミュレーション開始時刻
        String simulationStart = "0/00:00:00"; // シミュレーション開始時刻
        String simulationEnd = "7/00:00:00"; // シミュレーション終了時刻
        String tick = "1:00:00"; // １ステップの時間間隔
        List<Enum<?>> stages = List.of(EStage.AgentMoving, EStage.Aggregation); // ステージリスト
        Set<Enum<?>> agentTypes = new HashSet<>(); // 全エージェントタイプ
        Set<Enum<?>> spotTypes = new HashSet<>(); // 全スポットタイプ
        Collections.addAll(agentTypes, EAgentType.values()); // EAgentType に登録されているエージェントタイプをすべて追加
        Collections.addAll(spotTypes, ESpotType.values()); // ESpotType に登録されているスポットタイプをすべて追加
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes); // ビルダー作成

        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目．
        // *************************************************************************************************************

        // 並列化するステージを登録．第1引数はスレッド数，第2引数以降は並列実行するステージ．
        int noOfThreads = 4;
        builder.setParallelizationStages(noOfThreads, EStage.AgentMoving, EStage.Aggregation);

        // 定期実行ステージを登録．第1引数は対象ステージ，第2引数は実行開始時間．第3引数は実行間隔．
        builder.setPeriodicallyExecutedStage(EStage.Aggregation, "0/00:00:00", "1:00:00");

        // マスター乱数発生器のシード値設定
        long seed = 0L; // シード値
        builder.setRandomSeed(seed); // シード値設定

        // ログ出力設定
        String pathOfLogDir = "logs" + File.separator + "sample09"; // ログディレクトリ
        builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv") // ルールログ出力設定
               .setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv"); // ランタイムログ出力設定

        // グローバル共有変数の初期値を設定する．
        builder.setInitialValueOfGlobalSharedVariableSet(TRuleOfAggregation.HOME_KEY, Long.valueOf(0));
        builder.setInitialValueOfGlobalSharedVariableSet(TRuleOfAggregation.COMPANY_KEY, Long.valueOf(0));

        // *************************************************************************************************************
        // TSOARSBuilderの最適化設定項目．
        // *************************************************************************************************************

        // 臨時実行ルールの配列を保持するマップの初期サイズを指定する．今回は臨時実行ルールはないため0に設定してメモリを節約する．
        builder.setExpectedSizeOfTemporaryRulesMap(0);

        // ある時刻のステージに登録されるルールの配列の初期サイズを指定する．
        // ある時刻のAgentMovingステージに登録されるルールの数は父親の数と同じ．
        int noOfFathers = 100000; // 父親の数
        builder.setExpectedNoOfRulesPerStage(EStage.AgentMoving, noOfFathers);
        builder.setExpectedNoOfRulesPerStage(EStage.Aggregation, noOfFathers);

        // 削除されるオブジェクトを一時的に保持するリストの初期サイズを指定する．今回はオブジェクトの削除はないため0に設定してメモリを節約する．
        builder.setExpectedNoOfDeletedObjects(0);

        // エージェントタイプごとにエージェントを保持するリストの初期サイズを指定する．父親の生成数分のリストを確保する．
        builder.setExpectedNoOfAgents(EAgentType.Father, noOfFathers);

        // レイヤーとスポットタイプごとにスポットを保持するリストの初期サイズを指定する．レイヤーを入力しない場合は，デフォルトレイヤーが指定される．
        int noOfHomes = noOfFathers; // 家の数は父親の数と同じ．
        int noOfCompanies = 100; // 会社の数
        builder.setExpectedNoOfSpots(ESpotType.Home, noOfFathers);
        builder.setExpectedNoOfSpots(ESpotType.Company, noOfCompanies);

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
        //   - Home スポットを10万
        //   - Company スポットを100
        // *************************************************************************************************************

        // TSpotManager の最適化設定項目を利用する．createSpotsメソッドの以下の引数を指定する
        // 第3引数: スポットが持つ役割の数
        // 第4引数: スポットに滞在するエージェント数の予測値
        long startTimeOfSpotInitialization = System.nanoTime(); // スポット初期化開始時刻
        // Home スポット生成．役割数0，エージェント数の予測値1
        List<TSpot> homes = spotManager.createSpots(ESpotType.Home, noOfHomes, 0, 1);
        // Company スポット生成．役割数0，エージェント数の予測値10万/100
        List<TSpot> companies = spotManager.createSpots(ESpotType.Company, noOfCompanies, 0, noOfFathers / noOfCompanies);
        long timeOfSpotInitialization = System.nanoTime() - startTimeOfSpotInitialization; // スポット初期化時間

        // *************************************************************************************************************
        // エージェント作成
        //   - Father エージェントを10万
        //     - 初期スポットは Home スポット
        //     - 役割として父親役割を持つ．
        // *************************************************************************************************************

        // TAgentManager の最適化設定項目を利用する．createAgentsメソッドの以下の引数を指定する
        // 第3引数: スポットが持つ役割の数
        long startTimeOfAgentInitialization = System.nanoTime(); // エージェント初期化開始時刻
        // Father エージェント生成．役割数1
        List<TAgent> fathers = agentManager.createAgents(EAgentType.Father, noOfFathers, 1);
        for (int i = 0, n = fathers.size(); i < n; ++i) {
            TAgent father = fathers.get(i); // i番目のエージェントを取り出す．
            TSpot home = homes.get(i); // i番目のエージェントの自宅を取り出す．
            TSpot company = companies.get(i % noOfCompanies); // 会社に均等に割り振る．
            father.initializeCurrentSpot(home); // 初期位置を自宅に設定する．

            new TRoleOfFather(father, home, company); // 父親役割を生成する．
            father.activateRole(ERoleName.Father); // 父親役割をアクティブ化する．
        }
        long timeOfAgentInitialization = System.nanoTime() - startTimeOfAgentInitialization; // エージェント初期化時間

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        //   - 各時刻でエージェントの現在位置のログをとる (スポットログ, spot_log.csv)
        //   - 各時刻でグローバル共有変数のログをとる (グローバル共有変数ログ, global_shared_variable_set_log.csv)
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

        // グローバル共有変数ログ用PrintWriter
        PrintWriter globalSharedVariableSetLogPW = new PrintWriter(new BufferedWriter(
                new FileWriter(pathOfLogDir + File.separator + "global_shared_variable_set_log.csv")));
        // グローバル共有変数ログのカラム名出力
        globalSharedVariableSetLogPW.println("CurrentTime," + TRuleOfAggregation.HOME_KEY + "," + TRuleOfAggregation.COMPANY_KEY);

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

            // グローバル共有変数ログ出力
            globalSharedVariableSetLogPW.println(ruleExecutor.getCurrentTime() + "," +
                    globalSharedVariableSet.get(TRuleOfAggregation.HOME_KEY) + "," +
                    globalSharedVariableSet.get(TRuleOfAggregation.COMPANY_KEY));
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown(); // ルール実行器を終了する
        spotLogPW.close(); // スポットログを終了する
        globalSharedVariableSetLogPW.close(); // グローバル共有変数ログを終了する
        long timeOfSimulation = System.nanoTime() - startTimeOfSimulation; // シミュレーション実行時間

        // *************************************************************************************************************
        // シミュレーションのパフォーマンス集計
        // *************************************************************************************************************

        PrintWriter simulationLogPW = new PrintWriter(new BufferedWriter(
                new FileWriter(pathOfLogDir + File.separator + "simulationInfo.txt")));
        // シミュレーション基本情報
        simulationLogPW.println("sample10: Simulation of " + noOfHomes + " migrations. [parallel execution]");
        simulationLogPW.println("Simulation period = " + simulationEnd);
        simulationLogPW.println("Time per step = " + tick);
        simulationLogPW.println("Random seed = " + seed);
        simulationLogPW.println("Number of threads = " + noOfThreads);
        System.out.println("===========================================================================");
        System.out.println("sample10: Simulation of " + noOfHomes + " migrations. [parallel execution]");
        System.out.println("Simulation period = " + simulationEnd);
        System.out.println("Time per step = " + tick);
        System.out.println("Random seed = " + seed);
        System.out.println("Number of threads = " + noOfThreads);
        // シミュレーション総実行時間
        simulationLogPW.println("Total simulation run time = " + timeOfSimulation + "[ns] (" + (timeOfSimulation / 1_000_000) + "[ms])");
        System.out.println("Total simulation run time = " + timeOfSimulation + "[ns] (" + (timeOfSimulation / 1_000_000) + "[ms])\n");
        // 総スポット数とスポット初期化時間
        simulationLogPW.println("Total number of spots = " + spotManager.getSpotDB().size());
        simulationLogPW.println("Spot initialization time = " + timeOfSpotInitialization + "[ns] (" + (timeOfSpotInitialization / 1_000_000) + "[ms])");
        System.out.println("Total number of spots = " + spotManager.getSpotDB().size());
        System.out.println("Spot initialization time = " + timeOfSpotInitialization + "[ns] (" + (timeOfSpotInitialization / 1_000_000) + "[ms])\n");
        // 総エージェント数とエージェント初期化時間
        simulationLogPW.println("Total number of agents = " + agentManager.getAgentDB().size());
        simulationLogPW.println("Agent initialization time = " + timeOfAgentInitialization + "[ns] (" + (timeOfAgentInitialization / 1_000_000) + "[ms])");
        System.out.println("Total number of agents = " + agentManager.getAgentDB().size());
        System.out.println("Agent initialization time = " + timeOfAgentInitialization + "[ns] (" + (timeOfAgentInitialization / 1_000_000) + "[ms])\n");
        // 総実行ルール数とルール実行時間
        // ランタイムログを取得
        TCSimpleCsvData csv = new TCSimpleCsvData(pathOfLogDir + File.separator + "runtime_log.csv");
        long noOfExecutedRules = 0; // 総実行ルール数
        long ruleExecutionTime = 0; // 総実行時間 [ns]
        while (csv.readLine()) {
            noOfExecutedRules += csv.getElementAsLong(ERuntimeLogKey.RuleExecutionCount.toString());
            ruleExecutionTime += csv.getElementAsLong(ERuntimeLogKey.ExecutionTimeInNanoSec.toString());
        }
        csv.close();

        simulationLogPW.println("Total number of executed rules = " + noOfExecutedRules);
        simulationLogPW.println("Rule execution Time = " + ruleExecutionTime + "[ns] (" + (ruleExecutionTime / 1_000_000) + "[ms])\n");
        System.out.println("Total number of executed rules = " + noOfExecutedRules);
        System.out.println("Rule execution Time = " + ruleExecutionTime + "[ns] (" + (ruleExecutionTime / 1_000_000) + "[ms])\n");

        // グローバル共有変数の結果
        simulationLogPW.println("globalSharedVariableSet." + TRuleOfAggregation.HOME_KEY + " = " + globalSharedVariableSet.get(TRuleOfAggregation.HOME_KEY));
        simulationLogPW.println("globalSharedVariableSet." + TRuleOfAggregation.COMPANY_KEY + " = " + globalSharedVariableSet.get(TRuleOfAggregation.COMPANY_KEY));
        simulationLogPW.close();
        System.out.println("globalSharedVariableSet." + TRuleOfAggregation.HOME_KEY + " = " + globalSharedVariableSet.get(TRuleOfAggregation.HOME_KEY));
        System.out.println("globalSharedVariableSet." + TRuleOfAggregation.COMPANY_KEY + " = " + globalSharedVariableSet.get(TRuleOfAggregation.COMPANY_KEY));
        System.out.println("===========================================================================");
    }
}
```
