<!-- omit in toc -->
# sample07：スポット・エージェントの動的追加・削除

スポット・エージェントの動的追加・削除の注意点について

- スポット・エージェントの動的追加・削除を行うステージは並列化できない．
- スポットはエージェントが存在する場合削除することができない．
- エージェントの移動(moveTo)と同一時刻・同一ステージで実行することはできない．

- [シナリオとシミュレーション条件](#シナリオとシミュレーション条件)
- [シミュレーション定数の定義](#シミュレーション定数の定義)
- [ルールの定義](#ルールの定義)
  - [オブジェクト作成ルール](#オブジェクト作成ルール)
  - [オブジェクト削除ルール](#オブジェクト削除ルール)
- [役割の定義](#役割の定義)
  - [クリエイター役割](#クリエイター役割)
  - [キラー役割](#キラー役割)
- [メインクラスの定義](#メインクラスの定義)


## シナリオとシミュレーション条件

以下のシナリオを考える．

- 3人のクリエイター(Creator1, Creator2, Creator3)は毎日8時にダミースポットとダミーエージェントを１つずつ追加する．
- 3人のキラー(Killer1, Killer2, Killer3)は毎日16時にダミースポットとダミーエージェントをランダムに１つずつ削除する．

シミュレーション条件

- エージェント : Creator(3), Killer(3), Dummy(増減する)
- スポット : Dummy(増減する)
- ステージ : DynamicAdd, DynamicDelete
- 時刻ステップ間隔：1時間 / step
- シミュレーション期間：7日間

## シミュレーション定数の定義

sample07では以下の定数を定義する．

- エージェントタイプの定義
- スポットタイプの定義
- ステージの定義
- 役割名の定義


```java
public enum EAgentType {
    /** クリエイター */
    Creator,
    /** キラー */
    Killer,
    /** ダミー */
    Dummy
}
```

```java
public enum ESpotType {
    /** ダミー */
    Dummy
}
```

```java
public enum EStage {
    /** スポット・エージェント動的追加ステージ */
    DynamicAdd,
    /** スポット・エージェント動的削除ステージ */
    DynamicDelete
}
```

```java
public enum ERoleName {
    /** クリエイター役割 */
    Creator,
    /** キラー役割 */
    Killer
}
```

## ルールの定義

sample07では以下のルールを定義する．

- TRuleOfCreatingSpotAndAgent：オブジェクト作成ルール
- TRuleOfDeletingSpotAndAgent：オブジェクト削除ルール

### オブジェクト作成ルール

オブジェクト作成ルールは，エージェント管理，スポット管理を利用してエージェントを新たに生成する．

`TRuleOfCreatingSpotAndAgent.java`

```java
public final class TRuleOfCreatingSpotAndAgent extends TAgentRule {

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールを持つ役割
     */
    public TRuleOfCreatingSpotAndAgent(String name, TRole owner) {
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
        boolean debugFlag = true; // デバッグ情報出力フラグ
        // 新たなスポットの作成
        TSpot newSpot = spotManager.createSpots(ESpotType.Dummy, 1).get(0);
        appendToDebugInfo("created spot:" + newSpot.getName(), debugFlag);

        // 新たなエージェントの作成
        TAgent newAgent = agentManager.createAgents(EAgentType.Dummy, 1).get(0);
        appendToDebugInfo(" created agent:" + newAgent.getName(), debugFlag);
    }
}
```

### オブジェクト削除ルール

オブジェクト削除ルールは，エージェント管理，スポット管理を利用してエージェントを削除する．
オブジェクトの削除にはdeleteAgent, deleteSpotメソッドを利用する．
スポットはエージェントがいる場合削除できない点に注意．

`TRuleOfDeletingSpotAndAgent.java`

```java
public final class TRuleOfDeletingSpotAndAgent extends TAgentRule {

    /**
     * 削除ルール
     * @param name ルール名
     * @param owner このルールを持つ役割
     */
    public TRuleOfDeletingSpotAndAgent(String name, TRole owner) {
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
        boolean debugFlag = true; // デバッグ情報出力フラグ
        // ダミースポットをランダムに１つ削除
        List<TSpot> dummySpots = spotManager.getSpots(ESpotType.Dummy); // スポット管理からダミースポットのリストを取得
        TSpot spot = null;
        do {
            spot = dummySpots.get(getRandom().nextInt(dummySpots.size())); // 削除されるスポットをランダムに選択
        } while (!spot.getAgents().isEmpty()); // エージェントがいるスポットを消そうとするとエラーとなるため，その場合は再選択
        spotManager.deleteSpot(spot);
        appendToDebugInfo("deleted spot:" + spot.getName(), debugFlag);

        // ダミーエージェントをランダムに１つ削除
        List<TAgent> dummyAgents = agentManager.getAgents(EAgentType.Dummy); // エージェント管理からダミーエージェントのリストを取得
        TAgent agent = dummyAgents.get(getRandom().nextInt(dummyAgents.size())); // 削除されるエージェントをランダムに選択
        agentManager.deleteAgent(agent);
        appendToDebugInfo(" deleted agent:" + agent.getName(), debugFlag);
    }
}
```

## 役割の定義

sample07では以下の役割を定義する．

- TRoleOfCreator：クリエイター役割
- TRoleOfKiller：キラー役割

### クリエイター役割

`TRoleOfCreator.java`

```java
public final class TRoleOfCreator extends TRole {

    /** ダミースポットとダミーエージェントを作成するルール名 */
    public static final String RULE_NAME_OF_CREATOR = "Creator";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     */
    public TRoleOfCreator(TObject owner) {
        super(ERoleName.Creator, owner, 1, 0);

        // 毎日8時に実行されるようにスケジューリング．
        new TRuleOfCreatingSpotAndAgent(RULE_NAME_OF_CREATOR, this)
                .setTimeAndStage(8, 0, 0, EStage.DynamicAdd);
    }
}
```

### キラー役割

`TRoleOfKiller.java`

```java
public final class TRoleOfKiller extends TRole {

    /** ダミースポットとダミーエージェントを削除するルール名 */
    public static final String RULE_NAME_OF_KILLER = "Killer";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     */
    public TRoleOfKiller(TObject owner) {
        super(ERoleName.Killer, owner, 1, 0);

        // 毎日16時に実行されるようにスケジューリング．
        new TRuleOfDeletingSpotAndAgent(RULE_NAME_OF_KILLER, this)
                .setTimeAndStage(16, 0, 0, EStage.DynamicDelete);
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
        List<Enum<?>> stages = List.of(EStage.DynamicAdd, EStage.DynamicDelete); // ステージリスト
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
        String pathOfLogDir = "logs" + File.separator + "sample07"; // ログディレクトリ
        builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv") // ルールログ出力設定
               .setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv"); // ランタイムログ出力設定

        // ルールログのデバッグ情報出力設定
        builder.setRuleDebugMode(ERuleDebugMode.LOCAL); // ローカル設定に従う

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
        //   - Dummy スポットを5つ
        // *************************************************************************************************************

        int noOfDummies = 5; // ダミーの数
        spotManager.createSpots(ESpotType.Dummy, noOfDummies);

        // *************************************************************************************************************
        // エージェント作成
        //   - Creator エージェントを3つ
        //     - 役割としてクリエイター役割を持つ．
        //   - Killer エージェントを3つ
        //     - 役割としてキラー役割を持つ．
        //   - Dummy エージェントを5つ
        // *************************************************************************************************************

        // Creator エージェントを作成
        int noOfCreators = 3;
        List<TAgent> creators = agentManager.createAgents(EAgentType.Creator, noOfCreators);
        for (int i = 0; i < creators.size(); ++i) {
            TAgent creator = creators.get(i); // i番目のCreatorを取り出す．
            new TRoleOfCreator(creator); // クリエイター役割を生成する．
            creator.activateRole(ERoleName.Creator); // クリエイター役割をアクティブ化する．
        }

        // Killer エージェントを作成
        int noOfKillers = 3;
        List<TAgent> killers = agentManager.createAgents(EAgentType.Killer, noOfKillers);
        for (int i = 0; i < killers.size(); ++i) {
            TAgent killer = killers.get(i); // i番目のKillerを取り出す．
            new TRoleOfKiller(killer); // キラー役割を生成する．
            killer.activateRole(ERoleName.Killer); // キラー役割をアクティブ化する．
        }

        // Dummy エージェントを作成
        agentManager.createAgents(EAgentType.Dummy, noOfDummies);

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        //   - 各時刻で全エージェントのエージェント名のログをとる (エージェントログ, agent_log.txt)
        //   - 各時刻で全スポットのスポット名のログをとる (スポットログ, spot_log.txt)
        // *************************************************************************************************************

        // エージェントログ用PrintWriter
        PrintWriter agentLogPW = new PrintWriter(new BufferedWriter(
                new FileWriter(pathOfLogDir + File.separator + "agent_log.txt")));
        // スポットログ用PrintWriter
        PrintWriter spotLogPW = new PrintWriter(new BufferedWriter(
                new FileWriter(pathOfLogDir + File.separator + "spot_log.txt")));

        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        while (ruleExecutor.executeStep()) { // 1ステップ分のルールを実行
            // 標準出力に現在時刻を表示する
            System.out.println(ruleExecutor.getCurrentTime());

            // ログ出力
            agentLogPW.print(ruleExecutor.getCurrentTime());
            for (TAgent agent : agentManager.getAgents()) {
                agentLogPW.print("," + agent.getName());
            }
            agentLogPW.println();

            spotLogPW.print(ruleExecutor.getCurrentTime());
            for (TSpot spot : spotManager.getSpots()) {
                spotLogPW.print("," + spot.getName());
            }
            spotLogPW.println();
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown(); // ルール実行器を終了する
        agentLogPW.close();// エージェントログを終了する
        spotLogPW.close(); // スポットログを終了する
    }
}
```
