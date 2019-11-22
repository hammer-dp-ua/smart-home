package ua.dp.hammer.smarthome.entities;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "wi_fi_technical_device_info")
public class TechnicalDeviceInfoEntity {
   @Id
   @Column(name = "aa_id")
   @GenericGenerator(name = "TechnicalDeviceInfoEntityIdGenerator", strategy = "increment")
   /* increment — At Hibernate startup, reads the maximum (numeric) primary key
      column value of each entity’s table and increments the value by one each time a
      new row is inserted. Especially efficient if a non-clustered Hibernate application
      has exclusive access to the database; but don’t use it in any other scenario. */
   private Integer id;

   @NotNull
   @ManyToOne
   private DeviceTypeEntity type;

   @OneToOne(mappedBy = "technicalInfo", cascade = CascadeType.PERSIST)
   @OnDelete(action = OnDeleteAction.CASCADE) // Used for DDL generations and mostly just declarative
   private EnvSensorEntity envSensor;

   @Column(name = "device_name")
   private String name;

   @Column(name = "errors")
   private Integer errors;

   @Column(name = "uptime_sec")
   private Integer uptimeSec;

   @Column(name = "firmware_timestamp")
   private String firmwareTimestamp;

   @Column(name = "free_heap")
   private Integer freeHeap;

   @Column(name = "reset_reason")
   private String resetReason;

   @Column(name = "info_dt")
   private LocalDateTime infoDt;

   public Integer getId() {
      return id;
   }

   public DeviceTypeEntity getType() {
      return type;
   }

   public void setType(DeviceTypeEntity type) {
      this.type = type;
   }

   public EnvSensorEntity getEnvSensor() {
      return envSensor;
   }

   public void addEnvSensor(EnvSensorEntity envSensor) {
      envSensor.setTechnicalInfo(this);
      this.envSensor = envSensor;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Integer getErrors() {
      return errors;
   }

   public void setErrors(Integer errors) {
      this.errors = errors;
   }

   public Integer getUptimeSec() {
      return uptimeSec;
   }

   public void setUptimeSec(Integer uptimeSec) {
      this.uptimeSec = uptimeSec;
   }

   public String getFirmwareTimestamp() {
      return firmwareTimestamp;
   }

   public void setFirmwareTimestamp(String firmwareTimestamp) {
      this.firmwareTimestamp = firmwareTimestamp;
   }

   public Integer getFreeHeap() {
      return freeHeap;
   }

   public void setFreeHeap(Integer freeHeap) {
      this.freeHeap = freeHeap;
   }

   public String getResetReason() {
      return resetReason;
   }

   public void setResetReason(String resetReason) {
      this.resetReason = resetReason;
   }

   public LocalDateTime getInfoDt() {
      return infoDt;
   }

   public void setInfoDt(LocalDateTime infoDt) {
      this.infoDt = infoDt;
   }
}
