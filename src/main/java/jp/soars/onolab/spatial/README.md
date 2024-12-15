# スポット内座標モジュール

スポット内座標モジュール(2次元)の基本的な使い方とサンプルを紹介する．
スポット内座標モジュールには1次元，2次元，3次元用のモジュールが用意されているが基本的には同様である．

- [スポット内座標モジュール](#スポット内座標モジュール)
  - [概要](#概要)
  - [シナリオとシミュレーション条件](#シナリオとシミュレーション条件)
  - [シミュレーション定数の定義](#シミュレーション定数の定義)
  - [ルールの定義](#ルールの定義)
    - [TRuleOfAgentRandomMoving:エージェントランダム移動ルール](#truleofagentrandommovingエージェントランダム移動ルール)
  - [役割の定義](#役割の定義)
    - [TRoleOfAgent:エージェント役割](#troleofagentエージェント役割)
  - [メインクラスの定義](#メインクラスの定義)

## 概要

スポット内(2次元)座標モジュールは情報を持たせる役割クラスとして以下の二つを定義する．
- TRoleOf2DCoordinateSpot (役割名 TwoDimensionalCoordinateSpot)
  - スポット内座標を持たせたいスポットに持たせる役割．
  - スポット内座標の範囲の情報を持つ．
- TRoleOf2DCoordinateAgent (役割名 TwoDimensionalCoordinateAgent)
  - スポット内座標を利用させたいエージェントに持たせる役割．
  - T2DCoordinateを持つ．これはエージェントのスポットにおける座標を保持しているクラスである．
- TRuleOf2DCoordinateAgent
  - スポット内座標を利用する場合に，TAgentRuleの代わりに継承する抽象クラス．
  - TAgentRuleのメソッドに加えてスポット内座標を移動するためのメソッドが実装されている．

## シナリオとシミュレーション条件

以下のシナリオを考える．

- 20人のエージェント(Agent1〜Agent20)は，毎時刻20%の確率で10個のスポット(Spot1〜Spot10)をランダムに動き回る．また，80%の確率で現在いるスポット内のランダムな座標に移動する．

シミュレーション条件

- エージェント : Agent(20)
- スポット : Spot(10)
- ステージ : AgentMoving
- 時刻ステップ間隔：1時間 / step
- シミュレーション期間：7日間

## シミュレーション定数の定義

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

エージェントランダム移動ルールはコンストラクタで受け取ったスポットタイプ間もしくはスポット内座標をランダムに移動する．
TRuleOf2DCoordinateAgentはスポット間/スポット内座標の移動をサポートするTAgentRuleの継承クラスである．

TRuleOf2DCoordinateAgent基本的なメソッドとして以下のものが実装されている．
- moveTo(spot)
  - 指定したスポットに移動する．スポット内座標はNaNで初期化される．
- moveTo(spot, random)
  - 指定したスポットのランダムな座標に移動する．
- moveTo(spot, x, y)
  - 指定したスポットの指定した座標に移動する．
- moveInCurrentSpot(spotManager, random)
  - 現在いるスポットのランダムな座標に移動する．
- moveInCurrentSpot(x, y, spotManager)
  - 現在いるスポットの指定した座標に移動する．
- moveRelativeInCurrentSpot(dx, dy, spotManager)
  - 現在いるスポットの現在座標から相対座標指定で移動する．

`TRuleOfAgentRandomMoving.java`

```Java
public final class TRuleOfAgentRandomMoving extends TRuleOf2DCoordinateAgent {

    /** ランダム移動先のスポットタイプ */
    private final Enum<?> fSpotType;

    /** スポット間移動確率 */
    private static final double MOVING_PROBABILITY = 0.2;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールを持つ役割
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
        // *************************************************************************************************************
        // 現在スポットとは別のスポットへ移動する場合．(通常のスポット間移動) スポット内座標はNaNに設定される．
        // *************************************************************************************************************
        // ICRandom random = getRandom(); // 乱数発生器
        // List<TSpot> spots = spotManager.getSpots(fSpotType);
        // TSpot spot = spots.get(getRandom().nextInt(spots.size())); // 移動先スポットをランダムに選択
        // moveTo(spot); // 移動後にスポット内座標を初期化しない．スポット内座標はNaNに設定される．

        // *************************************************************************************************************
        // 現在スポットとは別のスポットのランダムなスポット内座標に移動する場合．
        // *************************************************************************************************************
        // ICRandom random = getRandom(); // 乱数発生器
        // List<TSpot> spots = spotManager.getSpots(fSpotType);
        // TSpot spot = spots.get(getRandom().nextInt(spots.size())); // 移動先スポットをランダムに選択
        // moveTo(spot, random); // 移動後にランダムなスポット内座標に設定する場合．

        // *************************************************************************************************************
        // 現在スポットとは別のスポットの指定したスポット内座標に移動する場合．
        // *************************************************************************************************************
        // ICRandom random = getRandom(); // 乱数発生器
        // List<TSpot> spots = spotManager.getSpots(fSpotType);
        // TSpot spot = spots.get(getRandom().nextInt(spots.size())); // 移動先スポットをランダムに選択
        // T2DSpace space = get2DSpace(spot); // スポット内2次元座標系
        // double lowerBoundX = space.getLowerBoundX(); // X座標の下限
        // double upperBoundX = space.getUpperBoundX(); // X座標の上限
        // double lowerBoundY = space.getLowerBoundY(); // Y座標の下限
        // double upperBoundY = space.getUpperBoundY(); // Y座標の上限
        // // 中心座標を計算
        // double x = lowerBoundX + (upperBoundX - lowerBoundX) / 2.0;
        // double y = lowerBoundY + (upperBoundY - lowerBoundY) / 2.0;
        // moveTo(spot, x, y); // 移動後に指定したスポット内座標に設定する場合．

        // *************************************************************************************************************
        // 現在スポットのランダムなスポット内座標に移動する場合．
        // *************************************************************************************************************
        // moveInCurrentSpot(spotManager, getRandom()); // スポット内座標のランダム移動

        // *************************************************************************************************************
        // 現在スポットの指定したスポット内座標に移動する場合．
        // *************************************************************************************************************
        // T2DSpace space = get2DSpaceOfCurrentSpot(spotManager); // 現在スポット内2次元座標系
        // double lowerBoundX = space.getLowerBoundX(); // X座標の下限
        // double upperBoundX = space.getUpperBoundX(); // X座標の上限
        // double lowerBoundY = space.getLowerBoundY(); // Y座標の下限
        // double upperBoundY = space.getUpperBoundY(); // Y座標の上限
        // // 中心座標を計算
        // double x = lowerBoundX + (upperBoundX - lowerBoundX) / 2.0;
        // double y = lowerBoundY + (upperBoundY - lowerBoundY) / 2.0;
        // moveInCurrentSpot(x, y, spotManager); // 移動

        // *************************************************************************************************************
        // 現在スポットのスポット内座標に相対座標指定で移動する場合．
        // *************************************************************************************************************
        // T2DSpace space = get2DSpaceOfCurrentSpot(spotManager); // 現在スポット内2次元座標系
        // double lowerBoundX = space.getLowerBoundX(); // X座標の下限
        // double upperBoundX = space.getUpperBoundX(); // X座標の上限
        // double lowerBoundY = space.getLowerBoundY(); // Y座標の下限
        // double upperBoundY = space.getUpperBoundY(); // Y座標の上限
        // // 移動量 (dx, dy) を計算
        // double dx = (upperBoundX - lowerBoundX) / 3.0;
        // double dy = (upperBoundY - lowerBoundY) / 3.0;
        // moveRelativeInCurrentSpot(dx, dy, spotManager); // 移動量を指定して移動


        ICRandom random = getRandom(); // 乱数発生器
        if (random.nextDouble() < MOVING_PROBABILITY) { // 確率でスポット間をランダム移動
            List<TSpot> spots = spotManager.getSpots(fSpotType);
            TSpot spot = spots.get(getRandom().nextInt(spots.size())); // 移動先スポットをランダムに選択
            moveTo(spot, random); // 移動後にランダムなスポット内座標に設定する場合．
        } else { // スポット内座標のランダム移動
            moveInCurrentSpot(spotManager, random); // 移動
        }
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
    public static final String RULE_NAME_OF_RANDOM_MOVING = "RandomMoving";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     */
    public TRoleOfAgent(TAgent owner) {
        // 親クラスのコンストラクタを呼び出す．
        // 以下の2つの引数は省略可能で，その場合デフォルト値で設定される．
        // 第3引数:この役割が持つルール数 (デフォルト値 10)
        super(ERoleName.Agent, owner, 1);

        // 役割が持つルールの登録
        // エージェントランダム移動ルール．エージェント移動ステージにステージ実行ルールとして予約する．
        new TRuleOfAgentRandomMoving(RULE_NAME_OF_RANDOM_MOVING, this, ESpotType.Spot)
                .setStage(EStage.AgentMoving);
    }
}
```

## メインクラスの定義

メインクラスでは，スポットに対してTRoleOf2DCoordinateSpot役割，エージェントに対してTRoleOf2DCoordinateAgent役割を生成して持たせておく必要がある．これらの役割は座標情報を保存するための役割である．また，TRoleOf2DCoordinateAgent役割を生成した時点でスポット内座標はNaNで初期化されるため，明示的に初期化する必要がある．

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
        String pathOfLogDir = "logs" + File.separator + "spatial";
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
        // スポット内座標の範囲指定
        double lowerBoundX = -5.0;
        double upperBoundX = 5.0;
        double lowerBoundY = -5.0;
        double upperBoundY = 5.0;
        boolean isToroidal = true; // トーラスにするか？
        List<TSpot> spots = spotManager.createSpots(ESpotType.Spot, noOfSpots);
        for (int i = 0; i < noOfSpots; ++i) {
            TSpot spot = spots.get(i); // i番目のスポット
            new TRoleOf2DCoordinateSpot(spot, lowerBoundX, upperBoundX, lowerBoundY, upperBoundY, isToroidal); // 2次元座標スポット役割作成
        }

        // *************************************************************************************************************
        // エージェント作成
        //   - Agent:Agent1-Agent20
        //     - 初期スポット:Spot
        //     - 役割:エージェント役割
        // *************************************************************************************************************

        int noOfAgents = 20; // エージェント数
        List<TAgent> agents = agentManager.createAgents(EAgentType.Agent, noOfAgents);
        for (int i = 0; i < noOfAgents; ++i) {
            TAgent agent = agents.get(i); // i番目のエージェント
            TSpot spot = spots.get(i % noOfSpots); // スポット
            agent.initializeCurrentSpot(spot); // 初期スポット設定

            new TRoleOfAgent(agent); // エージェント役割作成
            agent.activateRole(ERoleName.Agent); // エージェント役割をアクティブ化

            TRoleOf2DCoordinateAgent role = new TRoleOf2DCoordinateAgent(agent, spotManager); // 2次元座標利用エージェント役割作成
            // スポット内座標の初期化．現在スポットが設定されているかつ，スポットがスポット内座標役割を持っている必要がある点に注意．
            role.moveInCurrentSpot(spotManager, random);
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

        // 各時刻におけるエージェント同士の距離のログ
        PrintWriter distanceLogPW = new PrintWriter(new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "distance_log.txt")));


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
                spotLogPW.print(':');
                spotLogPW.print(((TRoleOf2DCoordinateAgent) agent.getRole(ESpatialSpotModuleRoleName.TwoDimensionalCoordinateAgent))
                        .getCoordinate(spotManager));
            }
            spotLogPW.println();

            // 距離ログ出力
            distanceLogPW.println(ruleExecutor.getCurrentTime());
            for (TSpot spot : spots) {
                distanceLogPW.println(spot.getName());
                for (TAgent agent: spot.getAgents()) { // 各エージェントの座標を最初に出力
                    distanceLogPW.print("  ");
                    distanceLogPW.print(agent.getName());
                    distanceLogPW.print(' ');
                    distanceLogPW.println(((TRoleOf2DCoordinateAgent) agent.getRole(ESpatialSpotModuleRoleName.TwoDimensionalCoordinateAgent))
                            .getCoordinate(spotManager));
                }
                distanceLogPW.println("  Distance:");
                for (TAgent agent1: spot.getAgents()) { // 各エージェントの距離を出力
                    for (TAgent agent2: spot.getAgents()) {
                        if (agent1.equals(agent2)) {
                            continue;
                        }
                        T2DCoordinate coordinate1 = ((TRoleOf2DCoordinateAgent) agent1.getRole(ESpatialSpotModuleRoleName.TwoDimensionalCoordinateAgent))
                                .getCoordinate(spotManager);
                        T2DCoordinate coordinate2 = ((TRoleOf2DCoordinateAgent) agent2.getRole(ESpatialSpotModuleRoleName.TwoDimensionalCoordinateAgent))
                                .getCoordinate(spotManager);
                        T2DSpace space = ((TRoleOf2DCoordinateAgent) agent1.getRole(ESpatialSpotModuleRoleName.TwoDimensionalCoordinateAgent))
                                .get2DSpaceOfCurrentSpot(spotManager);
                        double distance = space.distance(coordinate1, coordinate2);
                        distanceLogPW.print("    ");
                        distanceLogPW.print(agent1.getName());
                        distanceLogPW.print("-");
                        distanceLogPW.print(agent2.getName());
                        distanceLogPW.print(" ");
                        distanceLogPW.println(distance);
                    }
                }
            }
            distanceLogPW.println("==================================================================================================");
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown();
        spotLogPW.close();
        distanceLogPW.close();
    }
}
```
