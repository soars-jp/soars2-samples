# スポットによるセル空間構築

小野研のセル空間構築用モジュール`onolab-cell-module`を利用したスポットによるセル空間の構築方法のサンプル．
モジュールには1次元，2次元，3次元セル空間用のクラスが用意されているが，使用方法は全て同じなので2次元の場合で説明する．
このサンプルでは，スポットによる2次元セル空間を構築してエージェントが隣接するスポットをランダムに選択して移動するシミュレーションを実装する．

## モジュール

### T2DCellSpaceMap:2次元セル空間マップ

モジュールのメインとなるクラス．引数として以下のものを渡す．

- セル空間を構築するスポットリスト．
- セル空間の大きさ．
  - x,y軸の上下限を直接指定する方法と，width,hightで範囲を指定する方法がある．範囲で指定する場合，原点中心に設定される．
  - デフォルト値は[-16, 16] x [-16, 16] = 33 x 33
- 空間の端をトーラスのように繋げるかの設定．
  - デフォルト値はtrue(トーラスにする)

2次元セル空間マップは，getCell(x, y)メソッドで絶対座標指定でスポットを得ることができる．

```Java
T2DCellSpaceMap map = new T2DCellSpaceMap(spots, lowerBoundX, upperBoundX, lowerBoundY, upperBoundY, xAxisAsTorus, yAxisAsTorus);
T2DCellSpaceMap map = new T2DCellSpaceMap(spots, width, hight, xAxisAsTorus, yAxisAsTorus);

// 絶対座標でスポットを取得
map.getCell(x, y);
```

### TRoleOf2DCell:2次元セル役割

セル空間を構築しているスポットに設定される役割．
T2DCellSpaceMapのインスタンスを作成した時点で，引数として渡したセル空間を構築するスポットリストのスポット全てに TRoleOf2DCell 役割が設定される．

TRoleOf2DCell は近傍スポットへの参照と2次元セル空間マップを持っており，相対座標指定，絶対座標指定で空間上のスポットを得ることができる．

```Java
TRoleOf2DCell role;
// 役割を持つスポットを中心として，x,y ∈ {-1, 0, 1}で指定して近傍セルを得る．
role.getNeighborhood(x, y);
// 2次元セル空間マップを得る．絶対座標指定でスポットを検索できる．
role.getMap();
```

## シミュレーション

TRoleOfAgentはエージェントランダム移動ルールを持つだけの役割なので説明は省略する．

### TRuleOfAgentRandomMoving:エージェントランダム移動ルール

エージェントランダム移動ルールはセル空間上をランダムに移動する．移動先スポットは現在スポットの近傍8セルからランダムに選択する．

`TRuleOfAgentRandomMoving.java`

```Java
public class TRuleOfAgentRandomMoving extends TAgentRule {

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールを持つ役割
     */
    public TRuleOfAgentRandomMoving(String name, TRole owner) {
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
        // TRoleOf2DCell は自分自身と近傍8セルへの参照を持っており，x, y ∈ {-1, 0, 1}で指定して取得できる．
        // このルールでは x, y をランダムに選択して移動する．(斜め移動もする)
        // 空間端などで近傍セルが存在しない(= null) の場合，移動先として選択されたのが自分自身の場合は選び直す．
        ICRandom random = getRandom();
        TSpot currentSpot = getCurrentSpot();
        TRoleOf2DCell role = (TRoleOf2DCell) currentSpot.getRole(ECellModuleRoleName.Cell);
        TSpot spot;
        do {
            spot = role.getNeighborhood(random.nextInt(-1, 1), random.nextInt(-1, 1));
        } while (spot == null || spot.equals(currentSpot));
        moveTo(spot);
    }
}
```

### TMain:メインクラス

スポット作成後に，T2DCellSpaceMapのインスタンスを作成して2次元セル空間を構築している．

