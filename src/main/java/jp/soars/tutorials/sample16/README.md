前：[sample15:大規模実験のための最適化設定](../sample15/)

# sample16:オブジェクトの動的追加・削除 <!-- omit in toc -->

sample16では，オブジェクトの動的追加・削除の方法について解説する．
オブジェクトの動的追加・削除には以下の注意点がある．
- スポット・エージェントの動的追加・削除を行うステージは並列化できない．
- スポットはエージェントが存在する場合削除することができない．
- エージェントの移動(moveTo)と同一時刻・同一ステージで実行することはできない．

- [シナリオとシミュレーション条件](#シナリオとシミュレーション条件)
- [シミュレーション定数の定義](#シミュレーション定数の定義)
- [ルールの定義](#ルールの定義)
  - [TRuleOfAgentRandomMoving:エージェントランダム移動ルール](#truleofagentrandommovingエージェントランダム移動ルール)
- [役割の定義](#役割の定義)
  - [TRoleOfAgent:エージェント役割](#troleofagentエージェント役割)
- [メインクラスの定義](#メインクラスの定義)
  - [TMainSequential:逐次実行メインクラス](#tmainsequential逐次実行メインクラス)
  - [TMainParallel:並列実行メインクラス](#tmainparallel並列実行メインクラス)
- [シナリオとシミュレーション条件](#シナリオとシミュレーション条件-1)
- [シミュレーション定数の定義](#シミュレーション定数の定義-1)
- [ルールの定義](#ルールの定義-1)
  - [オブジェクト作成ルール](#オブジェクト作成ルール)
  - [オブジェクト削除ルール](#オブジェクト削除ルール)
- [役割の定義](#役割の定義-1)
  - [クリエイター役割](#クリエイター役割)
  - [キラー役割](#キラー役割)
- [メインクラスの定義](#メインクラスの定義-1)


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

`EAgentType.java`

```Java
public enum EAgentType {
    /** クリエイター */
    Creator,
    /** キラー */
    Killer,
    /** ダミー */
    Dummy
}
```

`ESpotType.java`

```Java
public enum ESpotType {
    /** ダミー */
    Dummy
}
```

`EStage.java`

```Java
public enum EStage {
    /** スポット・エージェント動的追加ステージ */
    DynamicAdd,
    /** スポット・エージェント動的削除ステージ */
    DynamicDelete
}
```

`ERoleName.java`

```Java
public enum ERoleName {
    /** クリエイター役割 */
    Creator,
    /** キラー役割 */
    Killer
}
```

## ルールの定義

### TRuleOfCreatingSpotAndAgent:動的オブジェクト作成ルール

TRuleOfCreatingSpotAndAgentはルール内でオブジェクトを新たに作成する．作成方法はメインクラスで作成する方法と同じである．

`TRuleOfCreatingSpotAndAgent.java`

```Java
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

### TRuleOfDeletingSpotAndAgent:動的オブジェクト削除ルール

TRuleOfDeletingSpotAndAgentはルール内からオブジェクトを削除する．
オブジェクトの削除は，agentManagerクラスのdeleteAgentメソッド，spotManagerクラスのdeleteSpotメソッドで実行する．

`TRuleOfDeletingSpotAndAgent.java`

```Java
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
        // ダミースポットをランダムに１つ選択して削除する．
        // 削除実行前に，スポットにエージェントがいないことを確認している．
        List<TSpot> dummySpots = spotManager.getSpots(ESpotType.Dummy);
        TSpot spot = null;
        do {
            spot = dummySpots.get(getRandom().nextInt(dummySpots.size()));
        } while (!spot.getAgents().isEmpty());
        spotManager.deleteSpot(spot);
        appendToDebugInfo("deleted spot:" + spot.getName(), debugFlag);

        // ダミーエージェントをランダムに１つ選択して削除する．
        List<TAgent> dummyAgents = agentManager.getAgents(EAgentType.Dummy);
        TAgent agent = dummyAgents.get(getRandom().nextInt(dummyAgents.size()));
        agentManager.deleteAgent(agent);
        appendToDebugInfo(" deleted agent:" + agent.getName(), debugFlag);
    }
}
```

## 役割の定義

### TRoleOfCreator:クリエイター役割

TRoleOfCreatorはダミーエージェントとダミースポットを新たに作成する．

`TRoleOfCreator.java`

```Java
public final class TRoleOfCreator extends TRole {

    /** 動的にオブジェクトを作成するルール名 */
    public static final String RULE_NAME_OF_CREATING_SPOT_AND_AGENT = "CreatingSpotAndAgent";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     */
    public TRoleOfCreator(TObject owner) {
        // 親クラスのコンストラクタを呼び出す．
        // 以下の2つの引数は省略可能で，その場合デフォルト値で設定される．
        // 第3引数:この役割が持つルール数 (デフォルト値 10)
        // 第4引数:この役割が持つ子役割数 (デフォルト値 5)
        super(ERoleName.Creator, owner, 1, 0);

        // 役割が持つルールの登録
        // 動的オブジェクト作成ルール．8:00:00/動的追加ステージに定時実行ルールとして予約する．
        new TRuleOfCreatingSpotAndAgent(RULE_NAME_OF_CREATING_SPOT_AND_AGENT, this)
                .setTimeAndStage(8, 0, 0, EStage.DynamicAdd);
    }
}
```

### TRoleOfKiller:キラー役割

TRoleOfKillerはダミーエージェントとダミースポットをランダムに選択して削除する．

`TRoleOfKiller.java`

```Java
public final class TRoleOfKiller extends TRole {

    /** 動的にオブジェクトを削除するルール名 */
    public static final String RULE_NAME_OF_DELETING_SPOT_AND_AGENT = "DeletingSpotAndAgent";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     */
    public TRoleOfKiller(TObject owner) {
        // 親クラスのコンストラクタを呼び出す．
        // 以下の2つの引数は省略可能で，その場合デフォルト値で設定される．
        // 第3引数:この役割が持つルール数 (デフォルト値 10)
        // 第4引数:この役割が持つ子役割数 (デフォルト値 5)
        super(ERoleName.Killer, owner, 1, 0);

        // 役割が持つルールの登録
        // 動的オブジェクト削除ルール．16:00:00/動的削除ステージに定時実行ルールとして予約する．
        new TRuleOfDeletingSpotAndAgent(RULE_NAME_OF_DELETING_SPOT_AND_AGENT, this)
                .setTimeAndStage(16, 0, 0, EStage.DynamicDelete);
    }
}
```

## メインクラスの定義

メインクラスでは，クリエイター，キラー，ダミーのオブジェクト生成と，
各時刻でシミュレーション中に存在するエージェントの一覧，スポットに一覧をそれぞれテキストファイルとして出力する．
これはcsv形式ではないためテキストファイルとしている．

`TMainParallel.java`

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
        List<Enum<?>> stages = List.of(EStage.DynamicAdd, EStage.DynamicDelete);
        Set<Enum<?>> agentTypes = new HashSet<>();
        Collections.addAll(agentTypes, EAgentType.values());
        Set<Enum<?>> spotTypes = new HashSet<>();
        Collections.addAll(spotTypes, ESpotType.values());
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes);

        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目
        // *************************************************************************************************************

        // マスター乱数発生器のシード値設定
        long seed = 0L;
        builder.setRandomSeed(seed);

        // ランタイムログの出力設定
        String pathOfLogDir = "logs" + File.separator + "tutorials" + File.separator + "sample16";
        builder.setRuntimeLoggingEnabled(pathOfLogDir + File.separator + "runtime_log.csv");
        builder.setRuleLoggingEnabled(pathOfLogDir + File.separator + "rule_log.csv");

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
        //   - Dummy:Dummy1-Dummy5
        // *************************************************************************************************************

        int noOfDummies = 5;
        spotManager.createSpots(ESpotType.Dummy, noOfDummies);

        // *************************************************************************************************************
        // エージェント作成
        //   - Creator:Creator1-Creator3
        //     - 役割:クリエイター役割
        //   - Killer:Killer1-Killer3
        //     - 役割:キラー役割
        //   - Dummy:Dummy1-Dummy5
        // *************************************************************************************************************

        // Creator エージェントを作成
        int noOfCreators = 3;
        List<TAgent> creators = agentManager.createAgents(EAgentType.Creator, noOfCreators);
        for (int i = 0; i < noOfCreators; ++i) {
            TAgent creator = creators.get(i); // i番目のCreatorを取り出す．
            new TRoleOfCreator(creator); // クリエイター役割を生成する．
            creator.activateRole(ERoleName.Creator); // クリエイター役割をアクティブ化する．
        }

        // Killer エージェントを作成
        int noOfKillers = 3;
        List<TAgent> killers = agentManager.createAgents(EAgentType.Killer, noOfKillers);
        for (int i = 0; i < noOfKillers; ++i) {
            TAgent killer = killers.get(i); // i番目のKillerを取り出す．
            new TRoleOfKiller(killer); // キラー役割を生成する．
            killer.activateRole(ERoleName.Killer); // キラー役割をアクティブ化する．
        }

        // Dummy エージェントを作成
        agentManager.createAgents(EAgentType.Dummy, noOfDummies);

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        //   - スポットログ:各時刻で存在するスポット名の一覧 (txtファイル)
        //   - エージェントログ:各時刻で存在するエージェント名の一覧 (txtファイル)
        // *************************************************************************************************************

        PrintWriter agentLogPW = new PrintWriter(new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "agent_log.txt")));
        PrintWriter spotLogPW = new PrintWriter(new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "spot_log.txt")));

        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        // 1ステップ分のルールを実行 (ruleExecutor.executeStage()で1ステージ毎に実行することもできる)
        // 実行された場合:true，実行されなかった(終了時刻)場合は:falseが帰ってくるため，while文で回すことができる．
        while (ruleExecutor.executeStep()) {
            // 標準出力に現在時刻を表示する
            System.out.println(ruleExecutor.getCurrentTime());

            // ログ出力
            agentLogPW.print(ruleExecutor.getCurrentTime());
            for (TAgent agent : agentManager.getAgents()) {
                agentLogPW.print(",");
                agentLogPW.print(agent.getName());
            }
            agentLogPW.println();

            spotLogPW.print(ruleExecutor.getCurrentTime());
            for (TSpot spot : spotManager.getSpots()) {
                spotLogPW.print(",");
                spotLogPW.print(spot.getName());
            }
            spotLogPW.println();
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown();
        agentLogPW.close();
        spotLogPW.close();
    }
}
```

前：[sample15:大規模実験のための最適化設定](../sample15/)


























































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
