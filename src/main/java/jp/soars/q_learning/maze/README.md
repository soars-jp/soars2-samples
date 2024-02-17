# Q-Learning 迷路問題

## Q-Learning(強化学習)とは

Q-Learning(強化学習)は，
エージェントが環境に対して何らかの行動をし，
その行動に対して環境から状態と報酬を受け取るようなモデルに対して，
累積報酬和が最大となるような行動パターンを学習する．
以下に強化学習における学習の流れを簡単に示す．

1. 強化学習の問題設定：問題はエージェントの行動集合A，状態集合Sをもち，エージェントの環境における初期状態 s(0) が設定されている．
2. エージェントがアルゴリズムに従って行動 a(i) を選択する．
3. 問題(環境)は，エージェントの現在の状態 s(i) と行動 a(i) から，エージェントの次の状態 s(i+1) と報酬 r(i+1) を返す．
4. エージェントの行動とそれによって得られた次の状態と報酬から，アルゴリズムに従ってパラメータを更新し，1.に戻る．

## 迷路問題

迷路問題は強化学習分野における最も基本的な問題である．
迷路問題は2次元セル空間上の迷路をエージェントが探索する問題である．
エージェントが選択可能な行動集合は A = {上に移動，下に移動，右に移動，左に移動} である．
また，エージェントの状態はエージェントがいるセルの絶対座標で与えられる．
報酬については，選択した行動で移動可能な場合に正の報酬，移動不可能な場合に負の報酬，ゴールした場合に大きな正の報酬を与える．

## SOARSによる表現とシミュレーション条件

- スポットをセルに見立ててセル空間を構築する．
- 各スポットは通路，壁，スタート，ゴールのいずれかである．
- エージェントは最初スタートに配置され，毎時刻ランダムに行動を選択してその結果の状態(座標)と報酬を得る．

このサンプルでは，与える報酬を以下のように決める．
- 移動可能:0
- 移動不可能:-1
- ゴール:100

セル空間の構築には`onolab-cell-module`を利用する．`onolab-cell-module`の使い方については
[セル空間モジュール](src/main/java/jp/soars/onolab/cell)を参照．

シミュレーション条件

- エージェント : Agent
- スポット : Cell
- ステージ : AgentAction
- 時刻ステップ間隔：1秒 / step
- シミュレーション期間：5分間

## シミュレーション定数の定義

