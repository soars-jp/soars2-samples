package jp.soars.onolab.spatial;

import java.util.List;
import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;
import jp.soars.modules.onolab.spatial.TRuleOf2DCoordinateAgent;
import jp.soars.utils.random.ICRandom;

/**
 * エージェントランダム移動ルール
 * @author nagakane
 */
public final class TRuleOfAgentRandomMoving extends TRuleOf2DCoordinateAgent {

    /** ランダム移動先のスポットタイプ */
    private final Enum<?> fSpotType;

    /** スポット間移動確率 */
    private static final double MOVING_PROBABILITY = 0.2;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールを持つ役割
     */
    public TRuleOfAgentRandomMoving(String name, TRole owner, Enum<?> spotType) {
        super(name, owner);
        fSpotType = spotType;
    }

    /**
     * ルールを実行する．
     * @param currentTime 現在時刻
     * @param currentStage 現在ステージ
     * @param spotManager スポット管理
     * @param agentManager エージェント管理
     * @param globalSharedVariables グローバル共有変数集合
     */
    @Override
    public final void doIt(TTime currentTime, Enum<?> currentStage, TSpotManager spotManager,
            TAgentManager agentManager, Map<String, Object> globalSharedVariables) {
        ICRandom random = getRandom(); // 乱数発生器
        if (random.nextDouble() < MOVING_PROBABILITY) { // 確率でスポット間をランダム移動
            List<TSpot> spots = spotManager.getSpots(fSpotType);
            TSpot spot = spots.get(getRandom().nextInt(spots.size())); // 移動先スポットをランダムに選択
            // moveTo(spot); // 移動後にスポット内座標を初期化しない場合．(座標はNaNに設定される．)
            moveTo(spot, random); // 移動後にランダムなスポット内座標に設定する場合．
        } else { // スポット内座標のランダム移動
            moveInCurrentSpot(spotManager, random); // 移動
        }

        // // 座標を指定して移動する場合
        // ICRandom random = getRandom(); // 乱数発生器
        // if (random.nextDouble() < MOVING_PROBABILITY) { // 確率でスポット間をランダム移動
        //     List<TSpot> spots = spotManager.getSpots(fSpotType);
        //     TSpot spot = spots.get(getRandom().nextInt(spots.size())); // 移動先スポットをランダムに選択
        //     // moveTo(spot); // 移動後にスポット内座標を初期化しない場合．(座標はNaNに設定される．)
        //     // 移動先の座標
        //     double x = 0.0;
        //     double y = 0.0;
        //     moveTo(spot, x, y); // 移動後に指定したスポット内座標に設定する場合．
        // } else { // スポット内座標のランダム移動
        //     // 移動先の座標
        //     double x = 0.0;
        //     double y = 0.0;
        //     moveInCurrentSpot(x, y, spotManager); // 移動
        // }
    }
}
