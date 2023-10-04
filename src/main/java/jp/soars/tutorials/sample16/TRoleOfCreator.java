package jp.soars.tutorials.sample16;

import jp.soars.core.TObject;
import jp.soars.core.TRole;

/**
 * クリエイター役割
 * @author nagakane
 */
public final class TRoleOfCreator extends TRole {

    /** 動的にオブジェクトを作成するルール名 */
    public static final String RULE_NAME_OF_CREATING_SPOT_AND_AGENT = "CreatingSpotAndAgent";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     */
    public TRoleOfCreator(TObject owner) {
        // 親クラスのコンストラクタを呼び出す．
        // 以下の2つの引数は省略可能で，その場合デフォルト値で設定される．
        // 第3引数:この役割が持つルール数 (デフォルト値 10)
        // 第4引数:この役割が持つ子役割数 (デフォルト値 5)
        super(ERoleName.Creator, owner, 1, 0);

        // 役割が持つルールの登録
        // 動的オブジェクト作成ルール．8:00:00/動的追加ステージに定時実行ルールとして予約する．
        new TRuleOfCreatingSpotAndAgent(RULE_NAME_OF_CREATING_SPOT_AND_AGENT, this)
                .setTimeAndStage(8, 0, 0, EStage.DynamicAdd);
    }
}
