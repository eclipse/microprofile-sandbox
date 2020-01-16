/*
 *******************************************************************************
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
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
 *******************************************************************************/

/**
 * Annotations to override the default values for RFC-7807 problem detail fields, for example:
 * <pre>
 *{@literal @}Type("https://example.com/probs/out-of-credit")
 *{@literal @}Title("You do not have enough credit.")
 *{@literal @}Status(FORBIDDEN)
 * public class OutOfCreditException extends RuntimeException {
 *    {@literal @}Instance private URI instance;
 *    {@literal @}Extension private int balance;
 *     private int cost;
 *    {@literal @}Extension private List<URI> accounts;
 *
 *    {@literal @}Detail public String getDetail() {
 *         return "Your current balance is " + balance + ", but that costs " + cost + ".";
 *     }
 *
 *     // ... constructors & getters
 * }
 * </pre>
 *
 * @since 1.0
 */
package org.eclipse.microprofile.problemdetails;
