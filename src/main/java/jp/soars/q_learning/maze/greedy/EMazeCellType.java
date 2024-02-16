package jp.soars.q_learning.maze.greedy;

/**
 * 迷路セルタイプ定義
 * 
 * @author nagakane
 */
public enum EMazeCellType {
    /** 通路 */
    Aisle,
    /** 壁 */
    Wall,
    /** スタート */
    Start,
    /** ゴール */
    Goal
}
