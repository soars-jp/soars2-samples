前：[sample10:ステージのロールバック](src/main/java/jp/soars/tutorials/sample10/)

次：[sample12:モジュール合成](src/main/java/jp/soars/tutorials/sample12/)

# sample11:ステージのアクティブ制御 <!-- omit in toc -->

sample11では，ステージのアクティブ制御について解説する．
sample10の定員ありスポットのシミュレーションで，ロールバック可能なステージは定期実行ステージのみであった．
しかし，エージェントの移動を定時実行ルールや臨時実行ルールで行いたい場合に，そのタイミングに合わせて定期実行ステージを登録するのは困難である．
また，毎時刻実行される定期実行ステージとして登録するのは実行性能が低下する．(エージェントが移動していない時刻にスポットの定員を調べてロールバックする必要はない)
sample11では，これらの問題にステージのアクティブ制御で対処する．

- [シナリオとシミュレーション条件](#シナリオとシミュレーション条件)
- [シミュレーション定数の定義](#シミュレーション定数の定義)
- [ルールの定義](#ルールの定義)
  - [TRuleOfAgentRandomMoving:エージェントランダム移動ルール](#truleofagentrandommovingエージェントランダム移動ルール)
  - [TRuleOfRevertAgentMoving:エージェントを戻すルール](#truleofrevertagentmovingエージェントを戻すルール)
  - [TRuleOfReset:リセットルール](#truleofresetリセットルール)
- [役割の定義](#役割の定義)
  - [TRoleOfSpotWithCapacity:定員ありスポット役割](#troleofspotwithcapacity定員ありスポット役割)
  - [TRoleOfAgent:エージェント役割](#troleofagentエージェント役割)
- [メインクラスの定義](#メインクラスの定義)


## シナリオとシミュレーション条件

以下のシナリオを考える．

- 1000人のエージェント(Agent1〜Agent1000)は，12時に20個のスポット(Spot1〜Spot20)からランダムに選択して移動する．
- 20個のスポットのうち10個(Spot1〜Spot10)はエージェントが10人しか入れない定員ありスポット．

シミュレーション条件

- エージェント : Agent(1000)
- スポット : Spot(20)
- ステージ : AgentMoving, RevertAgentMoving, Reset
- 時刻ステップ間隔：1時間 / step
- シミュレーション期間：7日間

## シミュレーション定数の定義

`ERoleName.java`

```Java
public enum ERoleName {
    /** エージェント役割 */
    Agent,
    /** 定員ありスポット役割 */
    SpotWithCapacity
}
```

`EStage.java`

```Java
public enum EStage {
    /** エージェント移動ステージ */
    AgentMoving,
    /** 定員条件を満たさない場合にエージェントの移動を取り消すステージ */
    RevertAgentMoving,
    /** リセットステージ */
    Reset
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

## ルールの定義

### TRuleOfAgentRandomMoving:エージェントランダム移動ルール

sample10と同じ．

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
        // fSpotType のスポットからランダムに移動先を選択
        boolean debugFlag = true;
        List<TSpot> spots = spotManager.getSpots(fSpotType);
        TSpot spot = spots.get(getRandom().nextInt(spots.size()));

        // スポットが定員ありスポット役割を持っているなら，エージェントと移動前スポットを追加
        TRoleOfSpotWithCapacity role = (TRoleOfSpotWithCapacity) spot.getRole(ERoleName.SpotWithCapacity);
        if (role != null) {
            role.addTemporaryAgent(getAgent(), getCurrentSpot());
        }

        // 移動
        moveTo(spot);
        appendToDebugInfo("move to " + spot.getName(), debugFlag);
    }
}
```

### TRuleOfRevertAgentMoving:エージェントを戻すルール

sample10と同じ．

`TRuleOfRevertAgentMoving.java`

```Java
public final class TRuleOfRevertAgentMoving extends TRule {

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールを持つ役割
     */
    public TRuleOfRevertAgentMoving(String name, TRole owner) {
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
        TRoleOfSpotWithCapacity role = (TRoleOfSpotWithCapacity) getOwnerRole();
        TSpot owner = (TSpot) role.getOwner();

        // 戻すエージェントの数を計算して，定員オーバーしている場合はエージェントを戻す
        int noOfReverts = owner.getNoOfAgents() - role.getCapacity();
        if (0 < noOfReverts) {
            // 直前に移動してきたエージェントと移動前スポットのマップからランダムに選択
            Map<TAgent, TSpot> preMovementSpotMap = role.getPreMovementSpotMap();
            List<Entry<TAgent, TSpot>> selectedAgents = getRandom()
                    .chooseWithoutReplacement(preMovementSpotMap.entrySet(), noOfReverts);

            // エージェントを戻して，マップから削除
            for (Entry<TAgent, TSpot> entry : selectedAgents) {
                entry.getKey().moveTo(entry.getValue());
                preMovementSpotMap.remove(entry.getKey());
            }
        }
    }
}
```

### TRuleOfReset:リセットルール

sample10と同じ．

`TRuleOfReset.java`

```Java
public final class TRuleOfReset extends TRule {

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールを持つ役割
     */
    public TRuleOfReset(String name, TRole owner) {
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
        // 直前に移動してきたエージェントと移動前スポットのマップをクリア
        ((TRoleOfSpotWithCapacity) getRole(ERoleName.SpotWithCapacity)).getPreMovementSpotMap().clear();
    }
}
```

## 役割の定義

### TRoleOfSpotWithCapacity:定員ありスポット役割

sample10と同じ．

`TRoleOfSpotWithCapacity.java`

```Java
public final class TRoleOfSpotWithCapacity extends TRole {

    /** スポットの定員 */
    private final int fCapacity;

    /** 直前に移動してきたエージェントと移動前スポットのマップ */
    private final Map<TAgent, TSpot> fPreMovementSpots;

    /** エージェントの移動を戻すルール名 */
    public static final String RULE_NAME_OF_REVERT_AGENT_MOVING = "RevertAgentMoving";

    /** 直前に移動してきたエージェントと移動前スポットのマップをクリアするルール名 */
    public static final String RULE_NAME_OF_RESET = "Reset";

    /**
     * コンストラクタ
     * @param owner この役割を持つスポット
     * @param capacity 定員
     * @throws RuntimeException 定員として負の数を指定した場合．
     */
    public TRoleOfSpotWithCapacity(TSpot owner, int capacity) {
        // 親クラスのコンストラクタを呼び出す．
        // 以下の2つの引数は省略可能で，その場合デフォルト値で設定される．
        // 第3引数:この役割が持つルール数 (デフォルト値 10)
        // 第4引数:この役割が持つ子役割数 (デフォルト値 5)
        super(ERoleName.SpotWithCapacity, owner, 1, 0);

        if (capacity < 0) {
            throw new RuntimeException("The capacity of spot must be at least 0.");
        }

        fCapacity = capacity;
        fPreMovementSpots = new ConcurrentHashMap<>();

        // 役割が持つルールの登録
        // エージェントを移動前スポットに戻すルール．
        // 定員条件を満たさない場合にエージェントの移動を取り消すステージにステージ実行ルールとして予約する．
        new TRuleOfRevertAgentMoving(RULE_NAME_OF_REVERT_AGENT_MOVING, this)
                .setStage(EStage.RevertAgentMoving);

        // 定員ありスポット役割の内部変数をクリアするルール．リセットステージにステージ実行ルールとして予約する．
        new TRuleOfReset(RULE_NAME_OF_RESET, this)
                .setStage(EStage.Reset);
    }

    /**
     * 直前に移動してきたエージェントと移動前スポットを追加
     * @param agent 直前に移動してきたエージェント
     * @param spot 移動前スポット
     */
    public final void addTemporaryAgent(TAgent agent, TSpot spot) {
        fPreMovementSpots.put(agent, spot);
    }

    /**
     * 直前に移動してきたエージェントと移動前スポットのマップを返す．
     * @return 直前に移動してきたエージェントと移動前スポットのマップ
     */
    public final Map<TAgent, TSpot> getPreMovementSpotMap() {
        return fPreMovementSpots;
    }

    /**
     * スポットの定員を返す．
     * @return スポットの定員
     */
    public final int getCapacity() {
        return fCapacity;
    }
}
```

### TRoleOfAgent:エージェント役割

sample10のエージェント役割を変更する．
エージェントランダム移動ルールをステージ実行ルールから，12:00:00/エージェント移動ステージに実行される定時実行ステージとする．

`TRoleOfAgent.java`

```Java
public final class TRoleOfAgent extends TRole {

    /** ランダム移動ルール名 */
    public static final String RULE_NAME_OF_RANDOM_MOVING = "RandomMoving";

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
        // エージェントランダム移動ルール．12:00:00/エージェント移動ステージに定時実行ルールとして予約する．
        new TRuleOfAgentRandomMoving(RULE_NAME_OF_RANDOM_MOVING, this, ESpotType.Spot)
                .setTimeAndStage(12, 0, 0, EStage.AgentMoving);
    }
}
```

## メインクラスの定義

sample10のメインクラスを拡張する．
メインループ実行前に，RevertAgentMoving, Reset ステージを非アクティブ化しておき，
これらのステージはエージェント移動ステージで，ルールが実行された場合にアクティブ化され，
Reset ステージ実行後に再び非アクティブ化される．

非アクティブステージはステージの実行自体がスキップされる仕様である．
例えば，エージェント移動ステージでルールが実行されず，RevertAgentMoving, Reset ステージが非アクティブ化のままである場合，
次のruleExecutor.executeStage()で実行されるのはエージェント移動ステージになる．
また，ランタイムログには非アクティブ化されている時刻の RevertAgentMoving, Reset ステージの情報は出力されない．

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
        List<Enum<?>> stages = List.of(EStage.AgentMoving,
                                       EStage.RevertAgentMoving,
                                       EStage.Reset);
        Set<Enum<?>> agentTypes = new HashSet<>();
        Collections.addAll(agentTypes, EAgentType.values());
        Set<Enum<?>> spotTypes = new HashSet<>();
        Collections.addAll(spotTypes, ESpotType.values());
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes);

        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目
        // *************************************************************************************************************

        // 定期実行ステージの登録
        builder.setPeriodicallyExecutedStage(EStage.RevertAgentMoving, simulationStart, tick);
        builder.setPeriodicallyExecutedStage(EStage.Reset, simulationStart, tick);

        // マスター乱数発生器のシード値設定
        long seed = 0L;
        builder.setRandomSeed(seed);

        // ルールログとランタイムログの出力設定
        String pathOfLogDir = "logs" + File.separator + "tutorials" + File.separator + "sample11";
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
        //   - Spot:Spot1-Spot10 (定員あり)
        //   - Spot:Spot11-Spot20 (定員なし)
        // *************************************************************************************************************

        int noOfSpotsWithCapacity = 10; // 定員ありスポットの数
        int capacity = 10; // 定員
        List<TSpot> spotsWithCapacity = spotManager.createSpots(ESpotType.Spot, noOfSpotsWithCapacity);
        for (TSpot spot : spotsWithCapacity) {
            new TRoleOfSpotWithCapacity(spot, capacity); // 定員役割を設定
            spot.activateRole(ERoleName.SpotWithCapacity); // 定員役割をアクティベート
        }

        int noOfSpots = 10; // 定員なしスポットの数
        List<TSpot> spots = spotManager.createSpots(ESpotType.Spot, noOfSpots);

        // *************************************************************************************************************
        // エージェント作成
        //   - Agent:Agent1-Agent1000
        //     - 初期スポット:定員なしスポットからランダムに選択
        //     - 役割:エージェント役割
        // *************************************************************************************************************

        int noOfAgents = 1000; // エージェントの数はスポットの数と同じ．
        List<TAgent> agents = agentManager.createAgents(EAgentType.Agent, noOfAgents);
        for (TAgent agent : agents) {
            agent.initializeCurrentSpot(spots.get(random.nextInt(noOfSpots))); // 初期スポット設定
            new TRoleOfAgent(agent); // エージェント役割作成
            agent.activateRole(ERoleName.Agent); // エージェント役割をアクティブ化
        }

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        //   - エージェント数ログ:各時刻で各スポットにいるエージェント数
        // *************************************************************************************************************

        // エージェント数ログ用PrintWriter
        PrintWriter noOfAgentsLogPW = new PrintWriter(new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "number_of_agents_in_spot.csv")));
        // エージェント数ログのカラム名出力
        noOfAgentsLogPW.print("CurrentTime,CurrentStage");
        for (TSpot spot : spotManager.getSpots()) {
            noOfAgentsLogPW.print(',');
            noOfAgentsLogPW.print(spot.getName());
        }
        noOfAgentsLogPW.println();

        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        // RevertAgentMoving, Reset ステージを非アクティブ化
        ruleExecutor.deactivateStage(EStage.RevertAgentMoving);
        ruleExecutor.deactivateStage(EStage.Reset);

        // 1ステージ分のルールを実行 (ruleExecutor.executeStep()で1ステップ毎に実行することもできる)
        // 実行された場合:true，実行されなかった(終了時刻)場合は:falseが帰ってくるため，while文で回すことができる．
        while (ruleExecutor.executeStage()) {
            // 標準出力に現在時刻，ステージを表示する
            System.out.println(ruleExecutor.getCurrentTime() + " : " + ruleExecutor.getCurrentStage());

            // エージェント数ログ出力
            noOfAgentsLogPW.print(ruleExecutor.getCurrentTime());
            noOfAgentsLogPW.print(',');
            noOfAgentsLogPW.print(ruleExecutor.getCurrentStage());
            for (TSpot spot : spotManager.getSpots()) {
                noOfAgentsLogPW.print(',');
                noOfAgentsLogPW.print(spot.getNoOfAgents());
            }
            noOfAgentsLogPW.println();

            if (ruleExecutor.getCurrentStage() == EStage.AgentMoving) { // AgentMoving ステージ実行後
                // ルールが実行されている場合は，RevertAgentMoving, Reset ステージをアクティブ化
                if (0 < ruleExecutor.getNoOfExecutedRules()) {
                    ruleExecutor.activateStage(EStage.RevertAgentMoving);
                    ruleExecutor.activateStage(EStage.Reset);
                }
            } else if (ruleExecutor.getCurrentStage() == EStage.RevertAgentMoving) { // RevertAgentMoving ステージ実行後
                // 定員ありスポットの定員条件が満たされているかをチェックして，満たされていない場合は RevertAgentMoving ステージにロールバック
                boolean capacityOver = false;
                for (TSpot spot : spotsWithCapacity) {
                    TRoleOfSpotWithCapacity role = (TRoleOfSpotWithCapacity) spot.getRole(ERoleName.SpotWithCapacity);
                    if (role.getCapacity() < spot.getNoOfAgents()) { // 定員オーバー
                        capacityOver = true;
                        break;
                    }
                }
                if (capacityOver) {
                    ruleExecutor.rollbackStage(EStage.RevertAgentMoving);
                }
            } else if (ruleExecutor.getCurrentStage() == EStage.Reset) { // Reset ステージ実行後
                // RevertAgentMoving, Reset ステージを非アクティブ化
                ruleExecutor.deactivateStage(EStage.RevertAgentMoving);
                ruleExecutor.deactivateStage(EStage.Reset);
            }
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown();
        noOfAgentsLogPW.close();
    }
}
```

前：[sample10:ステージのロールバック](src/main/java/jp/soars/tutorials/sample10/)

次：[sample12:モジュール合成](src/main/java/jp/soars/tutorials/sample12/)
