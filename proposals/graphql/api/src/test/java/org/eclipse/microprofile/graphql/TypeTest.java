/*
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.microprofile.graphql;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;


/**
 * Simple test mainly as a placeholder for now.
 */
public class TypeTest {

    @Type(value = "Starship", description = "A starship in StarWars", fieldsOrder = {"name", "length", "id"})
    private static class Starship {
        private String id;
        private String name;
        private float length;
    }

    @Test
    public void testTypeAnnotationOnStarshipClass() throws Exception {
        Type type = Starship.class.getAnnotation(Type.class);
        assertEquals(type.value(), "Starship");
        assertEquals(type.description(), "A starship in StarWars");
        assertEquals(type.fieldsOrder(), new String[]{"name", "length", "id"});
    }
}
