package jp.soars.tutorials.sample13.module2;

import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TAgentRule;
import jp.soars.core.TRole;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

/**
 * 病気から回復するルール
 * @author nagakane
 */
public final class TRuleOfRecoveringFromSick extends TAgentRule {

    /** 病気から回復した時にアクティブ化する役割名 */
    private final Enum<?> fOriginalRoleName;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールをもつ役割
     * @param originalRoleName 元の役割．病気から回復した時にアクティブ化する．
     */
    public TRuleOfRecoveringFromSick(String name, TRole owner, Enum<?> originalRoleName) {
        // 親クラスのコンストラクタを呼び出す．
        super(name, owner);
        fOriginalRoleName = originalRoleName;
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
        // 病人役割を非アクティブ化して父親役割か子ども役割をアクティブ化する．
        getAgent().deactivateRole(EModule2RoleName.SickPerson);
        getAgent().activateRole(fOriginalRoleName);
    }
}
