/**
 * Licensed to jclouds, Inc. (jclouds) under one or more
 * contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  jclouds licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jclouds.abiquo.strategy.cloud;

import static com.google.common.collect.Iterables.size;
import static org.jclouds.abiquo.reference.AbiquoTestConstants.PREFIX;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.jclouds.abiquo.domain.cloud.HardDisk;
import org.jclouds.abiquo.domain.cloud.VirtualDisk;
import org.jclouds.abiquo.domain.cloud.Volume;
import org.jclouds.abiquo.domain.infrastructure.Tier;
import org.jclouds.abiquo.predicates.infrastructure.TierPredicates;
import org.jclouds.abiquo.strategy.BaseAbiquoStrategyLiveApiTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

/**
 * Live tests for the {@link ListAttachedVirtualDisks} strategy.
 * 
 * @author Ignasi Barrera
 */
@Test(groups = "api", testName = "ListAttachedVirtualDisksLiveApiTest")
public class ListAttachedVirtualDisksLiveApiTest extends BaseAbiquoStrategyLiveApiTest {
   private ListAttachedVirtualDisks strategy;

   private Volume volume;

   private HardDisk hardDisk;

   @Override
   @BeforeClass(groups = "api")
   protected void setupStrategy() {
      this.strategy = env.context.getUtils().getInjector().getInstance(ListAttachedVirtualDisks.class);
      volume = createVolume();
      hardDisk = createHardDisk();

      env.virtualMachine.setVirtualDisks(Lists.<VirtualDisk<?>> newArrayList(volume, hardDisk));
   }

   @AfterClass(groups = "api")
   protected void tearDownStrategy() {
      env.virtualMachine.setVirtualDisks(Lists.<VirtualDisk<?>> newArrayList());
      deleteVolume(volume);
      deleteHardDisk(hardDisk);
   }

   public void testExecute() {
      Iterable<VirtualDisk<?>> vapps = strategy.execute(env.virtualMachine);
      assertNotNull(vapps);
      assertEquals(2, size(vapps));
   }

   public void testExecutePredicateWithoutResults() {
      Iterable<VirtualDisk<?>> vapps = strategy.execute(env.virtualMachine, new Predicate<VirtualDisk<?>>() {
         @Override
         public boolean apply(VirtualDisk<?> input) {
            return input.getSequence() < 0; // Impossible attachment order
         }
      });
      assertNotNull(vapps);
      assertEquals(size(vapps), 0);
   }

   public void testExecutePredicateWithResults() {
      Iterable<VirtualDisk<?>> vapps = strategy.execute(env.virtualMachine, new Predicate<VirtualDisk<?>>() {
         @Override
         public boolean apply(VirtualDisk<?> input) {
            return input instanceof HardDisk;
         }
      });
      assertNotNull(vapps);
      assertEquals(size(vapps), 1);
   }

   private Volume createVolume() {
      Tier tier = env.virtualDatacenter.findStorageTier(TierPredicates.name(env.tier.getName()));

      Volume volume = Volume.builder(env.context.getApiContext(), env.virtualDatacenter, tier)
            .name(PREFIX + "Hawaian volume").sizeInMb(32).build();
      volume.save();

      assertNotNull(volume.getId());
      assertNotNull(env.virtualDatacenter.getVolume(volume.getId()));

      return volume;
   }

   private void deleteVolume(final Volume volume) {
      Integer id = volume.getId();
      volume.delete();
      assertNull(env.virtualDatacenter.getVolume(id));
   }

   private HardDisk createHardDisk() {
      HardDisk hardDisk = HardDisk.builder(env.context.getApiContext(), env.virtualDatacenter).sizeInMb(64L).build();
      hardDisk.save();

      assertNotNull(hardDisk.getId());
      assertNotNull(hardDisk.getSequence());

      return hardDisk;
   }

   private void deleteHardDisk(final HardDisk hardDisk) {
      Integer id = hardDisk.getId();
      hardDisk.delete();
      assertNull(env.virtualDatacenter.getHardDisk(id));
   }
}
