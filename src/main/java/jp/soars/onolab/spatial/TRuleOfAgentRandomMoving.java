package jp.soars.onolab.spatial;

import java.util.List;
import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;
import jp.soars.modules.onolab.spatial.T2DSpace;
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
        // *************************************************************************************************************
        // 現在スポットとは別のスポットへ移動する場合．(通常のスポット間移動) スポット内座標はNaNに設定される．
        // *************************************************************************************************************
        // ICRandom random = getRandom(); // 乱数発生器
        // List<TSpot> spots = spotManager.getSpots(fSpotType);
        // TSpot spot = spots.get(getRandom().nextInt(spots.size())); // 移動先スポットをランダムに選択
        // moveTo(spot); // 移動後にスポット内座標を初期化しない．スポット内座標はNaNに設定される．

        // *************************************************************************************************************
        // 現在スポットとは別のスポットのランダムなスポット内座標に移動する場合．
        // *************************************************************************************************************
        // ICRandom random = getRandom(); // 乱数発生器
        // List<TSpot> spots = spotManager.getSpots(fSpotType);
        // TSpot spot = spots.get(getRandom().nextInt(spots.size())); // 移動先スポットをランダムに選択
        // moveTo(spot, random); // 移動後にランダムなスポット内座標に設定する場合．

        // *************************************************************************************************************
        // 現在スポットとは別のスポットの指定したスポット内座標に移動する場合．
        // *************************************************************************************************************
        // ICRandom random = getRandom(); // 乱数発生器
        // List<TSpot> spots = spotManager.getSpots(fSpotType);
        // TSpot spot = spots.get(getRandom().nextInt(spots.size())); // 移動先スポットをランダムに選択
        // T2DSpace space = get2DSpace(spot); // スポット内2次元座標系
        // double lowerBoundX = space.getLowerBoundX(); // X座標の下限
        // double upperBoundX = space.getUpperBoundX(); // X座標の上限
        // double lowerBoundY = space.getLowerBoundY(); // Y座標の下限
        // double upperBoundY = space.getUpperBoundY(); // Y座標の上限
        // // 中心座標を計算
        // double x = lowerBoundX + (upperBoundX - lowerBoundX) / 2.0;
        // double y = lowerBoundY + (upperBoundY - lowerBoundY) / 2.0;
        // moveTo(spot, x, y); // 移動後に指定したスポット内座標に設定する場合．

        // *************************************************************************************************************
        // 現在スポットのランダムなスポット内座標に移動する場合．
        // *************************************************************************************************************
        // moveInCurrentSpot(spotManager, getRandom()); // スポット内座標のランダム移動

        // *************************************************************************************************************
        // 現在スポットの指定したスポット内座標に移動する場合．
        // *************************************************************************************************************
        // T2DSpace space = get2DSpaceOfCurrentSpot(spotManager); // 現在スポット内2次元座標系
        // double lowerBoundX = space.getLowerBoundX(); // X座標の下限
        // double upperBoundX = space.getUpperBoundX(); // X座標の上限
        // double lowerBoundY = space.getLowerBoundY(); // Y座標の下限
        // double upperBoundY = space.getUpperBoundY(); // Y座標の上限
        // // 中心座標を計算
        // double x = lowerBoundX + (upperBoundX - lowerBoundX) / 2.0;
        // double y = lowerBoundY + (upperBoundY - lowerBoundY) / 2.0;
        // moveInCurrentSpot(x, y, spotManager); // 移動

        // *************************************************************************************************************
        // 現在スポットのスポット内座標に相対座標指定で移動する場合．
        // *************************************************************************************************************
        // T2DSpace space = get2DSpaceOfCurrentSpot(spotManager); // 現在スポット内2次元座標系
        // double lowerBoundX = space.getLowerBoundX(); // X座標の下限
        // double upperBoundX = space.getUpperBoundX(); // X座標の上限
        // double lowerBoundY = space.getLowerBoundY(); // Y座標の下限
        // double upperBoundY = space.getUpperBoundY(); // Y座標の上限
        // // 移動量 (dx, dy) を計算
        // double dx = (upperBoundX - lowerBoundX) / 3.0;
        // double dy = (upperBoundY - lowerBoundY) / 3.0;
        // moveRelativeInCurrentSpot(dx, dy, spotManager); // 移動量を指定して移動




        ICRandom random = getRandom(); // 乱数発生器
        if (random.nextDouble() < MOVING_PROBABILITY) { // 確率でスポット間をランダム移動
            List<TSpot> spots = spotManager.getSpots(fSpotType);
            TSpot spot = spots.get(getRandom().nextInt(spots.size())); // 移動先スポットをランダムに選択
            moveTo(spot, random); // 移動後にランダムなスポット内座標に設定する場合．
        } else { // スポット内座標のランダム移動
            moveInCurrentSpot(spotManager, random); // 移動
        }
    }
}
