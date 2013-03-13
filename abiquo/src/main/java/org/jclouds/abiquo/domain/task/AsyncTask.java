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

package org.jclouds.abiquo.domain.task;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.jclouds.abiquo.AbiquoApi;
import org.jclouds.abiquo.domain.DomainWrapper;
import org.jclouds.abiquo.reference.ValidationErrors;
import org.jclouds.abiquo.reference.rest.ParentLinkName;
import org.jclouds.http.HttpResponse;
import org.jclouds.http.functions.ParseXMLWithJAXB;
import org.jclouds.rest.ApiContext;

import com.abiquo.model.rest.RESTLink;
import com.abiquo.model.transport.SingleResourceTransportDto;
import com.abiquo.server.core.task.TaskDto;
import com.abiquo.server.core.task.enums.TaskState;
import com.abiquo.server.core.task.enums.TaskType;
import com.google.inject.TypeLiteral;

/**
 * Adds generic high level functionality to {TaskDto}.
 * 
 * @author Ignasi Barrera
 * @author Francesc Montserrat
 * @param <T>
 *           The type of the result generated by the task.
 * @param <D>
 *           The type of the dto generated by the task.
 */
public abstract class AsyncTask<T extends DomainWrapper<D>, D extends SingleResourceTransportDto> extends
      DomainWrapper<TaskDto> {
   /** The type of the result generated by the task. */
   private Class<T> resultDomainClass;

   /** The type of the dto generated by the task. */
   private Class<D> resultDtoClass;

   /**
    * Constructor to be used only by the builder.
    */
   protected AsyncTask(final ApiContext<AbiquoApi> context, final TaskDto target, final Class<T> resultDomainClass,
         final Class<D> resultDtoClass) {
      super(context, target);
      this.resultDomainClass = resultDomainClass;
      this.resultDtoClass = resultDtoClass;
   }

   // Domain operations

   /**
    * Refresh the state of the task.
    */
   @Override
   public void refresh() {
      RESTLink self = checkNotNull(target.searchLink("self"), ValidationErrors.MISSING_REQUIRED_LINK + "self");

      target = context.getApi().getTaskApi().getTask(self);
   }

   public T getResult() {
      RESTLink link = target.searchLink(ParentLinkName.TASK_RESULT);
      if (link == null) {
         // The task may still be in progress or have failed
         return null;
      }

      HttpResponse response = context.getApi().get(link);
      ParseXMLWithJAXB<D> parser = new ParseXMLWithJAXB<D>(context.utils().xml(), TypeLiteral.get(resultDtoClass));

      return wrap(context, resultDomainClass, parser.apply(response));
   }

   // Children access

   /**
    * Get the individual jobs that compose the current task.
    */
   public List<AsyncJob> getJobs() {
      return wrap(context, AsyncJob.class, target.getJobs().getCollection());
   }

   // Conversion helpers

   public ConversionTask asConversionTask() {
      return ConversionTask.class.cast(this);
   }

   public VirtualMachineTask asVirtualMachineTask() {
      return VirtualMachineTask.class.cast(this);
   }

   public VirtualMachineTemplateTask asVirtualMachineTemplateTask() {
      return VirtualMachineTemplateTask.class.cast(this);
   }

   // Delegate methods

   public String getOwnerId() {
      return target.getOwnerId();
   }

   public TaskState getState() {
      return target.getState();
   }

   public String getTaskId() {
      return target.getTaskId();
   }

   public long getTimestamp() {
      return target.getTimestamp();
   }

   public TaskType getType() {
      return target.getType();
   }

   public String getUserId() {
      return target.getUserId();
   }

   @Override
   public String toString() {
      return "AsyncTask [taskId=" + getTaskId() + ", ownerId=" + getOwnerId() + ", timestamp=" + getTimestamp()
            + ", userId=" + getUserId() + ", state=" + getState() + ", type=" + getType() + "]";
   }
}
