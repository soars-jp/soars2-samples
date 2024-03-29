前：[SOARSの基本概念](../sample00/)

次：[sample02:臨時実行ルールによる相対時刻指定](../sample02/)


# sample01:最も簡単なプログラム <!-- omit in toc -->

sample01では，最も単純で基本的なモデルでSOARS Toolkitを利用した
シミュレーションプログラムの構築方法について解説する．


- [シナリオとシミュレーション条件](#シナリオとシミュレーション条件)
- [SOARSシミュレーションの雛形](#soarsシミュレーションの雛形)
- [シミュレーションモデル構築 step1](#シミュレーションモデル構築-step1)
- [シミュレーションモデル構築 step2](#シミュレーションモデル構築-step2)
- [シミュレーションモデル構築 step3](#シミュレーションモデル構築-step3)
  - [TRoleOfFather:父親役割](#troleoffather父親役割)
  - [TRuleOfMoveFromHomeToCompany:自宅から会社に移動するルール](#truleofmovefromhometocompany自宅から会社に移動するルール)
  - [TRuleOfMoveFromCompanyToHome:会社から自宅に移動するルール](#truleofmovefromcompanytohome会社から自宅に移動するルール)
  - [メインクラス](#メインクラス)
- [シミュレーションモデル構築 step4](#シミュレーションモデル構築-step4)


## シナリオとシミュレーション条件

以下のシナリオを考える．

- 3人の父親(Father1, Father2, Father3)は，それぞれ自宅(Home1, Home2, Home3)を持つ．
- 父親は，9時に自宅から同じ会社(Company)に移動する．
- 父親は，17時にそれぞれの自宅に移動する．

シミュレーション条件

- エージェント : Father(3)
- スポット : Home(3), Company(1)
- ステージ : AgentMoving
- 時刻ステップ間隔：1時間 / step
- シミュレーション期間：7日間

## SOARSシミュレーションの雛形

SOARSシミュレーションのメイン文での基本的な流れは以下のようになる．
- TSOARSBuilderを作成し，シミュレーションに必要なクラスのインスタンスを作成する．
- スポットとエージェントを作成する．
- 終了時刻までルールを順番に実行する．
- 終了処理．

TSOARSBuilderで作成されるSOARSシミュレーションに必須のクラスは以下の通り．
- TRuleExecutor : ルール実行器．ルールを実行する．シミュレーションの現在時刻，現在ステージなどの情報を持つ．
- TAgentManager : エージェント管理．シミュレーションに存在するすべてのエージェントを管理する．シミュレーションに存在するエージェント数の取得，エージェントの検索などができる．
- TSpotManager : スポット管理．シミュレーションに存在するすべてのスポットを管理する．シミュレーションに存在するスポット数の取得，スポットの検索などができる．
- ICRandom : マスター乱数発生器．
- Map<String, Object> : グローバル共有変数集合．すべてのオブジェクトのルール間で共有しておきたい変数を保持する．

これらのクラスのインスタンスはシミュレーションモデル中で唯一である．
以下に，SOARS Toolkitを用いたシミュレーションのメインクラスの雛形を示す．

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

        String simulationStart = "dd/hh:mm:ss";
        String simulationEnd = "dd/hh:mm:ss";
        String tick = "hh:mm:ss";
        List<Enum<?>> stages = List.of();
        Set<Enum<?>> agentTypes = new HashSet<>();
        Set<Enum<?>> spotTypes = new HashSet<>();
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes);

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
        // *************************************************************************************************************

        // *************************************************************************************************************
        // エージェント作成
        // *************************************************************************************************************

        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        // 1ステップ分のルールを実行 (ruleExecutor.executeStage()で1ステージ毎に実行することもできる)
        // 実行された場合:true，実行されなかった(終了時刻)場合は:falseが帰ってくるため，while文で回すことができる．
        while (ruleExecutor.executeStep()) {
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown();
    }
}
```

## シミュレーションモデル構築 step1

sample01のシミュレーション条件に従ってシミュレーションに必要な定数を定義する．

sample01に必要なエージェントは父親のみである．

`EAgentType.java`

```Java
public enum EAgentType {
    /** 父親 */
    Father
}
```

sample01に必要なスポットは父親の自宅と会社である．

`ESpotType.java`

```Java
public enum ESpotType {
    /** 自宅 */
    Home,
    /** 会社 */
    Company
}
```

sample01に必要なステージはエージェント移動ステージである．
自宅から会社，会社から自宅への移動はそれぞれ9時と17時に実行されるため，1つのステージで競合することなく定義することができる．

`EStage.java`

```Java
public enum EStage {
    /** エージェント移動ステージ */
    AgentMoving
}
```

sample01に必要な役割名は父親役割のみである．

`ERoleName.java`

```Java
public enum ERoleName {
    /** 父親役割 */
    Father
}
```

以上のシミュレーションに必要な定数とシミュレーションの開始時刻，終了時刻，tick(1stepの時間間隔)を定義したメインクラスは以下のようになる．

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
        // *************************************************************************************************************

        // *************************************************************************************************************
        // エージェント作成
        // *************************************************************************************************************

        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        // 1ステップ分のルールを実行 (ruleExecutor.executeStage()で1ステージ毎に実行することもできる)
        // 実行された場合:true，実行されなかった(終了時刻)場合は:falseが帰ってくるため，while文で回すことができる．
        while (ruleExecutor.executeStep()) {
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown();
    }
}
```

## シミュレーションモデル構築 step2

次に，TAgentManager，TSpotManagerを使用してシミュレーションに必要なエージェントとスポットを生成する．
それぞれ，createAgents，createSpotsメソッドを利用する．
メインクラスは以下のようになる．

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
        // *************************************************************************************************************

        int noOfFathers = noOfHomes; // 父親の数は家の数と同じ．
        List<TAgent> fathers = agentManager.createAgents(EAgentType.Father, noOfFathers);

        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        // 1ステップ分のルールを実行 (ruleExecutor.executeStage()で1ステージ毎に実行することもできる)
        // 実行された場合:true，実行されなかった(終了時刻)場合は:falseが帰ってくるため，while文で回すことができる．
        while (ruleExecutor.executeStep()) {
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown();
    }
}
```

## シミュレーションモデル構築 step3

次に父親役割とエージェント移動ルールを定義し，メインクラスで父親エージェントに父親役割を設定する．

### TRoleOfFather:父親役割

役割はTRoleを継承する．
父親役割は，自宅と会社の情報をもち，ルールとして自宅から会社に移動するルール(TRuleOfMoveFromHomeToCompany)と，
会社から自宅に移動するルール(TRuleOfMoveFromCompanyToHome)を登録する．
TRuleOfMoveFromHomeToCompanyは毎日9時のエージェント移動ステージに実行されるように予約され，
TRuleOfMoveFromCompanyToHomeは毎日17時のエージェント移動ステージに実行されるように予約される．

`TRoleOfFather.java`

```Java
public final class TRoleOfFather extends TRole {

    /** 自宅 */
    private final TSpot fHome;

    /** 会社 */
    private final TSpot fCompany;

    /** 自宅から会社に移動するルール名 */
    public static final String RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY = "MoveFromHomeToCompany";

    /** 会社から自宅に移動するルール名 */
    public static final String RULE_NAME_OF_MOVE_FROM_COMPANY_TO_HOME = "MoveFromCompanyToHome";

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
        // 自宅から会社に移動するルール．9:00:00/エージェント移動ステージに定時実行ルールとして予約する．
        new TRuleOfMoveFromHomeToCompany(RULE_NAME_OF_MOVE_FROM_HOME_TO_COMPANY, this)
                .setTimeAndStage(9, 0, 0, EStage.AgentMoving);

        // 会社から自宅に移動するルール．17:00:00/エージェント移動ステージに定時実行ルールとして予約する．
        new TRuleOfMoveFromCompanyToHome(RULE_NAME_OF_MOVE_FROM_COMPANY_TO_HOME, this)
                .setTimeAndStage(17, 0, 0, EStage.AgentMoving);
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

### TRuleOfMoveFromHomeToCompany:自宅から会社に移動するルール

ルールはTRuleまたはTAgentRuleを継承し，親クラスのdoItメソッドをオーバーライドして，そこにルールの動作を定義する．
doItメソッドの引数は現在時刻，現在ステージ，スポット管理，エージェント管理，グローバル共有変数集合である．
スポット管理，エージェント管理，グローバル共有変数集合はメインクラスでTSOARSBuilderから受け取ったインスタンスと同じものである．
これにより，ルールはシミュレーション中に存在する他のエージェントやスポットを参照することができる．

TRuleOfMoveFromHomeToCompanyは自宅から会社に移動するルールである．
自宅と会社の情報は父親役割が持っており，そこから取得する．
取得したい役割がこのルールを持っている役割の場合には，getOwnerRole()メソッドで取得できる．
また，getRole(Enum roleName)メソッドでこのルールを持っている大元のオブジェクトに登録されている任意の役割を取得することができる．

TRuleOfMoveFromHomeToCompanyはTAgentRuleを継承することにより，エージェントの移動を実行する．
TAgentRuleはエージェント専用のメソッドが用意されており，現在地を判定するisAtや，スポットを移動するmoveToなどが実装されている．

※appendToDebugInfoメソッドは後述するルールログにデバッグ情報を出力するメソッドで，
第1引数がデバッグ情報文字列，第2引数はデバッグ情報を出力するか否かの制御boolean．

`TRuleOfMoveFromHomeToCompany.java`

```Java
public final class TRuleOfMoveFromHomeToCompany extends TAgentRule {

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     */
    public TRuleOfMoveFromHomeToCompany(String name, TRole owner) {
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
        // エージェントが自宅にいるならば，会社に移動する．
        // getOwnerRole()メソッドはこのルールを持っている役割を取得する．
        // appendToDebugInfoはルールログにユーザー定義のデバッグ情報を出力する．
        // 第1引数はデバッグ情報文字列，第2引数はデバッグ情報を出力するか否かの制御boolean．
        boolean debugFlag = true;
        TRoleOfFather role = (TRoleOfFather) getOwnerRole();
        if (isAt(role.getHome())) {
            moveTo(role.getCompany());
            appendToDebugInfo("success", debugFlag);
        } else {
            appendToDebugInfo("fail", debugFlag);
        }
    }
}
```

### TRuleOfMoveFromCompanyToHome:会社から自宅に移動するルール

TRuleOfMoveFromCompanyToHomeは会社から自宅に移動するルールである．
内容はTRuleOfMoveFromHomeToCompanyとほぼ同様である．

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
        // getOwnerRole()メソッドはこのルールを持っている役割を取得する．
        // appendToDebugInfoはルールログにユーザー定義のデバッグ情報を出力する．
        // 第1引数はデバッグ情報文字列，第2引数はデバッグ情報を出力するか否かの制御boolean．
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

### メインクラス

父親役割を父親エージェントに設定して，ルールが実行されるように父親役割をアクティブ化する．
また，父親エージェントの初期位置を自宅に設定する．
メインクラスは以下のようになる．

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
            new TRoleOfFather(father, home, company); // 父親役割を作成
            father.activateRole(ERoleName.Father); // 父親役割をアクティブ化
        }

        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        // 1ステップ分のルールを実行 (ruleExecutor.executeStage()で1ステージ毎に実行することもできる)
        // 実行された場合:true，実行されなかった(終了時刻)場合は:falseが帰ってくるため，while文で回すことができる．
        while (ruleExecutor.executeStep()) {
        }

        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown();
    }
}
```

## シミュレーションモデル構築 step4

シミュレーション自体はstep3までで完成しているが，シミュレーションの結果を受け取ったりデバッグ情報を出力するための設定をする必要がある．
それらの設定をしたメインクラスと変更点を以下に示す．

- マスター乱数発生器のシード値設定：
  - シミュレーション中で使用するすべての乱数発生器のシード値はマスター乱数発生器によって生成されており，マスター乱数発生器のシード値を設定することで全てのシード値固定ができる．
- ログ出力設定：
  - SOARS Toolkitにはルールログとランタイムログの出力機能がある．ルールログはルールの登録，実行，削除とその時の状態のログ，ランタイムログは各ステージの登録ルール数，実行ルール数，実行時間のログである．
- ルールログのデバッグ情報出力設定：
  - ルールクラスのdoItメソッドで使用したappendToDebugInfoメソッドのデバッグ情報を出力するかを制御する．LOCALはappendToDebugInfoの第2引数の設定に従う．このほかに強制出力のON，強制非出力のOFFがある．
- スポットログの出力設定：
  - シミュレーションモデルが正常に機能しているかを確かめるために各時刻におけるエージェントの現在地をスポットログとして出力する．このログはユーザーが独自に定義するものであり，ライブラリの機能によるものではない．

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
        String pathOfLogDir = "logs" + File.separator + "tutorials" + File.separator + "sample01";
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
            new TRoleOfFather(father, home, company); // 父親役割を作成
            father.activateRole(ERoleName.Father); // 父親役割をアクティブ化
        }

        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        //   - スポットログ:各時刻での各エージェントの現在位置ログ
        // *************************************************************************************************************

        // スポットログ用PrintWriter
        PrintWriter spotLogPW = new PrintWriter(new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "spot_log.csv")));
        // スポットログのカラム名出力
        spotLogPW.print("CurrentTime");
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


前：[SOARSの基本概念](../sample00/)

次：[sample02:臨時実行ルールによる相対時刻指定](../sample02/)
