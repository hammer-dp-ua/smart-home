package ua.dp.hammer.smarthome.entities;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "wi_fi_technical_device_info")
public class TechnicalDeviceInfoEntity {
   @Id
   @Column(name = "aa_id", unique = true, nullable = false)
   //@GeneratedValue(strategy = GenerationType.IDENTITY)
   /*@GenericGenerator(name = "TechnicalDeviceInfoEntitySequence",
         strategy = "enhanced-sequence",
         parameters = {@Parameter(name = "sequence_name", value = "wi_fi_technical_device_info_aa_id_seq")})*/
   @SequenceGenerator(name="wi_fi_technical_device_info_aa_id_seq",
                      sequenceName="wi_fi_technical_device_info_aa_id_seq",
                      allocationSize = 1)
   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="wi_fi_technical_device_info_aa_id_seq")
   private Integer id;

   @NotNull
   @ManyToOne
   @JoinColumn(name = "device_type", nullable = false)
   private DeviceTypeEntity type;

   @OneToOne(mappedBy = "technicalInfo", cascade = CascadeType.ALL, optional = true)
   @OnDelete(action = OnDeleteAction.CASCADE) // Used for DDL generations and mostly just declarative
   private EnvSensorEntity envSensor;

   @NotNull
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

   @Column(name = "gain")
   private Integer gain;

   @Column(name = "reset_reason")
   private String resetReason;

   @Column(name = "system_restart_reason")
   private String systemRestartReason;

   @NotNull
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

   public Integer getGain() {
      return gain;
   }

   public void setGain(Integer gain) {
      this.gain = gain;
   }

   public String getResetReason() {
      return resetReason;
   }

   public void setResetReason(String resetReason) {
      this.resetReason = resetReason;
   }

   public String getSystemRestartReason() {
      return systemRestartReason;
   }

   public void setSystemRestartReason(String systemRestartReason) {
      this.systemRestartReason = systemRestartReason;
   }

   public LocalDateTime getInfoDt() {
      return infoDt;
   }

   public void setInfoDt(LocalDateTime infoDt) {
      this.infoDt = infoDt;
   }
}
