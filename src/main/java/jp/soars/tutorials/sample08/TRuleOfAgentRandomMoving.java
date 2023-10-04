package jp.soars.tutorials.sample08;

import java.util.List;
import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TAgentRule;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;

/**
 * エージェントランダム移動ルール
 * @author nagakane
 */
public final class TRuleOfAgentRandomMoving extends TAgentRule {

    /** 移動先スポットタイプ */
    private final Enum<?> fSpotType;

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールを持つ役割
     * @param spotType 移動先スポットタイプ
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
        // fSpotType のスポットからランダムに移動先を選択して移動
        // スポットがあるレイヤーの情報はスポット自身が持っており，moveToは引数として入力されたスポットがあるレイヤー上を移動する．
        boolean debugFlag = true;
        { // Real
            List<TSpot> spots = spotManager.getSpotsInLayer(ELayer.Real, fSpotType);
            TSpot spot = spots.get(getRandom().nextInt(spots.size()));
            moveTo(spot);
            appendToDebugInfo("Real:" + spot.getName(), debugFlag);
        }
        { // SNS
            List<TSpot> spots = spotManager.getSpotsInLayer(ELayer.SNS, fSpotType);
            TSpot spot = spots.get(getRandom().nextInt(spots.size()));
            moveTo(spot);
            appendToDebugInfo(" SNS:" + spot.getName(), debugFlag);
        }
    }
}
