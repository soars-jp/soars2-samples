package jp.soars.tutorials.sample01;

import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TAgentRule;
import jp.soars.core.TRole;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

/**
 * 会社から自宅に移動するルール
 * @author nagakane
 */
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
