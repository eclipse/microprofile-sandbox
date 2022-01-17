/**
 *
 * @author Bruno Baptista.
 * <a href="https://twitter.com/brunobat_">https://twitter.com/brunobat_</a>
 *
 */

package org.acme.legume.data;

import javax.validation.constraints.NotBlank;

public class LegumeItem {
    private String id;

    @NotBlank
    private String name;

    private String description;

    public LegumeItem(String id, @NotBlank String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public LegumeItem() {}

    public static LegumeItemBuilder builder() {return new LegumeItemBuilder();}

    public String getId() {return this.id;}

    public @NotBlank String getName() {return this.name;}

    public String getDescription() {return this.description;}

    public void setId(String id) {this.id = id; }

    public void setName(@NotBlank String name) {this.name = name; }

    public void setDescription(String description) {this.description = description; }

    public String toString() {
        return "LegumeItem(id=" +
               this.getId() +
               ", name=" +
               this.getName() +
               ", description=" +
               this.getDescription() +
               ")";
    }

    public boolean equals(final Object o) {
        if (o == this) { return true; }
        if (!(o instanceof LegumeItem)) { return false; }
        final LegumeItem other = (LegumeItem) o;
        if (!other.canEqual((Object) this)) { return false; }
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) { return false; }
        return true;
    }

    protected boolean canEqual(final Object other) {return other instanceof LegumeItem;}

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        return result;
    }

    public static class LegumeItemBuilder {
        private String id;
        private @NotBlank String name;
        private String description;

        LegumeItemBuilder() {}

        public LegumeItemBuilder id(String id) {
            this.id = id;
            return this;
        }

        public LegumeItemBuilder name(@NotBlank String name) {
            this.name = name;
            return this;
        }

        public LegumeItemBuilder description(String description) {
            this.description = description;
            return this;
        }

        public LegumeItem build() {
            return new LegumeItem(id, name, description);
        }

        public String toString() {
            return "LegumeItem.LegumeItemBuilder(id=" +
                   this.id +
                   ", name=" +
                   this.name +
                   ", description=" +
                   this.description +
                   ")";
        }
    }
}
