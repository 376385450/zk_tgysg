package com.sinohealth.system.dto.common;

import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class IdAndName {
    private Long id;
    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IdAndName idAndName = (IdAndName) o;
        return Objects.equal(id, idAndName.id) &&
                Objects.equal(name, idAndName.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name);
    }
}

