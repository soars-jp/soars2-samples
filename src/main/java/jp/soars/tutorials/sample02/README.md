前：
次：
TODO:

# sample02:臨時実行ルールによる相対時刻指定 <!-- omit in toc -->

- [シナリオとシミュレーション条件](#シナリオとシミュレーション条件)
- [シミュレーション定数の定義](#シミュレーション定数の定義)
- [ルールの定義](#ルールの定義)
  - [TRuleOfMoveFromHomeToCompany:自宅から会社に移動するルール](#truleofmovefromhometocompany自宅から会社に移動するルール)
  - [TRuleOfMoveFromCompanyToHome:会社から自宅に移動するルール](#truleofmovefromcompanytohome会社から自宅に移動するルール)
- [役割の定義](#役割の定義)
  - [TRoleOfFather:父親役割](#troleoffather父親役割)
- [メインクラスの定義](#メインクラスの定義)


## シナリオとシミュレーション条件

以下のシナリオを考える．

- 3人の父親(Father1, Father2, Father3)は，それぞれ自宅(Home1, Home2, Home3)を持つ．
- 父親は，9時に自宅から同じ会社(Company)に移動する．
- 父親は，会社に移動してから32時間後に会社からそれぞれの自宅に移動する．

シミュレーション条件

- エージェント : Father(3)
- スポット : Home(3), Company(1)
- ステージ : AgentMoving
- 時刻ステップ間隔：1時間 / step
- シミュレーション期間：7日間

## シミュレーション定数の定義

`EAgentType.java`
```java
public enum EAgentType {
    /** 父親 */
    Father
}
```
`ESpotType.java`
```java
public enum ESpotType {
    /** 自宅 */
    Home,
    /** 会社 */
    Company
}
```
`EStage.java`
```java
public enum EStage {
    /** エージェント移動ステージ */
    AgentMoving
}
```
`ERoleName.java`
```java
public enum ERoleName {
    /** 父親役割 */
    Father
}
```

## ルールの定義

### TRuleOfMoveFromHomeToCompany:自宅から会社に移動するルール

sample01のTRuleOfMoveFromHomeToCompanyを拡張する．
会社から自宅に移動するルール，実行するまでの時間間隔，実行するステージを受け取り，
自宅から会社に移動した後，相対時刻指定で会社から自宅に移動するルールを臨時実行ルールとして予約する．

`TRuleOfMoveFromHomeToCompany.java`
```java
public final class TRuleOfMoveFromHomeToCompany extends TAgentRule {

    /** 会社から自宅に移動するルール */
    private final TRule fRuleOfReturnHome;

    /** 会社から自宅に移動するルールを実行するまでの時間間隔 */
    private final TTime fIntervalTimeOfReturnHome;

    /** 会社から自宅に移動するルールの発火時刻計算用 */
    private final TTime fTimeOfReturnHome;

    /** 会社から自宅に移動するルールを実行するステージ */
    private final Enum<?> fStageOfReturnHome;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     * @param ruleOfReturnHome 会社から自宅に移動するルール
     * @param intervalTimeOfReturnHome 会社から自宅に移動するルールを実行するまでの時間間隔
     * @param stageOfReturnHome 会社から自宅に移動するルールを実行するステージ
     */
    public TRuleOfMoveFromHomeToCompany(String name, TRole owner,
            TRule ruleOfReturnHome, String intervalTimeOfReturnHome, Enum<?> stageOfReturnHome) {
        // 親クラスのコンストラクタを呼び出す．
        super(name, owner);
        fRuleOfReturnHome = ruleOfReturnHome;
        fIntervalTimeOfReturnHome = new TTime(intervalTimeOfReturnHome);
        fTimeOfReturnHome = new TTime();
        fStageOfReturnHome = stageOfReturnHome;
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
        TRoleOfFather role = (TRoleOfFather) getOwnerRole(); // 父親役割(このルールを持っている役割)を取得
        if (isAt(role.getHome())) { // 自宅にいる場合
            // 会社に移動する
            moveTo(role.getCompany());
            // 移動ルールが正常に実行されたことをデバッグ情報としてルールログに出力
            appendToDebugInfo("success", debugFlag);

            // 現在時刻にインターバルを足した時刻を会社から自宅に移動するルールの発火時刻とする．
            fTimeOfReturnHome.copyFrom(currentTime).add(fIntervalTimeOfReturnHome);
            // 会社から自宅に移動するルールを臨時実行ルールとして予約する．
            fRuleOfReturnHome.setTimeAndStage(fTimeOfReturnHome.getDay(), fTimeOfReturnHome.getHour(),
                    fTimeOfReturnHome.getMinute(), fTimeOfReturnHome.getSecond(), fStageOfReturnHome);
        } else { // 自宅にいない場合
            // 移動ルールが実行されなかったことをデバッグ情報としてルールログに出力
            appendToDebugInfo("fail", debugFlag);
        }
    }
}
```

### TRuleOfMoveFromCompanyToHome:会社から自宅に移動するルール

sample01と同じ．

`TRuleOfMoveFromCompanyToHome.java`
```java
public final class TRuleOfMoveFromCompanyToHome extends TAgentRule {

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     */
    public TRuleOfMoveFromCompanyToHome(String name, TRole owner) {
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
        boolean debugFlag = true; // デバッグ情報出力フラグ
        TRoleOfFather role = (TRoleOfFather) getOwnerRole(); // 父親役割(このルールを持っている役割)を取得
        if (isAt(role.getCompany())) { // 会社にいる場合
            // 自宅に移動する
            moveTo(role.getHome());
            // 移動ルールが正常に実行されたことをデバッグ情報としてルールログに出力
            appendToDebugInfo("success", debugFlag);
        } else { // 会社にいない場合
            // 移動ルールが実行されなかったことをデバッグ情報としてルールログに出力
            appendToDebugInfo("fail", debugFlag);
        }
    }
}
```

## 役割の定義

### TRoleOfFather:父親役割

sample01のTRoleOfFatherを拡張する．
TRoleOfFatherではTRuleOfMoveFromCompanyToHomeの予約を行わない．
TRuleOfMoveFromHomeToCompanyを実行して，
32時間後のエージェント移動ステージにTRuleOfMoveFromCompanyToHomeが臨時実行ルールとして予約されるように設定する．
このとき，24時間以上の時間をTTimeで指定する場合は，dd/hh:mm:ss形式で指定する必要がある点に注意する．
"32:00:00"と指定した場合は"08:00:00"と解釈される．

`TRoleOfFather.java`
```java
public final class TRoleOfFather extends TRole {

    /** 自宅 */
    private final TSpot fHome;

    /** 会社 */
    private final TSpot fCompany;

    /** 自宅から会社に移動するルール名 */
    private static final String RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY = "MoveFromHomeToCompany";

    /** 会社から自宅に移動するルール名 */
    private static final String RULE_NAME_OF_MOVE_FROM_COMPANY_TO_HOME = "MoveFromCompanyToHome";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param home 自宅
     * @param company 会社
     */
    public TRoleOfFather(TAgent owner, TSpot home, TSpot company) {
        // 親クラスのコンストラクタを呼び出す．
        // 以下の2つの引数は省略可能で，その場合デフォルト値で設定される．
        // 第3引数:この役割が持つルール数 (デフォルト値 10)
        // 第4引数:この役割が持つ子役割数 (デフォルト値 5)
        super(ERoleName.Father, owner, 2, 0);

        fHome = home;
        fCompany = company;

        // 役割が持つルールの登録
        // 会社から自宅に移動するルール．予約はTRuleOfMoveFromHomeToCompanyで相対時刻指定で行われる．
        TRule ruleOfReturnHome = new TRuleOfMoveFromCompanyToHome(RULE_NAME_OF_MOVE_FROM_COMPANY_TO_HOME, this);

        // 自宅から会社に移動するルール．9:00:00/エージェント移動ステージに定時実行ルールとして予約する．
        // 相対時刻として32:00:00ではなく0/32:00:00とする点に注意．32:00:00はTTimeクラスで8:00:00と解釈され，32時間後を表現できない．
        new TRuleOfMoveFromHomeToCompany(RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY, this,
                ruleOfReturnHome, "0/32:00:00", EStage.AgentMoving)
                .setTimeAndStage(9, 0, 0, EStage.AgentMoving);
    }

    /**
     * 自宅を返す．
     * @return 自宅
     */
    public final TSpot getHome() {
        return fHome;
    }

    /**
     * 会社を返す．
     * @return 会社
     */
    public final TSpot getCompany() {
        return fCompany;
    }
}
```

## メインクラスの定義

sample01のメインクラスのログ出力ディレクトリを変更する．

`TMain.java`

```java
public class TMain {

    public static void main(String[] args) throws IOException {
        // *************************************************************************************************************
        // TSOARSBuilderの必須設定項目．
        //   - simulationStart: シミュレーション開始時刻
        //   - simulationEnd: シミュレーション終了時刻
        //   - tick: 1ステップの時間間隔
        //   - stages: 使用するステージリスト(実行順)
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
        String pathOfLogDir = "logs" + File.separator + "tutorials" + File.separator + "sample02"; // ログディレクトリ
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
        //   - Home(3)
        //   - Company(1)
        // *************************************************************************************************************

        int noOfHomes = 3; // 家の数
        List<TSpot> homes = spotManager.createSpots(ESpotType.Home, noOfHomes); // Homeスポットを生成．(Home1, Home2, Home3)
        TSpot company = spotManager.createSpot(ESpotType.Company); // Companyスポットを1つ生成．(Company)

        // *************************************************************************************************************
        // エージェント作成
        //   - Father(3)
        //     - 初期スポットは Home
        //     - 父親役割を持つ．
        // *************************************************************************************************************

        int noOfFathers = noOfHomes; // 父親の数は家の数と同じ．
        List<TAgent> fathers = agentManager.createAgents(EAgentType.Father, noOfFathers); // Fatherエージェントを生成．(Father1, Father2, Father3)
        for (int i = 0; i < noOfFathers; ++i) {
            TAgent father = fathers.get(i); // i番目のエージェントを取り出す．
            TSpot home = homes.get(i); // i番目のエージェントの自宅を取り出す．
            father.initializeCurrentSpot(home); // 初期位置を自宅に設定する．
            new TRoleOfFather(father, home, company); // 父親役割を生成する．
            father.activateRole(ERoleName.Father); // 父親役割をアクティブ化する．
        }

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        //   - 各時刻でエージェントの現在位置のログをとる (スポットログ, spot_log.csv)
        // *************************************************************************************************************

        // スポットログ用PrintWriter
        PrintWriter spotLogPW = new PrintWriter(new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "spot_log.csv")));
        // スポットログのカラム名出力
        spotLogPW.print("CurrentTime");
        for (TAgent father : fathers) {
            spotLogPW.print("," + father.getName());
        }
        spotLogPW.println();

        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        while (ruleExecutor.executeStep()) {
            // 標準出力に現在時刻を表示する
            System.out.println(ruleExecutor.getCurrentTime());

            // スポットログ出力
            spotLogPW.print(ruleExecutor.getCurrentTime());
            for (TAgent father : fathers) {
                spotLogPW.print("," + father.getCurrentSpotName());
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

前：
次：
TODO:
