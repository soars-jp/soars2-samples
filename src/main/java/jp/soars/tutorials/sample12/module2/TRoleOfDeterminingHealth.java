package jp.soars.tutorials.sample12.module2;

import jp.soars.core.TAgent;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;

/**
 * 健康状態決定役割
 * @author nagakane
 */
public final class TRoleOfDeterminingHealth extends TRole {

    /** 健康状態決定ルール名 */
    public static final String RULE_NAME_OF_DETERMINING_HEALTH = "DeterminingHealth";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param home 自宅
     * @param originalRoleName 元の役割．病気になった時に非アクティブ化する．
     */
    public TRoleOfDeterminingHealth(TAgent owner, TSpot home, Enum<?> originalRoleName) {
        super(EModule2RoleName.DeterminingHealth, owner, 1, 0);

        // 健康状態決定ルール．6:00:00/健康状態決定ステージに定時実行ルールとして予約する．病人になる確率は25%(0.25)とする．
        new TRuleOfDeterminingHealth(RULE_NAME_OF_DETERMINING_HEALTH, this, 0.25, home, originalRoleName)
                .setTimeAndStage(6, 0, 0, EModule2Stage.DeterminingHealth);
    }
}