迷路問題では以下の定数を定義する．

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
    /** セル */
    Cell
}
```

`EStage.java`
```Java
public enum EStage {
    /** エージェント行動ステージ */
    AgentAction
}
```

`ERoleName.java`
```Java
public enum ERoleName {
    /** 迷路セル役割 */
    MazeCell,
    /** エージェント役割 */
    Agent
}
```

`EAgentAction.java`
```Java
public enum EAgentAction {
    /** 上に移動 */
    Up,
    /** 下に移動 */
    Down,
    /** 右に移動 */
    Right,
    /** 左に移動 */
    Left
}
```

`EMazeCellType.java`
```Java
public enum EMazeCellType {
    /** 通路 */
    Aisle,
    /** 壁 */
    Wall,
    /** スタート */
    Start,
    /** ゴール */
    Goal
}
```

## ルールの定義

### TRuleOfAgentRandomAction:エージェントランダム行動ルール

エージェントランダム行動ルールは，エージェントの行動をランダムに選択してその結果得られる状態と報酬を計算する．

`TRuleOfAgentRandomAction.java`

```java
public class TRuleOfAgentRandomAction extends TAgentRule {

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールを持つ役割
     */
    public TRuleOfAgentRandomAction(String name, TRole owner) {
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
        // ランダム行動選択．穴掘り法で作成した迷路なので，一回で2マス移動する．
        EAgentAction[] actions = EAgentAction.values();
        EAgentAction action = actions[getRandom().nextInt(actions.length)];

        // 報酬は ゴール 100, 通路 0, 壁 -1
        TRoleOfAgent role = (TRoleOfAgent) getOwnerRole();
        role.setAgentAction(action);

        // 1マス先スポット
        TSpot spot1 = null;
        switch (action) {
            case Up:
                spot1 = ((TRoleOf2DCell) getCurrentSpot().getRole(ECellModuleRoleName.Cell))
                        .getNeighborhood(0, 1);
                break;
            case Down:
                spot1 = ((TRoleOf2DCell) getCurrentSpot().getRole(ECellModuleRoleName.Cell))
                        .getNeighborhood(0, -1);
                break;
            case Right:
                spot1 = ((TRoleOf2DCell) getCurrentSpot().getRole(ECellModuleRoleName.Cell))
                        .getNeighborhood(1, 0);
                break;
            case Left:
                spot1 = ((TRoleOf2DCell) getCurrentSpot().getRole(ECellModuleRoleName.Cell))
                        .getNeighborhood(-1, 0);
                break;
        }
        // 1マス先が壁 -> 状態は変化なし，報酬 -1
        if (((TRoleOfMazeCell) spot1.getRole(ERoleName.MazeCell)).getMazeCellType() == EMazeCellType.Wall) {
            role.setReword(-1);
            return;
        }

        // 2マス先スポット
        TSpot spot2 = null;
        // 1マス先が壁ではないので移動可能なことは確定．-> 状態書き換え
        int[] state = role.getState();
        switch (action) {
            case Up:
                spot2 = ((TRoleOf2DCell) spot1.getRole(ECellModuleRoleName.Cell))
                        .getNeighborhood(0, 1);
                state[1] += 2;
                break;
            case Down:
                spot2 = ((TRoleOf2DCell) spot1.getRole(ECellModuleRoleName.Cell))
                        .getNeighborhood(0, -1);
                state[1] -= 2;
                break;
            case Right:
                spot2 = ((TRoleOf2DCell) spot1.getRole(ECellModuleRoleName.Cell))
                        .getNeighborhood(1, 0);
                state[0] += 2;
                break;
            case Left:
                spot2 = ((TRoleOf2DCell) spot1.getRole(ECellModuleRoleName.Cell))
                        .getNeighborhood(-1, 0);
                state[0] -= 2;
                break;
        }
        // 2マス先がゴール -> 報酬 100
        if (((TRoleOfMazeCell) spot2.getRole(ERoleName.MazeCell)).getMazeCellType() == EMazeCellType.Goal) {
            role.setReword(100);
        } else { // 2マス先が通路 -> 報酬 0
            role.setReword(0);
        }
        // エージェント移動
        moveTo(spot2);
    }
}
```

## 役割の定義

### TRoleOfRandomAgent:ランダムエージェント役割

エージェント役割は，エージェントの行動，状態，報酬をもち，エージェントランダム行動ルールが登録されている．

`TRoleOfRandomAgent.java`

```java
public class TRoleOfRandomAgent extends TRole {

    /** エージェントが選択した行動 */
    private EAgentAction fAgentAction;

    /** 行動で得られた次の状態 (セルの絶対座標) */
    private final int[] fCoordinates;

    /** 行動で得られた報酬 */
    private int fReward;

    /** エージェント行動ランダム選択ルール */
    public static final String RULE_NAME_OF_AGENT_RANDOM_ACTION = "agentRandomAction";

    /**
     * コンストラクタ
     *
     * @param owner        この役割を持つエージェント
     * @param initialSpotX 初期スポットx座標
     * @param initialSpotY 初期スポットy座標
     */
    public TRoleOfRandomAgent(TAgent owner, int initialSpotX, int initialSpotY) {
        super(ERoleName.Agent, owner, 1, 0);
        fAgentAction = null;
        fCoordinates = new int[] { initialSpotX, initialSpotY };
        fReward = 0;

        new TRuleOfAgentRandomAction(RULE_NAME_OF_AGENT_RANDOM_ACTION, this)
                .setStage(EStage.AgentAction);
    }

    /**
     * エージェントが選択した行動を設定
     *
     * @param action エージェントが選択した行動
     */
    public final void setAgentAction(EAgentAction action) {
        fAgentAction = action;
    }

    /**
     * エージェントが選択した行動を返す．
     *
     * @return エージェントが選択した行動
     */
    public final EAgentAction getAgentAction() {
        return fAgentAction;
    }

    /**
     * 行動で得られた次の状態 (セルの絶対座標)を返す．
     *
     * @return 行動で得られた次の状態 (セルの絶対座標)
     */
    public final int[] getState() {
        return fCoordinates;
    }

    /**
     * 行動で得られた報酬を設定
     *
     * @param reward 行動で得られた報酬
     */
    public final void setReward(int reward) {
        fReward = reward;
    }

    /**
     * 行動で得られた報酬を返す．
     *
     * @return 行動で得られた報酬
     */
    public final int getReward() {
        return fReward;
    }
}
```

### TRoleOfMazeCell:迷路セル役割

迷路セル役割は，迷路セルタイプの情報を持つ．

`TRoleOfMazeCell.java`
```Java
public class TRoleOfMazeCell extends TRole {

