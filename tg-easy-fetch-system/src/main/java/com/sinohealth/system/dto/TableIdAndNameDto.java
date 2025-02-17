package com.sinohealth.system.dto;

import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class TableIdAndNameDto {
    private Long id;
    private String tableName;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TableIdAndNameDto idAndName = (TableIdAndNameDto) o;
        return Objects.equal(id, idAndName.id) &&
                Objects.equal(tableName, idAndName.tableName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, tableName);
    }
}

