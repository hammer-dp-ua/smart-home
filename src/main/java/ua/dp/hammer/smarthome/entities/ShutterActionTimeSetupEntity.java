package ua.dp.hammer.smarthome.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "shutters_action_time_setup")
public class ShutterActionTimeSetupEntity {
   @Id
   @Column(name = "aa_id", unique = true, nullable = false)
   @SequenceGenerator(name="shutters_action_time_setup_aa_id_seq",
         sequenceName="shutters_action_time_setup_aa_id_seq",
         allocationSize = 1)
   // The sequence will be called separately before every INSERT, producing sequential numeric values.
   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="shutters_action_time_setup_aa_id_seq")
   private Integer id;

   @NotNull
   @OneToOne(optional = false)
   @JoinColumn(name = "device_type_name")
   private DeviceTypeNameEntity typeName;

   @Column(name = "action_time")
   private Integer actionTime;

   public Integer getId() {
      return id;
   }

   public DeviceTypeNameEntity getTypeName() {
      return typeName;
   }

   public Integer getActionTime() {
      return actionTime;
   }
}