```Java
public class TMain {

    /**
     * シミュレーションのメインループの標準出力部分は Ubuntu22.04 以外での動作確認をしていないため，環境に合わせて適宜変更してほしい．
     */
    public static void main(String[] args) throws IOException, InterruptedException {
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
        String simulationEnd = "0/00:3:00";
        String tick = "00:00:01";
        List<Enum<?>> stages = List.of(EStage.AgentMoving);
        Set<Enum<?>> agentTypes = new HashSet<>();
        Collections.addAll(agentTypes, EAgentType.values());
        Set<Enum<?>> spotTypes = new HashSet<>();
        Collections.addAll(spotTypes, ESpotType.values());
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes);

        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目
        // *************************************************************************************************************

        // 定期実行ステージ設定
        builder.setPeriodicallyExecutedStage(EStage.AgentMoving, simulationStart, tick);

        // マスター乱数発生器のシード値設定
        long seed = 0L;
        builder.setRandomSeed(seed);

        // ルールログとランタイムログの出力設定
        String pathOfLogDir = "logs" + File.separator + "onolab" + File.separator + "cell";
        builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv");
        builder.setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv");

        // ルールログのデバッグ情報出力設定
        builder.setRuleDebugMode(ERuleDebugMode.OFF);

        // 以下，最適化設定
        // 空間のサイズ
        int width = 30;
        int hight = 15;
        int noOfSpots = width * hight;

        builder.setExpectedNoOfAgents(EAgentType.Agent, 1);
        builder.setExpectedNoOfSpots(ESpotType.Cell, noOfSpots);
        builder.setRulesNotShuffledBeforeExecuted(EStage.AgentMoving);
        builder.setExpectedNoOfRulesPerStage(EStage.AgentMoving, 1);
        builder.setExpectedSizeOfTemporaryRulesMap(0);
        builder.setExpectedNoOfDeletedObjects(0);

        // *************************************************************************************************************
        // TSOARSBuilderでシミュレーションに必要なインスタンスの作成と取得
        // *************************************************************************************************************

        builder.build();
        TRuleExecutor ruleExecutor = builder.getRuleExecutor();
        TAgentManager agentManager = builder.getAgentManager();
        TSpotManager spotManager = builder.getSpotManager();
        ICRandom random = builder.getRandom();

        // *************************************************************************************************************
        // スポット作成
        // *************************************************************************************************************

        List<TSpot> cells = spotManager.createSpots(ESpotType.Cell, noOfSpots, 1, 1);

        // onolab-cell-module でスポットによる2次元セル空間を構築．
        // T2DCellSpaceMap を作成した時点で，各スポットには TRoleOf2DCell が設定され，これは内部にムーア近傍にあるセルへの参照を持つ．
        // 第4引数はx軸方向，第5引数はy軸方向をトーラスのように繋げるかを指定する．(デフォルトでtrueなのでトーラスにしたい場合は入力しなくても良い)
        T2DCellSpaceMap map = new T2DCellSpaceMap(cells, width, hight, true, true);

        // *************************************************************************************************************
        // エージェント作成
        // *************************************************************************************************************

        TAgent agent = agentManager.createAgent(EAgentType.Agent, 1);
        agent.initializeCurrentSpot(cells.get(random.nextInt(cells.size())));
        new TRoleOfAgent(agent);
        agent.activateRole(ERoleName.Agent);

        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        do {
            // 画面クリア
            System.out.print("\033[H\033[2J");
            System.out.flush();
            // 画面表示
            System.out.println(ruleExecutor.getCurrentTime());
            for (int y = map.getUpperBoundY(), lenY = map.getLowerBoundY(); lenY <= y; --y) {
                for (int x = map.getLowerBoundX(), lenX = map.getUpperBoundX(); x <= lenX; ++x) {
                    if (map.getCell(x, y).getAgents().size() == 0) {
                        System.out.print("⬜︎");
                    } else {
                        System.out.print("👦");
                    }
                }
                System.out.println();
            }
            // ディレイ 100ms
            Thread.sleep(100);
        } while (ruleExecutor.executeStep());

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown();
    }
}
```
