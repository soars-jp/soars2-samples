package jp.soars.q_learning.maze;

import jp.soars.core.TAgent;
import jp.soars.core.TRole;

/**
 * エージェント役割
 * @author nagakane
 */
public class TRoleOfAgent extends TRole {

    /** エージェントが選択した行動 */
    private EAgentAction fAgentAction;

    /** 行動で得られた次の状態 (セルの絶対座標) */
    private final int[] fCoordinates;

    /** 行動で得られた報酬 */
    private int fReword;

    /** エージェント行動ランダム選択ルール */
    public static final String RULE_NAME_OF_AGENT_RANDOM_ACTION = "agentRandomAction";

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param initialSpotX 初期スポットx座標
     * @param initialSpotY 初期スポットy座標
     */
    public TRoleOfAgent(TAgent owner, int initialSpotX, int initialSpotY) {
        super(ERoleName.Agent, owner, 1, 0);
        fAgentAction = null;
        fCoordinates = new int[]{initialSpotX, initialSpotY};
        fReword = 0;

        new TRuleOfAgentRandomAction(RULE_NAME_OF_AGENT_RANDOM_ACTION, this)
                .setStage(EStage.AgentAction);
    }

    /**
     * エージェントが選択した行動を設定
     * @param action エージェントが選択した行動
     */
    public final void setAgentAction(EAgentAction action) {
        fAgentAction = action;
    }

    /**
     * エージェントが選択した行動を返す．
     * @return エージェントが選択した行動
     */
    public final EAgentAction getAgentAction() {
        return fAgentAction;
    }

    /**
     * 行動で得られた次の状態 (セルの絶対座標)を返す．
     * @return 行動で得られた次の状態 (セルの絶対座標)
     */
    public final int[] getState() {
        return fCoordinates;
    }

    /**
     * 行動で得られた報酬を設定
     * @param reword 行動で得られた報酬
     */
    public final void setReword(int reword) {
        fReword = reword;
    }

    /**
     * 行動で得られた報酬を返す．
     * @return 行動で得られた報酬
     */
    public final int getReword() {
        return fReword;
    }
}
