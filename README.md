# SOARS toolkit ver.2 samples

SOARS toolkit ver.2のサンプルプログラム

- [sample01：最も簡単なプログラム](src/main/java/jp/soars/samples/sample01/)
- [sample02：日を跨ぐ相対時刻指定](src/main/java/jp/soars/samples/sample02/)
- [sample03：確率的なルールの定義](src/main/java/jp/soars/samples/sample03/)
- [sample04：子役割と役割の変更](src/main/java/jp/soars/samples/sample04/)
- [sample05：臨時実行ルールの繰り返し](src/main/java/jp/soars/samples/sample05/)
- [sample06：臨時実行ルールの発火時刻をランダムに設定](src/main/java/jp/soars/samples/sample06/)
- [sample07：スポット・エージェントの動的追加・削除](src/main/java/jp/soars/samples/sample07/)
- [sample08：ルールの並列実行](src/main/java/jp/soars/samples/sample08/)
- [sample09：ステージ実行ルールとグローバル共有変数集合](src/main/java/jp/soars/samples/sample09/)
- [sample10：レイヤー機能](src/main/java/jp/soars/samples/sample10/)
- [sample11：モジュール合成](src/main/java/jp/soars/samples/sample11/)
- [sample12：スポットの定員](src/main/java/jp/soars/samples/sample12/)

## 目次

