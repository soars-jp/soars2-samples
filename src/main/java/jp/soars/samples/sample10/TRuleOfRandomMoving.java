package jp.soars.samples.sample10;

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
 * ランダムにレイヤーとスポットを選択し移動する．
 * @author nagakane
 */
public final class TRuleOfRandomMoving extends TAgentRule {

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールを持つ役割
     */
    public TRuleOfRandomMoving(String name, TRole owner) {
        super(name, owner);
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
        // レイヤーをランダムに選択する．
        ELayer[] layers = ELayer.values();
        ELayer layer = layers[getRandom().nextInt(layers.length)];
        // レイヤー上にあるスポットから，移動先のスポットを現在いるスポット以外からランダムに選択する．
        List<TSpot> spots = spotManager.getSpotsInLayer(layer);
        TSpot spot;
        do {
            spot = spots.get(getRandom().nextInt(spots.size()));
        } while (isAt(spot));
        // 移動
        moveTo(spot);
    }
}
