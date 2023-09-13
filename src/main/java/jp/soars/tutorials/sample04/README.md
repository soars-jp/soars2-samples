前：
次：
TODO:

# sample04:曜日概念の導入 <!-- omit in toc -->

- [シナリオとシミュレーション条件](#シナリオとシミュレーション条件)
- [シミュレーション定数の定義](#シミュレーション定数の定義)
- [ルールの定義](#ルールの定義)
  - [TRuleOfStochasticallyMoveFromHomeToCompany:確率的に自宅から会社に移動するルール](#truleofstochasticallymovefromhometocompany確率的に自宅から会社に移動するルール)
  - [TRuleOfMoveFromCompanyToHome:会社から自宅に移動するルール](#truleofmovefromcompanytohome会社から自宅に移動するルール)
- [役割の定義](#役割の定義)
  - [TRoleOfFather:父親役割](#troleoffather父親役割)
- [メインクラスの定義](#メインクラスの定義)


## シナリオとシミュレーション条件

以下のシナリオを考える．

- 3人の父親(Father1, Father2, Father3)は，それぞれ自宅(Home1, Home2, Home3)を持つ．
- 父親は，平日(土日以外)は50%の確率で9時，30%の確率で10時，20%の確率で11時に自宅から同じ会社(Company)に移動する．
- 父親は，会社に移動してから8時間後に会社からそれぞれの自宅に移動する．
- 父親は，休日(土日)は会社に移動せず自宅にいる．

シミュレーション条件

- エージェント : Father(3)
- スポット : Home(3), Company(1)
- ステージ : AgentMoving
- 時刻ステップ間隔：1時間 / step
- シミュレーション期間：7日間

## シミュレーション定数の定義

sample03に追加して，曜日を表すenumクラスEDayを定義する．

`EDay.java`

```Java
public enum EDay {
    /** 日曜日 */
    Sunday,
    /** 月曜日 */
    Monday,
    /** 火曜日 */
    Tuesday,
    /** 水曜日 */
    Wednesday,
    /** 木曜日 */
    Thursday,
    /** 金曜日 */
    Friday,
    /** 土曜日 */
    Saturday
}
```

`EAgentType.java`

```Java
public enum EAgentType {
    /** 父親 */
    Father
}
```

`ESpotType.java`

```Java
public enum ESpotType {
    /** 自宅 */
    Home,
    /** 会社 */
    Company
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
    /** 父親役割 */
    Father
}
```

## ルールの定義
### TRuleOfStochasticallyMoveFromHomeToCompany:確率的に自宅から会社に移動するルール

sample03のTRuleOfStochasticallyMoveFromHomeToCompanyを拡張する．
現在時刻の日付を7で割った余りで曜日を計算し，土日の場合は会社に移動せず，会社から自宅に移動するルールの予約も行わないように変更する．

`TRuleOfStochasticallyMoveFromHomeToCompany.java`

```Java
public final class TRuleOfStochasticallyMoveFromHomeToCompany extends TAgentRule {

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
    public TRuleOfStochasticallyMoveFromHomeToCompany(String name, TRole owner,
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
        boolean debugFlag = true;

        // エージェントが平日(土日以外)に自宅にいるならば，会社に移動する．
        // 日付を7で割った余りで曜日を取得．どの曜日が何番になっているかはenumの定義順による．
        EDay day = EDay.values()[currentTime.getDay() % 7];
        if (day != EDay.Sunday && day != EDay.Saturday) {
            TRoleOfFather role = (TRoleOfFather) getOwnerRole();
            if (isAt(role.getHome())) {
                moveTo(role.getCompany());
                appendToDebugInfo("success", debugFlag);

                // 会社から自宅に移動するルールの発火時刻を計算して臨時実行ルールとして予約する．
                fTimeOfReturnHome.copyFrom(currentTime).add(fIntervalTimeOfReturnHome);
                fRuleOfReturnHome.setTimeAndStage(fTimeOfReturnHome.getDay(), fTimeOfReturnHome.getHour(),
                        fTimeOfReturnHome.getMinute(), fTimeOfReturnHome.getSecond(), fStageOfReturnHome);
            } else {
                appendToDebugInfo("fail", debugFlag);
            }
        }

        // 次の日の9時(50%)，10時(30%)，11時(20%)のエージェント移動ステージに自分自身を再予約する．
        double p = getRandom().nextDouble();
        int hour;
        if (p <= 0.5) {
            hour = 9; // 50%
        } else if (p <= 0.8) {
            hour = 10; // 30%
        } else {
            hour = 11; // 20%
        }
        setTimeAndStage(currentTime.getDay() + 1, hour, 0, 0, EStage.AgentMoving);
        appendToDebugInfo("/next time = " + hour, debugFlag);
    }
}
```

### TRuleOfMoveFromCompanyToHome:会社から自宅に移動するルール

sample03と同じ．

`TRuleOfMoveFromCompanyToHome.java`

```Java
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
        // エージェントが会社にいるならば，自宅に移動する．
        boolean debugFlag = true;
        TRoleOfFather role = (TRoleOfFather) getOwnerRole();
        if (isAt(role.getCompany())) {
            moveTo(role.getHome());
            appendToDebugInfo("success", debugFlag);
        } else {
            appendToDebugInfo("fail", debugFlag);
        }
    }
}
```

## 役割の定義
### TRoleOfFather:父親役割

sample03と同じ．

`TRoleOfFather.java`

```Java
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

        // 自宅から会社に移動するルール．初日の9時(50%)，10時(30%)，11時(20%)/エージェント移動ステージに臨時実行ルールとして予約する．
        // 初日以降は，ルール自身が臨時実行ルールとして再予約する．
        double p = getRandom().nextDouble();
        int hour;
        if (p <= 0.5) {
            hour = 9; // 50%
        } else if (p <= 0.8) {
            hour = 10; // 30%
        } else {
            hour = 11; // 20%
        }
        new TRuleOfStochasticallyMoveFromHomeToCompany(RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY, this,
                ruleOfReturnHome, "8:00:00", EStage.AgentMoving)
                .setTimeAndStage(0, hour, 0, 0, EStage.AgentMoving);
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

sample03のメインクラスのログ出力ディレクトリを変更する．
また，スポットログに曜日を出力するように変更する．

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

        // マスター乱数発生器のシード値設定
        long seed = 0L;
        builder.setRandomSeed(seed);

        // ルールログとランタイムログの出力設定
        String pathOfLogDir = "logs" + File.separator + "tutorials" + File.separator + "sample04";
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
        //   - Home:Home1, Home2, Home3
        //   - Company:Company
        // *************************************************************************************************************

        int noOfHomes = 3; // 家の数
        List<TSpot> homes = spotManager.createSpots(ESpotType.Home, noOfHomes);
        TSpot company = spotManager.createSpot(ESpotType.Company);

        // *************************************************************************************************************
        // エージェント作成
        //   - Father:Father1, Father2, Father3
        //     - 初期スポット:Home
        //     - 役割:父親役割
        // *************************************************************************************************************

        int noOfFathers = noOfHomes; // 父親の数は家の数と同じ．
        List<TAgent> fathers = agentManager.createAgents(EAgentType.Father, noOfFathers);
        for (int i = 0; i < noOfFathers; ++i) {
            TAgent father = fathers.get(i); // i番目の父親エージェント
            TSpot home = homes.get(i); // i番目の父親エージェントの自宅
            father.initializeCurrentSpot(home); // 初期スポットを自宅に設定
            new TRoleOfFather(father, home, company); // 父親役割を生成
            father.activateRole(ERoleName.Father); // 父親役割をアクティブ化
        }

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        //   - スポットログ:各時刻での各エージェントの現在位置ログ
        // *************************************************************************************************************

        // スポットログ用PrintWriter
        PrintWriter spotLogPW = new PrintWriter(new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "spot_log.csv")));
        // スポットログのカラム名出力
        spotLogPW.print("CurrentTime,Day");
        for (TAgent father : fathers) {
            spotLogPW.print(',');
            spotLogPW.print(father.getName());
        }
        spotLogPW.println();

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
            spotLogPW.print(",");
            spotLogPW.print(EDay.values()[ruleExecutor.getCurrentTime().getDay() % 7]);
            for (TAgent father : fathers) {
                spotLogPW.print(',');
                spotLogPW.print(father.getCurrentSpotName());
            }
            spotLogPW.println();
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown();
        spotLogPW.close();
    }
}
```


前：
次：
TODO:
