/*
 * Copyright (c) 2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.microprofile.concurrent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * <p>A container-managed executor service that creates instances of CompletableFuture,
 * which it backs as the default asynchronous execution facility, both for the
 * CompletableFuture itself as well as all dependent stages created from it,
 * as well as all dependent stages created from those, and so on.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * <code>&commat;Inject</code> ManagedExecutor executor;
 * ...
 * CompletableFuture&lt;Integer&gt; future = executor
 *    .supplyAsync(supplier)
 *    .thenApplyAsync(function1)
 *    .thenApply(function2)
 *    ...
 * </pre>
 *
 * <p>This specification allows for managed executors that do not capture and propagate thread context,
 * which can offer better performance. If thread context propagation is desired only for specific stages,
 * the <code>ThreadContext.withCurrentContext</code> API can be used to propagate thread context to
 * individual actions.</p>
 *
 * <p>Example of single action with context propagation:</p>
 * <pre>
 * CompletableFuture&lt;?&gt; future = executor
 *    .runAsync(runnable1)
 *    .thenRun(threadContext.withCurrentContext(runnable2))
 *    .thenRunAsync(runnable3)
 *    ...
 * </pre>
 *
 * <p>For managed executors that are defined as capturing and propagating thread context,
 * it must be done in a consistent manner. Thread context is captured from the thread that creates
 * a completion stage and is applied to the thread that runs the action, being removed afterward.
 * When dependent stages are created from the completion stage, and likewise from any dependent stages
 * created from those, and so on, thread context is captured from the thread that creates the dependent
 * stage. This guarantees that the action performed by each stage always runs under the thread context
 * of the code that creates the stage. When applied to the ExecutorService methods,
 * <code>execute</code>, <code>invokeAll</code>, <code>invokeAny</code>, <code>submit</code>,
 * thread context is captured from the thread invoking the request and propagated to thread that runs
 * the task, being removed afterward.</p>
 *
 * <p>This interface is intentionally kept compatible with ManagedExecutorService,
 * with the hope that its methods might one day be contributed to that specification.</p>
 *
 * <p>Managed executors are managed by the container, not be the user. Therefore, all
 * life cycle methods must raise IllegalStateException. This includes:
 * awaitTermination, isShutdown, isTerminated, shutdown, shutdownNow</p>
 */
public interface ManagedExecutor extends ExecutorService {
    /**
     * <p>Returns a new CompletableFuture that is already completed with the specified value.</p>
     *
     * <p>This executor is the default asynchronous execution facility for the new completion stage
     * that is returned by this method and all dependent stages that are created from it,
     * and all dependent stages that are created from those, and so forth.</p>
     *
     * @param <U> result type of the completion stage.
     * @return the new completion stage.
     */
    <U> CompletableFuture<U> completedFuture(U value);

    /**
     * <p>Returns a new CompletionStage that is already completed with the specified value.</p>
     *
     * <p>This executor is the default asynchronous execution facility for the new completion stage
     * that is returned by this method and all dependent stages that are created from it,
     * and all dependent stages that are created from those, and so forth.</p>
     *
     * @param <U> result type of the completion stage.
     * @return the new completion stage.
     */
    <U> CompletionStage<U> completedStage(U value);

    /**
     * <p>Returns a new CompletableFuture that is already exceptionally completed with the specified Throwable.</p>
     *
     * <p>This executor is the default asynchronous execution facility for the new completion stage
     * that is returned by this method and all dependent stages that are created from it,
     * and all dependent stages that are created from those, and so forth.</p>
     *
     * @param <U> result type of the completion stage.
     * @return the new completion stage.
     */
    <U> CompletableFuture<U> failedFuture(Throwable ex);

    /**
     * <p>Returns a new CompletionStage that is already exceptionally completed with the specified Throwable.</p>
     *
     * <p>This executor is the default asynchronous execution facility for the new completion stage
     * that is returned by this method and all dependent stages that are created from it,
     * and all dependent stages that are created from those, and so forth.</p>
     *
     * @param <U> result type of the completion stage.
     * @return the new completion stage.
     */
    <U> CompletionStage<U> failedStage(Throwable ex);

    /**
     * <p>Returns a new CompletableFuture that is completed by a task running in this executor
     * after it runs the given action.</p>
     *
     * <p>This executor is the default asynchronous execution facility for the new completion stage
     * that is returned by this method and all dependent stages that are created from it,
     * and all dependent stages that are created from those, and so forth.</p>
     *
     * @param runnable the action to run before completing the returned completion stage.
     * @return the new completion stage.
     */
    CompletableFuture<Void> runAsync(Runnable runnable);

    /**
     * <p>Returns a new CompletableFuture that is completed by a task running in this executor
     * after it runs the given action.</p>
     *
     * <p>This executor is the default asynchronous execution facility for the new completion stage
     * that is returned by this method and all dependent stages that are created from it,
     * and all dependent stages that are created from those, and so forth.</p>
     *
     * @param <U> result type of the supplier and completion stage.
     * @param supplier an action returning the value to be used to complete the returned completion stage.
     * @return the new completion stage.
     */
    <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier);
}