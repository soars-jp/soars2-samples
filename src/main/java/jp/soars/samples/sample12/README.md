<!-- omit in toc -->
# sample12：スポットの定員

- [シナリオとシミュレーション条件](#シナリオとシミュレーション条件)
- [シミュレーション定数の定義](#シミュレーション定数の定義)
- [ルールの定義](#ルールの定義)
  - [エージェント移動ルール](#エージェント移動ルール)
- [役割の定義](#役割の定義)
  - [エージェント役割](#エージェント役割)
- [メインクラスの定義](#メインクラスの定義)


## シナリオとシミュレーション条件

以下のシナリオを考える．

- Spot2は定員が10に設定されている．
- 1000体のエージェント(Agent1〜Agent1000)の初期スポットを Spot1 とする．
- エージェントは，1秒ごとに Spot1, Spot2, Spot3 の順で Spot3 まで移動することを試みる．

シミュレーション条件

- エージェント : Agent(1000)
- スポット : Spot(3)
- ステージ : AgentMoving1, AgentMoving2
- 時刻ステップ間隔：1秒 / step
- シミュレーション期間：100秒間

## シミュレーション定数の定義

sample12では以下の定数を定義する．

- エージェントタイプの定義
- スポットタイプの定義
- ステージの定義
- 役割名の定義
- レイヤーの定義


```java
public enum EAgentType {
    /** エージェント */
    Agent
}
```

```java
public enum ESpotType {
    /** スポット */
    Spot
}
```

```java
public enum EStage {
    /** エージェント移動ステージ1 */
    AgentMoving1,
    /** エージェント移動ステージ2 */
    AgentMoving2,
}
```

```java
public enum ERoleName {
    /** エージェント役割 */
    Agent
}
```

## ルールの定義

sample12では以下のルールを定義する．

- TRuleOfAgentMoving：エージェント移動ルール

### エージェント移動ルール

sample12のエージェント移動ルールは，移動に失敗した場合グローバル共有変数に報告する．

`TRuleOfAgentMoving.java`

```java
public final class TRuleOfAgentMoving extends TAgentRule {

    /** 出発地 */
    private final TSpot fSource;

    /** 目的地 */
    private final TSpot fDestination;

    /** [グローバル共有変数集合キー] 移動に失敗した回数のカウント */
    public static final String FAILED_MOVE_COUNT = "failedMoveCount";

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     * @param source 出発地
     * @param destination 目的地
     */
    public TRuleOfAgentMoving(String name, TRole owner, TSpot source, TSpot destination) {
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
            // moveTo メソッドの返り値が null の場合は，スポットが満員で移動に失敗
            // 失敗した場合は，カウント
            if (moveTo(fDestination) ==null) {
                globalSharedVariables.compute(FAILED_MOVE_COUNT, (k, v) -> v = (int) v + 1);
            }
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

sample12では以下の役割を定義する．

- TRoleOfAgent：エージェント役割

### エージェント役割

エージェント役割では，初期スポット，中継地，目的地の順に移動するようにルールを定義する．
このとき，中継地に定員が定義されているため，中継地から目的地への移動ルールを先に実行する必要があり，ステージ分けをしている点に注意．

`TRoleOfAgent.java`

```java
public final class TRoleOfAgent extends TRole {

    /** エージェント移動ルール1 */
    public static final String RULE_NAME_OF_AGENT_MOVEING1 = "AgentMoving1";

    /** エージェント移動ルール2 */
    public static final String RULE_NAME_OF_AGENT_MOVEING2 = "AgentMoving2";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param source 初期スポット
     * @param stopover 中継地
     * @param destination 目的地
     */
    public TRoleOfAgent(TAgent owner, TSpot source, TSpot stopover, TSpot destination) {
        super(ERoleName.Agent, owner, 1, 0);

        // stopoverに定員があるため，stopoverから先にエージェントを移動させて，その後stopoverへの移動を定義する．
        new TRuleOfAgentMoving(RULE_NAME_OF_AGENT_MOVEING1, this, stopover, destination)
                .setStage(EStage.AgentMoving1);

        new TRuleOfAgentMoving(RULE_NAME_OF_AGENT_MOVEING2, this, source, stopover)
                .setStage(EStage.AgentMoving2);
    }
}
```

## メインクラスの定義

スポットの定員はTSpot.setCapacityメソッドで設定できる．

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
        String simulationEnd = "0/00:00:100"; // シミュレーション終了時刻
        String tick = "00:00:01"; // １ステップの時間間隔
        List<Enum<?>> stages = List.of(EStage.AgentMoving1, EStage.AgentMoving2); // ステージリスト
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
        builder.setParallelizationStages(noOfThreads, EStage.AgentMoving1, EStage.AgentMoving2);

        // 定期実行ステージを登録．第1引数は対象ステージ，第2引数は実行開始時間．第3引数は実行間隔．
        builder.setPeriodicallyExecutedStage(EStage.AgentMoving1, "0/00:00:00", "00:00:01");
        builder.setPeriodicallyExecutedStage(EStage.AgentMoving2, "0/00:00:00", "00:00:01");

        // マスター乱数発生器のシード値設定
        long seed = 0L; // シード値
        builder.setRandomSeed(seed); // シード値設定

        // ログ出力設定
        String pathOfLogDir = "logs" + File.separator + "sample12"; // ログディレクトリ
        builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv") // ルールログ出力設定
               .setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv"); // ランタイムログ出力設定

        // グローバル共有変数の初期値を設定する．
        builder.setInitialValueOfGlobalSharedVariableSet(TRuleOfAgentMoving.FAILED_MOVE_COUNT, Integer.valueOf(0));

        // *************************************************************************************************************
        // TSOARSBuilderの最適化設定項目．
        // *************************************************************************************************************

        // 臨時実行ルールの配列を保持するマップの初期サイズを指定する．今回は臨時実行ルールはないため0に設定してメモリを節約する．
        builder.setExpectedSizeOfTemporaryRulesMap(0);

        // ある時刻のステージに登録されるルールの配列の初期サイズを指定する．
        // ある時刻のAgentMovingステージに登録されるルールの数は父親の数と同じ．
        int noOfAgents = 1000; // 父親の数
        builder.setExpectedNoOfRulesPerStage(EStage.AgentMoving1, noOfAgents);
        builder.setExpectedNoOfRulesPerStage(EStage.AgentMoving2, noOfAgents);

        // 削除されるオブジェクトを一時的に保持するリストの初期サイズを指定する．今回はオブジェクトの削除はないため0に設定してメモリを節約する．
        builder.setExpectedNoOfDeletedObjects(0);

        // エージェントタイプごとにエージェントを保持するリストの初期サイズを指定する．父親の生成数分のリストを確保する．
        builder.setExpectedNoOfAgents(EAgentType.Agent, noOfAgents);

        // レイヤーとスポットタイプごとにスポットを保持するリストの初期サイズを指定する．レイヤーを入力しない場合は，デフォルトレイヤーが指定される．
        int noOfSpots = 3; // スポット数
        builder.setExpectedNoOfSpots(ESpotType.Spot, noOfSpots);

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
        //   - Spot1
        //   - Spot2
        //     - 定員 10
        //   - Spot3
        // *************************************************************************************************************

        TSpot spot1 = spotManager.createSpot(ESpotType.Spot, "Spot1", 0, noOfAgents);
        TSpot spot2 = spotManager.createSpot(ESpotType.Spot, "Spot2", 0, 10);
        TSpot spot3 = spotManager.createSpot(ESpotType.Spot, "Spot3", 0, noOfAgents);
        // スポットの定員を設定
        spot2.setCapacity(10);

        // *************************************************************************************************************
        // エージェント作成
        //   - Agent エージェントを1000
        //     - 初期スポットは Spot1 スポット
        //     - 役割としてエージェント役割を持つ．
        // *************************************************************************************************************

        List<TAgent> agents = agentManager.createAgents(EAgentType.Agent, noOfAgents, 0);
        for (int i = 0; i < agents.size(); ++i) {
            TAgent agent = agents.get(i); // i番目のエージェントを取り出す．
            agent.initializeCurrentSpot(spot1); // 初期位置を Spot1 に設定する

            new TRoleOfAgent(agent, spot1, spot2, spot3); // エージェント役割設定．
            agent.activateRole(ERoleName.Agent); // エージェント役割をアクティブ化する．
        }

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        //   - 各時刻でエージェントの現在位置のログをとる (スポットログ, spot_log.csv)
        //   - 各時刻でスポットに滞在するエージェント数のログをとる (エージェントカウントログ, agent_count_log.csv)
        // *************************************************************************************************************

        // スポットログ用PrintWriter
        PrintWriter spotLogPW = new PrintWriter(new BufferedWriter(
                new FileWriter(pathOfLogDir + File.separator + "spot_log.csv")));
        // スポットログのカラム名出力
        spotLogPW.print("CurrentTime");
        for (TAgent agent : agents) {
            spotLogPW.print("," + agent.getName());
        }
        spotLogPW.println();

        // エージェントカウントログ用 PrintWriter
        PrintWriter agentCountLogPW = new PrintWriter(new BufferedWriter(
                new FileWriter(pathOfLogDir + File.separator + "agent_count_log.csv")));
        // エージェントカウントログのカラム名出力
        agentCountLogPW.println("CurrentTime," + spot1.getName() + "," + spot2.getName() + "," + spot3.getName());

        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        while (ruleExecutor.executeStep()) { // 1ステップ分のルールを実行
            // 標準出力に現在時刻を表示する
            System.out.println(ruleExecutor.getCurrentTime());

            // スポットログ出力
            spotLogPW.print(ruleExecutor.getCurrentTime());
            for (TAgent agent : agents) {
                spotLogPW.print("," + agent.getCurrentSpotName());
            }
            spotLogPW.println();

            // エージェントカウントログ出力
            agentCountLogPW.println(ruleExecutor.getCurrentTime() + "," +
                    spot1.getNoOfAgents() + "," + spot2.getNoOfAgents() + "," + spot3.getNoOfAgents());
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown(); // ルール実行器を終了する
        spotLogPW.close(); // スポットログを終了する
        agentCountLogPW.close(); // エージェントカウントログを終了する

        // 移動に失敗した回数を表示
        System.out.println("Failed move count: " + globalSharedVariableSet.get(TRuleOfAgentMoving.FAILED_MOVE_COUNT));
    }
}
```
