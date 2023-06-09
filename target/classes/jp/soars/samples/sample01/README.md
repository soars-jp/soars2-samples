<!-- omit in toc -->
# sample01：最も簡単なプログラム

- [シナリオとシミュレーション条件](#シナリオとシミュレーション条件)
- [シミュレーション定数の定義](#シミュレーション定数の定義)
- [ルールの定義](#ルールの定義)
  - [エージェント移動ルール](#エージェント移動ルール)
- [役割の定義](#役割の定義)
  - [父親役割](#父親役割)
- [メインクラスの定義](#メインクラスの定義)


## シナリオとシミュレーション条件

以下のシナリオを考える．

- 3人の父親(Father1, Father2, Father3)は，それぞれ自宅(Home1, Home2, Home3)を持つ．
- 3人の父親は，9時に自宅から同じ会社(Company)に移動する．
- 3人の父親は，17時にそれぞれの自宅に移動する．

シミュレーション条件

- エージェント : Father(3)
- スポット : Home(3), Company(1)
- ステージ : AgentMoving
- 時刻ステップ間隔：1時間 / step
- シミュレーション期間：7日間

## シミュレーション定数の定義

sample01では以下の定数を定義する．

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

sample01では以下のルールを定義する．

- TRuleOfAgentMoving：エージェント移動ルール

### エージェント移動ルール

エージェント移動ルールは，出発地にいるのであれば，目的地に移動するルールである．
soars.coreパッケージで定義されているルールクラスTAgentRuleを継承することにより，エージェント移動ルール定義する．
TAgentRuleはエージェント専用のメソッドが用意されており，現在地を判定するisAtや，スポットを移動するmoveToなどが実装されている．

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

## 役割の定義

sample01では以下の役割を定義する．

- TRoleOfFather：父親役割

### 父親役割

soars.coreパッケージで定義されている役割クラスTRoleを継承することにより，父親役割を定義する．

父親役割は，家を出発地，会社を目的地とするエージェント移動ルールLeaveHomeと，会社を出発地，家を目的地とするエージェント移動ルールReturnHomeから構成される．

ルールは発火条件の観点から，３種類に分類される

- 定時実行ルール（regular rule）
  - 毎日決まった時刻に繰り返して実行されるルール．
  - setTimeAndStage(hour, minute, second, stage)で登録する．
- 臨時実行ルール（temporary rule）
  - 指定された時刻に１回だけ実行されるルール．あるルールが実行されないと発火時刻が決まらないルールが相当する．
  - setTimeAndStage(day, hour, minute, second, stage)で登録する．
- ステージ実行ルール (stage rule)
  - 指定されたステージと指定されたステップ間隔で定期的に実行されるルール．
  - setStage(stage)で登録する．
  - このとき，stageは定期実行ステージとして登録されている必要があり，逆に，定期実行ステージには，定時実行ルール・臨時実行ルールは登録できなくなる．

LeaveHomeは毎日9時のエージェント移動ステージに実行されるように予約され，ReturnHomeは毎日17時のエージェント移動ステージに実行されるように予約されている．

`TRoleOfFather.java`

```java
public final class TRoleOfFather extends TRole {

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

        // 自宅にいるならば，会社に移動する．毎日9時，エージェント移動ステージに発火するように予約する．
        new TRuleOfAgentMoving(RULE_NAME_OF_LEAVE_HOME, this, home, company)
                .setTimeAndStage(9, 0, 0, EStage.AgentMoving);

        // 会社にいるならば，自宅に移動する．毎日17時，エージェント移動ステージに発火するように予約する．
        new TRuleOfAgentMoving(RULE_NAME_OF_RETURN_HOME, this, company, home)
                .setTimeAndStage(17, 0, 0, EStage.AgentMoving);
    }
}
```

## メインクラスの定義

メインクラスでモデルを実行する流れは以下の通り，

1. TSOARSBuilderのインスタンスを作成する．コンストラクタには必須設定項目を渡す．
   - simulationStart: シミュレーション開始時刻
   - simulationEnd: シミュレーション終了時刻
   - tick: １ステップの秒数
   - stages: 使用するステージリスト
   - agentTypes: 使用するエージェントタイプ集合
   - spotTypes: 使用するスポットタイプ集合
4. TSOARSBuilderのメソッドを使用してシミュレーションの任意設定項目を設定する．
   - setRandomSeed: マスター乱数発生器のシード値を設定する．
   - setInitialValueOfGlobalSharedVariableSet: グローバル共有変数のキーと初期値を設定する．
   - setParallelizationStages: 並列化するステージと並列数を設定する．
   - mergeStages: ステージをマージする．
   - setPeriodicallyExecutedStage: 定期実行ステージを設定する．
   - setRuleLoggingEnabled: ルールログの出力設定をする．
   - setRuntimeLoggingEnabled: ランタイムログの出力設定をする．
   - setRulesSortedBeforeExecutedFlag: ルールを実行前にソートするかを設定する．
   - setRulesShuffledBeforeExecutedFlag: ルールを実行前にシャッフルするかを設定する．
   - setFlagOfCreatingRandomForEachAgent: 各エージェントに個別に乱数発生器を持たせるかを設定する．
   - setFlagOfCreatingRandomForEachSpot: 各スポットに個別に乱数発生器を持たせるかを設定する．
   - setWarningFlag:　警告メッセージを表示するかを設定する．
5. TSOARSBuilderでシミュレーションに必要なインスタンスを作成する．
   - build()メソッド実行後に，getterメソッドよってインスタンスを得ることができる．
   - getRuleExecutor: ルール実行器
   - getAgentManager: エージェント管理
   - getSpotManager: スポット管理
   - getRandom: マスター乱数発生器
   - getGlobalSharedVariableSet: グローバル共有変数集合
6. スポットを作成する．今回は自宅を3個(Home1, Home2, Home3)と会社を1個(Company)作成している．
7. エージェントを作成する．今回は父親を3個(Father1, Father2, Father3)生成している．父親にはそれぞれ父親役割が割り当てられ，自宅としてそれぞれHome1, Home2, Home3が初期スポットに設定されている．
8. シミュレーションを実行する．ルール実行器のexecuteStepを実行することで，現在時刻の全てのステージに登録されているルールを実行することができる．これを繰り返し実行することでシミュレーションが進む．ステージを1つずつ進めてログを取りたい場合は，executeStageメソッドを使用する．
9. 終了処理をする．ルール実行器はshutdownメソッドを実行して終了処理を行う必要がある．

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
        String pathOfLogDir = "logs" + File.separator + "sample01"; // ログディレクトリ
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
