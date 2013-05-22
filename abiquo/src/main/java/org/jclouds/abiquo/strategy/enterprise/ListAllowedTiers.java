/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.abiquo.strategy.enterprise;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.util.concurrent.Futures.allAsList;
import static com.google.common.util.concurrent.Futures.getUnchecked;
import static org.jclouds.abiquo.domain.DomainWrapper.wrap;

import java.util.List;
import java.util.concurrent.Callable;

import javax.annotation.Resource;
import javax.inject.Named;

import org.jclouds.Constants;
import org.jclouds.abiquo.AbiquoApi;
import org.jclouds.abiquo.domain.enterprise.Limits;
import org.jclouds.abiquo.domain.infrastructure.Datacenter;
import org.jclouds.abiquo.domain.infrastructure.Tier;
import org.jclouds.abiquo.reference.rest.ParentLinkName;
import org.jclouds.abiquo.strategy.ListEntities;
import org.jclouds.logging.Logger;
import org.jclouds.rest.ApiContext;

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
   protected final ApiContext<AbiquoApi> context;

   protected final ListeningExecutorService userExecutor;

   @Resource
   protected Logger logger = Logger.NULL;

   @Inject
   ListAllowedTiers(final ApiContext<AbiquoApi> context,
         @Named(Constants.PROPERTY_USER_THREADS) final ListeningExecutorService userExecutor) {
      this.context = checkNotNull(context, "context");
      this.userExecutor = checkNotNull(userExecutor, "userExecutor");
   }

   @Override
   public Iterable<Tier> execute(Limits limits) {
      return execute(userExecutor, limits);
   }

   @Override
   public Iterable<Tier> execute(Limits limits, Predicate<Tier> selector) {
      return execute(userExecutor, limits, selector);
   }

   public Iterable<Tier> execute(ListeningExecutorService executor, Limits limits) {
      Datacenter datacenter = checkNotNull(limits.getDatacenter(), "datacenter");
      List<RESTLink> tierLinks = limits.unwrap().searchLinks(ParentLinkName.TIER);

      return listConcurrentTiers(executor, tierLinks, datacenter);
   }

   public Iterable<Tier> execute(ListeningExecutorService executor, Limits limits, Predicate<Tier> selector) {
      return filter(execute(executor, limits), selector);
   }

   private Iterable<Tier> listConcurrentTiers(final ListeningExecutorService executor, List<RESTLink> tierLinks,
         final Datacenter datacenter) {
      ListenableFuture<List<TierDto>> futures = allAsList(transform(tierLinks,
            new Function<RESTLink, ListenableFuture<TierDto>>() {
               @Override
               public ListenableFuture<TierDto> apply(final RESTLink input) {
                  return executor.submit(new Callable<TierDto>() {
                     @Override
                     public TierDto call() throws Exception {
                        return context.getApi().getInfrastructureApi().getTier(datacenter.unwrap(), input.getId());
                     }
                  });
               }
            }));

      logger.trace("getting allowed tiers");
      return wrap(context, Tier.class, filter(getUnchecked(futures), notNull()));
   }

}
