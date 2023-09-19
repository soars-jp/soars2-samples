package jp.soars.netlogo.covid19.agent;

import jp.soars.core.TAgent;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;
import jp.soars.netlogo.covid19.EAgeGroup;
import jp.soars.netlogo.covid19.ERoleName;

/**
 * 住人役割
 * @author nagakane
 */
public class TRoleOfResident extends TRole {

    /** 年齢層 */
    private final EAgeGroup fAgeGroup;

    /** 自宅 */
    private final TSpot fHome;

    /**
     * コンストラクタ
     * @param owner この役割を持つエージェント
     * @param ageGroup 年齢層
     * @param home 自宅
     */
    public TRoleOfResident(TAgent owner, EAgeGroup ageGroup, TSpot home) {
        super(ERoleName.Resident, owner, , );
        fAgeGroup = ageGroup;
        fHome = home;
    }

    /**
     * 年齢層を返す．
     * @return 年齢層
     */
    public final EAgeGroup getAgeGroup() {
        return fAgeGroup;
    }

    /**
     * 自宅を返す．
     * @return 自宅
     */
    public final TSpot getHome() {
        return fHome;
    }
}
