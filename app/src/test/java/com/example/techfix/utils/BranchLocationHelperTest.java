package com.example.techfix.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BranchLocationHelperTest {
    @Test public void colomboCoordinatesSelectColombo() {
        assertEquals("Colombo Branch",
                BranchLocationHelper.nearestBranchName(6.893982, 79.854749));
    }

    @Test public void galleCoordinatesSelectGalle() {
        assertEquals("Galle Branch",
                BranchLocationHelper.nearestBranchName(6.032857, 80.214954));
    }

    @Test public void nearbyMountLaviniaSelectsColombo() {
        assertEquals("Colombo Branch",
                BranchLocationHelper.nearestBranchName(6.8290, 79.8633));
    }
}
