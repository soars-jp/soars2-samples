package jp.soars.tutorials.sample16;

import jp.soars.core.TObject;
import jp.soars.core.TRole;

/**
 * キラー役割
 * @author nagakane
 */
public final class TRoleOfKiller extends TRole {

    /** 動的にオブジェクトを削除するルール名 */
    public static final String RULE_NAME_OF_DELETING_SPOT_AND_AGENT = "DeletingSpotAndAgent";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     */
    public TRoleOfKiller(TObject owner) {
        // 親クラスのコンストラクタを呼び出す．
        // 以下の2つの引数は省略可能で，その場合デフォルト値で設定される．
        // 第3引数:この役割が持つルール数 (デフォルト値 10)
        // 第4引数:この役割が持つ子役割数 (デフォルト値 5)
        super(ERoleName.Killer, owner, 1, 0);

        // 役割が持つルールの登録
        // 動的オブジェクト削除ルール．16:00:00/動的削除ステージに定時実行ルールとして予約する．
        new TRuleOfDeletingSpotAndAgent(RULE_NAME_OF_DELETING_SPOT_AND_AGENT, this)
                .setTimeAndStage(16, 0, 0, EStage.DynamicDelete);
    }
}