    /** 迷路セルタイプ */
    private EMazeCellType fMazeCellType;

    /**
     * コンストラクタ
     * @param owner この役割を持つスポット
     * @param mazeCellType 迷路セルタイプ
     */
    public TRoleOfMazeCell(TSpot owner, EMazeCellType mazeCellType) {
        super(ERoleName.MazeCell, owner, 0, 0);
        fMazeCellType = mazeCellType;
    }

    /**
     * 迷路セルタイプを設定する．
     * @param mazeCellType 迷路セルタイプ
     */
    public final void setMazeCellType(EMazeCellType mazeCellType) {
        fMazeCellType = mazeCellType;
    }

    /**
     * 迷路セルタイプを返す．
     * @return 迷路セルタイプ
     */
    public final EMazeCellType getMazeCellType() {
        return fMazeCellType;
    }
}
```

## メインクラスの定義

メインクラスの標準出力部分は Ubuntu22.04 以外での動作確認をしていないため，環境に合わせて適宜変更してほしい．

`TMain.java`

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
        String simulationEnd = "0/00:5:00";
        String tick = "00:00:01";
        List<Enum<?>> stages = List.of(EStage.AgentAction);
        Set<Enum<?>> agentTypes = new HashSet<>();
        Collections.addAll(agentTypes, EAgentType.values());
        Set<Enum<?>> spotTypes = new HashSet<>();
        Collections.addAll(spotTypes, ESpotType.values());
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes);

        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目．
        // *************************************************************************************************************

        // 定期実行ステージ設定
        builder.setPeriodicallyExecutedStage(EStage.AgentAction, simulationStart, tick);

        // ログ出力設定
        String pathOfLogDir = "logs" + File.separator + "q_learning" + File.separator + "maze";
        builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv");
        builder.setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv");

        // ルールログのデバッグ情報出力設定
        builder.setRuleDebugMode(ERuleDebugMode.LOCAL);

        // 以下，最適化設定
        // 空間のサイズ -> 穴掘り法で迷路作成するので 奇数 x 奇数
        int width = 19;
        int hight = 9;
        int noOfSpots = width * hight;

        builder.setExpectedNoOfAgents(EAgentType.Agent, 1);
        builder.setExpectedNoOfSpots(ESpotType.Cell, noOfSpots);
        builder.setRulesNotShuffledBeforeExecuted(EStage.AgentAction);
        builder.setExpectedNoOfRulesPerStage(EStage.AgentAction, 1);
        builder.setExpectedSizeOfTemporaryRulesMap(0);
        builder.setExpectedNoOfDeletedObjects(0);

        // *************************************************************************************************************
        // TSOARSBuilderでシミュレーションに必要なインスタンスの作成と取得．
        // *************************************************************************************************************

        builder.build();
        TRuleExecutor ruleExecutor = builder.getRuleExecutor();
        TAgentManager agentManager = builder.getAgentManager();
        TSpotManager spotManager = builder.getSpotManager();
        ICRandom random = builder.getRandom();

        // *************************************************************************************************************
        // スポット作成
        // *************************************************************************************************************

        List<TSpot> cells = spotManager.createSpots(ESpotType.Cell, noOfSpots, 2, 0);
        // 原点を基準に正の座標かつ，トーラスではないセル空間を作成．
        T2DCellSpaceMap map = new T2DCellSpaceMap(cells, 0, width - 1, 0, hight - 1, false, false);

        // 穴掘り法による迷路作成．デフォルトで(1, 1), (width - 2, hight - 2)がスタートとゴール．
        boolean[][] maze = TMazeGenerator.generate2DMaze(width, hight, random);

        // 迷路セル役割設定
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < hight; ++j) {
                if (maze[i][j]) {
                    new TRoleOfMazeCell(map.getCell(i, j), EMazeCellType.Aisle);
                } else {
                    new TRoleOfMazeCell(map.getCell(i, j), EMazeCellType.Wall);
                }
            }
        }
        // スタートとゴールはセルタイプ変更
        ((TRoleOfMazeCell) map.getCell(1, 1).getRole(ERoleName.MazeCell)).setMazeCellType(EMazeCellType.Start);
        ((TRoleOfMazeCell) map.getCell(width - 2, hight - 2).getRole(ERoleName.MazeCell)).setMazeCellType(EMazeCellType.Goal);

        // *************************************************************************************************************
        // エージェント作成
        // *************************************************************************************************************

        TAgent agent = agentManager.createAgent(EAgentType.Agent, 1);
        agent.initializeCurrentSpot(map.getCell(1, 1));
        TRoleOfRandomAgent agentRole = new TRoleOfRandomAgent(agent, 1, 1);
        agent.activateRole(ERoleName.Agent);

        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        do {
            // 画面クリア
            System.out.print("\033[H\033[2J");
            System.out.flush();
            // 画面表示
            System.out.print(ruleExecutor.getCurrentTime());
            System.out.print("\n行動:");
            System.out.print(agentRole.getAgentAction());
            System.out.print("\n状態:(");
            System.out.print(agentRole.getState()[0]);
            System.out.print(", ");
            System.out.print(agentRole.getState()[1]);
            System.out.print(")\n報酬:");
            System.out.println(agentRole.getReword());
            for (int y = map.getUpperBoundY(), lenY = map.getLowerBoundY(); lenY <= y; --y) {
                for (int x = map.getLowerBoundX(), lenX = map.getUpperBoundX(); x <= lenX; ++x) {
                    TSpot spot = map.getCell(x, y);
                    if (spot.getAgents().size() != 0) { // エージェントがいるセル
                        System.out.print("👦");
                        continue;
                    }
                    EMazeCellType mazeCellType = ((TRoleOfMazeCell) spot.getRole(ERoleName.MazeCell)).getMazeCellType();
                    if (mazeCellType == EMazeCellType.Wall) {
                        System.out.print("⬛︎");
                    } else if (mazeCellType == EMazeCellType.Aisle) {
                        System.out.print("⬜︎");
                    } else if (mazeCellType == EMazeCellType.Start) {
                        System.out.print("🟦");
                    } else if (mazeCellType == EMazeCellType.Goal) {
                        System.out.print("🟥");
                    }
                }
                System.out.println();
            }
            // ディレイ 500ms
            Thread.sleep(500);
        } while (ruleExecutor.executeStep());

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown();
    }
}
```

