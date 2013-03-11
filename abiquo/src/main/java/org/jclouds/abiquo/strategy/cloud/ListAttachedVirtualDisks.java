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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static org.jclouds.abiquo.domain.DomainWrapper.wrap;

import javax.inject.Singleton;

import org.jclouds.abiquo.AbiquoApi;
import org.jclouds.abiquo.AbiquoAsyncApi;
import org.jclouds.abiquo.domain.cloud.HardDisk;
import org.jclouds.abiquo.domain.cloud.VirtualDisk;
import org.jclouds.abiquo.domain.cloud.VirtualMachine;
import org.jclouds.abiquo.domain.cloud.Volume;
import org.jclouds.abiquo.domain.util.LinkUtils;
import org.jclouds.abiquo.rest.internal.ExtendedUtils;
import org.jclouds.abiquo.strategy.ListEntities;
import org.jclouds.http.HttpResponse;
import org.jclouds.http.functions.ParseXMLWithJAXB;
import org.jclouds.rest.RestContext;

import com.abiquo.model.rest.RESTLink;
import com.abiquo.server.core.infrastructure.storage.DiskManagementDto;
import com.abiquo.server.core.infrastructure.storage.VolumeManagementDto;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import com.google.inject.TypeLiteral;

/**
 * List all {@link HardDisk} and {@link Volume} attached to a
 * {@link VirtualMachine}.
 * 
 * @author Ignasi Barrera
 */
@Singleton
public class ListAttachedVirtualDisks implements ListEntities<VirtualDisk<?>, VirtualMachine> {
   protected final RestContext<AbiquoApi, AbiquoAsyncApi> context;

   protected final ExtendedUtils extendedUtils;

   @Inject
   public ListAttachedVirtualDisks(final RestContext<AbiquoApi, AbiquoAsyncApi> context,
         final ExtendedUtils extendedUtils) {
      this.context = checkNotNull(context, "context");
      this.extendedUtils = checkNotNull(extendedUtils, "extendedUtils");
   }

   @Override
   public Iterable<VirtualDisk<?>> execute(VirtualMachine parent) {
      parent.refresh();
      Iterable<RESTLink> diskLinks = LinkUtils.filterDiskLinks(parent.unwrap().getLinks());
      return listVirtualDisks(diskLinks);
   }

   @Override
   public Iterable<VirtualDisk<?>> execute(VirtualMachine parent, Predicate<VirtualDisk<?>> selector) {
      return filter(execute(parent), selector);
   }

   private Iterable<VirtualDisk<?>> listVirtualDisks(final Iterable<RESTLink> diskLinks) {
      return transform(diskLinks, new Function<RESTLink, VirtualDisk<?>>() {
         @Override
         public VirtualDisk<?> apply(final RESTLink input) {
            HttpResponse response = extendedUtils.getAbiquoHttpClient().get(input);

            if (input.getType().equals(DiskManagementDto.BASE_MEDIA_TYPE)) {
               ParseXMLWithJAXB<DiskManagementDto> parser = new ParseXMLWithJAXB<DiskManagementDto>(extendedUtils
                     .getXml(), TypeLiteral.get(DiskManagementDto.class));

               return wrap(context, HardDisk.class, parser.apply(response));
            } else if (input.getType().equals(VolumeManagementDto.BASE_MEDIA_TYPE)) {
               ParseXMLWithJAXB<VolumeManagementDto> parser = new ParseXMLWithJAXB<VolumeManagementDto>(extendedUtils
                     .getXml(), TypeLiteral.get(VolumeManagementDto.class));

               return wrap(context, Volume.class, parser.apply(response));
            } else {
               throw new IllegalArgumentException("Unsupported media type: " + input.getType());
            }
         }
      });
   }
}
