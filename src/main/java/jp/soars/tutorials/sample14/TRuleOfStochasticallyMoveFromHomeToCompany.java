package jp.soars.tutorials.sample04;

import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TAgentRule;
import jp.soars.core.TRole;
import jp.soars.core.TRule;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

/**
 * 確率的に自宅から会社に移動するルール
 * @author nagakane
 */
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
        boolean debugFlag = true; // デバッグ情報出力フラグ

        // (日付を7で割った余り)番目の曜日を取得．どの曜日が何番になっているかはenumの定義順による．
        EDay day = EDay.values()[currentTime.getDay() % 7];
        if (day != EDay.Sunday && day != EDay.Saturday) { // 平日(土日以外)の場合は，会社に移動
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

        // 次の日の9時(50%)，10時(30%)，11時(20%)のエージェント移動ステージに自分自身を再予約する．
        double p = getRandom().nextDouble(); // [0, 1]のdouble
        int hour;
        if (p <= 0.5) {
            hour = 9; // 50%
        } else if (p <= 0.8) {
            hour = 10; // 30%
        } else {
            hour = 11; // 20%
        }
        setTimeAndStage(currentTime.getDay() + 1, hour, 0, 0, EStage.AgentMoving);
        // 設定された時刻をデバッグ情報としてルールログに出力
        appendToDebugInfo("/next time = " + hour, debugFlag);
    }
}
