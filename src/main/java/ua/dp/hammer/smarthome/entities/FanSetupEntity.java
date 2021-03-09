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
@Table(name = "fan_setup")
public class FanSetupEntity {
   @Id
   @Column(name = "aa_id", unique = true, nullable = false)
   @SequenceGenerator(name="fan_setup_aa_id_seq",
         sequenceName="fan_setup_aa_id_seq",
         allocationSize = 1)
   // The sequence will be called separately before every INSERT, producing sequential numeric values.
   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="fan_setup_aa_id_seq")
   private Integer id;

   @NotNull
   @OneToOne(optional = false)
   @JoinColumn(name = "device_type_name")
   private DeviceSetupEntity typeName;

   @Column(name = "turn_on_humidity_threshold")
   private Float turnOnHumidityThreshold;

   @Column(name = "manually_turned_on_timeout_minutes")
   private Integer manuallyTurnedOnTimeoutMinutes;

   @Column(name = "after_falling_threshold_work_timeout_minutes")
   private Integer afterFallingThresholdWorkTimeoutMinutes;

   public Integer getId() {
      return id;
   }

   public DeviceSetupEntity getTypeName() {
      return typeName;
   }

   public void setTypeName(DeviceSetupEntity typeName) {
      this.typeName = typeName;
   }

   public Float getTurnOnHumidityThreshold() {
      return turnOnHumidityThreshold;
   }

   public void setTurnOnHumidityThreshold(Float turnOnHumidityThreshold) {
      this.turnOnHumidityThreshold = turnOnHumidityThreshold;
   }

   public Integer getManuallyTurnedOnTimeoutMinutes() {
      return manuallyTurnedOnTimeoutMinutes;
   }

   public void setManuallyTurnedOnTimeoutMinutes(Integer manuallyTurnedOnTimeoutMinutes) {
      this.manuallyTurnedOnTimeoutMinutes = manuallyTurnedOnTimeoutMinutes;
   }

   public Integer getAfterFallingThresholdWorkTimeoutMinutes() {
      return afterFallingThresholdWorkTimeoutMinutes;
   }

   public void setAfterFallingThresholdWorkTimeoutMinutes(Integer afterFallingThresholdWorkTimeoutMinutes) {
      this.afterFallingThresholdWorkTimeoutMinutes = afterFallingThresholdWorkTimeoutMinutes;
   }
}
