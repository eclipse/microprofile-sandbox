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

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This interface offers various methods for capturing the context of the current thread
 * and applying it to various interfaces that are commonly used with completion stages
 * and executor services.  This allows you to contextualize specific actions that need
 * access to the context of the creator/submitter of the stage/task.
 *
 * <p>Example usage:</p>
 * <pre>
 * <code>&commat;Inject</code> ThreadContext threadContext;
 * ...
 * CompletableFuture&lt;Integer&gt; stage2 = stage1.thenApply(threadContext.withCurrentContext(function));
 * ...
 * Future&lt;Integer&gt; future = executor.submit(threadContext.withCurrentContext(callable));
 * </pre>
 *
 * <p>This interface is intentionally kept compatible with ContextService,
 * with the hope that its methods might one day be contributed to that specification.</p>
 */
public interface ThreadContext {
    /**
     * Identifier for all available thread context types that support capture
     * and propagation to other threads.
     * 
     * TODO Must not be used on 'unchanged' context, belongs on that JavaDoc.
     *
     * @see ManagedExecutorConfig#context
     * @see ThreadContextConfig#value
     */
    static final String ALL = "All";

    /**
     * Identifier for application context. Application context controls the
     * application component that is associated with a thread. It can determine
     * the thread context class loader as well as the set of resource references
     * that are available for lookup or resource injection. An empty/default
     * application context means that the thread is not associated with any
     * application.
     *
     * @see ManagedExecutorConfig#context
     * @see ThreadContextConfig
     */
    static final String APPLICATION = "Application";

    /**
     * Identifier for CDI context. CDI context controls the availability of CDI
     * scopes. An empty/default CDI context means that the thread does not have
     * access to the scope of the session, request, and so forth that created the
     * contextualized action.
     *
     * @see ManagedExecutorConfig#context
     * @see ThreadContextConfig
     */
    static final String CDI = "CDI";

    /**
     * Identifier for security context. Security context controls the credentials
     * that are associated with the thread. An empty/default security context
     * means that the thread is unauthenticated.
     * 
     * @see ManagedExecutorConfig#context
     * @see ThreadContextConfig
     */
    static final String SECURITY = "Security";

    /**
     * Identifier for transaction context. Transaction context controls the
     * active transaction scope that is associated with the thread.
     * Implementations are not expected to propagate transaction context across
     * threads. Instead, the concept of transaction context is provided for its
     * empty/default context, which means the active transaction on the thread
     * is suspended such that a new transaction can be started if so desired.
     * In most cases, the most desirable behavior will be to leave transaction
     * context unconfigured such that it is defaulted to empty (suspended),
     * in order to prevent dependent actions and tasks from accidentally
     * enlisting in transactions that are on the threads where they happen to
     * run.
     *
     * @see ThreadContextConfig#unchanged
     */
    static final String TRANSACTION = "Transaction";

    /**
     * <p>Wraps a <code>BiConsumer</code> with context that is captured from the thread that invokes
     * <code>withCurrentContext</code>.</p>
     * 
     * <p>When <code>accept</code> is invoked on the proxy instance,
     * context is first established on the thread that will run the <code>accept</code> method,
     * then the <code>accept</code> method of the provided <code>BiConsumer</code> is invoked.
     * Finally, the previous context is restored on the thread, and control is returned to the invoker.</p> 
     * 
     * @param <T> type of first parameter to consumer.
     * @param <U> type of second parameter to consumer.
     * @param consumer instance to contextualize.
     * @return contextualized proxy instance that wraps execution of the <code>accept</code> method with context.
     */
    <T, U> BiConsumer<T, U> withCurrentContext(BiConsumer<T, U> consumer);

