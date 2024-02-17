package jp.soars.q_learning.maze;

import jp.soars.core.TRole;
import jp.soars.core.TSpot;

/**
 * 迷路セル役割
 *
 * @author nagakane
 */
public class TRoleOfMazeCell extends TRole {

    /** 迷路セルタイプ */
    private EMazeCellType fMazeCellType;

    /**
     * コンストラクタ
     *
     * @param owner        この役割を持つスポット
     * @param mazeCellType 迷路セルタイプ
     */
    public TRoleOfMazeCell(TSpot owner, EMazeCellType mazeCellType) {
        super(ERoleName.MazeCell, owner, 0, 0);
        fMazeCellType = mazeCellType;
    }

    /**
     * 迷路セルタイプを設定する．
     *
     * @param mazeCellType 迷路セルタイプ
     */
    public final void setMazeCellType(EMazeCellType mazeCellType) {
        fMazeCellType = mazeCellType;
    }

    /**
     * 迷路セルタイプを返す．
     *
     * @return 迷路セルタイプ
     */
    public final EMazeCellType getMazeCellType() {
        return fMazeCellType;
    }
}
