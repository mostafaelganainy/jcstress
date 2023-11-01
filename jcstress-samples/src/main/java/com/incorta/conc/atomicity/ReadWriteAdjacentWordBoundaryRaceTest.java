/*
 * Copyright (c) 2014, 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
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
    public void writer1(L_Result r) {
        OffHeapUtils.setLong(page, offset - 8, -1L);
    }

    @Actor
    public void writer2(L_Result r) {
        OffHeapUtils.setLong(page, offset, -1L);
    }
    
    @Arbiter
    public void arbiter(L_Result r) {
        long v1 = OffHeapUtils.getLong(page, offset - 8);
        long v2 = OffHeapUtils.getLong(page, offset);
        r.r1 = v1 + v2;
    }
}