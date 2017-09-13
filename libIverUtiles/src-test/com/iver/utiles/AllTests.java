package com.iver.utiles;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.iver.utiles.vectorUtilities.VectorUtilitiesTest;

@RunWith(Suite.class)
@SuiteClasses({ DoubleUtilitiesTest.class, VectorUtilitiesTest.class })
public class AllTests {

}
