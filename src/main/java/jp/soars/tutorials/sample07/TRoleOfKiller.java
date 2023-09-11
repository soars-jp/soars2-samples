package jp.soars.samples.sample07;

import jp.soars.core.TObject;
import jp.soars.core.TRole;

/**
 * キラー役割
 * @author nagakane
 */
public final class TRoleOfKiller extends TRole {

    /** ダミースポットとダミーエージェントを削除するルール名 */
    public static final String RULE_NAME_OF_KILLER = "Killer";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     */
    public TRoleOfKiller(TObject owner) {
        super(ERoleName.Killer, owner, 1, 0);

        // 毎日16時に実行されるようにスケジューリング．
        new TRuleOfDeletingSpotAndAgent(RULE_NAME_OF_KILLER, this)
                .setTimeAndStage(16, 0, 0, EStage.DynamicDelete);
    }
}
