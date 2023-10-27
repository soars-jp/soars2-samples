次：[sample02:列車への乗車 (相対時刻指定)](../sample02/)

# sample01:列車への乗車 (絶対時刻指定)
sample01では，onolab-simple-transportation-moduleを用いた絶対時刻指定による列車への乗車シミュレーションモデルの構築方法について解説する．

- [シナリオとシミュレーション条件](#シナリオとシミュレーション条件)
- [駅，路線，列車の運行スケジュール](#駅，路線，列車の運行スケジュール)
- [シミュレーション定数の定義](#シミュレーション定数の定義)
- [ルールの定義](#ルールの定義)
    - [TRuleOfAgentMoving:エージェント移動ルール](#truleofagentmovingエージェント移動ルール)
- [役割の定義](#役割の定義)
    - [TRoleOfPather:父親役割](#troleofpather父親役割)
- [メインクラスの定義](#メインクラスの定義)

## シナリオとシミュレーション条件
以下のシナリオを考える．

- 1人の父親(Father)は自宅(Home)を持つ．
- 以下のシナリオによって，父親は自宅から会社に移動する．
    - 父親は，6:55に駅(station2)へ向けて自宅を出発する．
    - 父親は，7:00に駅(station2)に到着する．
    - 父親は，7:05に駅(station2)で，line1線上り方面(inbound)の列車(001)に乗車する．
    - 父親は，7:30に駅(station8)で，line1線上り方面(inbound)の列車(001)から降車する．
    - 父親は，7:33に会社(company)に向けて駅(station8)を出発する．
    - 父親は，7:43に会社に到着する．
- 以下のシナリオによって，父親は会社から自宅に移動する．
    - 父親は，17:55に駅(station8)へ向けて会社を出発する．
    - 父親は，18:05に駅(station8)に到着する．
    - 父親は，18:10に駅(station8)で，line1線下り方面(outbound)の列車(002)に乗車する．
    - 父親は，18:35に駅(station2)で，line1線下り方面(outbound)の列車(002)から降車する．
    - 父親は，18:40に自宅に向けて駅(station2)を出発する．
    - 父親は，18:45に自宅に到着する．
- line1線の上り方面(inbound)の列車(001)の運行スケジュールは以下の通りである．
    | Station | 001 |
    |---------|------|
    | station1 | 7:00 |
    | station2 | 7:05 |
    | station3 | 7:08 |
    | station4 | 7:13 |
    | station5 |7:17 |
    | station6 | 7:20 |
    | station7 | 7:26 |
    | station8 | 7:30 |
    | station9 | 7:34 |
    | station10 | 7:40 |

- line1線下り方面(outbound)の列車(001と002)の運航スケジュールは以下の通りである．
    | Station | 001 | 002 |
    |---------|-----|-----|
    | station10 | 6:30 | 18:00 |
    | station9 | 6:36 | 18:06 |
    | station8 | 6:40 | 18:10 |
    | station7 | 6:44 | 18:14 |
    | station6 | 6:50 | 18:20 |
    | station5 | 6:53 | 18:23 |
    | station4 | 6:57 | 18:27 |
    | station3 | 7:02 | 18:32 |
    | station2 | 7:05 | 18:35 |
    | station1 | 7:10 | 18:40 |

シミュレーション条件

- エージェント : Father(1)
- スポット : Home(1)，Company(1)，Station(10)，Transportation(3)
- ステージ : AgentMoving
- 時刻ステップ間隔：1分 / step
- シミュレーション期間：2日間

## 駅，路線，列車の運行スケジュール
駅，路線，列車の運行スケジュールの情報は，transportationDBディレクトリの下に収められている．ファイル，ディレクトリはそれぞれ以下の通りである．
- stations.csv <br>
    駅名
- lines.csv <br>
路線名
- line1ディレクトリ <br>
路線名がline1である路線の運行情報が収められている．lines.csv中の名前と一致している必要がある．
- line1/transportations.csv <br>
line1線の各列車の情報として，路線名（Line），方面（Direction），列車名（TrainName），列車タイプ（Type），始発駅（Source），始発駅の出発時刻（DepartureTime），終着駅（Destination），終着駅への到着時刻（ArrivalTime）が収められている．
- line1/inboundディレクトリ<br>
line1線上り方面（inbound）方面の各列車の運行情報のCSVファイルが収められている．ここで，inboundは上記transportations.csv中のDirectionの名前と一致している必要がある．
- line1/inbound/001.csv <br>
line1線上り方面（inbound）方面の列車001の運行スケジュールである．各駅の出発時刻と到着時刻が記載されている．ここで，駅の到着時刻と出発時刻，駅の出発時刻と次の駅の到着時刻の整合性を取る必要がある．ただし，駅の到着時刻と出発時刻は同時刻でも良い．
- line1/outboundディレクトリ<br>
line1線下り方面（outbound）方面の各列車の運行情報のCSVファイルが収められている．ここで，outboundは上記transportations.csv中のDirectionの名前と一致している必要がある．
- line1/outbound/001.csv, 002.csv<br>
line1線下り方面（outbound）方面の列車001, 002の運行スケジュールである．各駅の出発時刻と到着時刻が記載されている．ここで，駅の到着時刻と出発時刻，駅の出発時刻と次の駅の到着時刻の整合性を取る必要がある．ただし，駅の到着時刻と出発時刻は同時刻でも良い．

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
        // 親クラスのコンストラクタを呼び出す．
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
                getRule(fNextRule).setTimeAndStage(fNextTime.getDay(), fNextTime.getHour(), fNextTime.getMinute(), fNextTime.getSecond(), fStageOfNextRule);
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
[上記のシナリオ](#シナリオとシミュレーション条件)に従って，父親役割を定義する．

ここで，「列車に乗る」ルールとして，onolab.simple.transportation.TRuleOfGettingOnTransportationクラス，「列車から降りる」ルールをとして，onolab.simple.transportation.TRuleOfGettingOffTransportationクラスが用意されているので，これらを利用する．

onolab.simple.transportation.TRuleOfGettingOnTransportationのコンストラクタの引数の意味は以下の通りである．

- name:ルール名
- owner:このルールを持つ役割
- stationSpot:乗車駅スポット
- line:乗車路線
- direction:乗車方面
- transportationName:車両名

乗車時刻とステージは，setTimeAndStageメソッドで設定する．
路線，駅，時刻，乗車方面，車両名は，transportatiuonDBディレクトリ下で定義されているものを指定する必要があることに注意されたい．

onolab.simple.transportation.TRuleOfGettingOffTransportationのコンストラクタの引数の意味は以下の通りである．

- name:ルール名
- owner:このルールを持つ役割
- stationSpot:降車駅スポット
- line:乗車路線
- direction:乗車方面
- transportationName:車両名

降車時刻とステージは，setTimeAndStageメソッドで設定する．

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

        public TRoleOfFather(TAgent owner, TSpot home, TSpot company, TSpot mid, TSpot srcStationSpot, TSpot dstStationSpot, String line) {
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
                String trainName = "001"; // 乗車する列車名
                // 6:55に自宅を出発して乗車駅に向かう．
                new TRuleOfAgentMoving(LEAVE_HOME, this, fHome, fMid).setTimeAndStage(6, 55, 0, EStage.AgentMoving);
                // 7:00に乗車駅に到着する
                new TRuleOfAgentMoving(REACH_STATION, this, fMid, fSrcStationSpot).setTimeAndStage(7, 0, 0,
                                EStage.AgentMoving);
                // 7:05に電車にのる
                new TRuleOfGettingOnTransportation(GETON_TRANSPORTATION, this, fSrcStationSpot, fLine, direction,
                                trainName).setTimeAndStage(7, 5, 0, ETransportationStage.TransportationArriving);
                // 7:30に電車から降りる
                new TRuleOfGettingOffTransportation(GETOFF_TRANSPORTATION, this, fDstStaionSpot, fLine, direction,
                                trainName).setTimeAndStage(7, 30, 0, ETransportationStage.TransportationLeaving);
                // 7:33に降車駅を出発して会社に向かう．
                new TRuleOfAgentMoving(GO_COMPANY, this, fDstStaionSpot, fMid).setTimeAndStage(7, 33, 0,
                                EStage.AgentMoving);
                // 7:43に会社に到着する
                new TRuleOfAgentMoving(REACH_COMPANY, this, fMid, fCompany).setTimeAndStage(7, 43, 0,
                                EStage.AgentMoving);
        }

        /**
         * 退勤ルールを登録
         */
        private void moveFromCompanyToHome() {
                String direction = "outbound"; // 乗車する列車の方面
                String trainName = "002"; // 乗車する列車名
                // 17:55に会社を出発して乗車駅に向かう．
                new TRuleOfAgentMoving(LEAVE_COMPANY, this, fCompany, fMid).setTimeAndStage(17, 55, 0,
                                EStage.AgentMoving);
                // 18:05に乗車駅に到着する．
                new TRuleOfAgentMoving(REACH_STATION_BACK, this, fMid, fDstStaionSpot).setTimeAndStage(18, 5, 0,
                                EStage.AgentMoving);
                // 18:10に乗車駅で指定された列車に乗車する．
                new TRuleOfGettingOnTransportation(GETON_TRANSPORTATION_BACK, this, fDstStaionSpot, fLine, direction,
                                trainName).setTimeAndStage(18, 10, 0, ETransportationStage.TransportationArriving);
                // 18:35に降車駅で列車から降車する．
                new TRuleOfGettingOffTransportation(GETOFF_TRANSPORTATION_BACK, this, fSrcStationSpot, fLine, direction,
                                trainName).setTimeAndStage(18, 35, 0, ETransportationStage.TransportationLeaving);
                // 18:40に降車駅を出発して自宅に向かう
                new TRuleOfAgentMoving(GO_HOME, this, fSrcStationSpot, fMid).setTimeAndStage(18, 40, 0,
                                EStage.AgentMoving);
                // 18:45に自宅に到着する．
                new TRuleOfAgentMoving(REACH_HOME, this, fMid, fHome).setTimeAndStage(18, 45, 0, EStage.AgentMoving);
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

メインクラスでは，transportationDBに格納してある駅と乗り物のスポットを生成する．
駅スポットと乗り物スポットの生成は，onolab.simple.transportation.TTransportationAndStationManagerクラスで行う．
コンストラクタの引数の意味は以下の通りである．

- transportationDBDirectory:transportationDBまでのパス
- spotManager:スポット管理
- transportationEnum:乗り物スポットタイプ
- stationEnum:駅スポットタイプ
- checkscheduleFlag:時刻表の整合性をチェックするかのフラグ．デフォルトではチェックしない．

また，スポットログに加え，transportationのログも出力している．

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
                + "sample01";
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

        TSpot home = spotManager.createSpot(ESpotType.Home);
        TSpot company = spotManager.createSpot(ESpotType.Company);
        TSpot mid = spotManager.createSpot(ESpotType.Mid);

        // *************************************************************************************************************
        // 駅スポットと乗り物スポット作成
        // *************************************************************************************************************

        String PathOfTransportationLogDir = "src/main/java/jp/soars/onolab/transportation/transportationDB";
        TTransportationAndStationManager transportationAndStationManager = new TTransportationAndStationManager(
                PathOfTransportationLogDir, spotManager, ESpotType.Transportation,
                ESpotType.Station);

        // *************************************************************************************************************
        // エージェント作成
        // - Father:Father
        // - 初期スポット:Home
        // - 役割:父親役割
        // *************************************************************************************************************

        TAgent father = agentManager.createAgent(EAgentType.Father);
        TSpot srcStationSpot = spotManager.getSpotDB().get("station2");
        TSpot dstStationSpot = spotManager.getSpotDB().get("station8");
        father.initializeCurrentSpot(home);// 初期スポットを自宅に設定
        new TRoleOfFather(father, home, company, mid, srcStationSpot, dstStationSpot, "line1");
        father.activateRole(ERoleName.Father);

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        // - スポットログ:各時刻での各エージェントの現在位置ログ
        // *************************************************************************************************************

        // スポットログ用PrintWriter
        PrintWriter spotLogPW = new PrintWriter(
                new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "spot_log.csv")));
        // スポットログのカラム名出力
        spotLogPW.print("CurrentTime");
        spotLogPW.print(',');
        spotLogPW.print(father.getName());
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
            spotLogPW.print(',');
            spotLogPW.print(father.getCurrentSpotName());
            spotLogPW.println();

            // Transportationログ出力
            transportationLogPW.print(ruleExecutor.getCurrentTime());
            for (TSpot transportation : transportationAndStationManager.getTransportationSpotList()) {
                transportationLogPW.print(',');
                if (transportationAndStationManager.isTransportationInService(transportation)) {
                    transportationLogPW.print(transportationAndStationManager.getTransportationLocation(transportation));
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

次：[sample02:列車への乗車 (相対時刻指定)](../sample02/)
