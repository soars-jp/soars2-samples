package jp.soars.netlogo.covid19;

/**
 * COVID19モデルの病態レベル
 * @author nagakane
 */
public enum EDiseaseLevel {
    /** 未感染 */
    UNINFECTED,
    /** 潜伏期 (感染力なし) */
    LATENT_NO_CONTAGION,
    /** 潜伏期 (感染力あり) */
    LATENT_CONTAGIOUS,
    /** 悪化 */
    DETERIORATED,
    /** 軽い */
    MILD,
    /** 回復傾向 */
    RECOVERING,
    /** 重症化 */
    SEVERE,
    /** 死亡 */
    DECEASED,
    /** 免疫獲得 */
    IMMUNE
}
