package jp.soars.onolab.cell;

import java.util.Map;

import jp.soars.core.TAgentManager;
import jp.soars.core.TAgentRule;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.TTime;
import jp.soars.modules.onolab.cell.ECellModuleRoleName;
import jp.soars.modules.onolab.cell.TRoleOf2DCell;
import jp.soars.utils.random.ICRandom;

/**
 * エージェントランダム移動ルール
 * @author nagakane
 */
public class TRuleOfAgentRandomMoving extends TAgentRule {

    /**
     * コンストラクタ
     * @param name ルール名
     * @param owner このルールを持つ役割
     */
    public TRuleOfAgentRandomMoving(String name, TRole owner) {
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
        // TRoleOf2DCell は自分自身と近傍8セルへの参照を持っており，x, y ∈ {-1, 0, 1}で指定して取得できる．
        // このルールでは x, y をランダムに選択して移動する．(斜め移動もする)
        // 空間端などで近傍セルが存在しない(= null) の場合，移動先として選択されたのが自分自身の場合は選び直す．
        ICRandom random = getRandom();
        TSpot currentSpot = getCurrentSpot();
        TRoleOf2DCell role = (TRoleOf2DCell) currentSpot.getRole(ECellModuleRoleName.Cell);
        TSpot spot;
        do {
            spot = role.getNeighborhood(random.nextInt(-1, 1), random.nextInt(-1, 1));
        } while (spot == null || spot.equals(currentSpot));
        moveTo(spot);
    }
}
