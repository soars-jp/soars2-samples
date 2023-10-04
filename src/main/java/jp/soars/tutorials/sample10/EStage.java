package jp.soars.tutorials.sample10;

/**
 * ステージ定義
 * @author nagakane
 */
public enum EStage {
    /** エージェント移動ステージ */
    AgentMoving,
    /** 定員条件を満たさない場合にエージェントの移動を取り消すステージ */
    RevertAgentMoving,
    /** リセットステージ */
    Reset
}
