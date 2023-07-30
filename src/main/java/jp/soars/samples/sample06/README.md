<!-- omit in toc -->
# sample06：臨時実行ルールの発火時刻をランダムに設定

- [シナリオとシミュレーション条件](#シナリオとシミュレーション条件)
- [シミュレーション定数の定義](#シミュレーション定数の定義)
- [ルールの定義](#ルールの定義)
  - [ランダム移動ルール](#ランダム移動ルール)
- [役割の定義](#役割の定義)
  - [エージェント役割](#エージェント役割)
- [メインクラスの定義](#メインクラスの定義)


## シナリオとシミュレーション条件

以下のシナリオを考える．

- 10人のエージェント(Agent1〜Agent10)は，それぞれ自宅(Spot1〜Spot10)を持つ．
- エージェントは，8時から18時まで20個のスポット(Spot1〜Spot20)上をランダムに動き回る．
- エージェントは，スポットを移動するごとにそのスポットへの滞在時間を1, 2, 3時間からランダムに選択する．
- エージェントは，18時にそれぞれの自宅に戻る．

シミュレーション条件

- エージェント : Spot(20)
- スポット : Agent(10)
- ステージ : AgentMoving
- 時刻ステップ間隔：1時間 / step
- シミュレーション期間：7日間

## シミュレーション定数の定義

sample06では以下の定数を定義する．

- エージェントタイプの定義
- スポットタイプの定義
- ステージの定義
- 役割名の定義


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

## ルールの定義

sample06では以下のルールを定義する．

- TRuleOfRandomMoving：ランダム移動ルール

### ランダム移動ルール

sample05のランダム移動ルールとの差分は，時間間隔をコンストラクタで受け取らずに，乱数発生器を使用してランダムに滞在時間を決定している点である．
このとき，滞在時間が終了時刻を超えてしまう場合には，終了時刻にルールが発火するように予約する．

`TRuleOfRandomMoving.java`

```java
public final class TRuleOfRandomMoving extends TAgentRule {

    /** 自宅 */
    private final TSpot fHome;

    /** 出発地 */
    private TSpot fSource;

    /** 目的地のスポットタイプ． */
    private final Enum<?> fDestinationType;

    /** 次のルールの発火時刻計算用 */
    private final TTime fTimeOfNextRule;

    /** 終了時刻 */
    private final TTime fEndTime;

    /**
     * 2回目以降に繰り返し実行されるルール．
     * 2回目以降は臨時実行ルールのスケジュールを繰り返すことで使い回す．
     * 1回目の実行は定時実行ルールで行う．
     */
    private TRuleOfRandomMoving fRepeatedRule;

    /**
     * リピートルールのルール名．
     * 同じ役割に同じルール名のルールを登録しようとした場合，
     * 警告メッセージが出力され，上書きされてしまうため，ルール名は必ず変更する．
     */
    public static final String RULE_NAME_OF_REPEATED_RANDOM_MOVING = "RepeatedRandomMoving";

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールを持つ役割
     * @param home 自宅
     * @param destinationType 移動する候補地のスポットタイプ
     * @param interval 次のルールを実行するまでの時間間隔
     * @param endTime 終了時刻
     */
    public TRuleOfRandomMoving(String name, TRole owner, TSpot home, Enum<?> destinationType, TTime endTime) {
        super(name, owner);
        fHome = home;
        fSource = fHome;
        fDestinationType = destinationType;
        fTimeOfNextRule = new TTime();
        fEndTime = endTime;
        fRepeatedRule = null;
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
        if (isAt(fSource)) { // スポット条件が満たされたら
            if (currentTime.isEqualTo(fEndTime)) { // 終了時刻ならば
                moveTo(fHome); // 自宅へ移動する
            } else { // 終了時刻でないなら
                List<TSpot> spots = spotManager.getSpots(fDestinationType); // fDestinationType のスポットリストを取得．
                TSpot destination = spots.get(getRandom().nextInt(spots.size())); // ランダムに目的地を選択する
                moveTo(destination); // 目的地に移動する

                // 自分が臨時実行ルールならば，次回実行するルールとして自分を使い回す
                // 臨時実行ルールでないならば，fRepeatedRuleを使用する．
                TRuleOfRandomMoving rule = this;
                if (!rule.isTemporaryRule()) { // 臨時実行ルールでないならば
                    if (fRepeatedRule == null) { // 使い回すルールが作成されていなければ作成する
                        fRepeatedRule = new TRuleOfRandomMoving(RULE_NAME_OF_REPEATED_RANDOM_MOVING,
                                getOwnerRole(), fHome, fDestinationType, fEndTime);
                    }
                    rule = fRepeatedRule;
                }

                rule.setSource(destination); // 現在の命令の目的地を次のルールの出発地に設定
                // 滞在時間は1-3時間でランダムとし，次のルールの発火時刻を決定
                fTimeOfNextRule.copyFrom(currentTime)
                               .add(getRandom().nextInt(1, 3), 0, 0);
                if (fTimeOfNextRule.isGreaterThan(fEndTime)) { // 決定された時刻が終了時刻よりも大きい場合は，終了時刻に設定する．
                    rule.setTimeAndStage(fTimeOfNextRule.getDay(), fEndTime.getHour(),
                            fEndTime.getMinute(), fEndTime.getSecond(), getStage());
                } else { // そうでなければ，臨時実行ルールとして予約
                    rule.setTimeAndStage(fTimeOfNextRule.getDay(), fTimeOfNextRule.getHour(),
                            fTimeOfNextRule.getMinute(), fTimeOfNextRule.getSecond(), getStage());
                }
            }
        }
    }

    /**
     * 出発地を設定
     * @param source 出発地
     */
    private final void setSource(TSpot source) {
        fSource = source;
    }
}
```

## 役割の定義

sample06では以下の役割を定義する．

- TRoleOfAgent：エージェント役割

### エージェント役割

エージェント役割は，8時から18時までの間，1-3時間ごとにランダム移動するルールを持つ．

`TRoleOfAgent.java`

```java
public final class TRoleOfAgent extends TRole {

    /** ランダムに移動する */
    public static final String RULE_NAME_OF_RANDOM_MOVING = "RandomMoving";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param home 自宅
     */
    public TRoleOfAgent(TAgent owner, TSpot home) {
        super(ERoleName.Agent, owner, 1, 0);

        // 8時から18時までの間，1-3時間ごとにランダム移動するルールを設定する
        new TRuleOfRandomMoving(RULE_NAME_OF_RANDOM_MOVING, this, home, ESpotType.Spot, new TTime("18:00:00"))
                .setTimeAndStage(8, 0, 0, EStage.AgentMoving);
    }
}
```

## メインクラスの定義

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
        String simulationEnd = "7/00:00:00"; // シミュレーション終了時刻
        String tick = "1:00:00"; // １ステップの時間間隔
        List<Enum<?>> stages = List.of(EStage.AgentMoving); // ステージリスト
        Set<Enum<?>> agentTypes = new HashSet<>(); // 全エージェントタイプ
        Set<Enum<?>> spotTypes = new HashSet<>(); // 全スポットタイプ
        Collections.addAll(agentTypes, EAgentType.values()); // EAgentType に登録されているエージェントタイプをすべて追加
        Collections.addAll(spotTypes, ESpotType.values()); // ESpotType に登録されているスポットタイプをすべて追加
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes); // ビルダー作成

        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目．
        // *************************************************************************************************************

        // マスター乱数発生器のシード値設定
        long seed = 0L; // シード値
        builder.setRandomSeed(seed); // シード値設定

        // ログ出力設定
        String pathOfLogDir = "logs" + File.separator + "sample06"; // ログディレクトリ
        builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv") // ルールログ出力設定
               .setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv"); // ランタイムログ出力設定

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
        //   - Spot スポットを20つ
        // *************************************************************************************************************

        int noOfSpots = 20; // スポットの数
        List<TSpot> spots = spotManager.createSpots(ESpotType.Spot, noOfSpots);

        // *************************************************************************************************************
        // エージェント作成
        //   - Agent エージェントを10つ
        //     - 初期スポットは Spot スポット
        //     - 役割としてエージェント役割を持つ．
        // *************************************************************************************************************

        int noOfAgents = 10; // エージェント数
        List<TAgent> agents = agentManager.createAgents(EAgentType.Agent, noOfAgents);
        for (int i = 0; i < agents.size(); i++) {
            TAgent agent = agents.get(i);// i番目のエージェントを取り出す．
            TSpot home = spots.get(i); // i番目のエージェントの自宅を選択
            agent.initializeCurrentSpot(home); // 初期位置を自宅に設定する．

            new TRoleOfAgent(agent, home); // エージェント役割を生成する．
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
                spotLogPW.print("," + agent.getCurrentSpotName());
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
