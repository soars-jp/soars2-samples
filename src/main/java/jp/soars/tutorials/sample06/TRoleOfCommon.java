package jp.soars.tutorials.sample06;

import jp.soars.core.TAgent;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;

/**
 * 共通役割
 * @author nagakane
 */
public final class TRoleOfCommon extends TRole {

    /** 健康状態決定ルール名 */
    private static final String RULE_NAME_OF_DETERMINING_HEALTH = "DeterminingHealth";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param home 自宅
     */
    public TRoleOfCommon(TAgent owner, TSpot home) {
        super(ERoleName.Common, owner, 1, 0);

        // 健康状態決定ルール．6:00:00/健康状態決定ステージに定時実行ルールとして予約する．病人になる確率は25%(0.25)とする．
        new TRuleOfDeterminingHealth(RULE_NAME_OF_DETERMINING_HEALTH, this, 0.25, home)
                .setTimeAndStage(6, 0, 0, EStage.DeterminingHealth);
    }
}
