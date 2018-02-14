/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.runtime.services;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.RequestScoped;

import com.google.common.collect.Lists;

import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ServiceInstantiatorTest {

	@Rule
	public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

	private ServiceInstantiator serviceInstantiator;

	@JUnitRuleMockery2.Ignoring
	@Mock
	private ServicesInjector mockServiceInjector;

	@Before
	public void setUp() throws Exception {

		serviceInstantiator = new ServiceInstantiator();
		serviceInstantiator.setConfiguration(new IsisConfigurationDefault());
	}

	@Test
	public void singleton() {
		SingletonCalculator calculator = serviceInstantiator.createInstance(SingletonCalculator.class);
		assertThat(calculator.add(3,4), is(7));
	}

	@Test
	public void requestScoped_instantiate() {
		AccumulatingCalculator calculator = serviceInstantiator.createInstance(AccumulatingCalculator.class);
		assertThat(calculator instanceof RequestScopedService, is(true));
	}

	@Test
	public void requestScoped_justOneThread() {
		AccumulatingCalculator calculator = serviceInstantiator.createInstance(AccumulatingCalculator.class);
		try {
			((RequestScopedService)calculator).__isis_startRequest(mockServiceInjector);
			assertThat(calculator.add(3), is(3));
			assertThat(calculator.add(4), is(7));
			assertThat(calculator.getTotal(), is(7));
		} finally {
			((RequestScopedService)calculator).__isis_endRequest();
		}
	}

	@Test
	public void requestScoped_multipleThreads() throws InterruptedException, BrokenBarrierException {

		final AccumulatingCalculator calculator = serviceInstantiator.createInstance(AccumulatingCalculator.class);

		// will ask each thread's calculator to increment 10 times
		final int[] steps = new int[]{10};

		// each thread will post its totals here
		final int[] totals = new int[]{0,0,0};

		// after each step, all threads wait.  The +1 is for this thread (the co-ordinator)
		final CyclicBarrier barrier =
				new CyclicBarrier(totals.length+1, new Runnable() {
					public void run() {
						// all threads waiting; decrement number of steps
						steps[0]--;
					}
				});

		// start off all threads
		for(int i=0; i<totals.length; i++) {
			final int j=i;
			new Thread() {
				public void run() {
					try {
						((RequestScopedService)calculator).__isis_startRequest(mockServiceInjector);
						// keep incrementing, till no more steps
						while(steps[0]>0) {
							try {
								calculator.add((j+1));
								totals[j] = calculator.getTotal();
								barrier.await();
							} catch (InterruptedException | BrokenBarrierException e) {
								throw new RuntimeException(e);
							}
						}
					} finally {
						((RequestScopedService)calculator).__isis_endRequest();
					}
				};
			}.start();
		}

		// this thread is the co-ordinator; move onto next step when all are waiting
		while(steps[0]>0) {
			barrier.await();
		}

		assertThat(totals[0], is(10));
		assertThat(totals[1], is(20));
		assertThat(totals[2], is(30));
	}

	@Test
	public void requestScoped_childThreads() throws InterruptedException  {

		final AccumulatingCalculator calculator = 
				serviceInstantiator.createInstance(AccumulatingCalculator.class);


		final List<Integer> interimTotals = Collections.synchronizedList(Lists.newArrayList());

		final int n = 100;
		final int nThreads = 8;
		final ExecutorService execService = Executors.newFixedThreadPool(nThreads);

		// initialize the request scoped calculator on current thread ('main')
		((RequestScopedService)calculator).__isis_startRequest(mockServiceInjector);

		for(int i=1;i<=n;++i) {
			final int j=i;

			execService.submit(() -> {
                try {

                    // access the request scoped calculator on a child thread of 'main'
                    calculator.add(j);
                    interimTotals.add(calculator.getTotal());

                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            });

		}

		execService.shutdown();

		execService.awaitTermination(10, TimeUnit.SECONDS);

		((RequestScopedService)calculator).__isis_endRequest();

		assertThat(interimTotals.size(), is(n));
		final Integer maxTotal = Collections.max(interimTotals);
		assertThat(maxTotal, is(n*(n+1)/2));
	}

	public static class SingletonCalculator {
		public int add(int x, int y) {
			return x+y;
		}
	}

	@RequestScoped
	public static class AccumulatingCalculator {
		private int total;
		public int add(int x) {
			total += x;
			return getTotal();
		}
		public int getTotal() {
			return total;
		}
	}
}
