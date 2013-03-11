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

package org.jclouds.abiquo.strategy.enterprise;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.filter;
import static org.jclouds.abiquo.domain.DomainWrapper.wrap;
import static org.jclouds.concurrent.FutureIterables.transformParallel;

import java.util.List;

import javax.annotation.Resource;
import javax.inject.Named;

import org.jclouds.Constants;
import org.jclouds.abiquo.AbiquoApi;
import org.jclouds.abiquo.AbiquoAsyncApi;
import org.jclouds.abiquo.domain.enterprise.Limits;
import org.jclouds.abiquo.domain.infrastructure.Datacenter;
import org.jclouds.abiquo.domain.infrastructure.Tier;
import org.jclouds.abiquo.reference.rest.ParentLinkName;
import org.jclouds.abiquo.strategy.ListEntities;
import org.jclouds.logging.Logger;
import org.jclouds.rest.RestContext;

import com.abiquo.model.rest.RESTLink;
import com.abiquo.server.core.infrastructure.storage.TierDto;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * List the storage tiers that are allowed to an enterprise in a given
 * Datacenter.
 * 
 * @author Ignasi Barrera
 */
@Singleton
public class ListAllowedTiers implements ListEntities<Tier, Limits> {
   protected final RestContext<AbiquoApi, AbiquoAsyncApi> context;

   protected final ListeningExecutorService userExecutor;

   @Resource
   protected Logger logger = Logger.NULL;

   @Inject(optional = true)
   @Named(Constants.PROPERTY_REQUEST_TIMEOUT)
   protected Long maxTime;

   @Inject
   ListAllowedTiers(final RestContext<AbiquoApi, AbiquoAsyncApi> context,
         @Named(Constants.PROPERTY_USER_THREADS) final ListeningExecutorService userExecutor) {
      this.context = checkNotNull(context, "context");
      this.userExecutor = checkNotNull(userExecutor, "userExecutor");
   }

   @Override
   public Iterable<Tier> execute(Limits limits) {
      Datacenter datacenter = checkNotNull(limits.getDatacenter(), "datacenter");
      List<RESTLink> tierLinks = limits.unwrap().searchLinks(ParentLinkName.TIER);

      return listConcurrentTiers(tierLinks, datacenter);
   }

   @Override
   public Iterable<Tier> execute(Limits limits, Predicate<Tier> selector) {
      return filter(execute(limits), selector);
   }

   private Iterable<Tier> listConcurrentTiers(List<RESTLink> tierLinks, final Datacenter datacenter) {
      Iterable<TierDto> tiers = filter(
            transformParallel(tierLinks, new Function<RESTLink, ListenableFuture<? extends TierDto>>() {
               @Override
               public ListenableFuture<TierDto> apply(final RESTLink input) {
                  return context.getAsyncApi().getInfrastructureApi().getTier(datacenter.unwrap(), input.getId());
               }
            }, userExecutor, maxTime, logger, "getting allowed tiers"), notNull());

      return wrap(context, Tier.class, tiers);
   }

}
