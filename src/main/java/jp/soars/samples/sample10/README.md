<!-- omit in toc -->
# sample10：レイヤー機能

- [シナリオとシミュレーション条件](#シナリオとシミュレーション条件)
- [シミュレーション定数の定義](#シミュレーション定数の定義)
- [ルールの定義](#ルールの定義)
  - [ランダム移動ルール](#ランダム移動ルール)
- [役割の定義](#役割の定義)
  - [エージェント役割](#エージェント役割)
- [メインクラスの定義](#メインクラスの定義)


## シナリオとシミュレーション条件

以下のシナリオを考える．

- RealレイヤーにSpotスポットが10，SNSレイヤーにSpotスポットが20．
- 100人のエージェントの初期スポットはランダムに決定する．
- 100人のエージェントは1秒おきに，ランダムに選んだレイヤー上で，ランダムに選んだスポットに移動する．

シミュレーション条件

- エージェント : Agent(100)
- レイヤー : Real, SNS
- スポット : Real-Spot(10), SNS-Spot(20)
- ステージ : AgentMoving
- 時刻ステップ間隔：1秒 / step
- シミュレーション期間：10分間


## シミュレーション定数の定義

sample10では以下の定数を定義する．

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
    /** エージェント移動ステージ */
    AgentMoving
}
```

```java
public enum ERoleName {
    /** エージェント役割 */
    Agent
}
```

```java
public enum ELayer {
    /** 現実 */
    Real,
    /** SNS */
    SNS
}
```

## ルールの定義

sample10では以下のルールを定義する．

- TRuleOfRandomMoving：ランダム移動ルール

### ランダム移動ルール

ランダム移動ルールはランダムに選択したレイヤー上のスポットをランダムに選択して移動する．

`TRuleOfRandomMoving.java`

```java
public final class TRuleOfRandomMoving extends TAgentRule {

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールを持つ役割
     */
    public TRuleOfRandomMoving(String name, TRole owner) {
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
        // レイヤーをランダムに選択する．
        ELayer[] layers = ELayer.values();
        ELayer layer = layers[getRandom().nextInt(layers.length)];
        // レイヤー上にあるスポットから，移動先のスポットを現在いるスポット以外からランダムに選択する．
        List<TSpot> spots = spotManager.getSpotsInLayer(layer);
        TSpot spot;
        do {
            spot = spots.get(getRandom().nextInt(spots.size()));
        } while (isAt(spot));
        // 移動
        moveTo(spot);
    }
}
```

## 役割の定義

sample10では以下の役割を定義する．

- TRoleOfAgent：エージェント役割

### エージェント役割

`TRoleOfAgent.java`

```java
public final class TRoleOfAgent extends TRole {

    /** ランダムに移動する */
    public static final String RULE_NAME_OF_RANDOM_MOVEING = "RandomMoving";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     */
    public TRoleOfAgent(TAgent owner) {
        super(ERoleName.Agent, owner, 1, 0);

        // ステージルールとして登録
        new TRuleOfRandomMoving(RULE_NAME_OF_RANDOM_MOVEING, this)
                .setStage(EStage.AgentMoving);
    }
}
```

## メインクラスの定義

レイヤーはTSOARSBuilderのインスタンス作成時に指定する．このときデフォルトレイヤーを決める必要がある．
デフォルトレイヤーはレイヤーを指定する必要があるメソッドでデフォルト値として使用される．

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
        //   - layers: 使用するレイヤー集合
        //   - defaultLayer: デフォルトレイヤー
        // *************************************************************************************************************

        String simulationStart = "0/00:00:00"; // シミュレーション開始時刻
        String simulationEnd = "0/00:10:00"; // シミュレーション終了時刻
        String tick = "00:00:01"; // １ステップの時間間隔
        List<Enum<?>> stages = List.of(EStage.AgentMoving); // ステージリスト
        Set<Enum<?>> agentTypes = new HashSet<>(); // 全エージェントタイプ
        Set<Enum<?>> spotTypes = new HashSet<>(); // 全スポットタイプ
        Set<Enum<?>> layers = new HashSet<>(); // 全レイヤー
        Collections.addAll(agentTypes, EAgentType.values()); // EAgentType に登録されているエージェントタイプをすべて追加
        Collections.addAll(spotTypes, ESpotType.values()); // ESpotType に登録されているスポットタイプをすべて追加
        Collections.addAll(layers, ELayer.values()); // TLayer に登録されているレイヤーをすべて追加
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes, layers, ELayer.Real); // ビルダー作成

        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目．
        // *************************************************************************************************************

        // 並列化するステージを登録．第1引数はスレッド数，第2引数以降は並列実行するステージ．
        int noOfThreads = 4;
        builder.setParallelizationStages(noOfThreads, EStage.AgentMoving);

        // 定期実行ステージを登録．第1引数は対象ステージ，第2引数は実行開始時間．第3引数は実行間隔．
        builder.setPeriodicallyExecutedStage(EStage.AgentMoving, "0/00:00:00", "00:00:01");

        // マスター乱数発生器のシード値設定
        long seed = 0L; // シード値
        builder.setRandomSeed(seed); // シード値設定

        // ログ出力設定
        String pathOfLogDir = "logs" + File.separator + "sample10"; // ログディレクトリ
        builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv") // ルールログ出力設定
               .setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv"); // ランタイムログ出力設定

        // *************************************************************************************************************
        // TSOARSBuilderの最適化設定項目．
        // *************************************************************************************************************

        // 臨時実行ルールの配列を保持するマップの初期サイズを指定する．今回は臨時実行ルールはないため0に設定してメモリを節約する．
        builder.setExpectedSizeOfTemporaryRulesMap(0);

        // ある時刻のステージに登録されるルールの配列の初期サイズを指定する．
        // ある時刻のAgentMovingステージに登録されるルールの数は父親の数と同じ．
        int noOfAgents = 100; // 父親の数
        builder.setExpectedNoOfRulesPerStage(EStage.AgentMoving, noOfAgents);

        // 削除されるオブジェクトを一時的に保持するリストの初期サイズを指定する．今回はオブジェクトの削除はないため0に設定してメモリを節約する．
        builder.setExpectedNoOfDeletedObjects(0);

        // エージェントタイプごとにエージェントを保持するリストの初期サイズを指定する．父親の生成数分のリストを確保する．
        builder.setExpectedNoOfAgents(EAgentType.Agent, noOfAgents);

        // レイヤーとスポットタイプごとにスポットを保持するリストの初期サイズを指定する．レイヤーを入力しない場合は，デフォルトレイヤーが指定される．
        int noOfRealSpots = 10; // Real レイヤー上のスポット数
        int noOfSNSSpots = 20; // SNS レイヤー上のスポット数
        builder.setExpectedNoOfSpots(ELayer.Real, ESpotType.Spot, noOfRealSpots);
        builder.setExpectedNoOfSpots(ELayer.SNS, ESpotType.Spot, noOfSNSSpots);

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
        //   - Real レイヤー上の Spot スポットを10
        //   - SNS レイヤー上の Spot スポットを20
        // *************************************************************************************************************

        List<TSpot> realSpots = spotManager.createSpots(ESpotType.Spot, noOfRealSpots, ELayer.Real, 0 , noOfAgents);
        List<TSpot> snsSpots = spotManager.createSpots(ESpotType.Spot, noOfSNSSpots, ELayer.SNS, 0, noOfAgents);

        // *************************************************************************************************************
        // エージェント作成
        //   - Agent エージェントを100
        //     - 初期スポットは Spot スポット
        //     - 役割としてエージェント役割を持つ．
        // *************************************************************************************************************

        // エージェントの初期化
        List<TAgent> agents = agentManager.createAgents(EAgentType.Agent, noOfAgents, 1);
        for (int i = 0; i < agents.size(); ++i) {
            TAgent agent = agents.get(i); // i番目のエージェントを取り出す．
            agent.initializeCurrentSpot(realSpots.get(random.nextInt(realSpots.size()))); // Real レイヤー上の初期スポットをランダムに設定
            agent.initializeCurrentSpot(snsSpots.get(random.nextInt(snsSpots.size()))); // SNS レイヤー上の初期スポットをランダムに設定

            new TRoleOfAgent(agent); // エージェント役割を生成する．
            agent.activateRole(ERoleName.Agent); // エージェント役割をアクティブ化する．
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
        for (TAgent agent : agents) {
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
            for (TAgent agent : agents) {
                spotLogPW.print("," + ELayer.Real.toString() + ":" + agent.getCurrentSpotName(ELayer.Real)
                        + " " + ELayer.SNS.toString() + ":" + agent.getCurrentSpotName(ELayer.SNS));
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
