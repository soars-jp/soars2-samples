package jp.soars.tutorials.sample14;

import jp.soars.core.TAgent;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;

/**
 * 共通役割
 * @author nagakane
 */
public final class TRoleOfCommon extends TRole {

    /** 健康状態決定ルール名 */
    public static final String RULE_NAME_OF_DETERMINING_HEALTH = "DeterminingHealth";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param home 自宅
     * @param deactivatedRole 病人役割に切り替える時に非アクティブ化される役割
     */
    public TRoleOfCommon(TAgent owner, TSpot home, Enum<?> deactivatedRole) {
        super(ERoleName.Common, owner, 1, 0);

        // 6時，健康状態決定ステージ，自宅において，25%の確率で病気になる．
        new TRuleOfDeterminingHealth(RULE_NAME_OF_DETERMINING_HEALTH, this, home, 0.25, deactivatedRole)
                .setTimeAndStage(6, 0, 0, EStage.DeterminingHealth);
    }
}
