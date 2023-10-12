package jp.soars.onolab.transportation.sample01;

import jp.soars.core.TAgent;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;
import jp.soars.modules.onolab.simple.transportation.ETransportationStage;
import jp.soars.modules.onolab.simple.transportation.TRuleOfGettingOffTransportation;
import jp.soars.modules.onolab.simple.transportation.TRuleOfGettingOnTransportation;

/**
 * 父親役割
 *
 * @author nagakane
 */
public final class TRoleOfFather extends TRole {
        /** 自宅 */
        private final TSpot fHome;

        /** 会社 */
        private final TSpot fCompany;

        /** 出発地と目的地の中間スポット */
        private final TSpot fMid;

        /** 出発駅スポット */
        private final TSpot fSrcStationSpot;

        /** 到着駅スポット */
        private final TSpot fDstStaionSpot;

        /** 路線 */
        private final String fLine;

        /** 家を出発する */
        public static final String LEAVE_HOME = "leave_home";

        /** 家に到着する */
        public static final String REACH_HOME = "reach_home";

        /** 駅に到着する（出勤） */
        public static final String REACH_STATION = "reach_station";

        /** 駅に到着する（帰宅） */
        public static final String REACH_STATION_BACK = "reach_station_back";

        /** 電車に乗る（出勤） */
        public static final String GETON_TRANSPORTATION = "geton_transportation";

        /** 電車に乗る（帰宅） */
        public static final String GETON_TRANSPORTATION_BACK = "geton_transportation_back";

        /** 電車から降りる（出勤） */
        public static final String GETOFF_TRANSPORTATION = "getoff_transportation";

        /** 電車から降りる（帰宅） */
        public static final String GETOFF_TRANSPORTATION_BACK = "getoff_transportation_back";

        /** 駅から会社に向かう */
        public static final String GO_COMPANY = "go_company";

        /** 会社に到着する */
        public static final String REACH_COMPANY = "reach_company";

        /** 会社に出発する */
        public static final String LEAVE_COMPANY = "leave_company";

        /** 家に帰る */
        public static final String GO_HOME = "go_home";

        public TRoleOfFather(TAgent owner, TSpot home, TSpot company, TSpot mid, TSpot srcStationSpot,
                        TSpot dstStationSpot, String line) {
                // 親クラスのコンストラクタを呼び出す．
                // 以下の2つの引数は省略可能で，その場合デフォルト値で設定される．
                // 第3引数:この役割が持つルール数 (デフォルト値 10)
                // 第4引数:この役割が持つ子役割数 (デフォルト値 5)
                super(ERoleName.Father, owner, 12, 0);
                fHome = home;
                fCompany = company;
                fMid = mid;
                fSrcStationSpot = srcStationSpot;
                fDstStaionSpot = dstStationSpot;
                fLine = line;
                moveFromHomeToCompany();
                moveFromCompanyToHome();
        }

        /**
         * 出勤
         */
        private void moveFromHomeToCompany() {
                String direction = "inbound"; // 乗車する列車の方面
                String trainName = "001"; // 乗車する列車名
                // 6:55に自宅を出発して乗車駅に向かう．
                new TRuleOfAgentMoving(LEAVE_HOME, this, fHome, fMid).setTimeAndStage(6, 55, 0, EStage.AgentMoving);
                // 7:00に乗車駅に到着する
                new TRuleOfAgentMoving(REACH_STATION, this, fMid, fSrcStationSpot).setTimeAndStage(7, 0, 0,
                                EStage.AgentMoving);
                // 7:05に電車にのる
                new TRuleOfGettingOnTransportation(GETON_TRANSPORTATION, this, fSrcStationSpot, fLine, direction,
                                trainName).setTimeAndStage(7, 5, 0, ETransportationStage.TransportationArriving);
                // 7:30に電車から降りる
                new TRuleOfGettingOffTransportation(GETOFF_TRANSPORTATION, this, fDstStaionSpot, fLine, direction,
                                trainName).setTimeAndStage(7, 30, 0, ETransportationStage.TransportationLeaving);
                // 7:33に降車駅を出発して会社に向かう．
                new TRuleOfAgentMoving(GO_COMPANY, this, fDstStaionSpot, fMid).setTimeAndStage(7, 33, 0,
                                EStage.AgentMoving);
                // 7:43に会社に到着する
                new TRuleOfAgentMoving(REACH_COMPANY, this, fMid, fCompany).setTimeAndStage(7, 43, 0,
                                EStage.AgentMoving);
        }

        /**
         * 退勤ルールを登録
         */
        private void moveFromCompanyToHome() {
                String direction = "outbound"; // 乗車する列車の方面
                String trainName = "002"; // 乗車する列車名
                // 17:55に会社を出発して乗車駅に向かう．
                new TRuleOfAgentMoving(LEAVE_COMPANY, this, fCompany, fMid).setTimeAndStage(17, 55, 0,
                                EStage.AgentMoving);
                // 18:05に乗車駅に到着する．
                new TRuleOfAgentMoving(REACH_STATION_BACK, this, fMid, fDstStaionSpot).setTimeAndStage(18, 5, 0,
                                EStage.AgentMoving);
                // 18:10に乗車駅で指定された列車に乗車する．
                new TRuleOfGettingOnTransportation(GETON_TRANSPORTATION_BACK, this, fDstStaionSpot, fLine, direction,
                                trainName).setTimeAndStage(18, 10, 0, ETransportationStage.TransportationArriving);
                // 18:35に降車駅で列車から降車する．
                new TRuleOfGettingOffTransportation(GETOFF_TRANSPORTATION_BACK, this, fSrcStationSpot, fLine, direction,
                                trainName).setTimeAndStage(18, 35, 0, ETransportationStage.TransportationLeaving);
                // 18:40に降車駅を出発して自宅に向かう
                new TRuleOfAgentMoving(GO_HOME, this, fSrcStationSpot, fMid).setTimeAndStage(18, 40, 0,
                                EStage.AgentMoving);
                // 18:45に自宅に到着する．
                new TRuleOfAgentMoving(REACH_HOME, this, fMid, fHome).setTimeAndStage(18, 45, 0, EStage.AgentMoving);
        }

        /**
         * 自宅を返す．
         *
         * @return 自宅
         */
        public final TSpot getHome() {
                return fHome;
        }

}
