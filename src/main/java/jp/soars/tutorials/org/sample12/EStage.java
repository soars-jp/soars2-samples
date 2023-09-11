package jp.soars.samples.sample12;

/**
 * ステージ定義
 * @author nagakane
 */
public enum EStage {
    /** エージェント移動ステージ */
    AgentMoving,
    /** 定員条件を満たさない場合に，エージェントの移動を取り消すステージ */
    RevertAgentMoving,
    /** 定員条件チェックステージ */
    Check,
    /** リセットステージ */
    Reset
}
