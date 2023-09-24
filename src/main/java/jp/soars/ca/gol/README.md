# ライフゲーム <!-- omit in toc -->

- [ライフゲームとは](#ライフゲームとは)
- [SOARSによる表現とシミュレーション条件](#soarsによる表現とシミュレーション条件)
- [シミュレーション定数の定義](#シミュレーション定数の定義)
- [ルールの定義](#ルールの定義)
  - [TRuleOfCalculateNextState:次の状態決定ルール](#truleofcalculatenextstate次の状態決定ルール)
  - [TRuleOfStateTransition:状態遷移ルール](#truleofstatetransition状態遷移ルール)
- [役割の定義](#役割の定義)
  - [TRoleOfStateTransition:状態遷移役割](#troleofstatetransition状態遷移役割)
- [メインクラスの定義](#メインクラスの定義)
  - [TMainOfOscillator:振動子のシミュレーションメインクラス](#tmainofoscillator振動子のシミュレーションメインクラス)
  - [TMainOfGliderGun:グライダー銃のシミュレーションメインクラス](#tmainofglidergunグライダー銃のシミュレーションメインクラス)

## ライフゲームとは

[ライフゲーム(wiki)](https://ja.wikipedia.org/wiki/%E3%83%A9%E3%82%A4%E3%83%95%E3%82%B2%E3%83%BC%E3%83%A0)
> ライフゲーム (Conway's Game of Life) は1970年にイギリスの数学者ジョン・ホートン・コンウェイ (John Horton Conway) が考案した生命の誕生、進化、淘汰などのプロセスを簡易的なモデルで再現したシミュレーションゲームである．単純なルールでその模様の変化を楽しめるため，パズルの要素を持っている．生物集団においては，過疎でも過密でも個体の生存に適さないという個体群生態学的な側面を背景に持つ．セル・オートマトンのもっともよく知られた例でもある．

参考動画(日本語)：[THE RECURSIVE COSMOS: Conway's Game of Life - PART 1](https://www.youtube.com/watch?v=yw-j-4xYAN4&ab_channel=hamukun8686)

↑めちゃくちゃ面白くて，シリーズになっているので見やすいです．

## SOARSによる表現とシミュレーション条件

- スポットをセルに見立ててセル空間を構築する．各スポットは生と死の2状態を取り，周囲8マス(ムーア近傍)のスポットへの参照をもつ．
- ライフゲームは2次元空間の境界部分について，上下左右をつなげるトーラス実装か，境界外側に死状態の番人セルがいるとする実装の2つが一般的であるが，今回はトーラスで実装する．

セル空間の構築には`onolab-cell-module`を利用する．`onolab-cell-module`の使い方については
[セル空間モジュール](src/main/java/jp/soars/onolab/cell)を参照．

シミュレーション条件

- スポット : Cell
- ステージ : CalculateNextState, StateTransition
- 時刻ステップ間隔：1秒 / step
- シミュレーション期間：3分間

## シミュレーション定数の定義

ライフゲームでは以下の定数を定義する．

`ESpotType.java`
```Java
public enum ESpotType {
    /** セル */
    Cell
}
```

`EStage.java`
```Java
public enum EStage {
    /** 近傍エージェントの状態を集計して次の状態を決定するステージ */
    CalculateNextState,
    /** 状態遷移ステージ */
    StateTransition
}
```

`ERoleName.java`
```Java
public enum ERoleName {
    /** 状態遷移役割 */
    StateTransition
}
```

`EState.java`
```Java
public enum EState {
    /** 死 */
    DEATH,
    /** 生 */
    LIFE
}
```

## ルールの定義

### TRuleOfCalculateNextState:次の状態決定ルール

次の状態決定ルールは，近傍にいるエージェントのうち状態が生であるものの数をカウントして，自分自身の次の状態を決定するルールである．
近傍セルは`onolab-cell-module`のTRoleOf2DCell役割から取得できる．
セルの次の状態は以下の規則に従って決定する．

- 誕生:死んでいるセルに隣接する生きたセルがちょうど3つあれば，次の世代が誕生する．
- 生存:生きているセルに隣接する生きたセルが2つか3つならば，次の世代でも生存する．
- 過疎:生きているセルに隣接する生きたセルが1つ以下ならば，過疎により死滅する．
- 過密:生きているセルに隣接する生きたセルが4つ以上ならば，過密により死滅する．

`TRuleOfCalculateNextState.java`

```java
public final class TRuleOfCalculateNextState extends TRule {

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     */
    public TRuleOfCalculateNextState(String name, TRole owner) {
        // 親クラスのコンストラクタを呼び出す．
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
        int noOfAgentsAlive = 0; // 生きているエージェントの数
        // 2次元セル役割を取得
        TRoleOf2DCell roleOf2DCell = (TRoleOf2DCell) getRole(ECellModuleRoleName.Cell);
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                if (x == 0 && y == 0) { // (0, 0)は自分自身なのでスキップ
                    continue;
                }
                // 近傍セルの TRoleOfStateTransition 役割から現在の状態取得．
                // ordinal() は DEATH -> 0, LIFE -> 1
                noOfAgentsAlive += ((TRoleOfStateTransition) roleOf2DCell
                        .getNeighborhood(x, y)
                        .getRole(ERoleName.StateTransition))
                        .getState()
                        .ordinal();
            }
        }

        boolean debugFlag = true; // デバッグ情報出力フラグ
        TRoleOfStateTransition role = (TRoleOfStateTransition) getRole(ERoleName.StateTransition);
        EState state = role.getState();
        appendToDebugInfo("state:" + state + " noOfAgentsAlive:" + noOfAgentsAlive, debugFlag);
        // ライフゲームの状態遷移定義
        if (state == EState.DEATH) { // 自分自身が死んでいる場合
            if (noOfAgentsAlive == 3) {
                role.setNextState(EState.LIFE);
                appendToDebugInfo(" nextState:" + EState.LIFE, debugFlag);
            } else {
                role.setNextState(EState.DEATH);
                appendToDebugInfo(" nextState:" + EState.DEATH, debugFlag);
            }
        } else { // 自分自身が生きている場合
            if (noOfAgentsAlive == 2 || noOfAgentsAlive == 3) {
                role.setNextState(EState.LIFE);
                appendToDebugInfo(" nextState:" + EState.LIFE, debugFlag);
            } else {
                role.setNextState(EState.DEATH);
                appendToDebugInfo(" nextState:" + EState.DEATH, debugFlag);
            }
        }
    }
}
```

### TRuleOfStateTransition:状態遷移ルール

状態遷移では，状態をTRuleOfCalculateNextStateで計算した次の状態に更新する．

`TRuleOfStateTransition.java`

```java
public final class TRuleOfStateTransition extends TRule {

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     */
    public TRuleOfStateTransition(String name, TRole owner) {
        // 親クラスのコンストラクタを呼び出す．
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
        ((TRoleOfStateTransition) getRole(ERoleName.StateTransition)).stateTransition();; // 状態遷移
    }
}
```

## 役割の定義

### TRoleOfStateTransition:状態遷移役割

状態遷移役割は，現在の状態と次の状態を持ちそれらを扱うメソッドを提供する．
ルールは全てステージ実行ルールで，毎時刻実行される．

`TRoleOfStateTransition.java`

```java
public final class TRoleOfStateTransition extends TRole {

    /** 状態 */
    private EState fState;

    /** 次の状態 */
    private EState fNextState;

    /** 近傍セルの状態から次の状態を計算するルール名 */
    private static final String RULE_NAME_OF_CALCULATE_NEXT_STATE = "CalculateNextState";

    /** 状態遷移するルール名 */
    private static final String RULE_NAME_OF_STATE_TRANSITION = "StateTransition";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     */
    public TRoleOfStateTransition(TSpot owner, EState initialState) {
        // 親クラスのコンストラクタを呼び出す．
        super(ERoleName.StateTransition, owner, 2, 0);

        fState = initialState;
        fNextState = null;

        new TRuleOfCalculateNextState(RULE_NAME_OF_CALCULATE_NEXT_STATE, this)
                .setStage(EStage.CalculateNextState);
        new TRuleOfStateTransition(RULE_NAME_OF_STATE_TRANSITION, this)
                .setStage(EStage.StateTransition);
    }

    /**
     * 状態チェック
     * @param state 状態
     * @return 入力状態か？
     */
    public final boolean isState(EState state) {
        return fState == state;
    }

    /**
     * この役割を持つエージェントの状態を返す．
     * @return この役割を持つエージェントの状態
     */
    public final EState getState() {
        return fState;
    }

    /**
     * この役割を持つエージェントの状態を設定する．
     * @param state この役割を持つエージェントの状態
     */
    public final void setState(EState state) {
        fState = state;
    }

    /**
     * この役割を持つエージェントの次の状態を設定する．
     * @param state この役割を持つエージェントの次の状態
     */
    public final void setNextState(EState state) {
        fNextState = state;
    }

    /**
     * 状態遷移実行
     */
    public final void stateTransition() {
        fState = fNextState;
    }
}
```

## メインクラスの定義

メインクラスとして以下の二つを定義する．これらのメインクラスの違いは，セル空間の初期状態の違いのみである．
また，これらのメインクラスの標準出力部分は Ubuntu22.04 以外での動作確認をしていないため，環境に合わせて適宜変更してほしい．

### TMainOfOscillator:振動子のシミュレーションメインクラス

`TMainOfOscillator.java`

```java
public class TMainOfOscillator {

    /**
     * ライフゲームのいくつかの振動子のシミュレーション
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
        List<Enum<?>> stages = List.of(EStage.CalculateNextState, EStage.StateTransition);
        Set<Enum<?>> agentTypes = new HashSet<>();
        Set<Enum<?>> spotTypes = new HashSet<>();
        Collections.addAll(spotTypes, ESpotType.values());
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes);

        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目．
        // *************************************************************************************************************

        // 定期実行ステージ設定
        builder.setPeriodicallyExecutedStage(EStage.CalculateNextState, simulationStart, tick);
        builder.setPeriodicallyExecutedStage(EStage.StateTransition, simulationStart, tick);

        // ログ出力設定
        String pathOfLogDir = "logs" + File.separator + "ca" + File.separator + "gol" + File.separator + "oscillator";
        builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv");
        builder.setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv");

        // ルールログのデバッグ情報出力設定
        builder.setRuleDebugMode(ERuleDebugMode.LOCAL);

        // 以下，最適化設定
        // 空間のサイズ
        int width = 60;
        int hight = 30;
        int noOfSpots = width * hight;

        builder.setExpectedNoOfSpots(ESpotType.Cell, noOfSpots);
        builder.setRulesNotShuffledBeforeExecuted(EStage.CalculateNextState);
        builder.setRulesNotShuffledBeforeExecuted(EStage.StateTransition);
        builder.setExpectedNoOfRulesPerStage(EStage.CalculateNextState, noOfSpots);
        builder.setExpectedNoOfRulesPerStage(EStage.StateTransition, noOfSpots);
        builder.setExpectedSizeOfTemporaryRulesMap(0);
        builder.setExpectedNoOfDeletedObjects(0);

        // *************************************************************************************************************
        // TSOARSBuilderでシミュレーションに必要なインスタンスの作成と取得．
        // *************************************************************************************************************

        builder.build();
        TRuleExecutor ruleExecutor = builder.getRuleExecutor();
        TSpotManager spotManager = builder.getSpotManager();

        // *************************************************************************************************************
        // スポット作成
        // *************************************************************************************************************

        List<TSpot> cells = spotManager.createSpots(ESpotType.Cell, noOfSpots, 2, 0);
        T2DCellSpaceMap map = new T2DCellSpaceMap(cells, width, hight);

        // 状態遷移役割設定
        for (TSpot cell : cells) {
            new TRoleOfStateTransition(cell, EState.DEATH);
            cell.activateRole(ERoleName.StateTransition);
        }

        // パルサーを構成するためのマッピング
        int[][] pulsar = new int[][]
                {{1,1,1,1,1},
                 {1,0,0,0,1}};
        // 上記のマッピングをコピー． 1 -> LIFE とする
        for (int i = 0, lenY = pulsar.length, y = map.getUpperBoundY() - 13; i < lenY; ++i, --y) {
            for (int j = 0, lenX = pulsar[i].length, x = map.getLowerBoundX() + 8; j < lenX; ++j, ++x) {
                if (pulsar[i][j] == 1) {
                    ((TRoleOfStateTransition) map.getCell(x, y).getRole(ERoleName.StateTransition)).setState(EState.LIFE);
                }
            }
        }

        // 銀河を構成するためのマッピング
        int[][] galaxy = new int[][]
                {{1,1,0,1,1,1,1,1,1},
                 {1,1,0,1,1,1,1,1,1},
                 {1,1,0,0,0,0,0,0,0},
                 {1,1,0,0,0,0,0,1,1},
                 {1,1,0,0,0,0,0,1,1},
                 {1,1,0,0,0,0,0,1,1},
                 {0,0,0,0,0,0,0,1,1},
                 {1,1,1,1,1,1,0,1,1},
                 {1,1,1,1,1,1,0,1,1}};
        // 上記のマッピングをコピー． 1 -> LIFE とする
        for (int i = 0, lenY = galaxy.length, y = map.getUpperBoundY() - 9; i < lenY; ++i, --y) {
            for (int j = 0, lenX = galaxy[i].length, x = map.getLowerBoundX() + 25; j < lenX; ++j, ++x) {
                if (galaxy[i][j] == 1) {
                    ((TRoleOfStateTransition) map.getCell(x, y).getRole(ERoleName.StateTransition)).setState(EState.LIFE);
                }
            }
        }

        // タンブラーを構成するためのマッピング
        int[][] tumbler = new int[][]
                {{0,1,1,0,1,1,0},
                 {0,1,1,0,1,1,0},
                 {0,0,1,0,1,0,0},
                 {1,0,1,0,1,0,1},
                 {1,0,1,0,1,0,1},
                 {1,1,0,0,0,1,1}};
        // 上記のマッピングをコピー． 1 -> LIFE とする
        for (int i = 0, lenY = tumbler.length, y = map.getUpperBoundY() - 10; i < lenY; ++i, --y) {
            for (int j = 0, lenX = tumbler[i].length, x = map.getLowerBoundX() + 45; j < lenX; ++j, ++x) {
                if (tumbler[i][j] == 1) {
                    ((TRoleOfStateTransition) map.getCell(x, y).getRole(ERoleName.StateTransition)).setState(EState.LIFE);
                }
            }
        }

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
                    if (((TRoleOfStateTransition) map.getCell(x, y).getRole(ERoleName.StateTransition)).isState(EState.LIFE)) {
                        System.out.print("⬛︎");
                    } else {
                        System.out.print("⬜︎");
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

### TMainOfGliderGun:グライダー銃のシミュレーションメインクラス

`TMainOfGliderGun.java`

```java
public class TMainOfGliderGun {

    /**
     * ライフゲームのグライダー銃パターンのシミュレーション
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
        List<Enum<?>> stages = List.of(EStage.CalculateNextState, EStage.StateTransition);
        Set<Enum<?>> agentTypes = new HashSet<>();
        Set<Enum<?>> spotTypes = new HashSet<>();
        Collections.addAll(spotTypes, ESpotType.values());
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes);

        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目．
        // *************************************************************************************************************

        // 定期実行ステージ設定
        builder.setPeriodicallyExecutedStage(EStage.CalculateNextState, simulationStart, tick);
        builder.setPeriodicallyExecutedStage(EStage.StateTransition, simulationStart, tick);

        // ログ出力設定
        String pathOfLogDir = "logs" + File.separator + "ca" + File.separator + "gol" + File.separator + "glider_gun";
        builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv");
        builder.setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv");

        // ルールログのデバッグ情報出力設定
        builder.setRuleDebugMode(ERuleDebugMode.LOCAL);

        // 以下，最適化設定
        // 空間のサイズ
        int width = 60;
        int hight = 30;
        int noOfSpots = width * hight;

        builder.setExpectedNoOfSpots(ESpotType.Cell, noOfSpots);
        builder.setRulesNotShuffledBeforeExecuted(EStage.CalculateNextState);
        builder.setRulesNotShuffledBeforeExecuted(EStage.StateTransition);
        builder.setExpectedNoOfRulesPerStage(EStage.CalculateNextState, noOfSpots);
        builder.setExpectedNoOfRulesPerStage(EStage.StateTransition, noOfSpots);
        builder.setExpectedSizeOfTemporaryRulesMap(0);
        builder.setExpectedNoOfDeletedObjects(0);

        // *************************************************************************************************************
        // TSOARSBuilderでシミュレーションに必要なインスタンスの作成と取得．
        // *************************************************************************************************************

        builder.build();
        TRuleExecutor ruleExecutor = builder.getRuleExecutor();
        TSpotManager spotManager = builder.getSpotManager();

        // *************************************************************************************************************
        // スポット作成
        // *************************************************************************************************************

        List<TSpot> cells = spotManager.createSpots(ESpotType.Cell, noOfSpots, 2, 0);
        T2DCellSpaceMap map = new T2DCellSpaceMap(cells, width, hight);

        // 状態遷移役割設定
        for (TSpot cell : cells) {
            new TRoleOfStateTransition(cell, EState.DEATH);
            cell.activateRole(ERoleName.StateTransition);
        }

        // グライダー銃を構成するためのマッピング
        int[][] gliderGun = new int[][]
                {{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0},
                 {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0},
                 {0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1},
                 {0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1},
                 {1,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                 {1,1,0,0,0,0,0,0,0,0,1,0,0,0,1,0,1,1,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0},
                 {0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0},
                 {0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                 {0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}};
        // 左上を基準に上記のマッピングをコピー． 1 -> LIFE とする
        for (int i = 0, lenY = gliderGun.length, y = map.getUpperBoundY(); i < lenY; ++i, --y) {
            for (int j = 0, lenX = gliderGun[i].length, x = map.getLowerBoundX(); j < lenX; ++j, ++x) {
                if (gliderGun[i][j] == 1) {
                    ((TRoleOfStateTransition) map.getCell(x, y).getRole(ERoleName.StateTransition)).setState(EState.LIFE);
                }
            }
        }

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
                    if (((TRoleOfStateTransition) map.getCell(x, y).getRole(ERoleName.StateTransition)).isState(EState.LIFE)) {
                        System.out.print("⬛︎");
                    } else {
                        System.out.print("⬜︎");
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
