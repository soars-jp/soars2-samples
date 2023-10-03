package jp.soars.tutorials.sample11;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jp.soars.core.TAgent;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;

/**
 * 定員ありスポット役割
 * @author nagakane
 */
public final class TRoleOfSpotWithCapacity extends TRole {

    /** スポットの定員 */
    private final int fCapacity;

    /** 直前に移動してきたエージェントと移動前スポットのマップ */
    private final Map<TAgent, TSpot> fPreMovementSpots;

    /** エージェントの移動を戻すルール名 */
    public static final String RULE_NAME_OF_REVERT_AGENT_MOVING = "RevertAgentMoving";

    /** 直前に移動してきたエージェントと移動前スポットのマップをクリアするルール名 */
    public static final String RULE_NAME_OF_RESET = "Reset";

    /**
     * コンストラクタ
     * @param owner この役割を持つスポット
     * @param capacity 定員
     * @throws RuntimeException 定員として負の数を指定した場合．
     */
    public TRoleOfSpotWithCapacity(TSpot owner, int capacity) {
        // 親クラスのコンストラクタを呼び出す．
        // 以下の2つの引数は省略可能で，その場合デフォルト値で設定される．
        // 第3引数:この役割が持つルール数 (デフォルト値 10)
        // 第4引数:この役割が持つ子役割数 (デフォルト値 5)
        super(ERoleName.SpotWithCapacity, owner, 1, 0);

        if (capacity < 0) {
            throw new RuntimeException("The capacity of spot must be at least 0.");
        }

        fCapacity = capacity;
        fPreMovementSpots = new ConcurrentHashMap<>();

        // 役割が持つルールの登録
        // エージェントを移動前スポットに戻すルール．
        // 定員条件を満たさない場合にエージェントの移動を取り消すステージにステージ実行ルールとして予約する．
        new TRuleOfRevertAgentMoving(RULE_NAME_OF_REVERT_AGENT_MOVING, this)
                .setStage(EStage.RevertAgentMoving);

        // 定員ありスポット役割の内部変数をクリアするルール．リセットステージにステージ実行ルールとして予約する．
        new TRuleOfReset(RULE_NAME_OF_RESET, this)
                .setStage(EStage.Reset);
    }

    /**
     * 直前に移動してきたエージェントと移動前スポットを追加
     * @param agent 直前に移動してきたエージェント
     * @param spot 移動前スポット
     */
    public final void addTemporaryAgent(TAgent agent, TSpot spot) {
        fPreMovementSpots.put(agent, spot);
    }

    /**
     * 直前に移動してきたエージェントと移動前スポットのマップを返す．
     * @return 直前に移動してきたエージェントと移動前スポットのマップ
     */
    public final Map<TAgent, TSpot> getPreMovementSpotMap() {
        return fPreMovementSpots;
    }

    /**
     * スポットの定員を返す．
     * @return スポットの定員
     */
    public final int getCapacity() {
        return fCapacity;
    }
}
