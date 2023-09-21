package com.incorta.conc.atomicity;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.L_Result;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE_INTERESTING;

// Mark the class as JCStress test.
@JCStressTest

// These are the test outcomes.
//@Outcome(id = "1, 1", expect = ACCEPTABLE_INTERESTING, desc = "Both actors came up with the same value: atomicity failure.")
//@Outcome(id = "1, 2", expect = ACCEPTABLE, desc = "actor1 incremented, then actor2.")
//@Outcome(id = "2, 1", expect = ACCEPTABLE, desc = "actor2 incremented, then actor1.")
@Outcome(id = "-2",  expect = ACCEPTABLE,             desc = "Total Value")
@Outcome(           expect = ACCEPTABLE_INTERESTING, desc = "Other cases are allowed, because reads/writes are not atomic.")
// This is a state object
@State

public class ReadWriteAdjacentWordBoundaryRaceTest {
    private static final Random RANDOM = new Random();
    private static final int CACHE_LINE_SIZE = 128;
    
    private static final boolean alignAddresses = true;
    
    private final long page;
    private final long offset;

    public ReadWriteAdjacentWordBoundaryRaceTest() {
        page = OffHeapUtils.allocatePage(3 * CACHE_LINE_SIZE + 8) + CACHE_LINE_SIZE;
        offset = alignAddresses ? getAlignedOffset(page) : ThreadLocalRandom.current().nextInt( 2 * CACHE_LINE_SIZE - 8);;
    }
    
    private static long getAlignedOffset(long page) {
        return (int) (CACHE_LINE_SIZE - (page & (CACHE_LINE_SIZE - 1)));         
    }

    int getSize() {
        return 8;
    }

    @Actor
    public void writer1() {
        
//        buffer.putLong(0, -1L);
        OffHeapUtils.setLong(page, offset - 8, -1L);
    }

    @Actor
    public void writer2() {
        OffHeapUtils.setLong(page, offset, -1L);
    }
    
    @Arbiter
    public void arbiter(L_Result r) {
        long v1 = OffHeapUtils.getLong(page, offset - 8);
        long v2 = OffHeapUtils.getLong(page, offset);
        r.r1 = v1 + v2;
    }
}