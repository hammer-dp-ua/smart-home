package ua.dp.hammer.smarthome.entities;

import ua.dp.hammer.smarthome.models.setup.DeviceType;

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

   @Column(name = "keep_alive_interval_sec")
   private Integer keepAliveIntervalSec;

   public Integer getId() {
      return id;
   }

   public DeviceType getType() {
      return type;
   }

   public void setType(DeviceType type) {
      this.type = type;
   }

   public Integer getKeepAliveIntervalSec() {
      return keepAliveIntervalSec;
   }

   public void setKeepAliveIntervalSec(Integer keepAliveIntervalSec) {
      this.keepAliveIntervalSec = keepAliveIntervalSec;
   }
}
