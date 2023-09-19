package jp.soars.netlogo.covid19.agent;

import jp.soars.core.TAgent;
import jp.soars.core.TRole;
import jp.soars.netlogo.covid19.EDiseaseLevel;
import jp.soars.netlogo.covid19.ERoleName;

/**
 * COVID19役割
 * @author nagakane
 */
public class TRoleOfCOVID19 extends TRole {

    /** 感染しているか？ */
    private boolean fIsInfected;

    /** 病態レベル */
    private EDiseaseLevel fDiseaseLevel;

    /** 感染からの経過日数 */
    private int fDaysSinceInfection;

    public TRoleOfCOVID19(TAgent owner) {
        super(ERoleName.COVID19, owner, , );
        fIsInfected = false;
        fDiseaseLevel = EDiseaseLevel.UNINFECTED;
        fDaysSinceInfection = 0;
    }

    /**
     * エージェントが感染しているかを設定する．
     * @param isInfected 感染しているか？
     */
    public final void setInfected(boolean isInfected) {
        fIsInfected = isInfected;
    }

    /**
     * エージェントが感染しているかを返す，
     * @return エージェントが感染しているか？
     */
    public final boolean isInfected() {
        return fIsInfected;
    }

    /**
     * 病態レベルを設定する．
     * @param diseaseLevel 病態レベル
     */
    public final void setDiseaseLevel(EDiseaseLevel diseaseLevel) {
        fDiseaseLevel = diseaseLevel;
    }

    /**
     * 病態レベルを返す．
     * @return 病態レベル
     */
    public final EDiseaseLevel getDiseaseLevel() {
        return fDiseaseLevel;
    }

    /**
     * 感染からの経過日数を返す．
     * @return 感染からの経過日数
     */
    public final int getDaysSinceInfection() {
        return fDaysSinceInfection;
    }
}
