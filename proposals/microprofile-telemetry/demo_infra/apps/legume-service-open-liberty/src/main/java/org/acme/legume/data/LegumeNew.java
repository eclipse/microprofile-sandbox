/**
 *
 * @author Bruno Baptista.
 * <a href="https://twitter.com/brunobat_">https://twitter.com/brunobat_</a>
 *
 */

package org.acme.legume.data;

import javax.validation.constraints.NotBlank;

public class LegumeNew {

    @NotBlank
    private String name;

    private String description;

    public LegumeNew(@NotBlank String name, String description) {
        this.name = name;
        this.description = description;
    }

    public LegumeNew() {}

    public static LegumeNewBuilder builder() {return new LegumeNewBuilder();}

    public @NotBlank String getName() {return this.name;}

    public String getDescription() {return this.description;}

    public void setName(@NotBlank String name) {this.name = name; }

    public void setDescription(String description) {this.description = description; }

    public String toString() {
        return "LegumeNew(name=" +
               this.getName() +
               ", description=" +
               this.getDescription() +
               ")";
    }

    public boolean equals(final Object o) {
        if (o == this) { return true; }
        if (!(o instanceof LegumeNew)) { return false; }
        final LegumeNew other = (LegumeNew) o;
        if (!other.canEqual((Object) this)) { return false; }
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) { return false; }
        return true;
    }

    protected boolean canEqual(final Object other) {return other instanceof LegumeNew;}

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        return result;
    }

    public static class LegumeNewBuilder {
        private @NotBlank String name;
        private String description;

        LegumeNewBuilder() {}

        public LegumeNewBuilder name(@NotBlank String name) {
            this.name = name;
            return this;
        }

        public LegumeNewBuilder description(String description) {
            this.description = description;
            return this;
        }

        public LegumeNew build() {
            return new LegumeNew(name, description);
        }

        public String toString() {
            return "LegumeNew.LegumeNewBuilder(name=" +
                   this.name +
                   ", description=" +
                   this.description +
                   ")";
        }
    }
}
