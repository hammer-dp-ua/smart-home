package ua.dp.hammer.smarthome.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "device_types")
public class DeviceTypeEntity {
   @Id
   @Column(name = "aa_id")
   @GeneratedValue(strategy = GenerationType.AUTO)
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
}
