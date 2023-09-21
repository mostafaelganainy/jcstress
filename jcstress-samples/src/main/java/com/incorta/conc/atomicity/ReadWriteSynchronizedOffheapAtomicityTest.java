package com.incorta.conc.atomicity;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
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
@Outcome(id = "0",  expect = ACCEPTABLE,             desc = "Seeing the default value: writer had not acted yet.")
@Outcome(id = "-1", expect = ACCEPTABLE,             desc = "Seeing the full value.")
@Outcome(           expect = ACCEPTABLE_INTERESTING, desc = "Other cases are allowed, because reads/writes are not atomic.")
// This is a state object
@State

public class ReadWriteSynchronizedOffheapAtomicityTest {
    private static final Random RANDOM = new Random();
    private static final int CACHE_LINE_SIZE = 128;
    
    private static final boolean alignAddresses = false;
    
    // private final ByteBuffer buffer;
    
    private final long page;
    private final long offset;

    public ReadWriteSynchronizedOffheapAtomicityTest() {
//        final int size = getSize();
//        
//        ByteBuffer buffy = OffHeapUtils.allocateAlignedByteBuffer(CACHE_LINE_SIZE, CACHE_LINE_SIZE);
//        // new position is always unaligned for a type of size
//        int newPosition = (RANDOM.nextInt(CACHE_LINE_SIZE / size - 2)) * size
//                + 1 + ((size > 2) ? RANDOM.nextInt(size - 2) : 0);
//        buffy.position(newPosition);
//        buffer = buffy.slice().order(ByteOrder.nativeOrder());
        
        page = OffHeapUtils.allocatePage(2 * CACHE_LINE_SIZE);
        offset = alignAddresses ? getAlignedOffset(page) : ThreadLocalRandom.current().nextInt( 2 * CACHE_LINE_SIZE - 8);;
    }
    
    private static long getAlignedOffset(long page) {
        return (int) (CACHE_LINE_SIZE - (page & (CACHE_LINE_SIZE - 1)));         
    }

    int getSize() {
        return 8;
    }

    @Actor
    public void writer() {
        
//        buffer.putLong(0, -1L);
        synchronized (this) {
            OffHeapUtils.setLong(page, offset, -1L);
        }
    }

    @Actor
    public void reader(L_Result r) {
        // r.r1 = buffer.getLong(0);
        synchronized (this) {
            r.r1 = OffHeapUtils.getLong(page, offset);
        }
    }
}