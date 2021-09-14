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
import java.time.LocalDateTime;

@Entity
@Table(name = "alarm_sensors_data")
public class AlarmSensorEntity {
   @Id
   @Column(name = "aa_id", unique = true, nullable = false)
   @SequenceGenerator(name="alarm_sensors_data_aa_id_seq",
         sequenceName="alarm_sensors_data_aa_id_seq",
         allocationSize = 1)
   // The sequence will be called separately before every INSERT, producing sequential numeric values.
   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="alarm_sensors_data_aa_id_seq")
   private Integer id;

   @NotNull
   @OneToOne(optional = false)
   @JoinColumn(name = "device_type_name")
   private DeviceSetupEntity deviceSetup;

   @OneToOne(optional = true)
   @JoinColumn(name = "source")
   private AlarmSourceSetupEntity source;

   @NotNull
   @Column(name = "alarm_dt")
   private LocalDateTime alarmDateTime;

   public Integer getId() {
      return id;
   }

   public DeviceSetupEntity getDeviceSetup() {
      return deviceSetup;
   }

   public void setDeviceSetup(DeviceSetupEntity deviceSetup) {
      this.deviceSetup = deviceSetup;
   }

   public AlarmSourceSetupEntity getSource() {
      return source;
   }

   public void setSource(AlarmSourceSetupEntity source) {
      this.source = source;
   }

   public LocalDateTime getAlarmDateTime() {
      return alarmDateTime;
   }

   public void setAlarmDateTime(LocalDateTime alarmDateTime) {
      this.alarmDateTime = alarmDateTime;
   }
}
