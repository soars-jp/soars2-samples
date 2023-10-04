package jp.soars.tutorials.sample09;

import jp.soars.core.TAgent;
import jp.soars.core.TRole;

/**
 * エージェント役割
 * @author nagakane
 */
public final class TRoleOfAgent extends TRole {

    /** ランダム移動ルール名 */
    public static final String RULE_NAME_OF_RANDOM_MOVING = "RandomMoving";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     */
    public TRoleOfAgent(TAgent owner) {
        // 親クラスのコンストラクタを呼び出す．
        // 以下の2つの引数は省略可能で，その場合デフォルト値で設定される．
        // 第3引数:この役割が持つルール数 (デフォルト値 10)
        // 第4引数:この役割が持つ子役割数 (デフォルト値 5)
        super(ERoleName.Agent, owner, 1, 0);

        // 役割が持つルールの登録
        // エージェントランダム移動ルール．エージェント移動ステージにステージ実行ルールとして予約する．
        new TRuleOfAgentRandomMoving(RULE_NAME_OF_RANDOM_MOVING, this, ESpotType.Spot)
                .setStage(EStage.AgentMoving);
    }
}
