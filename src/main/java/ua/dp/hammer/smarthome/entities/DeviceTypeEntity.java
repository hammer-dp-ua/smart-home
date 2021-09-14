package ua.dp.hammer.smarthome.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "device_types")
public class DeviceTypeEntity {
   @Id
   @Column(name = "aa_id", unique = true, nullable = false)
   @SequenceGenerator(name="device_types_aa_id_seq",
         sequenceName="device_types_aa_id_seq",
         allocationSize = 1)
   // The sequence will be called separately before every INSERT, producing sequential numeric values.
   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="device_types_aa_id_seq")
   private Integer id;

   @NotNull
   @Column(name = "type_id")
   private String type;

   @Column(name = "keep_alive_interval_sec")
   private Integer keepAliveIntervalSec;

   public Integer getId() {
      return id;
   }

   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public Integer getKeepAliveIntervalSec() {
      return keepAliveIntervalSec;
   }

   public void setKeepAliveIntervalSec(Integer keepAliveIntervalSec) {
      this.keepAliveIntervalSec = keepAliveIntervalSec;
   }
}
