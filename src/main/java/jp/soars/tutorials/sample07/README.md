前：
次：

# sample07:ステージ実行ルールによるルールの定期実行 <!-- omit in toc -->

sample07では，ステージ実行ルールの利用方法について解説する．

- [シナリオとシミュレーション条件](#シナリオとシミュレーション条件)
- [シミュレーション定数の定義](#シミュレーション定数の定義)
- [ルールの定義](#ルールの定義)
  - [TRuleOfAgentRandomMoving:エージェントランダム移動ルール](#truleofagentrandommovingエージェントランダム移動ルール)
- [役割の定義](#役割の定義)
  - [TRoleOfAgent:エージェント役割](#troleofagentエージェント役割)
- [メインクラスの定義](#メインクラスの定義)


## シナリオとシミュレーション条件

以下のシナリオを考える．

- 10人のエージェント(Agent1〜Agent10)は，毎時刻10個のスポット(Spot1〜Spot10)上をランダムに動き回る．

シミュレーション条件

- エージェント : Agent(10)
- スポット : Spot(10)
- ステージ : AgentMoving
- 時刻ステップ間隔：1時間 / step
- シミュレーション期間：7日間

## シミュレーション定数の定義

sample07では，
エージェントタイプとしてエージェント，
スポットタイプとしてスポット，
ステージとしてエージェント移動ステージ，
役割名としてエージェント役割を定義する．


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

## ルールの定義

### TRuleOfAgentRandomMoving:エージェントランダム移動ルール

エージェントランダム移動ルールはコンストラクタで受け取ったスポットタイプのスポットの中からランダムに1つ選択してそこに移動する．

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
        boolean debugFlag = true;
        List<TSpot> spots = spotManager.getSpots(fSpotType);
        TSpot spot = spots.get(getRandom().nextInt(spots.size()));
        moveTo(spot);
        appendToDebugInfo("move to " + spot.getName(), debugFlag);
    }
}
```

## 役割の定義

### TRoleOfAgent:エージェント役割

エージェント役割はエージェントランダム移動ルールを1つだけ持つ役割．
エージェントランダム移動ルールは時刻指定せず，ステージ実行ルールとして登録する．
ステージ実行ルールはステージに設定された実行タイミングで定期的に実行される．
ステージ実行ルールを登録するための定期実行ステージの設定はメインクラスで行う．

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

メインクラスでは，エージェント移動ステージを定期実行ステージとして登録する．
定期実行ステージの登録は，TSOARSBuilderのsetPeriodicallyExecutedStage(stage, firstTime, interval)メソッドで行う．
引数の意味は以下の通りである．

- stage:定期実行ステージとして登録するステージ名．
- firstTime:定期実行を開始する時刻．sample07ではsimulationStartとしており，シミュレーション開始時刻が最初の実行時刻となる．
- interval:定期実行する時間間隔．sample07ではtickとしており，1tick間隔つまり毎時刻ルールが実行される．

定期実行ステージとして登録されたステージには定時実行ルール，臨時実行ルールは登録できず，
ステージ実行ルールとしてしかルールを登録できなくなる．
登録されたルールは上記のfirstTime,intervalの設定に従って定期的に実行される．

そのほかの変更点は，エージェント，スポットの作成部分とスポットログ部分であるが，内容は今までとほぼ同様なので説明は省略する．

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

        // エージェント移動ステージを毎時刻ルールが実行される定期実行ステージとして登録
        builder.setPeriodicallyExecutedStage(EStage.AgentMoving, simulationStart, tick);

        // マスター乱数発生器のシード値設定
        long seed = 0L;
        builder.setRandomSeed(seed);

        // ルールログとランタイムログの出力設定
        String pathOfLogDir = "logs" + File.separator + "tutorials" + File.separator + "sample07";
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
        //   - Spot:Spot1-Spot10
        // *************************************************************************************************************

        int noOfSpots = 10; // スポットの数
        List<TSpot> spots = spotManager.createSpots(ESpotType.Spot, noOfSpots);

        // *************************************************************************************************************
        // エージェント作成
        //   - Agent:Agent1-Agent10
        //     - 初期スポット:Home
        //     - 役割:エージェント役割
        // *************************************************************************************************************

        int noOfAgents = noOfSpots; // エージェントの数はスポットの数と同じ．
        List<TAgent> agents = agentManager.createAgents(EAgentType.Agent, noOfAgents);
        for (int i = 0; i < noOfAgents; ++i) {
            TAgent agent = agents.get(i); // i番目のエージェント
            TSpot spot = spots.get(i); // i番目のスポット
            agent.initializeCurrentSpot(spot); // 初期スポット設定
            new TRoleOfAgent(agent); // エージェント役割作成
            agent.activateRole(ERoleName.Agent); // エージェント役割をアクティブ化
        }

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        //   - スポットログ:各時刻での各エージェントの現在位置ログ
        // *************************************************************************************************************

        // スポットログ用PrintWriter
        PrintWriter spotLogPW = new PrintWriter(new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "spot_log.csv")));
        // スポットログのカラム名出力
        spotLogPW.print("CurrentTime");
        for (TAgent agent : agents) {
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
            for (TAgent agent : agents) {
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

前：
次：
