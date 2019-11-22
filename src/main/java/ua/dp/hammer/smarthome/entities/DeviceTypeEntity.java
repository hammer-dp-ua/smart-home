package ua.dp.hammer.smarthome.entities;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "device_types")
public class DeviceTypeEntity {
   @Id
   @Column(name = "aa_id")
   @GenericGenerator(name = "DeviceTypeEntityIdGenerator", strategy = "increment")
   private Integer id;

   @NotNull
   @Column(name = "type_id")
   @Enumerated(EnumType.STRING)
   private DeviceType type;

   public Integer getId() {
      return id;
   }

   public DeviceType getType() {
      return type;
   }

   public void setType(DeviceType type) {
      this.type = type;
   }
}
