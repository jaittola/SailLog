
package com.ja.saillog.test.purejava;

import junit.framework.Assert;
import junit.framework.TestCase;

import android.os.Bundle;

import com.ja.saillog.utilities.Propulsion;

public class TestPropulsion extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mainId = Propulsion.addSail(mainSailString);
        jibId = Propulsion.addSail(jibString);
        spinId = Propulsion.addSail(spinnakerString);

        Propulsion.configure(sailingString,
                           motorSailingString,
                           motoringString,
                           driftingString,
                           upString,
                           downString);

        sp = new Propulsion();

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

        Propulsion.clearSails();
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

        sp.setSail(mainId, Propulsion.up);
        Assert.assertEquals(1, sp.getSailPlan());

        sp.setSail(jibId, Propulsion.up);
        Assert.assertEquals(3, sp.getSailPlan());

        sp.setSail(mainId, Propulsion.down);
        Assert.assertEquals(2, sp.getSailPlan());
    }

    public void testSailPlanAssign() {
        sp.setSailPlan(3);
        Assert.assertEquals(3, sp.getSailPlan());

        sp.setSailPlan(0);
        Assert.assertEquals(0, sp.getSailPlan());
    }

    public void testGenericStatusStrings() {
        sp.setPropulsion(0, Propulsion.engineOn);
        Assert.assertEquals(motoringString,
                            sp.generalDescription());

        sp.setPropulsion(1, Propulsion.engineOn);
        Assert.assertEquals(motorSailingString,
                            sp.generalDescription());

        sp.setPropulsion(0, Propulsion.engineOff);
        Assert.assertEquals(driftingString,
                            sp.generalDescription());
    }

    public void testSailConfiguration() {
        th.setAndVerifySailPlan(Propulsion.up,
                                Propulsion.down,
                                Propulsion.down);
        th.setAndVerifySailPlan(Propulsion.up,
                                Propulsion.up,
                                Propulsion.up);
        th.setAndVerifySailPlan(Propulsion.down,
                                Propulsion.down,
                                Propulsion.down);
        th.setAndVerifySailPlan(Propulsion.up,
                                Propulsion.up,
                                Propulsion.down);
        th.setAndVerifySailPlan(Propulsion.up,
                                Propulsion.down,
                                Propulsion.up);
   }

    public void testBadSailId() {
        sp.setSailPlan(0);
        sp.setSail(3, Propulsion.up);
        Assert.assertEquals(0, sp.getSailPlan());
    }
    
    public void testPropulsionFromBundle() {
        Bundle b = new Bundle();
        b.putBoolean(Propulsion.engineStatusBundleKey, true);
        b.putLong(Propulsion.sailPlanBundleKey, 0x3L);
        
        Propulsion p = new Propulsion(b);
        Assert.assertEquals(0x3L, p.getSailPlan());
        Assert.assertEquals(true, p.getEngine());
    }
    
    public void testPropulsionToBundle() {
       
        Propulsion p = new Propulsion(0x3L, true);

        Bundle b = p.toBundle();
        Assert.assertTrue(b.getBoolean(Propulsion.engineStatusBundleKey));
        Assert.assertEquals(0x3L, b.getLong(Propulsion.sailPlanBundleKey));
    }

    private Propulsion sp;
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
