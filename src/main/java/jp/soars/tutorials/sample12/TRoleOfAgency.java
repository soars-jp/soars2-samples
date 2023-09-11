package jp.soars.samples.sample12;

import jp.soars.core.TRole;
import jp.soars.core.TSpot;

public final class TRoleOfAgency extends TRole {

    /** ランダムに移動する */
    public static final String RULE_NAME_OF_CHECK_CAPACITY = "CheckCapacity";

    /**
     * コンストラクタ
     * @param owner この役割を持つスポット
     */
    public TRoleOfAgency(TSpot owner) {
        super(ERoleName.Agency, owner, 1, 0);

        // 定員条件チェックルールをステージ実行として登録
        new TRuleOfCheckCapacity(RULE_NAME_OF_CHECK_CAPACITY, this)
                .setStage(EStage.Check);
    }
}
