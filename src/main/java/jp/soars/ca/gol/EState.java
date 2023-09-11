package jp.soars.ca.gol;

/**
 * エージェントの状態の定義
 * ここでの定義順を取得して近傍の生きているセルをカウントするので注意．
 * 以下の順で定義することで，ordinal() メソッドを使用することで
 * DEATH -> 0, LIFE -> 1 を得ることができる．
 * @author nagakane
 */
public enum EState {
    /** 死 */
    DEATH,
    /** 生 */
    LIFE
}
