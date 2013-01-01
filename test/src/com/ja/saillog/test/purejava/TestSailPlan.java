
package com.ja.saillog.test.purejava;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.ja.saillog.utilities.SailPlan;

public class TestSailPlan extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mainId = SailPlan.addSail(mainSailString);
        jibId = SailPlan.addSail(jibString);
        spinId = SailPlan.addSail(spinnakerString);

        SailPlan.configure(sailingString,
                           motorSailingString,
                           motoringString,
                           driftingString,
                           upString,
                           downString);

        sp = new SailPlan();

        th = new SailPlanTestHelper(mainSailString,
                                    jibString,
                                    spinnakerString,
                                    upString,
                                    downString,
                                    mainId,
                                    jibId,
                                    spinId,
                                    sp);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        SailPlan.clearSails();
    }

    public void testCorrectClassSetup() {
        Assert.assertTrue(-1 != mainId);
        Assert.assertTrue(-1 != jibId);
        Assert.assertTrue(-1 != spinId);
    }

    public void testSimpleSailStatus() {
        // The sail plan numbers are the same that get calculated
        // within SailPlan.
        Assert.assertEquals(0, sp.getSailPlan());

        sp.setSail(mainId, SailPlan.up);
        Assert.assertEquals(1, sp.getSailPlan());

        sp.setSail(jibId, SailPlan.up);
        Assert.assertEquals(3, sp.getSailPlan());

        sp.setSail(mainId, SailPlan.down);
        Assert.assertEquals(2, sp.getSailPlan());
    }

    public void testSailPlanAssign() {
        sp.setSailPlan(3);
        Assert.assertEquals(3, sp.getSailPlan());

        sp.setSailPlan(0);
        Assert.assertEquals(0, sp.getSailPlan());
    }

    public void testGenericStatusStrings() {
        sp.setSailPlan(0);
        Assert.assertEquals(motoringString,
                            sp.generalDescription(SailPlan.engineOn));

        sp.setSailPlan(1);
        Assert.assertEquals(motorSailingString,
                            sp.generalDescription(SailPlan.engineOn));

        sp.setSailPlan(0);
        Assert.assertEquals(motoringString,
                            sp.generalDescription(SailPlan.engineOn));

        sp.setSailPlan(0);
        Assert.assertEquals(driftingString,
                            sp.generalDescription(SailPlan.engineOff));
    }

    public void testSailConfiguration() {
        th.setAndVerifySailPlan(SailPlan.up,
                                SailPlan.down,
                                SailPlan.down);
        th.setAndVerifySailPlan(SailPlan.up,
                                SailPlan.up,
                                SailPlan.up);
        th.setAndVerifySailPlan(SailPlan.down,
                                SailPlan.down,
                                SailPlan.down);
        th.setAndVerifySailPlan(SailPlan.up,
                                SailPlan.up,
                                SailPlan.down);
        th.setAndVerifySailPlan(SailPlan.up,
                                SailPlan.down,
                                SailPlan.up);
   }

    public void testBadSailId() {
        sp.setSailPlan(0);
        sp.setSail(3, SailPlan.up);
        Assert.assertEquals(0, sp.getSailPlan());
    }

    private SailPlan sp;
    private SailPlanTestHelper th;

    private String mainSailString = "main";
    private String jibString = "jib";
    private String spinnakerString = "spin";
    private String motoringString = "Motoring";
    private String motorSailingString = "Motorsailing";
    private String sailingString = "Sailing";
    private String driftingString = "Drifting";
    private String upString = "Up";
    private String downString = "Down";

    private int mainId = -1;
    private int jibId = -1;
    private int spinId = -1;
}
