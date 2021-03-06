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
package org.jclouds.nodepool.internal;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.jclouds.nodepool.config.NodePoolProperties.BACKEND_GROUP;
import static org.jclouds.nodepool.config.NodePoolProperties.MAX_SIZE;
import static org.jclouds.nodepool.config.NodePoolProperties.MIN_SIZE;
import static org.jclouds.nodepool.config.NodePoolProperties.POOL_ADMIN_ACCESS;
import static org.jclouds.nodepool.config.NodePoolProperties.REMOVE_DESTROYED;

import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.compute.reference.ComputeServiceConstants;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.logging.Logger;
import org.jclouds.nodepool.Backend;
import org.jclouds.scriptbuilder.statements.login.AdminAccess;

import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

/**
 * An eager {@link NodePoolComputeService}. Eagerly builds and maintains a pool of nodes. It's only
 * "started" after min nodes are allocated and available.
 * 
 * @author David Alves
 * 
 */
@Singleton
public class EagerNodePoolComputeServiceAdapter extends BaseNodePoolComputeServiceAdapter {

   @Resource
   @Named(ComputeServiceConstants.COMPUTE_LOGGER)
   protected Logger logger = Logger.NULL;

   private final int maxSize;
   private final int minSize;
   private final boolean removeDestroyed;

   @Inject
   public EagerNodePoolComputeServiceAdapter(@Backend Supplier<ComputeService> backendComputeService,
            @Backend Supplier<Template> backendTemplate, @Named(BACKEND_GROUP) String poolGroupPrefix,
            @Named(MAX_SIZE) int maxSize, @Named(MIN_SIZE) int minSize,
            @Named(REMOVE_DESTROYED) boolean removeDestroyed, NodeMetadataStore storage,
            @Named(POOL_ADMIN_ACCESS) String poolNodeAdminAccess, AdminAccess.Configuration configuration) {
      super(backendComputeService, backendTemplate, poolGroupPrefix, storage, poolNodeAdminAccess, configuration);
      this.maxSize = maxSize;
      this.minSize = minSize;
      this.removeDestroyed = removeDestroyed;
   }

   @PostConstruct
   public void startEagerPool() {
      Set<? extends NodeMetadata> backendNodes = getBackendNodes();
      int currentNodes = backendNodes.size();
      int newNodes = backendNodes.size() < minSize ? minSize - backendNodes.size() : 0;
      logger.info(
               ">> initializing nodepool [backend provider: %s]. [existing nodes: %s, min nodes: %s, allocating: %s ]",
               backendComputeService.get().getClass().getSimpleName(), currentNodes, minSize, newNodes);
      if (backendNodes.size() < minSize) {
         addToPool(minSize - backendNodes.size());
      }
      logger.info("<< pool initialized.");
   }

   @Override
   public NodeWithInitialCredentials createNodeWithGroupEncodedIntoName(String group, String name, Template template) {
      int count = 1;
      synchronized (this) {
         TemplateOptions options = template.getOptions().clone();

         // if no user is provided we set the pool's user
         if (options.getLoginUser() == null) {
            options.overrideLoginCredentials(LoginCredentials.fromCredentials(checkNotNull(initialCredentialsBuilder
                     .build().getAdminCredentials())));
         }

         logger.info(">> assigning pool node to frontend group %s", group);
         Set<NodeMetadata> backendNodes = getBackendNodes();
         checkState(!backendNodes.isEmpty());
         Set<NodeMetadata> frontendNodes = metadataStore.loadAll(backendNodes);
         checkState(frontendNodes.size() + count <= maxSize,
                  "cannot add more nodes to pool [requested: %s, current: %s, max: %s]", count, frontendNodes.size(),
                  maxSize);

         SetView<NodeMetadata> availableNodes = Sets.difference(backendNodes, frontendNodes);

         if (availableNodes.size() < 1) {
            if (backendNodes.size() < maxSize && backendNodes.size() + count <= maxSize) {
               logger.info(
                        ">> all pool nodes are assigned, requiring additional nodes [requested: %s, current: %s, next: %s, max: %s]",
                        count, frontendNodes.size(), frontendNodes.size() + 1, maxSize);
               addToPool(count);
               // update backend and available sets, no need to update frontend
               backendNodes = getBackendNodes();
               availableNodes = Sets.difference(backendNodes, frontendNodes);
               logger.info("<< additional nodes added to the pool and ready");
            } else {
               logger.error("maximum pool size reached (%s)", maxSize);
               throw new IllegalStateException(String.format("maximum pool size reached (%s)", maxSize));
            }
         }
         NodeMetadata userNode = Iterables.get(availableNodes, 0);
         NodeMetadata node = metadataStore.store(userNode, options, group);
         logger.info("pool node assigned");
         return new NodeWithInitialCredentials(node);
      }
   }

   @Override
   public synchronized void destroyNode(String id) {
      checkState(getNode(id) != null);
      logger.info(">> destroying node %s", id);
      metadataStore.deleteMapping(id);
      if (removeDestroyed) {
         backendComputeService.get().destroyNode(id);
         if (currentSize() < minSize) {
            logger.info(">> policy is remove destroyed node and pool "
                     + "would fall below minsize, replacing node with id %s", id);
            Set<? extends NodeMetadata> replacement = addToPool(1);
            logger.info("<< node %s replaced with %s", id, Iterables.getOnlyElement(replacement));
         }
      }
      // TODO we should allow the user to hook a way to "clean" the node
      else {

      }
      logger.info("<< node destroyed %s", id);
   }

   @Override
   public int currentSize() {
      return getBackendNodes().size();
   }

   @Override
   public int idleNodes() {
      Set<NodeMetadata> backendNodes = getBackendNodes();
      Set<NodeMetadata> frontendNodes = metadataStore.loadAll(backendNodes);
      return backendNodes.size() - frontendNodes.size();
   }

   @Override
   public int maxNodes() {
      return maxSize;
   }

   @Override
   public int minNodes() {
      return minSize;
   }

   @Override
   public int usedNodes() {
      return metadataStore.loadAll(getBackendNodes()).size();
   }

}