- [SOARS toolkit ver.2 samples](#soars-toolkit-ver2-samples)
  - [目次](#目次)
  - [ver.1からの変更点](#ver1からの変更点)
    - [並列化機能](#並列化機能)
    - [ステージ実行ルール](#ステージ実行ルール)
    - [モジュール合成機能](#モジュール合成機能)
      - [ステージ名, オブジェクトタイプ, 役割名の定数化(Enum)](#ステージ名-オブジェクトタイプ-役割名の定数化enum)
      - [ステージマージ機能](#ステージマージ機能)
    - [アクティブ役割の仕様変更](#アクティブ役割の仕様変更)
    - [TModelの廃止とBuilderパターンの導入](#tmodelの廃止とbuilderパターンの導入)
    - [時間に秒を追加](#時間に秒を追加)
    - [レイヤー機能](#レイヤー機能)
    - [スポットの定員機能](#スポットの定員機能)
    - [ルールログのデバッグ情報出力の仕様変更](#ルールログのデバッグ情報出力の仕様変更)
    - [ランタイムログの導入](#ランタイムログの導入)
  - [ver.1から移行する場合の注意点](#ver1から移行する場合の注意点)
  - [TSOARSBuilderの使い方](#tsoarsbuilderの使い方)
  - [最適化のための設定項目](#最適化のための設定項目)
    - [TRoleの設定項目](#troleの設定項目)
    - [TAgentManager, TSpotManagerの設定項目](#tagentmanager-tspotmanagerの設定項目)
    - [TSOARSBuilderの設定項目](#tsoarsbuilderの設定項目)
  - [SOARS2 APIs (JavaDoc)](#soars2-apis-javadoc)

## ver.1からの変更点

### 並列化機能

ルールの並列実行のサポート．
ステージごとにルールを並列実行するか逐次実行するかを指定することで，並列化を簡単に実装できる機能を導入した．

### ステージ実行ルール

定時実行ルール，臨時実行ルールのほかにステージ実行ルールを導入する．
ステージ実行ルールは定期実行ステージに登録され，定期実行ステージの設定によって定期的に実行される．
例えば，1時間ごとにルールを実行する定期実行ステージを作成してルールを登録した場合，登録されているルールが1時間ごとに定期的に実行される．(当然役割がアクティブな場合のみ)

### モジュール合成機能

作成した役割，ルールをライブラリモジュールとして公開して他ユーザーが利用するためのモジュール合成機能を導入する．
モジュール合成のために導入された機能は以下の2つ．

#### ステージ名, オブジェクトタイプ, 役割名の定数化(Enum)

ステージ名, オブジェクトタイプ, 役割名をEnum型で定義するように変更．
Enum型は同じ名前を定義したとしても，定義場所が異なれば別物と判定されるため名前解決のために導入．

#### ステージマージ機能

ステージをマージする機能を追加．
あるステージ stage1 に stage2 をマージした場合，シミュレーション中で stage2 を指定しても全て stage1 として解釈され，実行設定なども stage1 の設定で実行される．

### アクティブ役割の仕様変更

- オブジェクトがアクティブ役割を複数持てるように仕様変更．
- この変更に伴って，[ver.1から移行する場合の注意点](#ver1から移行する場合の注意点)があるためそちらを参照．

### TModelの廃止とBuilderパターンの導入

- TModelを廃止し，シミュレーションに必要なインスタンスを作成するためのビルダーパターンを導入する．
- 実際のTSOARSBuilderの使い方については[TSOARSBuilderの使い方](#tsoarsbuilderの使い方), [SOARSサンプルプログラム](https://github.com/soars-jp/soars2-samples)を参照．

### 時間に秒を追加

- TTimeの最小単位を分から秒に変更．
- この変更に伴って，[ver.1から移行する場合の注意点](#ver1から移行する場合の注意点)があるためそちらを参照．

### レイヤー機能

- スポットをレイヤーごとに作成して管理することができる機能．
- 例えば，Real, SNSというレイヤーを作成してそれぞれのレイヤー上にスポットを作成する．エージェントはレイヤーごとに現在スポットをもち，スポットの移動は同じレイヤー上のみで行われる．

### スポットの定員機能

- スポットに定員を設定することができる．
- TSpot.setCapacity(int capacity)メソッドによって設定することができる．
- 定員が設定されているスポットが満員の場合に，エージェントが移動しようとすると移動に失敗する．
- スポットの定員機能を使用する場合の注意点として，ルールの実行順序がある．SOARSライブラリでは，ある時刻・ステージで実行されるルールは実行順序に左右されてはならないという制約がある．しかし，定員のあるスポットの移動はそのスポットからエージェントが先に移動するか，他のスポットからエージェントが先に移動してくるかという順序が発生する．そのため，定員を設けたスポット間の移動は通常の移動とはステージを分け，その移動順にも注意する必要がある．

### ルールログのデバッグ情報出力の仕様変更
- ルールログのデバッグ情報出力はver1ではdebugInfoメソッドをオーバーライドすることで実装していたが，ver2ではappendToDebugInfo(String, boolean)を呼ぶだすように変更された．
- ルールはデバッグ情報文字列を持っており，appendToDebugInfo(String, boolean)はそこに文字列を追記するメソッドである．第2引数は，実際に追記するか否かを指定する．
- デバッグ情報文字列は，ルールログに吐き出されるたびにクリアされるため，ユーザーの文字的なクリアは不要．
- また，builder.setRuleDebugModeでデバッグモードを選択でき，ONで強制出力，OFFで強制非出力，LOCALでローカル設定に従う．

### ランタイムログの導入

- 時刻・ステージごとの実行時間と実行ルール数のログをcsvファイルに出力する．
- TSOARSBuilder.setRuntimeLoggingEnabled(String runtimeLogCsvFilePath)メソッドによって出力の設定ができる．
- csvファイルのカラム名はERuntimeLogKeyで定義される．

## ver.1から移行する場合の注意点

- 時刻に秒が追加されたことに伴う注意点
  - TRule.setTimeAndStageの引数に秒が追加．定時実行ルールはsetTimeAndStage(hour, minute, second, stage)，臨時実行ルールはsetTimeAndStage(day, hour, minute, second, stage)に変更されている．特に，ver.1で臨時実行ルールはsetTimeAndStage(day, hour, minute, stage)で設定するが，この入力はver.2では定時実行ルールになるので注意が必要．
- アクティブ役割の仕様変更に伴う注意点
  - ver.1まではアクティブ役割はオブジェクトに対して1つのみで，複数役割のアクティブ化は子役割で定義する仕様だったが，ver2からはオブジェクトが複数の役割をアクティブ化することができる．これに伴って，ver.1ではある役割をアクティブ化すると今までアクティブだった役割が自動的にディアクティブ化されていたが，ver.2からはユーザーが明示的にディアクティブしなければならない．また，子役割の機能は役割のマージではなく，アクティブフラグの一括管理的立ち位置になっている．


## TSOARSBuilderの使い方

TSOARSBuilderは，設定を入力してbuildメソッドを実行することで，以下のシミュレーション用インスタンスを作成する．

- ルール実行器(TRuleExecutor)
- エージェント管理(TAgentManager)
- スポット管理(TSpotManager)
- マスター乱数発生器(ICRandom)
- グローバル共有変数集合(Map<String, Object>)


基本的な使い方の流れは以下の通り．

- TSOARSBuilderのインスタンスを作成．
- setterメソッドを利用してシミュレーションの設定を入力．
- TSOARSBuilder.build()メソッドを実行して，シミュレーション用インスタンスを作成．
- getterメソッドで作成したインスタンスを取得．
- シミュレーションの実行．
- シミュレーションの実行後に，ルール実行器(TRuleExecutor)のshutdown()メソッドを必ず実行する．

TSOARSBuilderのコンストラクタ

- TSOARSBuilder(int tick, List<Enum<?>> stages, Set<Enum<?>> agentTypes, Set<Enum<?>> spotTypes, Set<Enum<?>> layers, Enum<?> defaultLayer):
- TSOARSBuilder(int tick, List<Enum<?>> stages, Set<Enum<?>> agentTypes, Set<Enum<?>> spotTypes):
  - tick: シミュレーションの時間間隔．単位は秒．例えば，tick=60に設定すると1ステップが60秒(1分)間隔のシミュレーションに設定される．
  - stages: シミュレーションで使用するステージリスト．
  - agentTypes: シミュレーションで使用するエージェントタイプ集合．
  - spotTypes: シミュレーションで使用するスポットタイプ集合．
  - layers: シミュレーションで使用するレイヤー集合．入力しない場合は，コアライブラリで定義されるデフォルトレイヤーのみ．
  - defaultLayer: layersでデフォルトレイヤーとして設定するレイヤー．入力しない場合は，コアライブラリで定義されるデフォルトレイヤー．

インスタンスの作成と取得用メソッド

- build():
  - シミュレーション用の各種インスタンスを作成．
- getRuleExecutor():
  - ルール実行器を取得．
- getAgentManager():
  - エージェント管理を取得．
- getSpotManager():
  - スポット管理を取得．
- getRandom():
  - マスター乱数発生器を取得．
- getGlobalSharedVariableSet():
  - グローバル共有変数集合を取得．



基本的な設定項目

- setRandomSeed(long seed):
  - マスター乱数発生器のシード値を指定する．
- setInitialValueOfGlobalSharedVariableSet(String key, Object initialValue):
  - グローバル共有変数のキーと初期値を設定する．
- setParallelizationStages(int noOfThreads, Enum<?>... stages):
  - [並列化機能](#並列化機能)の導入に基づく設定項目．
  - 引数で指定したステージに登録されているルールを並列に実行する．並列数は第1引数で指定する．
- mergeStages(Enum<?>... stages):
  - [ステージマージ機能](#ステージマージ機能)の導入に基づく設定項目．
  - 第1引数のステージと第2引数以降のステージを同一視し，ルールの登録や実行などで第2引数以降で入力したステージを指定しても第1引数で入力したステージ上で実行される．
  - 並列化や定期実行ステージの設定などは，第1引数で入力したステージの設定に統一される．
- setPeriodicallyExecutedStage(Enum<?> stage, String startTime, String interval):
  - [ステージ実行ルール](#ステージ実行ルール)の導入に基づく設定項目．
  - 引数で指定したステージを定期実行ステージとして登録する．第2,第3引数は定期実行の開始時刻と時間間隔を指定する．
- setRuleLoggingEnabled(String ruleLogCsvFilePath):
- setRuntimeLoggingEnabled(String runtimeLogCsvFilePath):
  - [ランタイムログの導入](#ランタイムログの導入)に基づく設定項目．
  - ルールログ, ランタイムログを出力するように設定する．
- setRulesSortedBeforeExecutedFlag(boolean flag):
- setRulesShuffledBeforeExecutedFlag(boolean flag):
  - ルールを実行前にソート, シャッフルするかを指定する．
  - ソートは再現性を確保して，実装のチェックを行う場合に利用する．
  - シャッフルは並列化機能を利用する場合に，ルールの実行順序で結果が変わらないか確認するために利用する．
  - これらのフラグを同時にtrueにすることはできない．
- setFlagOfCreatingRandomForEachAgent(boolean flagOfCreatingRandomForEachAgent):
- setFlagOfCreatingRandomForEachSpot(boolean flagOfCreatingRandomForEachSpot):
  - 各エージェント, スポットに個別に乱数発生器を持たせるかを指定する．
  - 並列化機能を利用する場合に，乱数発生器のインスタンスが1つしかないと待機が発生するため個別に生成する必要がある．
  - デフォルトでtrueになっているため，個別生成したくない場合に利用する．
- setWarningFlag(boolean flag):
  - 警告メッセージを表示するかを設定する．
  - 警告メッセージを抑制したい場合に使用する．
- setRuleDebugMode(ERuleDebugMode debugMode):
  - ルールログへのデバッグ情報の吐き出し設定をする．
  - ONで強制出力，OFFで強制非出力，LOCALでローカル設定に従う．


## 最適化のための設定項目

以下では，必須設定項目ではないが設定した場合にSOARSシミュレーションの高速化・省メモリ化に繋がる設定項目について解説する．
基本的には，SOARS toolkit ライブラリ内で使用している List や Map の初期サイズを指定するものが多く，
これを適切に設定することで容量拡大に伴うメモリの余分な確保や，参照コピーの発生を最小限に抑えることができる．

### TRoleの設定項目

TRoleのコンストラクタ引数．

- noOfRules: 役割が持つルール数．(10)
- noOfChildRoles: 役割が持つ子役割数．(5)

これらは，それぞれのマップの初期化サイズを指定するパラメータである(括弧内はデフォルト値)．
このとき，実際にはマップの負荷係数を考慮したサイズが設定される．
例えば，役割が持つルール数として10を指定した場合，javaのマップの負荷係数0.75を考慮して実際には14でマップサイズが初期化される．

### TAgentManager, TSpotManagerの設定項目

TAgentManager, TSpotManagerでエージェント，スポットを作成するときにエージェント，スポットが持つ役割数を指定することができる．
また，スポットを作成する場合はさらに，スポットに滞在するエージェント数の最大値の予測値を設定することができる．

### TSOARSBuilderの設定項目

TSOARSBuilderのメソッド．

- setExpectedNoOfRulesPerStage(Enum<?> stage, int expectedNoOfRules):
  - ある時刻のステージ(stage)に登録されるルールの配列の初期サイズを指定する．
  - ルールの配列は，その時刻・ステージにルールが登録されるときに初めて作成される．
  - ある時刻・ステージに登録されるルール数の最大値の予測値を指定するのがよい．
- setExpectedSizeOfTemporaryRulesMap(int expectedSizeOfTemporaryRulesMap):
  - 臨時実行ルールの配列を保持するマップの初期サイズを指定する．
  - 臨時実行ルールが発行される時刻・ステージ数の予測値を指定するのがよい．
- setExpectedNoOfDeletedObjects(int expectedNoOfDeletedObjects):
  - 削除されるオブジェクトを一時的に保持するリストの初期サイズを指定する．
  - ある時刻・ステージのルールの実行で削除されるオブジェクト数の最大値の予測値を指定するのがよい．
- setExpectedNoOfAgents(Enum<?> agentType, int expectedMaxNoOfAgents):
  - エージェントタイプごとにエージェントを保持するリストの初期サイズを指定する．
  - エージェントタイプごとのシミュレーションで作成されるエージェント数の最大値の予測値を指定するのがよい．
- setExpectedNoOfSpots(Enum<?> layer, Enum<?> spotType, int expectedMaxNoOfSpots):
- setExpectedNoOfSpots(Enum<?> spotType, int expectedMaxNoOfSpots):
  - レイヤーとスポットタイプごとにスポットを保持するリストの初期サイズを指定する．
  - レイヤーを入力しない場合は，デフォルトレイヤーが指定される．
  - レイヤーとスポットタイプごとのシミュレーションで作成されるスポット数の最大値の予測値を指定するのがよい．

## SOARS2 APIs (JavaDoc)
- http://www.ic.dis.titech.ac.jp/soars/soars2-apidocs_2_2_0/index.html
