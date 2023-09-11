<!-- omit in toc -->
# ライフゲーム

- [ライフゲームとは](#ライフゲームとは)
- [SOARSによる表現とシミュレーション条件](#soarsによる表現とシミュレーション条件)
- [シミュレーション定数の定義](#シミュレーション定数の定義)
- [ルールの定義](#ルールの定義)
  - [近傍エージェントの状態チェックと次の状態決定ルール](#近傍エージェントの状態チェックと次の状態決定ルール)
  - [セルのアップデートルール](#セルのアップデートルール)
- [役割の定義](#役割の定義)
  - [セル役割](#セル役割)
- [メインクラスの定義](#メインクラスの定義)

## ライフゲームとは

[ライフゲーム(wiki)](https://ja.wikipedia.org/wiki/%E3%83%A9%E3%82%A4%E3%83%95%E3%82%B2%E3%83%BC%E3%83%A0)
> ライフゲーム (Conway's Game of Life) は1970年にイギリスの数学者ジョン・ホートン・コンウェイ (John Horton Conway) が考案した生命の誕生、進化、淘汰などのプロセスを簡易的なモデルで再現したシミュレーションゲームである．単純なルールでその模様の変化を楽しめるため，パズルの要素を持っている．生物集団においては，過疎でも過密でも個体の生存に適さないという個体群生態学的な側面を背景に持つ．セル・オートマトンのもっともよく知られた例でもある．

参考動画(日本語)：[THE RECURSIVE COSMOS: Conway's Game of Life - PART 1](https://www.youtube.com/watch?v=yw-j-4xYAN4&ab_channel=hamukun8686)

↑めちゃくちゃ面白くて，シリーズになっているので見やすいです．

## SOARSによる表現とシミュレーション条件

- エージェントをセルに見立ててセル空間を構築する．
各エージェントは生と死の2状態を取り，周囲8マス(ムーア近傍)にいるエージェントへの参照をもつ．
- ライフゲームは2次元空間の境界部分について，上下左右をつなげる(トーラス)実装か，境界外側に死状態の番人セルがいるとする実装の2パターンが一般的であるが，今回は番人パターンで実装する．

シミュレーション条件

- エージェント : Cell ((width + 2) * (hight + 2))
- ステージ : CalcNextState, UpdateCell
- 時刻ステップ間隔：1秒 / step
- シミュレーション期間：5分間

## シミュレーション定数の定義

ライフゲームでは以下の定数を定義する．

- エージェントの状態定義
- エージェントタイプの定義
- ステージの定義
- 役割名の定義

```java
public enum EState {
    /** 死 */
    DEATH,
    /** 生 */
    LIFE
}
```

```java
public enum EAgentType {
    /** セル */
    Cell
}
```

```java
public enum EStage {
    /** 近傍エージェントの状態を集計して次の状態を決定するステージ */
    CalculateNextState,
    /** セルのアップデートステージ */
    UpdateCell
}
```

```java
public enum ERoleName {
    /** セル役割 */
    Cell
}
```

## ルールの定義

ライフゲームでは以下のルールを定義する．

- TRuleOfCalculateNextState：近傍エージェントの状態チェックと次の状態決定ルール
- TRuleOfUpdateCell：セルのアップデートルール

### 近傍エージェントの状態チェックと次の状態決定ルール

近傍エージェントの状態チェックと次の状態決定ルールは，
近傍にいるエージェントのうち状態が生であるものの数をカウントして，自分自身の次の状態を決定するルールである．
セルの次の状態は以下の規則に従う．

- 誕生:死んでいるセルに隣接する生きたセルがちょうど3つあれば，次の世代が誕生する．
- 生存:生きているセルに隣接する生きたセルが2つか3つならば，次の世代でも生存する．
- 過疎:生きているセルに隣接する生きたセルが1つ以下ならば，過疎により死滅する．
- 過密:生きているセルに隣接する生きたセルが4つ以上ならば，過密により死滅する．

`TRuleOfCalculateNextState.java`

```java
public final class TRuleOfCalculateNextState extends TRule {

    /** ムーア近傍エージェントリスト */
    private final List<TAgent> fNeighborhoods;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     * @param neighborhoods ムーア近傍にいるエージェントリスト．
     */
    public TRuleOfCalculateNextState(String name, TRole owner, List<TAgent> neighborhoods) {
        // 親クラスのコンストラクタを呼び出す．
        super(name, owner);
        fNeighborhoods = neighborhoods;
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
        for (TAgent agent : fNeighborhoods) {
            // ordinal() は DEATH -> 0, LIFE -> 1
            noOfAgentsAlive += ((TRoleOfCell) agent.getRole(ERoleName.Cell)).getState().ordinal();
        }

        boolean debugFlag = true; // デバッグ情報出力フラグ
        TRoleOfCell role = (TRoleOfCell) getRole(ERoleName.Cell);
        EState state = role.getState();
        appendToDebugInfo("state:" + state + " noOfAgentsAlive:" + noOfAgentsAlive, debugFlag);
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

### セルのアップデートルール

セルのアップデートルールでは，役割にある状態を計算した次の状態に更新する．

`TRuleOfUpdateCell.java`

```java
public final class TRuleOfUpdateCell extends TRule {

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     */
    public TRuleOfUpdateCell(String name, TRole owner) {
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
        ((TRoleOfCell) getRole(ERoleName.Cell)).updateState(); // アップデートを実行
    }
}
```

## 役割の定義

ライフゲームでは以下の役割を定義する．

- TRoleOfCell：セル役割

### セル役割

セル役割は，現在の状態と次の状態を持ちそれらを扱うメソッドを提供する．
番人にするセルはルールを登録せず，死状態から変更されないようにする．
ルールは全てステージ実行ルールで，毎時刻実行される．

`TRoleOfCell.java`

```java
public final class TRoleOfCell extends TRole {

    /** 状態 */
    private EState fState;

    /** 次の状態 */
    private EState fNextState;

    /** 近傍エージェントの状態をチェックするルール名 */
    private static final String RULE_NAME_OF_CHECK_NEIGHBORHOODS = "CheckNeighborhoods";

    /** セルをアップデートするルール名 */
    private static final String RULE_NAME_OF_UPDATE_CELL = "UpdateCell";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param neighborhoods ムーア近傍にいるエージェントリスト，この役割を持つエージェントが番人の場合はnull．
     */
    public TRoleOfCell(TAgent owner, List<TAgent> neighborhoods) {
        // 親クラスのコンストラクタを呼び出す．
        // 以下の2つの引数は省略可能で，その場合デフォルト値で設定される．
        // 第3引数 : この役割が持つルール数 (デフォルト値 10)
        // 第4引数 : この役割が持つ子役割数 (デフォルト値 5)
        super(ERoleName.Cell, owner, neighborhoods != null ? 2 : 0, 0);

        fState = EState.DEATH;
        fNextState = null;

        // 番人ならばルールは登録しない．(アップデートされない)
        if (neighborhoods != null) {
            new TRuleOfCalculateNextState(RULE_NAME_OF_CHECK_NEIGHBORHOODS, this, neighborhoods)
                    .setStage(EStage.CalculateNextState);
            new TRuleOfUpdateCell(RULE_NAME_OF_UPDATE_CELL, this)
                    .setStage(EStage.UpdateCell);
        }
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
     * 状態をアップデートする．
     */
    public final void updateState() {
        fState = fNextState;
    }
}
```

## メインクラスの定義

メインクラスとして以下の二つを定義する．これらのメインクラスの違いは，セル空間の初期状態の違いのみである．
また，これらのメインクラスは標準出力にセル空間を表現した文字列を更新表示していくが，その際に画面クリアのコマンドとして`\033[H\033[2J`を利用している．
作者の実行環境 Ubuntu22.04 以外での実行チェックは行なっていないため，適宜変更してほしい．

- TMainOfOscillator : いくつかの振動子のサンプル
- TMainOfGliderGun : グライダー銃のサンプル

`TMainOfOscillator.java`

```java
public class TMainOfOscillator {

    /**
     * ライフゲームのいくつかの振動子のシミュレーション
     * 作者実行環境 Ubuntu22.04 以外で実行確認していないのであしからず．(特に標準出力画面をクリアする部分)
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
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
        String simulationEnd = "0/00:05:00"; // シミュレーション終了時刻
        String tick = "00:00:01"; // １ステップの時間間隔
        List<Enum<?>> stages = List.of(EStage.CalculateNextState, EStage.UpdateCell); // ステージリスト
        Set<Enum<?>> agentTypes = new HashSet<>(); // 全エージェントタイプ
        Set<Enum<?>> spotTypes = new HashSet<>(); // 全スポットタイプ
        Collections.addAll(agentTypes, EAgentType.values()); // EAgentType に登録されているエージェントタイプをすべて追加
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes); // ビルダー作成

        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目．
        // *************************************************************************************************************

        // 定期実行ステージ設定
        builder.setPeriodicallyExecutedStage(EStage.CalculateNextState, simulationStart, tick)
               .setPeriodicallyExecutedStage(EStage.UpdateCell, simulationStart, tick);

        // 並列化設定
        int noOfThreads = 10;
        builder.setParallelizationStages(noOfThreads, EStage.CalculateNextState, EStage.UpdateCell);

        // ログ出力設定
        String pathOfLogDir = "logs" + File.separator + "ca" + File.separator + "gol"; // ログディレクトリ
        builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv") // ルールログ出力設定
               .setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv"); // ランタイムログ出力設定

        // ルールのシャッフルオフ
        builder.setRulesNotShuffledBeforeExecuted(EStage.CalculateNextState)
               .setRulesNotShuffledBeforeExecuted(EStage.UpdateCell);

        // ルールログのデバッグ情報出力設定
        builder.setRuleDebugMode(ERuleDebugMode.LOCAL); // ローカル設定に従う

        // 空間のサイズ
        int width = 60;
        int hight = 30;
        int noOfAgents = (width + 2) * (hight + 2); // 領域の外側に番人が必要なことに注意
        int noOfUpdatedAgents = width * hight; // 状態のアップデートが実行されるエージェント数．(番人以外)

        // 以下最適化設定
        builder.setExpectedNoOfAgents(EAgentType.Cell, noOfAgents);
        builder.setExpectedNoOfRulesPerStage(EStage.CalculateNextState, noOfUpdatedAgents);
        builder.setExpectedNoOfRulesPerStage(EStage.UpdateCell, noOfUpdatedAgents);
        builder.setFlagOfCreatingRandomForEachAgent(false);
        builder.setExpectedSizeOfTemporaryRulesMap(0);
        builder.setExpectedNoOfDeletedObjects(0);

        // *************************************************************************************************************
        // TSOARSBuilderでシミュレーションに必要なインスタンスの作成と取得．
        // *************************************************************************************************************

        builder.build(); // インスタンスのビルド
        TRuleExecutor ruleExecutor = builder.getRuleExecutor(); // ルール実行器
        TAgentManager agentManager = builder.getAgentManager(); // エージェント管理

        // *************************************************************************************************************
        // エージェント作成
        // *************************************************************************************************************

        List<TAgent> agents = agentManager.createAgents(EAgentType.Cell, noOfAgents, 1);
        List<TAgent> updatedAgents = new ArrayList<>(noOfUpdatedAgents); // アップデートされるエージェントリスト
        int width2 = width + 2;
        int tmp1 = (width + 2) * (hight + 1);
        for (int i = 0; i < noOfAgents; ++i) {
            if (i % width2 == 0 || (i + 1) % width2 == 0 || i <= width || tmp1 < i) { // 番人の条件
                TAgent agent = agents.get(i);
                // 役割生成
                new TRoleOfCell(agent, null);
            } else { // 番人ではない
                TAgent agent = agents.get(i);
                updatedAgents.add(agent);
                List<TAgent> neighborhoods = new ArrayList<>(8);
                // 上段
                int tmp = i - width2;
                neighborhoods.add(agents.get(tmp - 1));
                neighborhoods.add(agents.get(tmp));
                neighborhoods.add(agents.get(tmp + 1));
                // 左右
                neighborhoods.add(agents.get(i - 1));
                neighborhoods.add(agents.get(i + 1));
                // 下段
                tmp = i + width2;
                neighborhoods.add(agents.get(tmp - 1));
                neighborhoods.add(agents.get(tmp));
                neighborhoods.add(agents.get(tmp + 1));

                // 役割生成
                new TRoleOfCell(agent, neighborhoods);
                agent.activateRole(ERoleName.Cell);
            }
        }

        // パルサーを構成するためのマッピング
        int[][] pulsar = new int[][]
                {{1,1,1,1,1},
                 {1,0,0,0,1}};
        // (13, 8) を基準に上記のマッピングをコピー． 1 -> LIFE とする
        int ul = 13 * width + 8; // (13, 8)のインデックス
        for (int h = 0, len1 = pulsar.length; h < len1; ++h) {
            int[] indexes = pulsar[h];
            int sIndex = ul + h * width; // マッピング1行の開始インデックス
            for (int w = 0, len2 = indexes.length; w < len2; ++w) {
                if (pulsar[h][w] == 1) {
                    ((TRoleOfCell) updatedAgents.get(sIndex + w).getRole(ERoleName.Cell)).setState(EState.LIFE);
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
        // (9, 25) を基準に上記のマッピングをコピー． 1 -> LIFE とする
        ul = 9 * width + 25; // (9, 25)のインデックス
        for (int h = 0, len1 = galaxy.length; h < len1; ++h) {
            int[] indexes = galaxy[h];
            int sIndex = ul + h * width; // マッピング1行の開始インデックス
            for (int w = 0, len2 = indexes.length; w < len2; ++w) {
                if (galaxy[h][w] == 1) {
                    ((TRoleOfCell) updatedAgents.get(sIndex + w).getRole(ERoleName.Cell)).setState(EState.LIFE);
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
        // (10, 45) を基準に上記のマッピングをコピー． 1 -> LIFE とする
        ul = 10 * width + 45; // (10, 45)のインデックス
        for (int h = 0, len1 = tumbler.length; h < len1; ++h) {
            int[] indexes = tumbler[h];
            int sIndex = ul + h * width; // マッピング1行の開始インデックス
            for (int w = 0, len2 = indexes.length; w < len2; ++w) {
                if (tumbler[h][w] == 1) {
                    ((TRoleOfCell) updatedAgents.get(sIndex + w).getRole(ERoleName.Cell)).setState(EState.LIFE);
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
            for (int i = 0; i < noOfUpdatedAgents; ++i) {
                if (((TRoleOfCell) updatedAgents.get(i).getRole(ERoleName.Cell)).getState() == EState.LIFE) {
                    System.out.print("⬛︎");
                } else {
                    System.out.print("⬜︎");
                }
                if ((i + 1) % width == 0) {
                    System.out.println();
                }
            }
            // ディレイ 100ms
            Thread.sleep(100);
        } while (ruleExecutor.executeStep()); // 1ステップ分のルールを実行

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown(); // ルール実行器を終了する
    }
}
```


`TMainOfGliderGun.java`

```java
public class TMainOfGliderGun {

    /**
     * ライフゲームのグライダー銃パターンのシミュレーション
     * 作者実行環境 Ubuntu22.04 以外で実行確認していないのであしからず．(特に標準出力画面をクリアする部分)
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
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
        String simulationEnd = "0/00:5:00"; // シミュレーション終了時刻
        String tick = "00:00:01"; // １ステップの時間間隔
        List<Enum<?>> stages = List.of(EStage.CalculateNextState, EStage.UpdateCell); // ステージリスト
        Set<Enum<?>> agentTypes = new HashSet<>(); // 全エージェントタイプ
        Set<Enum<?>> spotTypes = new HashSet<>(); // 全スポットタイプ
        Collections.addAll(agentTypes, EAgentType.values()); // EAgentType に登録されているエージェントタイプをすべて追加
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes); // ビルダー作成

        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目．
        // *************************************************************************************************************

        // 定期実行ステージ設定
        builder.setPeriodicallyExecutedStage(EStage.CalculateNextState, simulationStart, tick)
               .setPeriodicallyExecutedStage(EStage.UpdateCell, simulationStart, tick);

        // 並列化設定
        int noOfThreads = 10;
        builder.setParallelizationStages(noOfThreads, EStage.CalculateNextState, EStage.UpdateCell);

        // ログ出力設定
        String pathOfLogDir = "logs" + File.separator + "ca" + File.separator + "gol"; // ログディレクトリ
        builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv") // ルールログ出力設定
               .setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv"); // ランタイムログ出力設定

        // ルールのシャッフルオフ
        builder.setRulesNotShuffledBeforeExecuted(EStage.CalculateNextState)
               .setRulesNotShuffledBeforeExecuted(EStage.UpdateCell);

        // ルールログのデバッグ情報出力設定
        builder.setRuleDebugMode(ERuleDebugMode.LOCAL); // ローカル設定に従う

        // 空間のサイズ
        int width = 60;
        int hight = 30;
        int noOfAgents = (width + 2) * (hight + 2); // 領域の外側に番人が必要なことに注意
        int noOfUpdatedAgents = width * hight; // 状態のアップデートが実行されるエージェント数．(番人以外)

        // 以下最適化設定
        builder.setExpectedNoOfAgents(EAgentType.Cell, noOfAgents);
        builder.setExpectedNoOfRulesPerStage(EStage.CalculateNextState, noOfUpdatedAgents);
        builder.setExpectedNoOfRulesPerStage(EStage.UpdateCell, noOfUpdatedAgents);
        builder.setFlagOfCreatingRandomForEachAgent(false);
        builder.setExpectedSizeOfTemporaryRulesMap(0);
        builder.setExpectedNoOfDeletedObjects(0);

        // *************************************************************************************************************
        // TSOARSBuilderでシミュレーションに必要なインスタンスの作成と取得．
        // *************************************************************************************************************

        builder.build(); // インスタンスのビルド
        TRuleExecutor ruleExecutor = builder.getRuleExecutor(); // ルール実行器
        TAgentManager agentManager = builder.getAgentManager(); // エージェント管理

        // *************************************************************************************************************
        // エージェント作成
        // *************************************************************************************************************

        List<TAgent> agents = agentManager.createAgents(EAgentType.Cell, noOfAgents, 1);
        List<TAgent> updatedAgents = new ArrayList<>(noOfUpdatedAgents); // アップデートされるエージェントリスト
        int width2 = width + 2;
        int tmp1 = (width + 2) * (hight + 1);
        for (int i = 0; i < noOfAgents; ++i) {
            if (i % width2 == 0 || (i + 1) % width2 == 0 || i <= width || tmp1 < i) { // 番人の条件
                TAgent agent = agents.get(i);
                // 役割生成
                new TRoleOfCell(agent, null);
            } else { // 番人ではない
                TAgent agent = agents.get(i);
                updatedAgents.add(agent);
                List<TAgent> neighborhoods = new ArrayList<>(8);
                // 上段
                int tmp = i - width2;
                neighborhoods.add(agents.get(tmp - 1));
                neighborhoods.add(agents.get(tmp));
                neighborhoods.add(agents.get(tmp + 1));
                // 左右
                neighborhoods.add(agents.get(i - 1));
                neighborhoods.add(agents.get(i + 1));
                // 下段
                tmp = i + width2;
                neighborhoods.add(agents.get(tmp - 1));
                neighborhoods.add(agents.get(tmp));
                neighborhoods.add(agents.get(tmp + 1));

                // 役割生成
                new TRoleOfCell(agent, neighborhoods);
                agent.activateRole(ERoleName.Cell);
            }
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
        // (1, 1) を基準に上記のマッピングをコピー． 1 -> LIFE とする
        int ul = width + 1; // (1, 1)のインデックス
        for (int h = 0, len1 = gliderGun.length; h < len1; ++h) {
            int[] indexes = gliderGun[h];
            int sIndex = ul + h * width; // マッピング1行の開始インデックス
            for (int w = 0, len2 = indexes.length; w < len2; ++w) {
                if (gliderGun[h][w] == 1) {
                    ((TRoleOfCell) updatedAgents.get(sIndex + w).getRole(ERoleName.Cell)).setState(EState.LIFE);
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
            for (int i = 0; i < noOfUpdatedAgents; ++i) {
                if (((TRoleOfCell) updatedAgents.get(i).getRole(ERoleName.Cell)).getState() == EState.LIFE) {
                    System.out.print("⬛︎");
                } else {
                    System.out.print("⬜︎");
                }
                if ((i + 1) % width == 0) {
                    System.out.println();
                }
            }
            // ディレイ 100ms
            Thread.sleep(100);
        } while (ruleExecutor.executeStep()); // 1ステップ分のルールを実行

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown(); // ルール実行器を終了する
    }
}
```