## 付録:ε-Greedy法による迷路探索

ε-greedy法は，確率 $ 1 - \epsilon $ で一番大きい価値を持つ行動を決定的に選択し，確率 $ \epsilon (0  \leq  \epsilon  \leq  1) $ でランダムな行動を選択する方法である．

Q学習では時刻 $ t $ において，状態集合 $ S $ の中で状態 $ s_{t} $ にエージェントがあり，エージェントが行動集合 $ A $ の中の $a_{t} $ を選択したとすると，次の状態 $s_{t+1} $ とエージェントが取った行動により得られる報酬 $ r_{t} $ を用いて $ Q $ の価値を更新する．
更新式は以下のように表される．
<div style="text-align: center;">

$ Q(s_{t},a_{t})  \leftarrow Q(s_{t},a_{t}) + \alpha ( r_{t} + \gamma \cdot\underset{a^{'} \in \mathcal{A}}{\max}Q(s_{t+1},a^{'}) - Q(s_{t}, a_{t})) $

</div>

ここで，$ \alpha $ はステップサイズ，$ \gamma $ は割引率である．
このサンプルでは，ステップサイズ $ \alpha $ を以下のように定義している．

<div style="text-align: center;">

$ \alpha = 1.0 /$状態 $ (s_{t},a_{t} )$ への訪問回数

</div>

