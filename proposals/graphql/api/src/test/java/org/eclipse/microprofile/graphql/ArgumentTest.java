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

import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * Simple test mainly as a placeholder for now.
 */
public class ArgumentTest {

    private static class Character {

        private String name;

        public Character(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Query(value = "friendsOf", description = "Returns all the friends of a character")
        public List<Character> getFriendsOf(
                @Argument(value = "whomFriends", defaultValue = "Han Solo", description = "Whom friends to fetch") Character character) {
            if (character.getName().equals("Han Solo")) {
                return Collections.singletonList(new Character("Chewbacca"));
            }
            return Collections.emptyList();
        }
    }

    @Test
    public void testArgumentAnnotationOnCharacterParameter() throws Exception {
        Argument argument = (Argument)Character.class.getDeclaredMethod("getFriendsOf", Character.class).getParameterAnnotations()[0][0];
        assertEquals(argument.value(), "whomFriends");
        assertEquals(argument.description(), "Whom friends to fetch");
        assertEquals(argument.defaultValue(), "Han Solo");
    }
}
