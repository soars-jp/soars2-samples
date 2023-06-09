package jp.soars.samples.sample06;

import java.util.List;
import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TAgentRule;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

/**
 * ランダム移動ルール
 * 終了時刻まで指定された時間間隔でランダムに移動を繰り返し，終了時刻に自宅に移動する．
 * @author nagakane
 */
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