詳しくは以下を参照されたい．
- [ε-グリーディ法（ε-greedy）の概要とアルゴリズム及び実装例について](https://deus-ex-machina-ism.com/?p=58448)
- [Qiita Q-learningで迷路探索をしてみた](https://qiita.com/miraclegliders44/items/f9337d6de9daf5b0ef37)

### TRuleOfAgentEpsilonGreedyAction:エージェントε-グリーディ行動ルール

エージェントε-グリーディ行動ルールは，エージェントの行動をε-Greedy法により選択してその結果得られる状態と報酬を計算する．

デフォルトの引数として，確率ε=0.6，割引率γ=0.99としている．

`TRuleOfAgentEpsilonGreedyAction.java`

```java
public class TRuleOfAgentEpsilonGreedyAction extends TAgentRule {

    // Q関数
    private Map<int[], Map<EAgentAction, Double>> fQMap;

    // 状態行動対への訪問回数
    private Map<int[], Map<EAgentAction, Integer>> fAlphaMap;

    // 確率 ε
    private double fEpsilon;

    // 割引率
    private double fGamma;

    /**
     * コンストラクタ
     *
     * @param name  ルール名
     * @param owner このルールを持つ役割
     */
    public TRuleOfAgentEpsilonGreedyAction(String name, TRole owner) {
        this(name, owner, 0.6, 0.99);
    }

    /**
     * コンストラクタ
     *
     * @param name    ルール名
     * @param owner   このルールを持つ役割
     * @param epsilon 確率 ε
     * @param gamma   割引率
     */
    public TRuleOfAgentEpsilonGreedyAction(String name, TRole owner, double epsilon, double gamma) {
        super(name, owner);
        fQMap = new HashMap<>();
        fAlphaMap = new HashMap<>();
        fEpsilon = epsilon;
        fGamma = gamma;
    }

    /**
     * ルールを実行する．
     *
     * @param currentTime           現在時刻
     * @param currentStage          現在ステージ
     * @param spotManager           スポット管理
     * @param agentManager          エージェント管理
     * @param globalSharedVariables グローバル共有変数集合
     */
    @Override
    public final void doIt(TTime currentTime, Enum<?> currentStage, TSpotManager spotManager,
            TAgentManager agentManager, Map<String, Object> globalSharedVariables) {
        // 穴掘り法で作成した迷路なので，一回で2マス移動する．
        EAgentAction[] actions = EAgentAction.values();

        // 報酬は ゴール 100, 通路 0, 壁 -1
        TRoleOfEpsilonGreedyAgent role = (TRoleOfEpsilonGreedyAgent) getOwnerRole();
        int[] state = role.getState();

        // Q学習のための初期化
        fQMap.putIfAbsent(state, new HashMap<>());
        fAlphaMap.putIfAbsent(state, new HashMap<>());
        for (EAgentAction a : actions) {
            fQMap.get(state).putIfAbsent(a, 0.0);
            fAlphaMap.get(state).putIfAbsent(a, 0);
        }

        EAgentAction action;
        if (getRandom().nextDouble() < fEpsilon) {
            // 確率εでランダムな行動を選択
            action = actions[getRandom().nextInt(actions.length)];
        } else {
            // 最良の行動を選択
            action = fQMap.get(state).entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
        }
        role.setAgentAction(action);

        int reward = 0; // 報酬
        int done = 0; // 終了判定

        // 1マス先スポット
        TSpot spot1 = null;
        switch (action) {
            case Up:
                spot1 = ((TRoleOf2DCell) getCurrentSpot().getRole(ECellModuleRoleName.Cell))
                        .getCellByRelativeCoordinates(0, 1);
                break;
            case Down:
                spot1 = ((TRoleOf2DCell) getCurrentSpot().getRole(ECellModuleRoleName.Cell))
                        .getCellByRelativeCoordinates(0, -1);
                break;
            case Right:
                spot1 = ((TRoleOf2DCell) getCurrentSpot().getRole(ECellModuleRoleName.Cell))
                        .getCellByRelativeCoordinates(1, 0);
                break;
            case Left:
                spot1 = ((TRoleOf2DCell) getCurrentSpot().getRole(ECellModuleRoleName.Cell))
                        .getCellByRelativeCoordinates(-1, 0);
                break;
        }
        // 1マス先が壁 -> 状態は変化なし，報酬 -1
        if (((TRoleOfMazeCell) spot1.getRole(ERoleName.MazeCell)).getMazeCellType() == EMazeCellType.Wall) {
            reward = -1;
            done = 1;
        } else {
            // 2マス先スポット
            TSpot spot2 = null;
            // 1マス先が壁ではないので移動可能なことは確定．-> 状態書き換え
            switch (action) {
                case Up:
                    spot2 = ((TRoleOf2DCell) spot1.getRole(ECellModuleRoleName.Cell))
                            .getCellByRelativeCoordinates(0, 1);
                    state[1] += 2;
                    break;
                case Down:
                    spot2 = ((TRoleOf2DCell) spot1.getRole(ECellModuleRoleName.Cell))
                            .getCellByRelativeCoordinates(0, -1);
                    state[1] -= 2;
                    break;
                case Right:
                    spot2 = ((TRoleOf2DCell) spot1.getRole(ECellModuleRoleName.Cell))
                            .getCellByRelativeCoordinates(1, 0);
                    state[0] += 2;
                    break;
                case Left:
                    spot2 = ((TRoleOf2DCell) spot1.getRole(ECellModuleRoleName.Cell))
                            .getCellByRelativeCoordinates(-1, 0);
                    state[0] -= 2;
                    break;
            }
            // 2マス先がゴール -> 報酬 100
            if (((TRoleOfMazeCell) spot2.getRole(ERoleName.MazeCell)).getMazeCellType() == EMazeCellType.Goal) {
                done = 1;
                reward = 100;
            } else { // 2マス先が通路 -> 報酬 0
                done = 0;
                reward = 0;
            }
            // エージェント移動
            moveTo(spot2);
        }
        role.setReward(reward); // 報酬の獲得
        // 次の状態
        int[] nextState = role.getState();
        // 次の状態行動対のQ関数の初期化
        fQMap.putIfAbsent(nextState, new HashMap<>());
        for (EAgentAction a : actions) {
            fQMap.get(nextState).putIfAbsent(a, 0.0);
        }
        // 状態行動対への訪問回数をインクリメント
        fAlphaMap.get(state).put(action, fAlphaMap.get(state).get(action) + 1);
        // ステップサイズ
        double alpha = 1.0 / (double) fAlphaMap.get(state).get(action);
        // Q関数の更新
        fQMap.get(state).put(action,
                fQMap.get(state).get(action) + alpha * ((double) reward + fGamma * (double) (1 - done)
                        * fQMap.get(nextState).entrySet().stream().max(Map.Entry.comparingByValue()).get()
                                .getValue()
                        - fQMap.get(state).get(action)));

    }
}
```

### TRoleOfEpsilonGreedyAgent:ε-グリーディエージェント役割

エージェント役割は，エージェントの行動，状態，報酬をもち，ε-グリーディ法による行動ルールが登録されている．

`TRoleOfEpsilonGreedyAgent.java`

```java
public class TRoleOfEpsilonGreedyAgent extends TRole {

    /** エージェントが選択した行動 */
    private EAgentAction fAgentAction;

    /** 行動で得られた次の状態 (セルの絶対座標) */
    private final int[] fCoordinates;

    /** 行動で得られた報酬 */
    private int fReward;

    /** エージェント行動ε-greedy選択ルール */
    public static final String RULE_NAME_OF_AGENT_EPSILON_GREEDY_ACTION = "agentEpsilonGreedyAction";

    /**
     * コンストラクタ
     *
     * @param owner        この役割を持つエージェント
     * @param initialSpotX 初期スポットx座標
     * @param initialSpotY 初期スポットy座標
     */
    public TRoleOfEpsilonGreedyAgent(TAgent owner, int initialSpotX, int initialSpotY) {
        super(ERoleName.Agent, owner, 1, 0);
        fAgentAction = null;
        fCoordinates = new int[] { initialSpotX, initialSpotY };
        fReward = 0;

        new TRuleOfAgentEpsilonGreedyAction(RULE_NAME_OF_AGENT_EPSILON_GREEDY_ACTION, this)
                .setStage(EStage.AgentAction);
    }

    /**
     * エージェントが選択した行動を設定
     *
     * @param action エージェントが選択した行動
     */
    public final void setAgentAction(EAgentAction action) {
        fAgentAction = action;
    }

    /**
     * エージェントが選択した行動を返す．
     *
     * @return エージェントが選択した行動
     */
    public final EAgentAction getAgentAction() {
        return fAgentAction;
    }

    /**
     * 行動で得られた次の状態 (セルの絶対座標)を返す．
     *
     * @return 行動で得られた次の状態 (セルの絶対座標)
     */
    public final int[] getState() {
        return fCoordinates;
    }

    /**
     * 行動で得られた報酬を設定
     *
     * @param reward 行動で得られた報酬
     */
    public final void setReward(int reward) {
        fReward = reward;
    }

    /**
     * 行動で得られた報酬を返す．
     *
     * @return 行動で得られた報酬
     */
    public final int getReward() {
        return fReward;
    }
}
```

### メインクラスの定義

上記のサンプルで実装したシミュレーションのメインクラスにおいて，エージェントを生成している箇所を以下のように書き換える．

`TMain.java`

```Java
        // *************************************************************************************************************
        // エージェント作成
        // *************************************************************************************************************

        TAgent agent = agentManager.createAgent(EAgentType.Agent, 1);
        agent.initializeCurrentSpot(map.getCell(1, 1));
        TRoleOfGreedyAgent agentRole = new TRoleOfEpsilonGreedyAgent(agent, 1, 1);
        agent.activateRole(ERoleName.Agent);
```
