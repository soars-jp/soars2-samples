前：[sample01:列車への乗車 (絶対時刻指定)](../sample01/)

# sample02:列車への乗車 (相対時刻指定)
sample02では，onolab-simple-transportation-moduleを用いた相対時刻指定による列車への乗車シミュレーションモデルの構築方法について解説する．


- [シナリオとシミュレーション条件](#シナリオとシミュレーション条件)
- [シミュレーション定数の定義](#シミュレーション定数の定義)
- [ルールの定義](#ルールの定義)
    - [TRuleOfAgentMoving:エージェント移動ルール](#truleofagentmovingエージェント移動ルール)
- [役割の定義](#役割の定義)
    - [TRoleOfPather:父親役割](#troleofpather父親役割)
- [メインクラスの定義](#メインクラスの定義)

## シナリオとシミュレーション条件
以下のシナリオを考える．

- 5人の父親(Father1, Father2, Father3, Father4, Father5)はそれぞれ自宅(Home1, Home2, Home3, Home4, Home5)を持つ．
- 以下のシナリオによって，父親は自宅から同じ会社に移動する．
    - 父親は，6:55に父親ごとに設定された駅へ向けて自宅を出発する．
    - 父親は，5分後に父親ごとに設定された駅に到着する．
    - 父親は，line1線上り方面(inbound)の最初に来た列車に乗車する．
    - 父親は，駅(station8)で列車から降車する．
    - 父親は，3分後に会社(company)に向けて駅(station8)を出発する．
    - 父親は，10分後に会社に到着する．
- 以下のシナリオによって，父親は会社からそれぞれの自宅に移動する．
    - 父親は，17:55に駅(station8)に向けて会社を出発する．
    - 父親は，10分後に駅(station8)に到着する．
    - 父親は，line1下り方面(outbound)の最初に来た列車に乗車する．
    - 父親は，父親ごとに設定された駅で列車から降車する．
    - 父親は，5分後に自宅に向けて父親ごとに設定された駅を出発する．
    - 父親は，5分後に自宅に到着する．
- 時刻表はsample01と同様である．

シミュレーション条件

- エージェント : Father(5)
- スポット : Home(5)，Company(1)，Station(10)，Transportation(3)
- ステージ : AgentMoving
- 時刻ステップ間隔：1分 / step
- シミュレーション期間：2日間

## シミュレーション定数の定義

`EAgentType.java`

```Java
public enum EAgentType {
    /** 父親 */
    Father,
}
```

`ESpotType.java`

```Java
public enum ESpotType {
    /** 自宅 */
    Home,
    /** 出発地と目的地の中間スポット */
    Mid,
    /** 会社 */
    Company,
    /** 乗り物 */
    Transportation,
    /** 駅 */
    Station,
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

### TRuleOfAgentMoving:エージェント移動ルール
エージェント移動ルールは出発地と目的地をコンストラクタで受け取ることで，2スポット間の移動を定義する．

`TRuleOfAgentMoving.java`

```Java
public final class TRuleOfAgentMoving extends TAgentRule {

    /** 出発地 */
    private final TSpot fSource;

    /** 目的地 */
    private final TSpot fDestination;

    /** 次のルールを実行するまでの時間 */
    private TTime fTimeToNextRule;

    /** 次のルールを実行するステージ */
    private Enum<?> fStageOfNextRule;

    /** 次に実行するルール名 */
    private String fNextRule;

    /** 次の実行時刻を計算するためのワークメモリ */
    private TTime fNextTime;

    /**
     * コンストラクタ
     *
     * @param name        ルール名
     * @param owner       このルールをもつ役割
     * @param source      出発地
     * @param destination 目的地
     */
    public TRuleOfAgentMoving(String name, TRole owner, TSpot source, TSpot destination) {
        super(name, owner);
        fSource = source;
        fDestination = destination;
        fTimeToNextRule = null;
        fStageOfNextRule = null;
        fNextRule = null;
    }

    /**
     * コンストラクタ
     *
     * @param name        ルール名
     * @param owner       このルールをもつ役割
     * @param source      出発地
     * @param destination 目的地
     */
    public TRuleOfAgentMoving(String name, TRole owner, TSpot source, TSpot destination, TTime timeToNextRule,
            Enum<?> stageOfNextRule, String nextRule) {
        // 親クラスのコンストラクタを呼び出す．
        super(name, owner);
        fSource = source;
        fDestination = destination;
        fTimeToNextRule = timeToNextRule;
        fStageOfNextRule = stageOfNextRule;
        fNextRule = nextRule;
        fNextTime = new TTime();

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
        // エージェントが出発地にいるならば，目的地に移動する．
        boolean debugFlag = true;
        if (isAt(fSource)) {
            moveTo(fDestination); // 目的地へ移動する．
            if (fNextRule != null) {// 次に実行するルールが定義されていたら
                fNextTime.copyFrom(currentTime).add(fTimeToNextRule);
                getRule(fNextRule).setTimeAndStage(fNextTime.getDay(), fNextTime.getHour(), fNextTime.getMinute(),
                        fNextTime.getSecond(), fStageOfNextRule);
            }
            appendToDebugInfo("success", debugFlag);
        } else {
            appendToDebugInfo("fail", debugFlag);
        }
    }

}
```

## 役割の定義

### TRoleOfPather:父親役割
sample01と同様に，「列車に乗る」ルールとしてonolab.simple.transportation.TRuleOfGettingOnTransportationクラス，「列車から降りる」ルールとしてonolab.simple.transportation.TRuleOfGettingOffTransportationクラスを利用する．
ただし，駅に到着後，路線条件・方面条件・列車タイプ条件・行き先条件を満たす最初に来た列車に乗車する場合のコンストラクタを用いる．

sample01と異なる部分としては「駅に到着する」ルールとして TRuleOfAgentMovingクラスを用いるのではなく，onolab.simple.transportation.TRuleOfAgentMovingStationクラスを利用する．
このクラスは，エージェントが駅へ移動して電車を待つルールが定義されており，登録された乗車ルールは，条件を満たす列車が到着した際に同時刻のAgentMovingステージに登録される．

onolab.simple.transportation.TRuleOfGettingOnTransportationのコンストラクタの引数の意味は以下の通りである．

- name:ルール名
- owner:このルールを持つ役割
- stationSpot:乗車駅スポット
- line:乗車路線
- direction:乗車方面
- transportationTypes:乗車したい列車の列車タイプ
    - 列車タイプには複数のタイプを指定することができる．
    - onolab.simple.transportation.TRuleOfGettingOnTransportation.ANY(="")を指定すると全ての列車タイプとマッチする．
- transportationDestinations:乗車したい列車の行き先
    - 行き先は，transportationDBディレクトリの各路線ディレクトリの下にあるtransportations.csvの中のTransportationTypeに書かれているものを指定する．
    - 行き先には複数の行き先を指定することができる．
    - onolab.simple.transportation.TRuleOfGettingOnTransportation.ANY(="")を指定すると全ての行き先とマッチする．
- stageOfNextRule:降車ルールを実行するステージ
- nextRule:降車ルール名

onolab.simple.transportation.TRuleOfGettingOffTransportationのコンストラクタの引数の意味は以下の通りである．

- name:ルール名
- owner:このルールを持つ役割
- stationSpot:降車駅スポット
- timeToNextRule:次のルールを実行するインターバル
- stageOfNextRule:次に実行するルールを実行するステージ
- nextRule:次に実行するルール名

onolab.simple.transportation.TRuleOfAgentMovingStationのコンストラクタの引数の意味は以下の通りである．

- name:ルール名
- owner:このルールを持つ役割
- source:出発地スポット
- destination:目的地スポット
- stageOfNextRule:次に実行するステージ名
- nextRule:次に実行するルール名

父親役割のソースコードを以下に示す．
コンストラクタから，自宅から会社に列車で移動するルール群を生成するためのmoveFromHomeToCompanyメソッド，および，会社から自宅に列車で移動するルール群を生成するためのmoveFromCompanyToHomeメソッドを呼び出している．

`TRoleOfFather.java`

```Java
public final class TRoleOfFather extends TRole {
        /** 自宅 */
        private final TSpot fHome;

        /** 会社 */
        private final TSpot fCompany;

        /** 出発地と目的地の中間スポット */
        private final TSpot fMid;

        /** 出発駅スポット */
        private final TSpot fSrcStationSpot;

        /** 到着駅スポット */
        private final TSpot fDstStaionSpot;

        /** 路線 */
        private final String fLine;

        /** 家を出発する */
        public static final String LEAVE_HOME = "leave_home";

        /** 家に到着する */
        public static final String REACH_HOME = "reach_home";

        /** 駅に到着する（出勤） */
        public static final String REACH_STATION = "reach_station";

        /** 駅に到着する（帰宅） */
        public static final String REACH_STATION_BACK = "reach_station_back";

        /** 電車に乗る（出勤） */
        public static final String GETON_TRANSPORTATION = "geton_transportation";

        /** 電車に乗る（帰宅） */
        public static final String GETON_TRANSPORTATION_BACK = "geton_transportation_back";

        /** 電車から降りる（出勤） */
        public static final String GETOFF_TRANSPORTATION = "getoff_transportation";

        /** 電車から降りる（帰宅） */
        public static final String GETOFF_TRANSPORTATION_BACK = "getoff_transportation_back";

        /** 駅から会社に向かう */
        public static final String GO_COMPANY = "go_company";

        /** 会社に到着する */
        public static final String REACH_COMPANY = "reach_company";

        /** 会社に出発する */
        public static final String LEAVE_COMPANY = "leave_company";

        /** 家に帰る */
        public static final String GO_HOME = "go_home";

        public TRoleOfFather(TAgent owner, TSpot home, TSpot company, TSpot mid, TSpot srcStationSpot,TSpot dstStationSpot, String line) {
                // 親クラスのコンストラクタを呼び出す．
                // 以下の2つの引数は省略可能で，その場合デフォルト値で設定される．
                // 第3引数:この役割が持つルール数 (デフォルト値 10)
                // 第4引数:この役割が持つ子役割数 (デフォルト値 5)
                super(ERoleName.Father, owner, 12, 0);
                fHome = home;
                fCompany = company;
                fMid = mid;
                fSrcStationSpot = srcStationSpot;
                fDstStaionSpot = dstStationSpot;
                fLine = line;
                moveFromHomeToCompany();
                moveFromCompanyToHome();
        }

        /**
         * 出勤
         */
        private void moveFromHomeToCompany() {
                String direction = "inbound"; // 乗車する列車の方面
                Set<String> trainTypes = Set.of(TRuleOfGettingOnTransportation.ANY); // 乗車する列車の種類の条件：全ての種類の列車に乗る．
                Set<String> trainDestinations = Set.of(TRuleOfGettingOnTransportation.ANY); // 乗車する列車の行き先の条件：全ての行き先の列車に乗る．
                // 6:55に自宅を出発して乗車駅に向かう．
                new TRuleOfAgentMoving(LEAVE_HOME, this, fHome, fMid).setTimeAndStage(6, 55, 0, EStage.AgentMoving);
                // 7:00に乗車駅に到着する
                new TRuleOfAgentMovingStation(REACH_STATION, this, fMid, fSrcStationSpot,
                                ETransportationStage.AgentMoving,GETON_TRANSPORTATION).setTimeAndStage(7, 0, 0, EStage.AgentMoving);
                // 最初に来た電車にのる
                new TRuleOfGettingOnTransportation(GETON_TRANSPORTATION, this, fSrcStationSpot, fLine, direction,
                                trainTypes,trainDestinations, ETransportationStage.AgentMoving, GETOFF_TRANSPORTATION);
                // 電車から降りる
                new TRuleOfGettingOffTransportation(GETOFF_TRANSPORTATION, this, fDstStaionSpot, new TTime(0, 0, 3, 0),
                                EStage.AgentMoving, GO_COMPANY);
                // 7:33に降車駅を出発して会社に向かう．
                new TRuleOfAgentMoving(GO_COMPANY, this, fDstStaionSpot, fMid, new TTime(0, 0, 10, 0),
                                EStage.AgentMoving,REACH_COMPANY);
                // 7:43に会社に到着する
                new TRuleOfAgentMoving(REACH_COMPANY, this, fMid, fCompany);
        }

        /**
         * 退勤ルールを登録
         */
        private void moveFromCompanyToHome() {
                String direction = "outbound"; // 乗車する列車の方面
                Set<String> trainTypes = Set.of(TRuleOfGettingOnTransportation.ANY); // 乗車する列車の種類の条件：全ての種類の列車に乗る．
                Set<String> trainDestinations = Set.of(TRuleOfGettingOnTransportation.ANY); // 乗車する列車の行き先の条件：全ての行き先の列車に乗る．
                // 17:55に会社を出発して乗車駅に向かう．
                new TRuleOfAgentMoving(LEAVE_COMPANY, this, fCompany, fMid, new TTime(0, 0, 10, 0), EStage.AgentMoving,
                                REACH_STATION_BACK).setTimeAndStage(17, 55, 0, EStage.AgentMoving);
                // 18:05に乗車駅に到着する．
                new TRuleOfAgentMovingStation(REACH_STATION_BACK, this, fMid, fDstStaionSpot,
                                ETransportationStage.AgentMoving, GETON_TRANSPORTATION_BACK);
                // 18:10に乗車駅で指定された列車に乗車する．
                new TRuleOfGettingOnTransportation(GETON_TRANSPORTATION_BACK, this, fDstStaionSpot, fLine, direction,
                                trainTypes, trainDestinations, ETransportationStage.AgentMoving, GETOFF_TRANSPORTATION_BACK);
                // 降車駅で列車から降車する．
                new TRuleOfGettingOffTransportation(GETOFF_TRANSPORTATION_BACK, this, fSrcStationSpot,
                                new TTime(0, 0, 5, 0),EStage.AgentMoving, GO_HOME);
                // 降車駅を出発して自宅に向かう．
                new TRuleOfAgentMoving(GO_HOME, this, fSrcStationSpot, fMid, new TTime(0, 0, 5, 0), EStage.AgentMoving, REACH_HOME);
                new TRuleOfAgentMoving(REACH_HOME, this, fMid, fHome);
        }

        /**
         * 自宅を返す．
         *
         * @return 自宅
         */
        public final TSpot getHome() {
                return fHome;
        }

}
```


## メインクラスの定義

sample01との差分は以下のようになる．
- sample01のログ出力ディレクトリを変更する．
- 父親の数と家の数を変更する．
- 父親ごとに駅を設定する．

`TMain.java`

```Java
public class TMain {
    public static void main(String[] args) throws IOException {

        // *************************************************************************************************************
        // TSOARSBuilderの必須設定項目
        // - simulationStart:シミュレーション開始時刻
        // - simulationEnd:シミュレーション終了時刻
        // - tick:1ステップの時間間隔
        // - stages:使用するステージリスト(実行順)
        // - agentTypes:使用するエージェントタイプ集合
        // - spotTypes:使用するスポットタイプ集合
        // *************************************************************************************************************

        String simulationStart = "0/00:00:00";
        String simulationEnd = "2/00:00:00";
        String tick = "0:01:00";
        List<Enum<?>> stages = List.of(ETransportationStage.NewTransportation, EStage.AgentMoving,
                ETransportationStage.TransportationArriving, ETransportationStage.AgentMoving,
                ETransportationStage.TransportationLeaving, ETransportationStage.DeleteTransportation);
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
        String pathOfLogDir = "logs" + File.separator + "onolab" + File.separator + "transportation" + File.separator
                + "sample02";
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
        // - Home:Home
        // - Company:Company
        // - MidWay:Mid
        // *************************************************************************************************************

        int noOfHomes = 5; // 家の数
        List<TSpot> homes = spotManager.createSpots(ESpotType.Home, noOfHomes);
        TSpot company = spotManager.createSpot(ESpotType.Company);
        TSpot mid = spotManager.createSpot(ESpotType.Mid);

        // *************************************************************************************************************
        // 駅スポットと乗り物スポット作成
        // *************************************************************************************************************

        String PathOfTransportationLogDir = "src/main/java/jp/soars/onolab/transportation/transportationDB";
        TTransportationAndStationManager transportationAndStationManager = new TTransportationAndStationManager(
                PathOfTransportationLogDir, spotManager, ESpotType.Transportation, ESpotType.Station, true);

        // *************************************************************************************************************
        // エージェント作成
        // - Father:Father
        // - 初期スポット:Home
        // - 役割:父親役割
        // *************************************************************************************************************

        int noOfFathers = noOfHomes; // 父親の数は家の数と同じ．
        List<TAgent> fathers = agentManager.createAgents(EAgentType.Father, noOfFathers);
        // それぞれの父親はstation2,station3,station4,..から通勤する
        for (int i = 0; i < noOfFathers; ++i) {
            TAgent father = fathers.get(i); // i番目の父親エージェント
            TSpot home = homes.get(i); // i番目の父親エージェントの自宅
            father.initializeCurrentSpot(home); // 初期スポットを自宅に設定
            TSpot srcStationSpot = spotManager.getSpotDB().get("station" + (i + 2));
            TSpot dstStationSpot = spotManager.getSpotDB().get("station8");
            new TRoleOfFather(father, home, company, mid, srcStationSpot, dstStationSpot, "line1");
            father.activateRole(ERoleName.Father);
        }

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        // - スポットログ:各時刻での各エージェントの現在位置ログ
        // *************************************************************************************************************

        // スポットログ用PrintWriter
        PrintWriter spotLogPW = new PrintWriter(
                new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "spot_log.csv")));
        // スポットログのカラム名出力
        spotLogPW.print("CurrentTime");
        for (TAgent father : fathers) {
            spotLogPW.print(',');
            spotLogPW.print(father.getName());
        }
        spotLogPW.println();

        // Transportation用PrintWriter
        PrintWriter transportationLogPW = new PrintWriter(
                new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "transportation_log.csv")));
        // スポットログのカラム名出力
        transportationLogPW.print("CurrentTime");
        for (TSpot transportation : transportationAndStationManager.getTransportationSpotList()) {
            transportationLogPW.print(",");
            transportationLogPW.print(transportation.getName());
        }
        transportationLogPW.println();

        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        // 1ステップ分のルールを実行 (ruleExecutor.executeStage()で1ステージ毎に実行することもできる)
        // 実行された場合:true，実行されなかった(終了時刻)場合は:falseが帰ってくるため，while文で回すことができる．
        while (ruleExecutor.executeStep()) {
            // スポットログ出力
            spotLogPW.print(ruleExecutor.getCurrentTime());
            for (TAgent father : fathers) {
                spotLogPW.print(',');
                spotLogPW.print(father.getCurrentSpotName());
            }
            spotLogPW.println();

            // Transportationログ出力
            transportationLogPW.print(ruleExecutor.getCurrentTime());
            for (TSpot transportation : transportationAndStationManager.getTransportationSpotList()) {
                transportationLogPW.print(',');
                if (transportationAndStationManager.isTransportationInService(transportation)) {
                    transportationLogPW
                            .print(transportationAndStationManager.getTransportationLocation(transportation));
                }
            }
            transportationLogPW.println();
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown();
        spotLogPW.close();
        transportationLogPW.close();
    }
}
```

前：[sample01:列車への乗車 (絶対時刻指定)](../sample01/)
