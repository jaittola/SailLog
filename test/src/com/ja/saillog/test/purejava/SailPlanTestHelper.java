
package com.ja.saillog.test.purejava;

import java.util.List;

import junit.framework.Assert;

import android.util.Pair;

import com.ja.saillog.utilities.Propulsion;


public class SailPlanTestHelper {
    private int mainId;
    private int jibId;
    private int spinnakerId;
    public SailPlanTestHelper(String mainSailString,
                              String jibString,
                              String spinnakerString,
                              String upString,
                              String downString,
                              int mainId,
                              int jibId,
                              int spinnakerId,
                              Propulsion sp) {
        this.mainSailString = mainSailString;
        this.jibString = jibString;
        this.spinnakerString = spinnakerString;
        this.upString = upString;
        this.downString = downString;
        this.mainId = mainId;
        this.jibId = jibId;
        this.spinnakerId = spinnakerId;
        this.sp = sp;
    }

    public void setAndVerifySailPlan(boolean mainUp,
                                     boolean jibUp,
                                     boolean spinnakerUp) {
        sp.setSailPlan(0);
        sp.setSail(mainId, mainUp);
        sp.setSail(jibId, jibUp);
        sp.setSail(spinnakerId, spinnakerUp);

        verifySailPlan(mainUp, jibUp, spinnakerUp);
    }

    public void verifySailPlan(boolean mainUp,
                               boolean jibUp,
                               boolean spinnakerUp) {
        List<Pair<String, String> > sails = sp.currentSails();
        Assert.assertEquals(3, sails.size()); // We have configured 3 sails.
        for (Pair<String, String> sail: sails) {
            if (sail.first == mainSailString) {
                Assert.assertEquals(sailStatus(mainUp), sail.second);
            }
            else if (sail.first == jibString) {
                Assert.assertEquals(sailStatus(jibUp), sail.second);
            }
            else if (sail.first == spinnakerString) {
                Assert.assertEquals(sailStatus(spinnakerUp), sail.second);
            }
        }
    }

    public String sailStatus(boolean isUp) {

        if (Propulsion.up == isUp) {
            return upString;
        }
        return downString;
    }


    public String mainSailString;
    public String jibString;
    public String spinnakerString;
    public String sailingString;
    public String motorSailingString;
    public String motoringString;
    public String driftingString;
    public String upString;
    public String downString;
    public Propulsion sp;
}
