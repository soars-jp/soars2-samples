package jp.soars.ca.gol;

import java.util.List;

import jp.soars.core.TAgent;
import jp.soars.core.TRole;

/**
 * セル役割
 * @author nagakane
 */
public final class TRoleOfCell extends TRole {

    /** 状態 */
    private EState fState;

    /** 次の状態 */
    private EState fNextState;

    /** 近傍エージェントの状態をチェックするルール名 */
    private static final String RULE_NAME_OF_CHECK_NEIGHBORHOODS = "CheckNeighborhoods";

    /** セルをアップデートするルール名 */
    private static final String RULE_NAME_OF_UPDATE_CELL = "UpdateCell";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param neighborhoods ムーア近傍にいるエージェントリスト，この役割を持つエージェントが番人の場合はnull．
     */
    public TRoleOfCell(TAgent owner, List<TAgent> neighborhoods) {
        // 親クラスのコンストラクタを呼び出す．
        // 以下の2つの引数は省略可能で，その場合デフォルト値で設定される．
        // 第3引数 : この役割が持つルール数 (デフォルト値 10)
        // 第4引数 : この役割が持つ子役割数 (デフォルト値 5)
        super(ERoleName.Cell, owner, neighborhoods != null ? 2 : 0, 0);

        fState = EState.DEATH;
        fNextState = null;

        // 番人ならばルールは登録しない．(アップデートされない)
        if (neighborhoods != null) {
            new TRuleOfCalculateNextState(RULE_NAME_OF_CHECK_NEIGHBORHOODS, this, neighborhoods)
                    .setStage(EStage.CalculateNextState);
            new TRuleOfUpdateCell(RULE_NAME_OF_UPDATE_CELL, this)
                    .setStage(EStage.UpdateCell);
        }
    }

    /**
     * この役割を持つエージェントの状態を返す．
     * @return この役割を持つエージェントの状態
     */
    public final EState getState() {
        return fState;
    }

    /**
     * この役割を持つエージェントの状態を設定する．
     * @param state この役割を持つエージェントの状態
     */
    public final void setState(EState state) {
        fState = state;
    }

    /**
     * この役割を持つエージェントの次の状態を設定する．
     * @param state この役割を持つエージェントの次の状態
     */
    public final void setNextState(EState state) {
        fNextState = state;
    }

    /**
     * 状態をアップデートする．
     */
    public final void updateState() {
        fState = fNextState;
    }
}
