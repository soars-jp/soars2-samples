package jp.soars.ca.gol;

import jp.soars.core.TRole;
import jp.soars.core.TSpot;

/**
 * セル役割
 * @author nagakane
 */
public final class TRoleOfStateTransition extends TRole {

    /** 状態 */
    private EState fState;

    /** 次の状態 */
    private EState fNextState;

    /** 近傍セルの状態から次の状態を計算するルール名 */
    private static final String RULE_NAME_OF_CALCULATE_NEXT_STATE = "CalculateNextState";

    /** 状態遷移するルール名 */
    private static final String RULE_NAME_OF_STATE_TRANSITION = "StateTransition";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     */
    public TRoleOfStateTransition(TSpot owner, EState initialState) {
        // 親クラスのコンストラクタを呼び出す．
        super(ERoleName.StateTransition, owner, 2, 0);

        fState = initialState;
        fNextState = null;

        new TRuleOfCalculateNextState(RULE_NAME_OF_CALCULATE_NEXT_STATE, this)
                .setStage(EStage.CalculateNextState);
        new TRuleOfStateTransition(RULE_NAME_OF_STATE_TRANSITION, this)
                .setStage(EStage.StateTransition);
    }

    /**
     * 状態チェック
     * @param state 状態
     * @return 入力状態か？
     */
    public final boolean isState(EState state) {
        return fState == state;
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
     * 状態遷移実行
     */
    public final void stateTransition() {
        fState = fNextState;
    }
}
