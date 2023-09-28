前：[sample08:レイヤ機能の利用](src/main/java/jp/soars/tutorials/sample08/)

次：

# sample09:グローバル共有変数集合の利用 <!-- omit in toc -->

sample09ではグローバル共有変数集合の使い方について解説する．

- [シナリオとシミュレーション条件](#シナリオとシミュレーション条件)
- [シミュレーション定数の定義](#シミュレーション定数の定義)
- [ルールの定義](#ルールの定義)
  - [TRuleOfAgentRandomMoving:エージェントランダム移動ルール](#truleofagentrandommovingエージェントランダム移動ルール)
- [役割の定義](#役割の定義)
  - [TRoleOfAgent:エージェント役割](#troleofagentエージェント役割)
- [メインクラスの定義](#メインクラスの定義)


## シナリオとシミュレーション条件

以下のシナリオを考える．

- 10人のエージェント(Agent1〜Agent10)は，毎時刻現実レイヤ(Real)上の10個のスポット(Spot1〜Spot10)上をランダムに動き回る．
- 10人のエージェント(Agent1〜Agent10)は，毎時刻SNSレイヤ(SNS)上の10個のスポット(Spot11〜Spot20)上をランダムに動き回る．
- グローバル共有変数集合には，エージェントが移動しなかった累積回数とエージェントが移動した累積回数をカウントする．
- 10人のエージェント(Agent1〜Agent10)は，移動前スポットと同じスポットが移動先スポットに選ばれた場合，グローバル共有変数集合のエージェントが移動しなかった累積回数を1増やし，それ以外の場合はエージェントが移動した累積回数を1増やす．

シミュレーション条件

- エージェント : Agent(10)
- スポット : Spot(20)
- ステージ : AgentMoving
- レイヤ : Real, SNS
- 時刻ステップ間隔：1時間 / step
- シミュレーション期間：7日間

## シミュレーション定数の定義

sample08に追加して，
グローバル共有変数集合のキー名として，エージェントが移動した累積回数 MOVE とエージェントが移動しなかった累積回数 NO_MOVE を新たに定義する．

`TGlobalSharedVariableSetKey.java`
```Java
public class TGlobalSharedVariableSetKey {
    /** エージェントが移動した累積回数 */
    public static final String MOVE = "MOVE";
    /** エージェントが移動しなかった累積回数 */
    public static final String NO_MOVE = "NO_MOVE";
}
```

`EAgentType.java`

```Java
public enum EAgentType {
    /** エージェント */
    Agent
}
```

`ESpotType.java`

```Java
public enum ESpotType {
    /** スポット */
    Spot
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
    /** エージェント役割 */
    Agent
}
```

`ELayer.java`
```Java
public enum ELayer {
    /** 現実 */
    Real,
    /** SNS */
    SNS
}
```

## ルールの定義

### TRuleOfAgentRandomMoving:エージェントランダム移動ルール

sample08のTRuleOfAgentRandomMovingを拡張する．
移動先スポットを選択した後，エージェントが現在そのスポットにいるかいないかで，グローバル共有変数集合を更新する．

`TRuleOfAgentRandomMoving.java`

```Java
public final class TRuleOfAgentRandomMoving extends TAgentRule {

    /** 移動先スポットタイプ */
    private final Enum<?> fSpotType;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールを持つ役割
     * @param spotType 移動先スポットタイプ
     */
    public TRuleOfAgentRandomMoving(String name, TRole owner, Enum<?> spotType) {
        super(name, owner);
        fSpotType = spotType;
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
        // fSpotType のスポットからランダムに移動先を選択して移動
        // 移動先スポットに応じてグローバル共有変数集合の値を更新する．
        boolean debugFlag = true;
        { // Real
            List<TSpot> spots = spotManager.getSpotsInLayer(ELayer.Real, fSpotType);
            TSpot spot = spots.get(getRandom().nextInt(spots.size()));
            if (isAt(spot)) {
                globalSharedVariables.compute(TGlobalSharedVariableSetKey.NO_MOVE, (k, v) -> (int) v + 1);
            } else {
                globalSharedVariables.compute(TGlobalSharedVariableSetKey.MOVE, (k, v) -> (int) v + 1);
            }
            moveTo(spot);
            appendToDebugInfo("Real:" + spot.getName(), debugFlag);
        }
        { // SNS
            List<TSpot> spots = spotManager.getSpotsInLayer(ELayer.SNS, fSpotType);
            TSpot spot = spots.get(getRandom().nextInt(spots.size()));
            if (isAt(spot)) {
                globalSharedVariables.compute(TGlobalSharedVariableSetKey.NO_MOVE, (k, v) -> (int) v + 1);
            } else {
                globalSharedVariables.compute(TGlobalSharedVariableSetKey.MOVE, (k, v) -> (int) v + 1);
            }
            moveTo(spot);
            appendToDebugInfo(" SNS:" + spot.getName(), debugFlag);
        }
    }
}
```

## 役割の定義

### TRoleOfAgent:エージェント役割

sample08と同じ．

`TRoleOfAgent.java`

```Java
public final class TRoleOfAgent extends TRole {

    /** ランダム移動ルール名 */
    private static final String RULE_NAME_OF_RANDOM_MOVING = "RandomMoving";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     */
    public TRoleOfAgent(TAgent owner) {
        // 親クラスのコンストラクタを呼び出す．
        // 以下の2つの引数は省略可能で，その場合デフォルト値で設定される．
        // 第3引数:この役割が持つルール数 (デフォルト値 10)
        // 第4引数:この役割が持つ子役割数 (デフォルト値 5)
        super(ERoleName.Agent, owner, 1, 0);

        // 役割が持つルールの登録
        // エージェントランダム移動ルール．エージェント移動ステージにステージ実行ルールとして予約する．
        new TRuleOfAgentRandomMoving(RULE_NAME_OF_RANDOM_MOVING, this, ESpotType.Spot)
                .setStage(EStage.AgentMoving);
    }
}
```

## メインクラスの定義

メインクラスの変更点は，グローバル共有変数集合の初期化を行なっている点と独自作成のログをグローバル共有変数集合の時間変化のログに変更した点である．

グローバル共有変数集合はTSOARSBuilderで作成されるが，その実体はConcurrentHashMapである．
ConcurrentHashMapの詳しい仕様は，[公式JavaDoc](https://docs.oracle.com/javase/jp/11/docs/api/java.base/java/util/concurrent/ConcurrentHashMap.html)を参照してもらいたいが，簡単に説明すると多くのメソッドがアトミックであることが保証されていて，かつ高速に動作するハッシュマップである
ConcurrentHashMapのアトミックなメソッドを利用することで，並列化に容易に対応できる．
使い方は通常のハッシュマップと同様で，初期値の設定は put メソッドで行う．

独自に作成するログは，グローバル共有変数集合の値を毎時刻出力するログに変更する．

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
        //   - layers:使用するレイヤー集合
        //   - defaultLayer:デフォルトレイヤー
        // *************************************************************************************************************

        String simulationStart = "0/00:00:00";
        String simulationEnd = "7/00:00:00";
        String tick = "1:00:00";
        List<Enum<?>> stages = List.of(EStage.AgentMoving);
        Set<Enum<?>> agentTypes = new HashSet<>();
        Collections.addAll(agentTypes, EAgentType.values());
        Set<Enum<?>> spotTypes = new HashSet<>();
        Collections.addAll(spotTypes, ESpotType.values());
        Set<Enum<?>> layers = new HashSet<>();
        Collections.addAll(layers, ELayer.values());
        ELayer defaultLayer = ELayer.Real;
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes, layers, defaultLayer);

        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目
        // *************************************************************************************************************

        // エージェント移動ステージを毎時刻ルールが実行される定期実行ステージとして登録
        builder.setPeriodicallyExecutedStage(EStage.AgentMoving, simulationStart, tick);

        // マスター乱数発生器のシード値設定
        long seed = 0L;
        builder.setRandomSeed(seed);

        // ルールログとランタイムログの出力設定
        String pathOfLogDir = "logs" + File.separator + "tutorials" + File.separator + "sample09";
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
        //   - Spot:Spot1-Spot10 (Real)
        //   - Spot:Spot11-Spot20 (SNS)
        // *************************************************************************************************************

        int noOfSpots = 10; // スポットの数
        List<TSpot> realSpots = spotManager.createSpots(ESpotType.Spot, noOfSpots, ELayer.Real);
        List<TSpot> snsSpots = spotManager.createSpots(ESpotType.Spot, noOfSpots, ELayer.SNS);

        // *************************************************************************************************************
        // エージェント作成
        //   - Agent:Agent1-Agent10
        //     - 初期スポット:Spot
        //     - 役割:エージェント役割
        // *************************************************************************************************************

        int noOfAgents = noOfSpots; // エージェントの数はスポットの数と同じ．
        List<TAgent> agents = agentManager.createAgents(EAgentType.Agent, noOfAgents);
        for (int i = 0; i < noOfAgents; ++i) {
            TAgent agent = agents.get(i); // i番目のエージェント
            TSpot realSpot = realSpots.get(i); // i番目の現実スポット
            TSpot snsSpot = snsSpots.get(i); // i番目のSNSスポット
            agent.initializeCurrentSpot(realSpot); // 現実の初期スポット設定
            agent.initializeCurrentSpot(snsSpot); // SNSの初期スポット設定
            new TRoleOfAgent(agent); // エージェント役割作成
            agent.activateRole(ERoleName.Agent); // エージェント役割をアクティブ化
        }

        // *************************************************************************************************************
        // グローバル共有変数集合の初期値設定
        // *************************************************************************************************************

        globalSharedVariableSet.put(TGlobalSharedVariableSetKey.MOVE, 0);
        globalSharedVariableSet.put(TGlobalSharedVariableSetKey.NO_MOVE, 0);

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        //   - グローバル共有変数集合ログ
        // *************************************************************************************************************

        // グローバル共有変数集合ログ用PrintWriter
        PrintWriter gsbsPW = new PrintWriter(new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "global_shared_variable_log.csv")));
        // グローバル共有変数集合ログのカラム名出力
        gsbsPW.print("CurrentTime,");
        gsbsPW.print(TGlobalSharedVariableSetKey.MOVE);
        gsbsPW.print(',');
        gsbsPW.println(TGlobalSharedVariableSetKey.NO_MOVE);

        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        // 1ステップ分のルールを実行 (ruleExecutor.executeStage()で1ステージ毎に実行することもできる)
        // 実行された場合:true，実行されなかった(終了時刻)場合は:falseが帰ってくるため，while文で回すことができる．
        while (ruleExecutor.executeStep()) {
            // 標準出力に現在時刻を表示する
            System.out.println(ruleExecutor.getCurrentTime());

            // グローバル共有変数集合ログ出力
            gsbsPW.print(ruleExecutor.getCurrentTime());
            gsbsPW.print(',');
            gsbsPW.print(globalSharedVariableSet.get(TGlobalSharedVariableSetKey.MOVE));
            gsbsPW.print(',');
            gsbsPW.println(globalSharedVariableSet.get(TGlobalSharedVariableSetKey.NO_MOVE));
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown();
        gsbsPW.close();
    }
}
```

前：[sample08:レイヤ機能の利用](src/main/java/jp/soars/tutorials/sample08/)

次：
