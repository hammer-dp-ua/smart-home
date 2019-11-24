package ua.dp.hammer.smarthome.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "device_type_names")
public class DeviceTypeNameEntity {
   @Id
   @Column(name = "aa_id", unique = true, nullable = false)
   @SequenceGenerator(name="device_type_names_aa_id_seq",
                      sequenceName="device_type_names_aa_id_seq",
                      allocationSize = 1)
   // The sequence will be called separately before every INSERT, producing sequential numeric values.
   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="device_type_names_aa_id_seq")
   private Integer id;

   @NotNull
   @Column(name = "device_name")
   private String name;

   @NotNull
   @ManyToOne
   @JoinColumn(name = "device_type", nullable = false)
   private DeviceTypeEntity type;

   public Integer getId() {
      return id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public DeviceTypeEntity getType() {
      return type;
   }

   public void setType(DeviceTypeEntity type) {
      this.type = type;
   }
}
