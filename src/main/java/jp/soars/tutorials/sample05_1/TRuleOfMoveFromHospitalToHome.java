package jp.soars.tutorials.sample05_1;

import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TAgentRule;
import jp.soars.core.TRole;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

/**
 * 病院から自宅に移動するルール
 * @author nagakane
 */
public final class TRuleOfMoveFromHospitalToHome extends TAgentRule {

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     */
    public TRuleOfMoveFromHospitalToHome(String name, TRole owner) {
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
        // エージェントが病院にいるならば，自宅に移動する．
        boolean debugFlag = true;
        // スポットタイプ Hospital は1つしか作成しないので，spotManager からスポットタイプが
        // ESpotType.Hospital のスポットリストを受け取って，最初のスポットをとる．
        if (isAt(spotManager.getSpots(ESpotType.Hospital).get(0))) {
            moveTo(((TRoleOfFather) getRole(ERoleName.Father)).getHome());
            appendToDebugInfo("success", debugFlag);
        } else {
            appendToDebugInfo("fail", debugFlag);
        }
    }
}