    /**
     * <p>Wraps a <code>BiFunction</code> with context that is captured from the thread that invokes
     * <code>withCurrentContext</code>.</p>
     * 
     * <p>When <code>apply</code> is invoked on the proxy instance,
     * context is first established on the thread that will run the <code>apply</code> method,
     * then the <code>apply</code> method of the provided <code>BiFunction</code> is invoked.
     * Finally, the previous context is restored on the thread, and the result of the
     * <code>BiFunction</code> is returned to the invoker.</p>
     *
     * @param <T> type of first parameter to function.
     * @param <U> type of second parameter to function.
     * @param <R> function result type.
     * @param function instance to contextualize.
     * @return contextualized proxy instance that wraps execution of the <code>apply</code> method with context.
     */
    <T, U, R> BiFunction<T, U, R> withCurrentContext(BiFunction<T, U, R> function);

    /**
     * <p>Wraps a <code>Callable</code> with context that is captured from the thread that invokes
     * <code>withCurrentContext</code>.</p>
     * 
     * <p>When <code>call</code> is invoked on the proxy instance,
     * context is first established on the thread that will run the <code>call</code> method,
     * then the <code>call</code> method of the provided <code>Callable</code> is invoked.
     * Finally, the previous context is restored on the thread, and the result of the
     * <code>Callable</code> is returned to the invoker.</p> 
     *
     * @param <R> callable result type.
     * @param callable instance to contextualize.
     * @return contextualized proxy instance that wraps execution of the <code>call</code> method with context.
     */
    <R> Callable<R> withCurrentContext(Callable<R> callable);

    /**
     * <p>Wraps a <code>Consumer</code> with context that is captured from the thread that invokes
     * <code>withCurrentContext</code>.</p>
     * 
     * <p>When <code>accept</code> is invoked on the proxy instance,
     * context is first established on the thread that will run the <code>accept</code> method,
     * then the <code>accept</code> method of the provided <code>Consumer</code> is invoked.
     * Finally, the previous context is restored on the thread, and control is returned to the invoker.</p> 
     *
     * @param <T> type of parameter to consumer.
     * @param consumer instance to contextualize.
     * @return contextualized proxy instance that wraps execution of the <code>accept</code> method with context.
     */
    <T> Consumer<T> withCurrentContext(Consumer<T> consumer);

    /**
     * <p>Wraps a <code>Function</code> with context that is captured from the thread that invokes
     * <code>withCurrentContext</code>.</p>
     * 
     * <p>When <code>apply</code> is invoked on the proxy instance,
     * context is first established on the thread that will run the <code>apply</code> method,
     * then the <code>apply</code> method of the provided <code>Function</code> is invoked.
     * Finally, the previous context is restored on the thread, and the result of the
     * <code>Function</code> is returned to the invoker.</p> 
     *
     * @param <T> type of parameter to function.
     * @param <R> function result type.
     * @param function instance to contextualize.
     * @return contextualized proxy instance that wraps execution of the <code>apply</code> method with context.
     */
    <T, R> Function<T, R> withCurrentContext(Function<T, R> function);

    /**
     * <p>Wraps a <code>Runnable</code> with context that is captured from the thread that invokes
     * <code>withCurrentContext</code>.</p>
     * 
     * <p>When <code>run</code> is invoked on the proxy instance,
     * context is first established on the thread that will run the <code>run</code> method,
     * then the <code>run</code> method of the provided <code>Runnable</code> is invoked.
     * Finally, the previous context is restored on the thread, and control is returned to the invoker.</p> 
     * 
     * @param runnable instance to contextualize.
     * @return contextualized proxy instance that wraps execution of the <code>run</code> method with context.
     */
    Runnable withCurrentContext(Runnable runnable);

    /**
     * <p>Wraps a <code>Supplier</code> with context captured from the thread that invokes
     * <code>withCurrentContext</code>.</p>
     * 
     * <p>When <code>supply</code> is invoked on the proxy instance,
     * context is first established on the thread that will run the <code>supply</code> method,
     * then the <code>supply</code> method of the provided <code>Supplier</code> is invoked.
     * Finally, the previous context is restored on the thread, and the result of the
     * <code>Supplier</code> is returned to the invoker.</p> 
     *
     * @param <R> supplier result type.
     * @param supplier instance to contextualize.
     * @return contextualized proxy instance that wraps execution of the <code>supply</code> method with context.
     */
    <R> Supplier<R> withCurrentContext(Supplier<R> supplier);
}