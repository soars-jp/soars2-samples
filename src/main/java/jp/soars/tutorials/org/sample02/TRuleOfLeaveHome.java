package jp.soars.tutorials.sample02;

import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TAgentRule;
import jp.soars.core.TRole;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

/**
 * 自宅から会社に移動するルール
 * @author nagakane
 */
public final class TRuleOfLeaveHome extends TAgentRule {

    /** 会社から自宅に移動するルール */
    private final TRuleOfMoveFromCompanyToHome fReturnHomeRule;

    /** 会社から自宅に移動するルールを実行するまでの時間間隔 */
    private final TTime fIntervalTimeToReturnHomeRule;

    /** 会社から自宅に移動するルールの発火時刻計算用 */
    private final TTime fTimeOfReturnHomeRule;

    /** 会社から自宅に移動するルールを実行するステージ */
    private final Enum<?> fStageOfReturnHomeRule;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     * @param returnHomeRule 会社から自宅に移動するルール
     * @param intervalTimeToReturnHomeRule 会社から自宅に移動するルールを実行するまでの時間間隔
     * @param stageOfReturnHomeRule 会社から自宅に移動するルールを実行するステージ
     */
    public TRuleOfLeaveHome(String name, TRole owner,
            TRuleOfMoveFromCompanyToHome returnHomeRule, String intervalTimeToReturnHomeRule, Enum<?> stageOfReturnHomeRule) {
        // 親クラスのコンストラクタを呼び出す．
        super(name, owner);
        fReturnHomeRule = returnHomeRule;
        fIntervalTimeToReturnHomeRule = new TTime(intervalTimeToReturnHomeRule);
        fTimeOfReturnHomeRule = new TTime();
        fStageOfReturnHomeRule = stageOfReturnHomeRule;
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
        TRoleOfFather role = (TRoleOfFather) getRole(ERoleName.Father); // エージェントに登録されている父親役割を取得
        if (isAt(role.getHome())) { // 自宅にいる場合
            // 会社に移動する
            moveTo(role.getCompany());
            // 移動ルールが正常に実行されたことをデバッグ情報としてルールログに出力
            appendToDebugInfo("success", debugFlag);

            // 現在時刻にインターバルを足した時刻を会社から自宅に移動するルールの発火時刻とする．
            fTimeOfReturnHomeRule.copyFrom(currentTime).add(fIntervalTimeToReturnHomeRule);
            // 会社から自宅に移動するルールを臨時実行ルールとして登録．
            fReturnHomeRule.setTimeAndStage(fTimeOfReturnHomeRule.getDay(), fTimeOfReturnHomeRule.getHour(),
                    fTimeOfReturnHomeRule.getMinute(), fTimeOfReturnHomeRule.getSecond(), fStageOfReturnHomeRule);
        } else { // 自宅にいない場合
            // 移動ルールが実行されなかったことをデバッグ情報としてルールログに出力
            appendToDebugInfo("fail", debugFlag);
        }
    }
}
