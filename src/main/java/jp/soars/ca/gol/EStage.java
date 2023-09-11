package jp.soars.ca.gol;

/**
 * ステージ定義
 * @author nagakane
 */
public enum EStage {
    /** 近傍エージェントの状態を集計して次の状態を決定するステージ */
    CalculateNextState,
    /** 状態遷移ステージ */
    StateTransition
}
