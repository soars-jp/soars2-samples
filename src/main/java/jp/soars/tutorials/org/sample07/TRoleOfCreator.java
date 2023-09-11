package jp.soars.samples.sample07;

import jp.soars.core.TObject;
import jp.soars.core.TRole;

/**
 * クリエイター役割
 * @author nagakane
 */
public final class TRoleOfCreator extends TRole {

    /** ダミースポットとダミーエージェントを作成するルール名 */
    public static final String RULE_NAME_OF_CREATOR = "Creator";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     */
    public TRoleOfCreator(TObject owner) {
        super(ERoleName.Creator, owner, 1, 0);

        // 毎日8時に実行されるようにスケジューリング．
        new TRuleOfCreatingSpotAndAgent(RULE_NAME_OF_CREATOR, this)
                .setTimeAndStage(8, 0, 0, EStage.DynamicAdd);
    }
}
