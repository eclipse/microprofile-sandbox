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
package org.eclipse.microprofile.concurrent.spi;

/**
 * <p>Represents context that is applied to a particular thread, along with any
 * state that is associated with it or that is necessary for restoring the
 * previous context afterward.</p>
 *
 * <p>When the context is no longer needed on the thread, the
 * <code>ManagedExecutor</code> or <code>ThreadContext</code> must invoke the
 * <code>end</code> method.</p>
 */
public interface ActiveThreadContext {
    /**
     * <p>Invoked by the <code>ManagedExecutor</code> or
     * <code>ThreadContext</code> to remove the thread context represented by
     * this <code>ActiveThreadContext</code> instance and restore the previous
     * context that was on the thread before the <code>ActiveThreadContext</code>
     * was started on the thread. The <code>ManagedExecutor</code> or
     * <code>ThreadContext</code> must invoke the <code>end</code> method exactly
     * once for each <code>ActiveThreadContext</code> instance that it creates.</p>
     *
     * <p>Typically, patterns such as the following will be observed:</p>
     * <pre><code>
     * activeContextA1 = contextSnapshotA.begin();
     * activeContextB1 = contextSnapshotB.begin();
     * activeContextC1 = contextSnapshotC.begin();
     * ...
     * activeContextC1.end();
     * activeContextB1.end();
     * activeContextA1.end();
     * </code></pre>
     *
     * <p>However, more advanced sequences such as the following are also valid:</p>
     * <pre><code>
     * activeContextA1 = contextSnapshotA.begin();
     * activeContextB1 = contextSnapshotB.begin();
     * ...
     * activeContextC1 = contextSnapshotC.begin();
     * ...
     * activeContextC1.end();
     * ...
     * activeContextB2 = contextSnapshotB.begin();
     * activeContextC2 = contextSnapshotC.begin();
     * ...
     * activeContextC2.end();
     * activeContextB2.end();
     * ...
     * activeContextB1.end();
     * activeContextA1.end();
     * </code></pre>
     *
     * @throws IllegalStateException if invoked more than once on the same instance.
     */
    void end() throws IllegalStateException;
}